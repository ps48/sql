# Multivalue Functions - Calcite Integration Plan

## 🎯 **Executive Summary**

This document outlines the strategic plan for implementing multivalue functions in OpenSearch SQL by leveraging Apache Calcite's extensive array function library. With the V3 Calcite engine 86% complete (36/42 sub-issues), this represents the optimal time to integrate multivalue functionality.

## 📊 **Current State Analysis**

### **Existing Infrastructure**
- ✅ **V3 Calcite Engine**: Beta status, enabled via `plugins.calcite.enabled=true`
- ✅ **Function Framework**: Centralized registry and DSL system in place
- ✅ **Test Infrastructure**: `CalciteArrayFunctionIT.java` suggests active development
- ❌ **Multivalue Functions**: No implementation found in V2 engine
- 🔄 **Work in Progress**: `MvAppendFunctionTest.java` indicates ongoing development

### **Calcite Function Library Assessment**
Apache Calcite provides 30+ array functions that can be leveraged:

| Category | Calcite Functions | OpenSearch Multivalue Equivalent |
|----------|-------------------|-----------------------------------|
| **Construction** | `ARRAY()`, `ARRAY_REPEAT()` | `mv_create()`, `mv_repeat()` |
| **Modification** | `ARRAY_APPEND()`, `ARRAY_PREPEND()` | `mv_append()`, `mv_prepend()` |
| **Combination** | `ARRAY_CONCAT()`, `ARRAY_UNION()` | `mv_concat()`, `mv_union()` |
| **Filtering** | `ARRAY_DISTINCT()`, `ARRAY_COMPACT()` | `mv_dedupe()`, `mv_compact()` |
| **Analysis** | `ARRAY_CONTAINS()`, `ARRAY_LENGTH()` | `mv_contains()`, `mv_length()` |
| **Extraction** | `ARRAY_SLICE()`, `ARRAY_POSITION()` | `mv_slice()`, `mv_index()` |
| **Aggregation** | `ARRAY_MAX()`, `ARRAY_MIN()` | `mv_max()`, `mv_min()` |
| **Transformation** | `ARRAY_REVERSE()`, `ARRAY_TO_STRING()` | `mv_reverse()`, `mv_join()` |

## 🏗️ **Architecture Design**

### **Integration Strategy**
```
OpenSearch Multivalue Functions
         ↓
┌─────────────────┬─────────────────┐
│  Direct Mapping │ Custom Wrapper  │
│                 │                 │
│ Calcite Native  │ OpenSearch      │
│ Array Functions │ Specific Logic  │
└─────────────────┴─────────────────┘
         ↓
    Calcite Engine (V3)
         ↓
    OpenSearch API
```

### **Implementation Layers**

#### **Layer 1: Function Registry Integration**
```java
// Location: core/src/main/java/org/opensearch/sql/expression/function/
public class MultivalueFunctionRepository {
    // Register both Calcite-native and custom multivalue functions
    private void registerMultivalueFunctions() {
        // Direct Calcite mappings
        register("mv_append", CalciteArrayFunctions.ARRAY_APPEND);
        register("mv_concat", CalciteArrayFunctions.ARRAY_CONCAT);
        
        // Custom OpenSearch implementations
        register("mv_dedupe", new MvDedupeFunctionImpl());
        register("mv_sort", new MvSortFunctionImpl());
    }
}
```

#### **Layer 2: Function Implementation**
```java
// Location: core/src/main/java/org/opensearch/sql/expression/function/multivalue/
public abstract class MultivalueFunction extends FunctionImplementation {
    // Base class for all multivalue functions
    // Handles type checking, null handling, and error management
}

public class MvAppendFunction extends MultivalueFunction {
    // Wraps Calcite's ARRAY_APPEND with OpenSearch-specific behavior
}
```

#### **Layer 3: Calcite Integration**
```java
// Location: core/src/main/java/org/opensearch/sql/calcite/
public class MultivalueCalciteIntegration {
    // Maps OpenSearch multivalue functions to Calcite RelNode operations
    // Handles type conversion and optimization
}
```

## 📋 **Implementation Roadmap**

### **Phase 1: Foundation (Weeks 1-2)**
**Objective**: Establish multivalue function infrastructure

