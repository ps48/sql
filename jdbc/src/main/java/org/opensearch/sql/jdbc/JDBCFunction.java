/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.sql.jdbc;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Supplier;
import org.opensearch.sql.data.model.ExprValue;
import org.opensearch.sql.data.type.ExprCoreType;
import org.opensearch.sql.data.type.ExprType;
import org.opensearch.sql.expression.Expression;
import org.opensearch.sql.expression.FunctionExpression;
import org.opensearch.sql.expression.env.Environment;
import org.opensearch.sql.expression.function.FunctionName;
import org.opensearch.sql.expression.function.TableFunctionImplementation;
import org.opensearch.sql.planner.DefaultImplementor;
import org.opensearch.sql.planner.logical.LogicalPlan;
import org.opensearch.sql.planner.logical.LogicalProject;
import org.opensearch.sql.planner.physical.PhysicalPlan;
import org.opensearch.sql.storage.Table;
import org.opensearch.sql.storage.TableScanOperator;
import org.opensearch.sql.storage.read.TableScanBuilder;

public class JDBCFunction extends FunctionExpression implements TableFunctionImplementation {

  private final Supplier<TableScanOperator> tableScanOperatorSupplier;

  public JDBCFunction(
      FunctionName functionName, String url, String sqlQuery, Properties properties) {
    super(functionName, List.of());
    tableScanOperatorSupplier = () -> new JDBCQueryOperator(url, sqlQuery, properties);
  }

  @Override
  public ExprValue valueOf(Environment<Expression, ExprValue> valueEnv) {
    throw new UnsupportedOperationException("JDBC function is only supported in source command");
  }

  @Override
  public ExprType type() {
    return ExprCoreType.STRUCT;
  }

  @Override
  public Table applyArguments() {
    return new JDBCTable();
  }

  @VisibleForTesting
  protected class JDBCTable implements Table {
    /**
     * return empty map at query analysis stage.
     */
    @Override
    public Map<String, ExprType> getFieldTypes() {
      return ImmutableMap.of();
    }

    // todo, the implement interface should be removed. create an issue....
    @Override
    public PhysicalPlan implement(LogicalPlan plan) {
      return plan.accept(new DefaultImplementor<Void>(), null);
    }

    @Override
    public TableScanBuilder createScanBuilder() {
      return new JDBCTableScanBuilder();
    }
  }

  @VisibleForTesting
  protected class JDBCTableScanBuilder extends TableScanBuilder {

    @Override
    public TableScanOperator build() {
      return tableScanOperatorSupplier.get();
    }

    /**
     * ignore project operator.
     */
    @Override
    public boolean pushDownProject(LogicalProject project) {
      return true;
    }
  }
}
