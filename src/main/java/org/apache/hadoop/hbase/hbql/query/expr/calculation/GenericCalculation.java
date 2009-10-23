package org.apache.hadoop.hbase.hbql.query.expr.calculation;

import org.apache.hadoop.hbase.hbql.query.expr.DelegateStmt;
import org.apache.hadoop.hbase.hbql.query.expr.Operator;
import org.apache.hadoop.hbase.hbql.query.expr.node.GenericValue;

public abstract class GenericCalculation extends DelegateStmt<GenericCalculation> {

    private final Operator operator;

    protected GenericCalculation(final Type type,
                                 final GenericValue arg0,
                                 final Operator operator,
                                 final GenericValue arg1) {
        super(type, arg0, arg1);
        this.operator = operator;
    }

    protected Operator getOperator() {
        return this.operator;
    }

    public String asString() {
        if (this.getOperator() == Operator.NEGATIVE)
            return "-" + this.getArg(0).asString();
        else
            return this.getArg(0).asString() + " " + this.getOperator() + " " + this.getArg(1).asString();
    }
}