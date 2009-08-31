package com.imap4j.hbase.hbql;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: pambrose
 * Date: Aug 20, 2009
 * Time: 10:39:59 PM
 */
public abstract class HQueryListenerAdapter<T extends HPersistable> implements HQueryListener<T>, Serializable {

    public void onQueryInit() {
    }

    public void onEachRow(T val) throws HPersistException {
    }

    public void onQueryCompletion() {
    }
}