#### **Tasks:**
1. **Create Base Infrastructure**
   ```
   📁 core/src/main/java/org/opensearch/sql/expression/function/multivalue/
   ├── MultivalueFunction.java              # Base abstract class
   ├── MultivalueFunctionRepository.java    # Function registry
   ├── MultivalueTypeChecker.java          # Type validation
   └── MultivalueUtils.java                # Common utilities
   ```

2. **Integration Points**
   - Update `BuiltinFunctionRepository.java` to include multivalue functions
   - Extend `PPLBuiltinOperators.java` for PPL support
   - Add multivalue type support to `OpenSearchSchema.java`

3. **Test Infrastructure**
   ```
   📁 core/src/test/java/org/opensearch/sql/expression/function/multivalue/
   ├── MultivalueFunctionTestBase.java      # Base test class
   └── MultivalueTestUtils.java            # Test utilities
   
   📁 integ-test/src/test/java/org/opensearch/sql/calcite/remote/
   └── MultivalueFunctionIT.java           # Integration tests
   ```

### **Phase 2: Core Functions (Weeks 3-4)**
**Objective**: Implement essential multivalue functions

#### **Priority 1 Functions (Direct Calcite Mapping)**
1. **`mv_append(array, value)`** → `ARRAY_APPEND()`
2. **`mv_concat(array1, array2)`** → `ARRAY_CONCAT()`
3. **`mv_length(array)`** → `ARRAY_LENGTH()`
4. **`mv_contains(array, value)`** → `ARRAY_CONTAINS()`

#### **Priority 2 Functions (Custom Implementation)**
1. **`mv_dedupe(array)`** → Custom logic using `ARRAY_DISTINCT()`
2. **`mv_sort(array, [order])`** → Custom implementation
3. **`mv_join(array, delimiter)`** → Wrapper around `ARRAY_TO_STRING()`
4. **`mv_slice(array, start, [length])`** → `ARRAY_SLICE()`

### **Phase 3: Advanced Functions (Weeks 5-6)**
**Objective**: Implement complex multivalue operations

#### **Advanced Functions**
1. **`mv_zip(array1, array2)`** → Custom implementation
2. **`mv_filter(array, condition)`** → Lambda-based filtering
3. **`mv_transform(array, expression)`** → Lambda-based transformation
4. **`mv_reduce(array, initial, expression)`** → Custom aggregation

### **Phase 4: Integration & Optimization (Weeks 7-8)**
**Objective**: Complete integration and performance optimization

#### **Tasks:**
1. **PPL Integration**: Ensure all functions work in PPL queries
2. **SQL Integration**: Verify SQL compatibility
3. **Performance Testing**: Benchmark against large datasets
4. **Documentation**: Complete user and developer documentation

## 🔧 **Technical Implementation Details**

### **Function Signature Design**
```java
// Standard multivalue function signature
public class MvAppendFunction extends MultivalueFunction {
    @Override
    public FunctionSignature getFunctionSignature() {
        return FunctionSignature.builder()
            .name("mv_append")
            .returnType(ExprType.ARRAY)
            .arguments(
                new FunctionArgument("array", ExprType.ARRAY),
                new FunctionArgument("value", ExprType.UNKNOWN)
            )
            .build();
    }
    
    @Override
    public ExprValue apply(ExprValue... arguments) {
        // Delegate to Calcite's ARRAY_APPEND implementation
        return CalciteArrayFunctions.arrayAppend(arguments[0], arguments[1]);
    }
}
```

### **Type System Integration**
```java
// Extend OpenSearch type system for multivalue support
public class MultivalueType extends ExprType {
    private final ExprType elementType;
    
    public static MultivalueType of(ExprType elementType) {
        return new MultivalueType(elementType);
    }
    
    // Type checking and conversion logic
}
```

### **Calcite RelNode Integration**
```java
// Convert multivalue functions to Calcite RelNode operations
public class MultivalueRelNodeConverter {
    public RelNode convertMvAppend(RexCall call) {
        // Convert mv_append to Calcite's array append operation
        return relBuilder
            .project(
                relBuilder.call(SqlLibraryOperators.ARRAY_APPEND, 
                    call.getOperands())
            )
            .build();
    }
}
```

## 🧪 **Testing Strategy**

