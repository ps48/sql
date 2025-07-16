/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.sql.expression.function.CollectionUDF;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.calcite.adapter.enumerable.NotNullImplementor;
import org.apache.calcite.adapter.enumerable.NullPolicy;
import org.apache.calcite.adapter.enumerable.RexToLixTranslator;
import org.apache.calcite.linq4j.tree.Expression;
import org.apache.calcite.linq4j.tree.Expressions;
import org.apache.calcite.linq4j.tree.Types;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rex.RexCall;
import org.apache.calcite.sql.fun.SqlLibraryOperators;
import org.apache.calcite.sql.type.SqlReturnTypeInference;
import org.opensearch.sql.expression.function.ImplementorUDF;
import org.opensearch.sql.expression.function.UDFOperandMetadata;

/**
 * Implementation of ARRAY_ZIP function that combines multiple arrays element-wise
 * into an array of struct objects using Calcite's ARRAYS_ZIP operator.
 */
public class ArrayZipFunctionImpl extends ImplementorUDF {
  
  public ArrayZipFunctionImpl() {
    super(new ArrayZipImplementor(), NullPolicy.ANY);
  }

  @Override
  public SqlReturnTypeInference getReturnTypeInference() {
    return sqlOperatorBinding -> {
      RelDataTypeFactory typeFactory = sqlOperatorBinding.getTypeFactory();
      try {
        // Use Calcite's ARRAYS_ZIP return type inference
        return SqlLibraryOperators.ARRAYS_ZIP.getReturnTypeInference()
            .inferReturnType(sqlOperatorBinding);
      } catch (Exception e) {
        throw new RuntimeException("Failed to infer return type for array_zip: " + e.getMessage());
      }
    };
  }

  @Override
  public UDFOperandMetadata getOperandMetadata() {
    return null; // Allow variable number of array arguments
  }

  public static class ArrayZipImplementor implements NotNullImplementor {
    @Override
    public Expression implement(
        RexToLixTranslator translator, RexCall call, List<Expression> translatedOperands) {
      // Call our internal zip implementation
      return Expressions.call(
          Types.lookupMethod(ArrayZipFunctionImpl.class, "internalZip", Object[].class), 
          translatedOperands);
    }
  }

  /**
   * Internal implementation of array zip functionality.
   * Takes multiple arrays and returns an array of structs (represented as Maps).
   */
  public static Object internalZip(Object... arrays) {
    if (arrays == null || arrays.length == 0) {
      return new ArrayList<>();
    }

    // Convert all inputs to lists
    List<List<Object>> inputLists = new ArrayList<>();
    int minLength = Integer.MAX_VALUE;
    
    for (Object array : arrays) {
      if (array instanceof List) {
        List<Object> list = (List<Object>) array;
        inputLists.add(list);
        minLength = Math.min(minLength, list.size());
      } else {
        // Handle non-list inputs gracefully
        return new ArrayList<>();
      }
    }

    // If any array is empty, return empty result
    if (minLength == 0) {
      return new ArrayList<>();
    }

    // Create result array of structs (Maps)
    List<Map<String, Object>> result = new ArrayList<>();
    
    for (int i = 0; i < minLength; i++) {
      Map<String, Object> struct = new HashMap<>();
      for (int j = 0; j < inputLists.size(); j++) {
        // Use indexed field names: "0", "1", "2", etc.
        struct.put(String.valueOf(j), inputLists.get(j).get(i));
      }
      result.add(struct);
    }

    return result;
  }
}
