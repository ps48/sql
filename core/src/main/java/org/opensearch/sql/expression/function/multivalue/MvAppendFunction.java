/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.sql.expression.function.multivalue;

import static org.apache.calcite.sql.type.SqlTypeUtil.createArrayType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.calcite.adapter.enumerable.NotNullImplementor;
import org.apache.calcite.adapter.enumerable.NullPolicy;
import org.apache.calcite.adapter.enumerable.RexToLixTranslator;
import org.apache.calcite.linq4j.tree.Expression;
import org.apache.calcite.linq4j.tree.Expressions;
import org.apache.calcite.linq4j.tree.Types;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rex.RexCall;
import org.apache.calcite.sql.type.SqlReturnTypeInference;
import org.apache.calcite.sql.type.SqlTypeName;
import org.opensearch.sql.expression.function.ImplementorUDF;
import org.opensearch.sql.expression.function.UDFOperandMetadata;

/**
 * Implementation of mvappend function that combines one or more values into a single multivalue field.
 * 
 * <p>Syntax: mvappend(value1, value2, ..., valueN)
 * 
 * <p>Features:
 * - Accepts variable number of arguments
 * - Flattens nested arrays into a single array
 * - Handles mixed types by converting to strings
 * - Preserves argument order
 * - Includes null values in the result
 */
public class MvAppendFunction extends ImplementorUDF {
  
  public MvAppendFunction() {
    super(new MvAppendImplementor(), NullPolicy.ANY);
  }

  /**
   * Return type inference for mvappend function.
   * Creates an array type that can hold nullable elements of any type.
   */
  @Override
  public SqlReturnTypeInference getReturnTypeInference() {
    return sqlOperatorBinding -> {
      RelDataTypeFactory typeFactory = sqlOperatorBinding.getTypeFactory();
      try {
        // Create array type with VARCHAR component type to handle mixed types
        RelDataType componentType = typeFactory.createSqlType(SqlTypeName.VARCHAR);
        RelDataType nullableComponentType = typeFactory.createTypeWithNullability(componentType, true);
        return createArrayType(typeFactory, nullableComponentType, true);
      } catch (Exception e) {
        throw new RuntimeException("Failed to create mvappend return type: " + e.getMessage());
      }
    };
  }

  @Override
  public UDFOperandMetadata getOperandMetadata() {
    return null;
  }

  /**
   * Implementor class that handles the actual mvappend logic during query execution.
   */
  public static class MvAppendImplementor implements NotNullImplementor {
    @Override
    public Expression implement(
        RexToLixTranslator translator, RexCall call, List<Expression> translatedOperands) {
      
      // Create arguments for the mvappend method call
      List<Expression> newArgs = new ArrayList<>();
      
      // Convert operands to Object array for variable arguments
      Expression operandsArray = Expressions.newArrayInit(
          Object.class, translatedOperands);
      newArgs.add(operandsArray);
      
      // Call the static mvappend method
      return Expressions.call(
          Types.lookupMethod(MvAppendFunction.class, "mvappend", Object[].class), 
          newArgs);
    }
  }

  /**
   * Static method that performs the actual mvappend logic.
   * This method is called at runtime to combine the input values.
   * 
   * @param args Variable number of arguments to append
   * @return List containing all flattened values
   */
  public static List<Object> mvappend(Object... args) {
    List<Object> result = new ArrayList<>();
    
    for (Object arg : args) {
      if (arg == null) {
        // Include null values in the result
        result.add(null);
      } else if (arg instanceof List) {
        // Flatten arrays by adding all their elements
        List<?> list = (List<?>) arg;
        for (Object item : list) {
          result.add(convertToString(item));
        }
      } else if (arg.getClass().isArray()) {
        // Handle primitive arrays
        Object[] array = (Object[]) arg;
        for (Object item : array) {
          result.add(convertToString(item));
        }
      } else {
        // Add single values, converting to string for consistency
        result.add(convertToString(arg));
      }
    }
    
    return result;
  }
  
  /**
   * Converts a value to string representation for consistent handling of mixed types.
   * 
   * @param value The value to convert
   * @return String representation of the value, or null if input is null
   */
  private static Object convertToString(Object value) {
    if (value == null) {
      return null;
    }
    
    // Handle different types appropriately
    if (value instanceof String) {
      return value;
    } else if (value instanceof Number) {
      // Preserve numeric precision in string form
      if (value instanceof BigDecimal) {
        return ((BigDecimal) value).toPlainString();
      } else if (value instanceof Double) {
        Double d = (Double) value;
        // Handle special cases
        if (d.isInfinite() || d.isNaN()) {
          return d.toString();
        }
        // Handle -0.0 case
        if (d == 0.0) {
          return "0";
        }
        // Use plain string representation to avoid scientific notation
        return String.valueOf(d);
      } else if (value instanceof Float) {
        Float f = (Float) value;
        // Handle special cases
        if (f.isInfinite() || f.isNaN()) {
          return f.toString();
        }
        // Handle -0.0 case
        if (f == 0.0f) {
          return "0";
        }
        // Use plain string representation
        return String.valueOf(f);
      } else {
        return value.toString();
      }
    } else if (value instanceof Boolean) {
      return value.toString();
    } else {
      // For any other type, use toString()
      return value.toString();
    }
  }
}
