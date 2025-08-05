# Multivalue Functions Implementation Specification for OpenSearch PPL

## Overview

This document provides a comprehensive specification for implementing Splunk-compatible multivalue (mv) functions in OpenSearch's Piped Processing Language (PPL). These functions enable manipulation and analysis of array/multivalue fields, which are common in log analysis and data processing scenarios.

## Table of Contents

1. [Function Specifications](#function-specifications)
2. [Implementation Architecture](#implementation-architecture)
3. [Technical Details](#technical-details)
4. [Test Specifications](#test-specifications)
5. [Implementation Roadmap](#implementation-roadmap)
6. [Examples and Use Cases](#examples-and-use-cases)

---

## Function Specifications

### 1. mvappend(values...)

**Purpose**: Combines one or more values into a single multivalue field.

**Syntax**: `mvappend(value1, value2, ..., valueN)`

**Parameters**:
- `value1, value2, ..., valueN`: Any number of values (strings, numbers, arrays, or single values)

**Return Type**: `ARRAY<ANY>`

**Behavior**:
- Accepts variable number of arguments
- Flattens nested arrays into a single array
- Preserves order of arguments
- Handles null values by including them in the result
- Mixed types are converted to strings for consistency

**Implementation Details**:
- Use Calcite's `ARRAY` constructor with variable arguments
- Implement type coercion to handle mixed types
- Support nested `mvappend` calls

**Examples**:
```sql
-- Basic usage
| eval combined = mvappend("a", "b", "c")
-- Result: ["a", "b", "c"]

-- Mixed types
| eval mixed = mvappend(1, "hello", 3.14)
-- Result: ["1", "hello", "3.14"]

-- With existing arrays
| eval extended = mvappend(existing_array, "new_value")
-- Result: [existing_values..., "new_value"]
```

---

### 2. mvcount(mv_field)

**Purpose**: Returns the count of values in a multivalue field.

**Syntax**: `mvcount(mv_field)`

**Parameters**:
- `mv_field`: A multivalue field or array

**Return Type**: `INTEGER`

**Behavior**:
- Returns number of elements in the array
- Returns 1 for single-value fields
- Returns 0 for empty arrays
- Returns NULL for null fields

**Implementation Details**:
- Use Calcite's `ARRAY_LENGTH` function
- Handle null checking with CASE statement
- Simple wrapper around existing array length functionality

**Examples**:
```sql
-- Count array elements
| eval count = mvcount(ip_addresses)
-- If ip_addresses = ["192.168.1.1", "10.0.0.1"], result: 2

-- Single value
| eval count = mvcount(single_field)
-- If single_field = "value", result: 1

-- Empty array
| eval count = mvcount(empty_array)
-- Result: 0
```

---

### 3. mvdedup(mv_field)

**Purpose**: Removes duplicate values from a multivalue field.

**Syntax**: `mvdedup(mv_field)`

**Parameters**:
- `mv_field`: A multivalue field or array

**Return Type**: `ARRAY<T>` (same type as input)

**Behavior**:
- Removes duplicate values while preserving order of first occurrence
- Case-sensitive for strings
- Maintains original data types
- Returns empty array for empty input

**Implementation Details**:
- Implement custom UDF using Java Set for deduplication
- Preserve insertion order using LinkedHashSet
- Handle type-specific comparison logic

**Examples**:
```sql
-- Remove duplicates
| eval unique_ips = mvdedup(ip_list)
-- If ip_list = ["192.168.1.1", "10.0.0.1", "192.168.1.1"], result: ["192.168.1.1", "10.0.0.1"]

-- String deduplication
| eval unique_names = mvdedup(names)
-- If names = ["Alice", "Bob", "Alice", "Charlie"], result: ["Alice", "Bob", "Charlie"]
```

---

### 4. mvfilter(predicate)

**Purpose**: Filters a multivalue field based on a boolean expression.

**Syntax**: `mvfilter(predicate_expression)`

**Parameters**:
- `predicate_expression`: Boolean expression that can reference field values

**Return Type**: `ARRAY<T>` (same type as input)

**Behavior**:
- Evaluates predicate for each array element
- Returns array containing only elements where predicate is true
- Supports complex expressions with functions and operators
- Returns empty array if no elements match

**Implementation Details**:
- Implement as lambda function similar to existing `filter` function
- Use Calcite's lambda expression support
- Support field references within the predicate

**Examples**:
```sql
-- Filter by pattern
| eval filtered_emails = mvfilter(match(email, ".*@company\.com$"))
-- Keeps only emails ending with @company.com

-- Numeric filtering
| eval high_scores = mvfilter(score > 80)
-- Keeps only scores greater than 80

-- String filtering
| eval long_names = mvfilter(length(name) > 5)
-- Keeps only names longer than 5 characters
```

---

### 5. mvfind(mv_field, regex)

**Purpose**: Finds the index of the first value matching a regular expression.

**Syntax**: `mvfind(mv_field, regex_pattern)`

**Parameters**:
- `mv_field`: A multivalue field or array
- `regex_pattern`: Regular expression pattern (string)

**Return Type**: `INTEGER`

**Behavior**:
- Returns 0-based index of first matching element
- Returns NULL if no match found
- Uses Java regex syntax
- Case-sensitive matching

**Implementation Details**:
- Implement custom UDF with regex matching
- Use Java Pattern/Matcher classes
- Return Optional<Integer> converted to nullable integer

**Examples**:
```sql
-- Find error pattern
| eval error_index = mvfind(log_messages, "ERROR.*")
-- Returns index of first message containing "ERROR"

-- Find IP pattern
| eval ip_index = mvfind(addresses, "192\.168\..*")
-- Returns index of first address starting with "192.168."
```

---

### 6. mvindex(mv_field, start [, end])

**Purpose**: Returns a subset of multivalue field using index range.

**Syntax**: `mvindex(mv_field, start [, end])`

**Parameters**:
- `mv_field`: A multivalue field or array
- `start`: Starting index (0-based, can be negative)
- `end`: Ending index (optional, inclusive, can be negative)

**Return Type**: `ARRAY<T>` or `T` (single value if only start specified)

**Behavior**:
- 0-based indexing (first element is index 0)
- Negative indices count from end (-1 is last element)
- If only start specified, returns single element
- If start and end specified, returns array slice
- Returns NULL for out-of-bounds indices

**Implementation Details**:
- Implement bounds checking and negative index handling
- Use Calcite's array slicing capabilities where available
- Handle edge cases (empty arrays, invalid ranges)

**Examples**:
```sql
-- Get first element
| eval first = mvindex(names, 0)
-- Returns first element as single value

-- Get last element
| eval last = mvindex(names, -1)
-- Returns last element as single value

-- Get range
| eval middle = mvindex(names, 1, 3)
-- Returns elements at indices 1, 2, 3 as array

-- Get last 3 elements
| eval last_three = mvindex(names, -3, -1)
-- Returns last 3 elements as array
```

---

### 7. mvjoin(mv_field, delimiter)

**Purpose**: Concatenates multivalue field elements with a delimiter.

**Syntax**: `mvjoin(mv_field, delimiter)`

**Parameters**:
- `mv_field`: A multivalue field or array
- `delimiter`: String delimiter to join elements

**Return Type**: `STRING`

**Behavior**:
- Joins all array elements with specified delimiter
- Converts non-string elements to strings
- Returns empty string for empty arrays
- Handles null elements by converting to empty string

**Implementation Details**:
- Use Calcite's string concatenation functions
- Implement custom UDF for array joining
- Handle type conversion for non-string elements

**Examples**:
```sql
-- Join with comma
| eval ip_list = mvjoin(ip_addresses, ", ")
-- If ip_addresses = ["192.168.1.1", "10.0.0.1"], result: "192.168.1.1, 10.0.0.1"

-- Join with pipe
| eval combined = mvjoin(tags, " | ")
-- If tags = ["web", "server", "prod"], result: "web | server | prod"
```

---

### 8. mvmap(mv_field, expression)

**Purpose**: Applies an expression to each element in a multivalue field.

**Syntax**: `mvmap(mv_field, expression)`

**Parameters**:
- `mv_field`: A multivalue field or array
- `expression`: Expression to apply to each element

**Return Type**: `ARRAY<T>` (type depends on expression result)

**Behavior**:
- Applies expression to each array element
- Returns new array with transformed values
- Expression can reference the current element
- Supports complex expressions and function calls

**Implementation Details**:
- Implement as lambda function similar to existing `transform` function
- Use Calcite's lambda expression support
- Handle type inference for result array

**Examples**:
```sql
-- Multiply each number by 10
| eval scaled = mvmap(numbers, numbers * 10)
-- If numbers = [1, 2, 3], result: [10, 20, 30]

-- Convert to uppercase
| eval upper_names = mvmap(names, upper(names))
-- If names = ["alice", "bob"], result: ["ALICE", "BOB"]

-- Complex expression
| eval processed = mvmap(values, if(values > 100, values * 0.9, values))
-- Apply 10% discount to values over 100
```

---

### 9. mvrange(start, end [, step])

**Purpose**: Creates a multivalue field containing a range of numbers.

**Syntax**: `mvrange(start, end [, step])`

**Parameters**:
- `start`: Starting number (inclusive)
- `end`: Ending number (exclusive)
- `step`: Step increment (optional, default 1)

**Return Type**: `ARRAY<INTEGER>` or `ARRAY<TIMESTAMP>` (if step is time-based)

**Behavior**:
- Generates sequence from start to end (exclusive)
- Default step is 1
- Supports negative steps for descending sequences
- Supports time-based steps (e.g., "7d" for 7 days)
- Returns empty array if invalid range

**Implementation Details**:
- Implement custom UDF for number sequence generation
- Support time-based ranges using timestamp arithmetic
- Handle edge cases (zero step, invalid ranges)

**Examples**:
```sql
-- Simple range
| eval numbers = mvrange(1, 6)
-- Result: [1, 2, 3, 4, 5]

-- Range with step
| eval evens = mvrange(0, 11, 2)
-- Result: [0, 2, 4, 6, 8, 10]

-- Descending range
| eval countdown = mvrange(10, 0, -1)
-- Result: [10, 9, 8, 7, 6, 5, 4, 3, 2, 1]

-- Time-based range (if supported)
| eval dates = mvrange(1514834731, 1524134919, "7d")
-- Result: array of timestamps 7 days apart
```

---

### 10. mvreverse(mv_field)

**Purpose**: Reverses the order of values in a multivalue field.

**Syntax**: `mvreverse(mv_field)`

**Parameters**:
- `mv_field`: A multivalue field or array

**Return Type**: `ARRAY<T>` (same type as input)

**Behavior**:
- Reverses element order in the array
- Preserves original data types
- Returns empty array for empty input
- Returns single-element array unchanged

**Implementation Details**:
- Implement custom UDF using Java Collections.reverse()
- Handle immutable array types by creating new array
- Simple array reversal logic

**Examples**:
```sql
-- Reverse array
| eval reversed = mvreverse(names)
-- If names = ["Alice", "Bob", "Charlie"], result: ["Charlie", "Bob", "Alice"]

-- Reverse numbers
| eval backwards = mvreverse(scores)
-- If scores = [10, 20, 30], result: [30, 20, 10]
```

---

### 11. mvsort(mv_field)

**Purpose**: Sorts a multivalue field lexicographically.

**Syntax**: `mvsort(mv_field)`

**Parameters**:
- `mv_field`: A multivalue field or array

**Return Type**: `ARRAY<T>` (same type as input)

**Behavior**:
- Sorts elements in lexicographical order
- Numbers sorted as strings (10 comes before 2)
- Case-sensitive sorting
- Null values sorted to beginning
- Preserves duplicates

**Implementation Details**:
- Implement custom UDF using Java Collections.sort()
- Convert all elements to strings for comparison
- Handle null values explicitly

**Examples**:
```sql
-- Sort strings
| eval sorted_names = mvsort(names)
-- If names = ["Charlie", "Alice", "Bob"], result: ["Alice", "Bob", "Charlie"]

-- Sort numbers (lexicographically)
| eval sorted_nums = mvsort(numbers)
-- If numbers = [100, 20, 3], result: [100, 20, 3] (lexicographical order)
```

---

### 12. mvzip(mv_left, mv_right [, delimiter])

**Purpose**: Combines values from two multivalue fields element-wise.

**Syntax**: `mvzip(mv_left, mv_right [, delimiter])`

**Parameters**:
- `mv_left`: First multivalue field or array
- `mv_right`: Second multivalue field or array
- `delimiter`: String delimiter (optional, default comma)

**Return Type**: `ARRAY<STRING>`

**Behavior**:
- Combines corresponding elements from both arrays
- Uses specified delimiter to join paired elements
- Stops when shorter array is exhausted
- Default delimiter is comma (",")

**Implementation Details**:
- Implement custom UDF for element-wise combination
- Handle arrays of different lengths
- Convert elements to strings before joining

**Examples**:
```sql
-- Basic zip
| eval combined = mvzip(hosts, ports)
-- If hosts = ["web1", "web2"], ports = ["80", "443"], result: ["web1,80", "web2,443"]

-- Custom delimiter
| eval servers = mvzip(hosts, ports, ":")
-- Result: ["web1:80", "web2:443"]

-- Different length arrays
| eval partial = mvzip(["a", "b", "c"], ["1", "2"])
-- Result: ["a,1", "b,2"] (stops at shorter array)
```

---

### 13. mv_to_json_array(mv_field [, infer_types])

**Purpose**: Converts a multivalue field to a JSON array.

**Syntax**: `mv_to_json_array(mv_field [, infer_types])`

**Parameters**:
- `mv_field`: A multivalue field or array
- `infer_types`: Boolean flag to infer JSON types (optional, default false)

**Return Type**: `STRING` (JSON array format)

**Behavior**:
- Converts array to JSON array string
- If infer_types is false, all values become JSON strings
- If infer_types is true, attempts to infer proper JSON types
- Handles null values as JSON null
- Returns "[]" for empty arrays

**Implementation Details**:
- Use existing JSON functions or implement custom JSON serialization
- Handle type inference for numbers, booleans, null values
- Ensure proper JSON escaping

**Examples**:
```sql
-- Basic conversion
| eval json_array = mv_to_json_array(names)
-- If names = ["Alice", "Bob"], result: '["Alice","Bob"]'

-- With type inference
| eval typed_json = mv_to_json_array(mixed_values, true())
-- If mixed_values = ["Alice", "123", "true"], result: '["Alice",123,true]'

-- With nulls
| eval with_nulls = mv_to_json_array(sparse_array, true())
-- If sparse_array = ["value", null, "other"], result: '["value",null,"other"]'
```

---

### 14. split(string, delimiter)

**Purpose**: Splits a string into a multivalue field using a delimiter.

**Syntax**: `split(string, delimiter)`

**Parameters**:
- `string`: String to split
- `delimiter`: Delimiter string or regex pattern

**Return Type**: `ARRAY<STRING>`

**Behavior**:
- Splits string on delimiter occurrences
- Returns array of string parts
- Empty delimiter splits into individual characters
- Handles consecutive delimiters by creating empty strings
- Returns single-element array if no delimiter found

**Implementation Details**:
- May already exist in current system - verify compatibility
- Use Java String.split() or regex-based splitting
- Handle edge cases (empty string, empty delimiter)

**Examples**:
```sql
-- Split on comma
| eval parts = split("a,b,c", ",")
-- Result: ["a", "b", "c"]

-- Split on multiple characters
| eval words = split("hello::world::test", "::")
-- Result: ["hello", "world", "test"]

-- Split into characters
| eval chars = split("abc", "")
-- Result: ["a", "b", "c"]
```

---

### 15. commands(search_string)

**Purpose**: Extracts command names from a search string.

**Syntax**: `commands(search_string)`

**Parameters**:
- `search_string`: String containing search commands

**Return Type**: `ARRAY<STRING>`

**Behavior**:
- Parses search string to identify command names
- Returns array of command names in order
- Splunk-specific function, may need adaptation for PPL
- Generally not recommended except for audit log analysis

**Implementation Details**:
- Low priority implementation
- Requires parsing PPL/SPL syntax
- May not be necessary for OpenSearch use cases

**Examples**:
```sql
-- Extract commands
| eval cmd_list = commands("search error | stats count | sort count")
-- Result: ["search", "stats", "sort"]
```

---

## Implementation Architecture

### Core Components

1. **Function Registration**
   - Add function names to `BuiltinFunctionName.java`
   - Register implementations in `PPLFuncImpTable.java`

2. **Function Implementations**
   - Create `multivalue` package under `expression/function/`
   - Implement each function as UDF or Calcite operator wrapper
   - Use existing patterns from `CollectionUDF` package

3. **Type System Integration**
   - Use Calcite's `ARRAY` type for multivalue fields
   - Implement proper type checking with `PPLTypeChecker`
   - Handle type coercion and casting

### File Structure

```
core/src/main/java/org/opensearch/sql/expression/function/
├── BuiltinFunctionName.java (add MV_* constants)
├── PPLFuncImpTable.java (register functions)
├── multivalue/
│   ├── MvAppendFunction.java
│   ├── MvCountFunction.java
│   ├── MvDedupFunction.java
│   ├── MvFilterFunction.java
│   ├── MvFindFunction.java
│   ├── MvIndexFunction.java
│   ├── MvJoinFunction.java
│   ├── MvMapFunction.java
│   ├── MvRangeFunction.java
│   ├── MvReverseFunction.java
│   ├── MvSortFunction.java
│   ├── MvZipFunction.java
│   ├── MvToJsonArrayFunction.java
│   ├── SplitFunction.java
│   └── CommandsFunction.java
└── PPLBuiltinOperators.java (add MV operators)
```

---

## Technical Details

### Type Handling

**Array Types**:
- Use Calcite's `ARRAY<T>` type system
- Support nested arrays where appropriate
- Handle mixed-type arrays by converting to strings

**Type Coercion**:
- Follow existing patterns in `TypeCastOperators`
- Implement automatic casting for compatible types
- Preserve type information where possible

**Null Handling**:
- Follow SQL null semantics
- Functions return null for null inputs (where appropriate)
- Handle null elements within arrays consistently

### Performance Considerations

**Memory Usage**:
- Avoid unnecessary array copying
- Use streaming operations where possible
- Implement lazy evaluation for large arrays

**Optimization**:
- Leverage Calcite's optimization framework
- Use built-in operators where available
- Implement efficient algorithms for sorting, deduplication

### Error Handling

**Input Validation**:
- Validate array bounds for index operations
- Check regex patterns for validity
- Provide meaningful error messages

**Edge Cases**:
- Empty arrays
- Single-element arrays
- Very large arrays
- Invalid input types

---

## Test Specifications

### Unit Tests

**Basic Functionality**:
```java
// Example test structure
@Test
public void testMvAppend() {
    // Test basic functionality
    assertArrayEquals(["a", "b", "c"], mvappend("a", "b", "c"));
    
    // Test with arrays
    assertArrayEquals(["a", "b", "c", "d"], mvappend(["a", "b"], ["c", "d"]));
    
    // Test with nulls
    assertArrayEquals(["a", null, "c"], mvappend("a", null, "c"));
}
```

**Type Testing**:
- Test with different input types
- Verify type coercion behavior
- Test mixed-type scenarios

**Edge Cases**:
- Empty inputs
- Null inputs
- Very large arrays
- Invalid parameters

### Integration Tests

**PPL Query Tests**:
```sql
-- Test in actual PPL queries
source=logs | eval combined = mvappend(field1, field2) | where mvcount(combined) > 1

-- Test chaining
source=logs | eval sorted = mvsort(mvdedup(tags)) | eval count = mvcount(sorted)

-- Test with other functions
source=logs | eval filtered = mvfilter(match(urls, ".*\.com$")) | eval joined = mvjoin(filtered, "; ")
```

**Performance Tests**:
- Test with large datasets
- Measure memory usage
- Benchmark against existing functions

### Test Data

**Sample Arrays**:
```json
{
  "string_array": ["apple", "banana", "cherry"],
  "number_array": [1, 2, 3, 4, 5],
  "mixed_array": ["text", 123, true, null],
  "empty_array": [],
  "single_element": ["only"],
  "large_array": [/* 1000+ elements */],
  "duplicate_array": ["a", "b", "a", "c", "b"],
  "ip_addresses": ["192.168.1.1", "10.0.0.1", "172.16.0.1"]
}
```

---

## Implementation Roadmap

### Phase 1: Foundation (Week 1-2)
**Priority: High**

1. **Setup Infrastructure**
   - Add function names to `BuiltinFunctionName.java`
   - Create `multivalue` package structure
   - Set up basic test framework

2. **Core Functions**
   - `mvcount` - Simple array length function
   - `mvappend` - Basic array concatenation
   - `mvindex` - Array element access
   - `mvjoin` - Array to string conversion

**Deliverables**:
- Basic multivalue function framework
- 4 core functions implemented and tested
- Integration with PPL parser

### Phase 2: Data Manipulation (Week 3-4)
**Priority: High**

3. **Array Operations**
   - `mvdedup` - Remove duplicates
   - `mvsort` - Sort array elements
   - `mvreverse` - Reverse array order
   - `split` - String to array conversion

**Deliverables**:
- 4 additional functions implemented
- Comprehensive test suite
- Performance benchmarks

### Phase 3: Advanced Operations (Week 5-6)
**Priority: Medium**

4. **Complex Functions**
   - `mvfilter` - Predicate-based filtering
   - `mvmap` - Element transformation
   - `mvzip` - Array combination
   - `mvfind` - Regex-based search

**Deliverables**:
- Advanced multivalue operations
- Lambda expression support
- Complex query examples

### Phase 4: Specialized Functions (Week 7-8)
**Priority: Low**

5. **Utility Functions**
   - `mvrange` - Number sequence generation
   - `mv_to_json_array` - JSON conversion
   - `commands` - Command extraction (if needed)

**Deliverables**:
- Complete multivalue function set
- JSON integration
- Documentation and examples

### Phase 5: Optimization and Polish (Week 9-10)
**Priority: Medium**

6. **Performance and Quality**
   - Performance optimization
   - Memory usage optimization
   - Error handling improvements
   - Documentation completion

**Deliverables**:
- Production-ready implementation
- Complete documentation
   - Performance benchmarks
   - Migration guide from Splunk

---

## Examples and Use Cases

### Log Analysis

```sql
-- Extract and analyze error codes
source=application_logs 
| eval error_codes = split(error_field, ",")
| eval unique_errors = mvdedup(error_codes)
| eval error_count = mvcount(unique_errors)
| eval error_summary = mvjoin(mvsort(unique_errors), " | ")

-- Filter and process IP addresses
source=access_logs
| eval client_ips = split(x_forwarded_for, ",")
| eval internal_ips = mvfilter(match(client_ips, "^(10\.|192\.168\.|172\.(1[6-9]|2[0-9]|3[01])\.)")
| eval external_ips = mvfilter(NOT match(client_ips, "^(10\.|192\.168\.|172\.(1[6-9]|2[0-9]|3[01])\.)")
| eval ip_summary = mvjoin(mvappend("Internal:", mvjoin(internal_ips, ",")), " ")
```

### Data Enrichment

```sql
-- Combine multiple tag fields
source=events
| eval all_tags = mvappend(category_tags, user_tags, system_tags)
| eval unique_tags = mvdedup(all_tags)
| eval tag_count = mvcount(unique_tags)
| eval sorted_tags = mvsort(unique_tags)

-- Process user permissions
source=user_events
| eval permissions = split(permission_string, ";")
| eval admin_perms = mvfilter(match(permissions, "admin.*"))
| eval user_perms = mvfilter(match(permissions, "user.*"))
| eval has_admin = if(mvcount(admin_perms) > 0, "yes", "no")
```

### Security Analysis

```sql
-- Analyze failed login attempts
source=security_logs event_type="failed_login"
| eval attempted_users = split(username_attempts, ",")
| eval unique_attempts = mvdedup(attempted_users)
| eval attempt_count = mvcount(attempted_users)
| eval unique_count = mvcount(unique_attempts)
| eval repeat_attempts = attempt_count - unique_count
| where repeat_attempts > 5

-- Process threat indicators
source=threat_intel
| eval indicators = split(ioc_list, "|")
| eval ip_indicators = mvfilter(match(indicators, "^[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}$"))
| eval domain_indicators = mvfilter(match(indicators, "^[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$"))
| eval indicator_summary = mvjoin(mvappend("IPs:", mvjoin(ip_indicators, ",")), " ")
```

### Performance Monitoring

```sql
-- Analyze response times
source=performance_logs
| eval response_times = split(timing_data, ",")
| eval numeric_times = mvmap(response_times, tonumber(response_times))
| eval sorted_times = mvsort(numeric_times)
| eval median_time = mvindex(sorted_times, mvcount(sorted_times)/2)
| eval slow_requests = mvfilter(numeric_times > 1000)
| eval slow_count = mvcount(slow_requests)

-- Process server metrics
source=server_metrics
| eval cpu_readings = split(cpu_data, ";")
| eval high_cpu = mvfilter(tonumber(cpu_readings) > 80)
| eval cpu_spikes = mvcount(high_cpu)
| eval max_cpu = mvindex(mvsort(mvreverse(cpu_readings)), 0)
```

---

## Migration from Splunk

### Compatibility Notes

**Syntax Compatibility**:
- Function names and basic syntax should match Splunk exactly
- Parameter order and types should be identical
- Return types should be compatible

**Behavioral Differences**:
- Document any differences in null handling
- Note differences in type coercion
- Explain any performance characteristics

**Migration Examples**:

```sql
-- Splunk SPL
... | eval combined = mvappend(field1, field2, "literal")
... | eval count = mvcount(combined)
... | eval filtered = mvfilter(match(emails, ".*@company\.com"))

-- OpenSearch PPL (should be identical)
... | eval combined = mvappend(field1, field2, "literal")
... | eval count = mvcount(combined)  
... | eval filtered = mvfilter(match(emails, ".*@company\.com"))
```

### Best Practices

**Performance**:
- Use `mvcount` before expensive operations on large arrays
- Consider `mvdedup` early in processing chains
- Use `mvfilter` to reduce data volume before other operations

**Type Safety**:
- Be explicit about type expectations
- Use appropriate casting functions when needed
- Handle null values explicitly

**Readability**:
- Chain operations logically
- Use meaningful variable names
- Comment complex multivalue expressions

---

## Conclusion

This specification provides a comprehensive roadmap for implementing Splunk-compatible multivalue functions in OpenSearch PPL. The implementation should focus on:

1. **Compatibility**: Maintaining Splunk syntax and behavior
2. **Performance**: Efficient handling of large arrays
3. **Integration**: Seamless integration with existing PPL functions
4. **Extensibility**: Framework for adding more functions in the future

The phased approach allows for incremental delivery while ensuring core functionality is available early in the development cycle.