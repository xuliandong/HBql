package org.apache.hadoop.hbase.hbql.query.expr.value.literal;

import org.apache.hadoop.hbase.hbql.client.HBqlException;
import org.apache.hadoop.hbase.hbql.query.expr.node.BooleanValue;
import org.apache.hadoop.hbase.hbql.query.expr.node.GenericValue;
import org.apache.hadoop.hbase.hbql.query.expr.value.GenericExpr;

/**
 * Created by IntelliJ IDEA.
 * User: pambrose
 * Date: Aug 29, 2009
 * Time: 2:35:57 PM
 */
public class BooleanExpr extends GenericExpr implements BooleanValue {

    public BooleanExpr(final GenericValue arg0) {
        super(Type.BOOLEANEXPR, arg0);
    }

    @Override
    public Boolean getValue(final Object object) throws HBqlException {
        return (Boolean)this.getArg(0).getValue(object);
    }

    @Override
    public String asString() {
        return this.getArg(0).asString();
    }
}