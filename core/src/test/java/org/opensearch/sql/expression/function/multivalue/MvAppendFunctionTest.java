/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.sql.expression.function.multivalue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for MvAppendFunction.
 * Tests the static mvappend method that performs the actual multivalue append logic.
 */
public class MvAppendFunctionTest {

  @Test
  public void testBasicMvAppend() {
    // Test basic functionality with string arguments
    List<Object> result = MvAppendFunction.mvappend("a", "b", "c");
    
    assertNotNull(result);
    assertEquals(3, result.size());
    assertEquals("a", result.get(0));
    assertEquals("b", result.get(1));
    assertEquals("c", result.get(2));
  }

  @Test
  public void testMixedTypes() {
    // Test with mixed types - should convert to strings
    List<Object> result = MvAppendFunction.mvappend(1, "hello", 3.14);
    
    assertNotNull(result);
    assertEquals(3, result.size());
    assertEquals("1", result.get(0));
    assertEquals("hello", result.get(1));
    assertEquals("3.14", result.get(2));
  }

  @Test
  public void testArrayFlattening() {
    // Test flattening of nested arrays
    List<String> array1 = Arrays.asList("a", "b");
    List<String> array2 = Arrays.asList("d", "e");
    
    List<Object> result = MvAppendFunction.mvappend(array1, "c", array2);
    
    assertNotNull(result);
    assertEquals(5, result.size());
    assertEquals("a", result.get(0));
    assertEquals("b", result.get(1));
    assertEquals("c", result.get(2));
    assertEquals("d", result.get(3));
    assertEquals("e", result.get(4));
  }

  @Test
  public void testNullHandling() {
    // Test handling of null values
    List<Object> result = MvAppendFunction.mvappend("a", null, "c");
    
    assertNotNull(result);
    assertEquals(3, result.size());
    assertEquals("a", result.get(0));
    assertNull(result.get(1));
    assertEquals("c", result.get(2));
  }

  @Test
  public void testEmptyArrays() {
    // Test with empty arrays
    List<String> emptyArray = Collections.emptyList();
    List<Object> result = MvAppendFunction.mvappend(emptyArray, "a", emptyArray);
    
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("a", result.get(0));
  }

  @Test
  public void testSingleArgument() {
    // Test with single argument
    List<Object> result = MvAppendFunction.mvappend("single");
    
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("single", result.get(0));
  }

  @Test
  public void testNoArguments() {
    // Test with no arguments - edge case
    List<Object> result = MvAppendFunction.mvappend();
    
    assertNotNull(result);
    assertEquals(0, result.size());
    assertTrue(result.isEmpty());
  }

  @Test
  public void testBooleanValues() {
    // Test with boolean values
    List<Object> result = MvAppendFunction.mvappend(true, false, "text");
    
    assertNotNull(result);
    assertEquals(3, result.size());
    assertEquals("true", result.get(0));
    assertEquals("false", result.get(1));
    assertEquals("text", result.get(2));
  }

  @Test
  public void testBigDecimalValues() {
    // Test with BigDecimal values
    BigDecimal bd1 = new BigDecimal("123.456");
    BigDecimal bd2 = new BigDecimal("789.012");
    
    List<Object> result = MvAppendFunction.mvappend(bd1, bd2);
    
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals("123.456", result.get(0));
    assertEquals("789.012", result.get(1));
  }

  @Test
  public void testFloatingPointNumbers() {
    // Test with floating point numbers
    List<Object> result = MvAppendFunction.mvappend(3.14159, 2.71828f);
    
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals("3.14159", result.get(0));
    // Float precision may vary, so we check the actual converted value
    assertEquals(String.valueOf(2.71828f), result.get(1));
  }

  @Test
  public void testLargeNumbers() {
    // Test with large numbers to avoid scientific notation
    List<Object> result = MvAppendFunction.mvappend(1000000, 0.000001);
    
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals("1000000", result.get(0));
    // Double precision may result in scientific notation for very small numbers
    assertEquals(String.valueOf(0.000001), result.get(1));
  }

  @Test
  public void testNestedArraysWithNulls() {
    // Test complex scenario with nested arrays containing nulls
    List<Object> array1 = Arrays.asList("a", null, "b");
    List<Object> array2 = Arrays.asList(null, "c");
    
    List<Object> result = MvAppendFunction.mvappend(array1, "middle", array2);
    
    assertNotNull(result);
    assertEquals(6, result.size());
    assertEquals("a", result.get(0));
    assertNull(result.get(1));
    assertEquals("b", result.get(2));
    assertEquals("middle", result.get(3));
    assertNull(result.get(4));
    assertEquals("c", result.get(5));
  }

  @Test
  public void testArrayOfArrays() {
    // Test with arrays containing other arrays
    List<String> innerArray1 = Arrays.asList("x", "y");
    List<String> innerArray2 = Arrays.asList("z");
    List<List<String>> outerArray = Arrays.asList(innerArray1, innerArray2);
    
    List<Object> result = MvAppendFunction.mvappend(outerArray, "end");
    
    assertNotNull(result);
    // The outer array should be flattened, but inner arrays become strings
    assertEquals(3, result.size());
    assertEquals("[x, y]", result.get(0));
    assertEquals("[z]", result.get(1));
    assertEquals("end", result.get(2));
  }

  @Test
  public void testVeryLargeArray() {
    // Test performance with larger array
    Object[] args = new Object[1000];
    for (int i = 0; i < 1000; i++) {
      args[i] = "item" + i;
    }
    
    List<Object> result = MvAppendFunction.mvappend(args);
    
    assertNotNull(result);
    assertEquals(1000, result.size());
    assertEquals("item0", result.get(0));
    assertEquals("item999", result.get(999));
  }

  @Test
  public void testMixedArraysAndValues() {
    // Test complex mixing of arrays and individual values
    List<String> stringArray = Arrays.asList("from", "array");
    List<Integer> numberArray = Arrays.asList(1, 2, 3);
    
    List<Object> result = MvAppendFunction.mvappend(
        "start", 
        stringArray, 
        42, 
        numberArray, 
        "end"
    );
    
    assertNotNull(result);
    assertEquals(8, result.size());
    assertEquals("start", result.get(0));
    assertEquals("from", result.get(1));
    assertEquals("array", result.get(2));
    assertEquals("42", result.get(3));
    assertEquals("1", result.get(4));
    assertEquals("2", result.get(5));
    assertEquals("3", result.get(6));
    assertEquals("end", result.get(7));
  }

  @Test
  public void testSpecialFloatingPointValues() {
    // Test special floating point values
    List<Object> result = MvAppendFunction.mvappend(
        Double.POSITIVE_INFINITY,
        Double.NEGATIVE_INFINITY,
        Double.NaN,
        0.0,
        -0.0
    );
    
    assertNotNull(result);
    assertEquals(5, result.size());
    assertEquals("Infinity", result.get(0));
    assertEquals("-Infinity", result.get(1));
    assertEquals("NaN", result.get(2));
    assertEquals("0", result.get(3));
    assertEquals("0", result.get(4));
  }

  @Test
  public void testStringRepresentationConsistency() {
    // Test that string conversion is consistent
    Integer intValue = 42;
    String stringValue = "42";
    
    List<Object> result = MvAppendFunction.mvappend(intValue, stringValue);
    
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals("42", result.get(0));
    assertEquals("42", result.get(1));
    // Both should be strings with same value
    assertEquals(result.get(0), result.get(1));
  }
}
