package com.imap4j.hbase.antlr.args;

import com.imap4j.hbase.hbql.expr.predicate.ExprEvalTree;

/**
 * Created by IntelliJ IDEA.
 * User: pambrose
 * Date: Aug 20, 2009
 * Time: 11:43:49 PM
 */
public class DeleteArgs implements ExecArgs {

    private final String tableName;
    private final ExprEvalTree filterExpr;
    private final ExprEvalTree whereExpr;

    public DeleteArgs(final String tableName, final ExprEvalTree filterExpr, final ExprEvalTree whereExpr) {
        this.tableName = tableName;
        this.filterExpr = filterExpr;
        this.whereExpr = whereExpr;
    }

    public String getTableName() {
        return this.tableName;
    }

    public ExprEvalTree getWhereExpr() {
        return this.whereExpr;
    }

    public ExprEvalTree getFilterExpr() {
        return filterExpr;
    }
}