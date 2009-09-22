package org.apache.hadoop.hbase.hbql.query.expr.predicate;

import org.apache.hadoop.hbase.hbql.client.HPersistException;
import org.apache.hadoop.hbase.hbql.query.expr.ExprTree;
import org.apache.hadoop.hbase.hbql.query.expr.ExprVariable;
import org.apache.hadoop.hbase.hbql.query.expr.node.BooleanValue;
import org.apache.hadoop.hbase.hbql.query.expr.value.literal.BooleanLiteral;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: pambrose
 * Date: Aug 25, 2009
 * Time: 6:58:31 PM
 */
public class CompareExpr implements BooleanValue {

    public enum OP {
        AND,
        OR
    }

    private BooleanValue expr1 = null, expr2 = null;
    private final CompareExpr.OP op;

    public CompareExpr(final BooleanValue expr1, final CompareExpr.OP op, final BooleanValue expr2) {
        this.expr1 = expr1;
        this.op = op;
        this.expr2 = expr2;
    }

    private BooleanValue getExpr1() {
        return expr1;
    }

    private BooleanValue getExpr2() {
        return expr2;
    }

    private OP getOp() {
        return op;
    }

    @Override
    public List<ExprVariable> getExprVariables() {
        final List<ExprVariable> retval = this.getExpr1().getExprVariables();
        retval.addAll(this.getExpr2().getExprVariables());
        return retval;
    }

    @Override
    public boolean optimizeForConstants(final Object object) throws HPersistException {

        boolean retval = true;

        if (this.getExpr1().optimizeForConstants(object))
            this.expr1 = new BooleanLiteral(this.getExpr1().getValue(object));
        else
            retval = false;

        if (this.getExpr2() != null) {
            if (this.getExpr2().optimizeForConstants(object))
                this.expr2 = new BooleanLiteral(this.getExpr2().getValue(object));
            else
                retval = false;
        }

        return retval;
    }

    @Override
    public Boolean getValue(final Object object) throws HPersistException {

        final boolean expr1val = this.getExpr1().getValue(object);

        if (this.getExpr2() == null)
            return expr1val;

        switch (this.getOp()) {
            case OR:
                return expr1val || this.getExpr2().getValue(object);
            case AND:
                return expr1val && this.getExpr2().getValue(object);

            default:
                throw new HPersistException("Error in BooleanExpr.evaluate()");

        }
    }

    @Override
    public boolean isAConstant() {
        return this.getExpr1().isAConstant() && this.getExpr2().isAConstant();
    }

    @Override
    public void setContext(final ExprTree context) {
        this.getExpr1().setContext(context);
        this.getExpr2().setContext(context);
    }
}
