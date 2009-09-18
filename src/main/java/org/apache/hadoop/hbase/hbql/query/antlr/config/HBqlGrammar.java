package org.apache.hadoop.hbase.hbql.query.antlr.config;

import com.imap4j.imap.antlr.util.GrammarDef;

/**
 * Created by IntelliJ IDEA.
 * User: pambrose
 * Date: Aug 20, 2009
 * Time: 9:57:05 PM
 */
public enum HBqlGrammar {

    HBql("HBql");

    private final static String HOME = "org.apache.hadoop.hbase.hbql.query.antlr.";
    private final GrammarDef grammarDef;

    HBqlGrammar(final String name) {
        this.grammarDef = GrammarDef.newInstance(HOME + name);
    }

    public GrammarDef getGrammarDef() {
        return grammarDef;
    }

    public com.imap4j.imap.antlr.util.Grammar newGrammar() {
        return this.getGrammarDef().newGrammar();
    }

}