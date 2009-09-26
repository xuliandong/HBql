package org.apache.hadoop.hbase.hbql.query.expr.value.func;

import org.apache.hadoop.hbase.hbql.client.HBqlException;
import org.apache.hadoop.hbase.hbql.client.TypeException;
import org.apache.hadoop.hbase.hbql.query.expr.ExprTree;
import org.apache.hadoop.hbase.hbql.query.expr.node.BooleanValue;
import org.apache.hadoop.hbase.hbql.query.expr.node.ValueExpr;
import org.apache.hadoop.hbase.hbql.query.expr.value.literal.BooleanLiteral;
import org.apache.hadoop.hbase.hbql.query.schema.HUtil;

/**
 * Created by IntelliJ IDEA.
 * User: pambrose
 * Date: Aug 31, 2009
 * Time: 2:00:25 PM
 */
public abstract class GenericBetweenStmt extends GenericNotValue {

    private ValueExpr expr = null;
    private ValueExpr lower = null, upper = null;

    protected GenericBetweenStmt(final boolean not, final ValueExpr expr, final ValueExpr lower, final ValueExpr upper) {
        super(not);
        this.expr = expr;
        this.lower = lower;
        this.upper = upper;
    }

    protected ValueExpr getExpr() {
        return this.expr;
    }

    protected ValueExpr getLower() {
        return this.lower;
    }

    protected ValueExpr getUpper() {
        return this.upper;
    }

    public void setExpr(final ValueExpr expr) {
        this.expr = expr;
    }

    public void setLower(final ValueExpr lower) {
        this.lower = lower;
    }

    public void setUpper(final ValueExpr upper) {
        this.upper = upper;
    }

    @Override
    public ValueExpr getOptimizedValue() throws HBqlException {

        this.setExpr(this.getExpr().getOptimizedValue());
        this.setLower(this.getLower().getOptimizedValue());
        this.setUpper(this.getUpper().getOptimizedValue());

        return this.isAConstant() ? new BooleanLiteral(this.getValue(null)) : this;
    }

    @Override
    public boolean isAConstant() {
        return this.getExpr().isAConstant() && this.getLower().isAConstant() && this.getUpper().isAConstant();
    }

    @Override
    public void setContext(final ExprTree context) {
        this.getExpr().setContext(context);
        this.getLower().setContext(context);
        this.getUpper().setContext(context);
    }

    protected Class<? extends ValueExpr> validateType(final Class<? extends ValueExpr> clazz) throws TypeException {
        HUtil.validateParentClass(this,
                                  clazz,
                                  this.getExpr().validateTypes(this, false),
                                  this.getLower().validateTypes(this, false),
                                  this.getUpper().validateTypes(this, false));
        return BooleanValue.class;
    }

    @Override
    public String asString() {
        return this.getExpr().asString() + notAsString() + " BETWEEN "
               + this.getLower().asString() + " AND " + this.getUpper().asString();
    }
}
