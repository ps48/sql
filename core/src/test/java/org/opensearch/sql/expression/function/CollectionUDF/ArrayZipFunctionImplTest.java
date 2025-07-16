/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.sql.expression.function.CollectionUDF;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Unit tests for ArrayZipFunctionImpl. */
public class ArrayZipFunctionImplTest {

  @Test
  public void testInternalZipBasicFunctionality() {
    List<Integer> array1 = Arrays.asList(1, 2, 3);
    List<String> array2 = Arrays.asList("a", "b", "c");

    Object result = ArrayZipFunctionImpl.internalZip(array1, array2);

    assertTrue(result instanceof List);
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> resultList = (List<Map<String, Object>>) result;

    assertEquals(3, resultList.size());

    // Check first element
    Map<String, Object> first = resultList.get(0);
    assertEquals(1, first.get("0"));
    assertEquals("a", first.get("1"));

    // Check second element
    Map<String, Object> second = resultList.get(1);
    assertEquals(2, second.get("0"));
    assertEquals("b", second.get("1"));

    // Check third element
    Map<String, Object> third = resultList.get(2);
    assertEquals(3, third.get("0"));
    assertEquals("c", third.get("1"));
  }

  @Test
  public void testInternalZipDifferentLengths() {
    List<Integer> array1 = Arrays.asList(1, 2, 3, 4);
    List<String> array2 = Arrays.asList("a", "b");

    Object result = ArrayZipFunctionImpl.internalZip(array1, array2);

    assertTrue(result instanceof List);
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> resultList = (List<Map<String, Object>>) result;

    // Should zip to the shorter length
    assertEquals(2, resultList.size());

    Map<String, Object> first = resultList.get(0);
    assertEquals(1, first.get("0"));
    assertEquals("a", first.get("1"));

    Map<String, Object> second = resultList.get(1);
    assertEquals(2, second.get("0"));
    assertEquals("b", second.get("1"));
  }

  @Test
  public void testInternalZipEmptyArrays() {
    List<Integer> array1 = new ArrayList<>();
    List<String> array2 = Arrays.asList("a", "b");

    Object result = ArrayZipFunctionImpl.internalZip(array1, array2);

    assertTrue(result instanceof List);
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> resultList = (List<Map<String, Object>>) result;

    assertEquals(0, resultList.size());
  }

  @Test
  public void testInternalZipMultipleArrays() {
    List<Integer> array1 = Arrays.asList(1, 2);
    List<String> array2 = Arrays.asList("a", "b");
    List<Boolean> array3 = Arrays.asList(true, false);

    Object result = ArrayZipFunctionImpl.internalZip(array1, array2, array3);

    assertTrue(result instanceof List);
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> resultList = (List<Map<String, Object>>) result;

    assertEquals(2, resultList.size());

    // Check first element
    Map<String, Object> first = resultList.get(0);
    assertEquals(1, first.get("0"));
    assertEquals("a", first.get("1"));
    assertEquals(true, first.get("2"));

    // Check second element
    Map<String, Object> second = resultList.get(1);
    assertEquals(2, second.get("0"));
    assertEquals("b", second.get("1"));
    assertEquals(false, second.get("2"));
  }

  @Test
  public void testInternalZipSingleArray() {
    List<Integer> array1 = Arrays.asList(1, 2, 3);

    Object result = ArrayZipFunctionImpl.internalZip(array1);

    assertTrue(result instanceof List);
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> resultList = (List<Map<String, Object>>) result;

    assertEquals(3, resultList.size());

    // Check first element
    Map<String, Object> first = resultList.get(0);
    assertEquals(1, first.get("0"));
    assertEquals(1, first.size()); // Should only have one key

    // Check second element
    Map<String, Object> second = resultList.get(1);
    assertEquals(2, second.get("0"));
    assertEquals(1, second.size());

    // Check third element
    Map<String, Object> third = resultList.get(2);
    assertEquals(3, third.get("0"));
    assertEquals(1, third.size());
  }

  @Test
  public void testInternalZipNoArguments() {
    Object result = ArrayZipFunctionImpl.internalZip();

    assertTrue(result instanceof List);
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> resultList = (List<Map<String, Object>>) result;

    assertEquals(0, resultList.size());
  }

  @Test
  public void testInternalZipNullArguments() {
    Object result = ArrayZipFunctionImpl.internalZip((Object[]) null);

    assertTrue(result instanceof List);
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> resultList = (List<Map<String, Object>>) result;

    assertEquals(0, resultList.size());
  }

  @Test
  public void testInternalZipNonListArguments() {
    String notAList = "not a list";
    List<Integer> array1 = Arrays.asList(1, 2, 3);

    Object result = ArrayZipFunctionImpl.internalZip(notAList, array1);

    assertTrue(result instanceof List);
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> resultList = (List<Map<String, Object>>) result;

    assertEquals(0, resultList.size());
  }
}