### **Unit Tests**
```java
// Example unit test structure
@Test
public void testMvAppend() {
    // Test basic functionality
    ExprValue array = ExprValueUtils.arrayValue(1, 2, 3);
    ExprValue value = ExprValueUtils.integerValue(4);
    ExprValue result = mvAppendFunction.apply(array, value);
    
    assertEquals(Arrays.asList(1, 2, 3, 4), result.arrayValue());
}

@Test
public void testMvAppendWithNulls() {
    // Test null handling
    ExprValue array = ExprValueUtils.arrayValue(1, null, 3);
    ExprValue value = ExprValueUtils.integerValue(4);
    ExprValue result = mvAppendFunction.apply(array, value);
    
    assertEquals(Arrays.asList(1, null, 3, 4), result.arrayValue());
}
```

### **Integration Tests**
```java
// Example integration test
@Test
public void testMvAppendInPPLQuery() {
    String query = "source=test | eval new_array = mv_append(array_field, 'new_value')";
    JSONObject result = executeQuery(query);
    
    // Verify results
    assertThat(result, hasEntry("new_array", contains("existing", "values", "new_value")));
}
```

### **Performance Tests**
```java
// Benchmark multivalue operations
@Benchmark
public void benchmarkMvAppendLargeArray() {
    ExprValue largeArray = createArrayWithSize(10000);
    ExprValue value = ExprValueUtils.stringValue("test");
    
    // Measure performance
    mvAppendFunction.apply(largeArray, value);
}
```

## 📈 **Success Metrics**

### **Functional Metrics**
- ✅ **Function Coverage**: 15+ multivalue functions implemented
- ✅ **Test Coverage**: >95% code coverage
- ✅ **Integration**: Works in both SQL and PPL
- ✅ **Performance**: <10ms latency for typical operations

### **Quality Metrics**
- ✅ **Compatibility**: Backward compatible with existing queries
- ✅ **Documentation**: Complete user and developer docs
- ✅ **Error Handling**: Graceful error messages and recovery
- ✅ **Type Safety**: Strong type checking and validation

## 🚀 **Deployment Strategy**

### **Feature Flag Approach**
```yaml
# opensearch.yml configuration
plugins:
  calcite:
    enabled: true
  sql:
    multivalue_functions:
      enabled: true
      experimental: true  # Initially experimental
```

### **Rollout Plan**
1. **Phase 1**: Internal testing with experimental flag
2. **Phase 2**: Beta release with select customers
3. **Phase 3**: General availability in next major release
4. **Phase 4**: Performance optimization and additional functions

## 🔗 **Dependencies & Prerequisites**

### **Required Components**
- ✅ **Calcite V3 Engine**: Must be enabled and stable
- ✅ **Function Framework**: Core function infrastructure
- ✅ **Type System**: Array type support
- ⚠️ **Performance Testing**: Benchmark infrastructure needed

### **External Dependencies**
- **Apache Calcite**: Version compatibility with OpenSearch
- **ANTLR4**: Parser grammar updates for new functions
- **JUnit**: Testing framework for comprehensive test coverage

## 📚 **Documentation Plan**

### **User Documentation**
1. **Function Reference**: Complete list of multivalue functions
2. **Usage Examples**: SQL and PPL query examples
3. **Performance Guide**: Best practices for large datasets
4. **Migration Guide**: Upgrading from array-based queries

### **Developer Documentation**
1. **Architecture Guide**: Implementation details and design decisions
2. **Extension Guide**: How to add new multivalue functions
3. **Testing Guide**: How to test multivalue functionality
4. **Troubleshooting**: Common issues and solutions

---

## 🎯 **Next Actions**

### **Immediate Steps (This Week)**
1. **Review existing spec files** in detail
2. **Analyze `MvAppendFunctionTest.java`** to understand current progress
3. **Create proof-of-concept** for one simple function (mv_append)
4. **Validate Calcite integration** approach

### **Short-term Goals (Next 2 Weeks)**
1. **Implement Phase 1** foundation infrastructure
2. **Create first working multivalue function**
3. **Establish testing patterns**
4. **Document implementation approach**

---

*Last Updated: January 21, 2025*
*Status: Planning Phase - Ready for Implementation*
*Confidence Level: 9/10 - Strong foundation and clear path forward*
