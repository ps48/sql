/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.sql.jdbc;

import static org.opensearch.sql.data.type.ExprCoreType.BOOLEAN;
import static org.opensearch.sql.data.type.ExprCoreType.BYTE;
import static org.opensearch.sql.data.type.ExprCoreType.DATE;
import static org.opensearch.sql.data.type.ExprCoreType.DOUBLE;
import static org.opensearch.sql.data.type.ExprCoreType.INTEGER;
import static org.opensearch.sql.data.type.ExprCoreType.LONG;
import static org.opensearch.sql.data.type.ExprCoreType.SHORT;
import static org.opensearch.sql.data.type.ExprCoreType.STRING;
import static org.opensearch.sql.data.type.ExprCoreType.TIME;
import static org.opensearch.sql.data.type.ExprCoreType.TIMESTAMP;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.opensearch.sql.data.model.ExprTupleValue;
import org.opensearch.sql.data.model.ExprValue;
import org.opensearch.sql.data.type.ExprType;
import org.opensearch.sql.exception.ExpressionEvaluationException;
import org.opensearch.sql.executor.ExecutionEngine;

public class JDBCResponseHandle {

  private final List<ColumnHandle> columnHandleList;

  @SneakyThrows
  public JDBCResponseHandle(ResultSetMetaData metaData) {
    columnHandleList = new ArrayList<>();
    int columnCount = metaData.getColumnCount();
    for (int i = 1; i <= columnCount; i++) {
      // the default type is STRING.
      ExprType exprType = jdbcTypeToCoreType(metaData.getColumnType(i)).orElse(STRING);
      columnHandleList.add(new ColumnHandle(i, metaData.getColumnName(i), exprType));
    }
  }

  public ExecutionEngine.Schema schema() {
    return new ExecutionEngine.Schema(
        columnHandleList.stream()
            .map(c -> new ExecutionEngine.Schema.Column(c.getName(), c.getName(), c.getType()))
            .collect(Collectors.toList()));
  }

  public ExprValue parse(ResultSet rs) {
    LinkedHashMap<String, ExprValue> result = new LinkedHashMap<>();

    for (ColumnHandle columnHandle : columnHandleList) {
      result.put(columnHandle.getName(), columnHandle.parse(rs));
    }
    return new ExprTupleValue(result);
  }

  @Getter
  @RequiredArgsConstructor
  static class ColumnHandle {
    private final int index;

    private final String name;

    private final ExprType type;

    @SneakyThrows
    ExprValue parse(ResultSet rs) {
      return new JDBCRowExprValue(rs.getObject(index));
    }
  }

  @RequiredArgsConstructor
  public static class JDBCRowExprValue implements ExprValue {

    private final Object value;

    @Override
    public Object value() {
      return value;
    }

    @Override
    public ExprType type() {
      throw new ExpressionEvaluationException("[BUG] - invalid to get type");
    }

    @Override
    public int compareTo(ExprValue o) {
      throw new ExpressionEvaluationException("[BUG] - invalid to compare");
    }
  }

  static Optional<ExprType> jdbcTypeToCoreType(int type) {
    switch (type) {
      case Types.BIT:
      case Types.BOOLEAN:
        return Optional.of(BOOLEAN);

      case Types.TINYINT:
        return Optional.of(BYTE);

      case Types.SMALLINT:
        return Optional.of(SHORT);

      case Types.INTEGER:
        return Optional.of(INTEGER);

      case Types.BIGINT:
        return Optional.of(LONG);

      case Types.REAL:
      case Types.FLOAT:
      case Types.DOUBLE:
      case Types.NUMERIC:
      case Types.DECIMAL:
        return Optional.of(DOUBLE);

      case Types.CHAR:
      case Types.NCHAR:
      case Types.VARCHAR:
      case Types.NVARCHAR:
      case Types.LONGVARCHAR:
      case Types.LONGNVARCHAR:
      case Types.BINARY:
      case Types.VARBINARY:
      case Types.LONGVARBINARY:
        return Optional.of(STRING);

      case Types.DATE:
        return Optional.of(DATE);

      case Types.TIME:
        return Optional.of(TIME);

      case Types.TIMESTAMP:
        return Optional.of(TIMESTAMP);

      // we assume the result is json encoded string. refer https://docs.cloudera.com/HDPDocuments/HDP2/HDP-2.0.0.2/ds_Hive/jdbc-hs2.html,
      case Types.ARRAY:
      case Types.JAVA_OBJECT:
      case Types.STRUCT:
        return Optional.of(STRING);
    }
    return Optional.empty();
  }
}
