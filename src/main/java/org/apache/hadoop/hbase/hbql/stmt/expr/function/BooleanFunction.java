package org.apache.hadoop.hbase.hbql.stmt.expr.function;

import org.apache.hadoop.hbase.hbql.client.HBqlException;
import org.apache.hadoop.hbase.hbql.client.ResultMissingColumnException;
import org.apache.hadoop.hbase.hbql.client.TypeException;
import org.apache.hadoop.hbase.hbql.stmt.antlr.HBql;
import org.apache.hadoop.hbase.hbql.stmt.expr.ExprContext;
import org.apache.hadoop.hbase.hbql.stmt.expr.ExprTree;
import org.apache.hadoop.hbase.hbql.stmt.expr.node.BooleanValue;
import org.apache.hadoop.hbase.hbql.stmt.expr.node.GenericValue;
import org.apache.hadoop.hbase.hbql.stmt.expr.var.DelegateColumn;
import org.apache.hadoop.hbase.hbql.stmt.schema.Schema;

import java.util.List;

public class BooleanFunction extends Function implements BooleanValue {

    private Schema schema = null;

    public BooleanFunction(final FunctionType functionType, final GenericValue... exprs) {
        super(functionType, exprs);
    }

    public BooleanFunction(final FunctionType functionType, final List<GenericValue> exprs) {
        super(functionType, exprs);
    }

    public Class<? extends GenericValue> validateTypes(final GenericValue parentExpr,
                                                       final boolean allowsCollections) throws HBqlException {

        switch (this.getFunctionType()) {

            case DEFINEDINROW: {
                if (!(this.getArg(0) instanceof DelegateColumn))
                    throw new TypeException("Argument should be a column reference in: " + this.asString());
            }
        }
        return BooleanValue.class;
        //return super.validateTypes(parentExpr, allowsCollections);
    }

    public void setExprContext(final ExprContext context) throws HBqlException {
        super.setExprContext(context);
        this.schema = context.getSchema();
    }

    private Schema getSchema() {
        return this.schema;
    }

    public Boolean getValue(final Object object) throws HBqlException, ResultMissingColumnException {

        switch (this.getFunctionType()) {

            case DEFINEDINROW: {
                try {
                    this.getArg(0).getValue(object);
                    return true;
                }
                catch (ResultMissingColumnException e) {
                    return false;
                }
            }

            case EVAL: {
                final String exprStr = (String)this.getArg(0).getValue(object);
                final ExprTree exprTree = HBql.parseWhereExpression(exprStr, this.getSchema());
                return exprTree.evaluate(object);
            }

            default:
                throw new HBqlException("Invalid function: " + this.getFunctionType());
        }
    }
}