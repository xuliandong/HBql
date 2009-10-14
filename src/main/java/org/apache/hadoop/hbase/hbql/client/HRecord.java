package org.apache.hadoop.hbase.hbql.client;

import java.util.Map;
import java.util.NavigableMap;

/**
 * Created by IntelliJ IDEA.
 * User: pambrose
 * Date: Oct 12, 2009
 * Time: 12:15:54 AM
 */
public interface HRecord {

    Object getCurrentValue(String name) throws HBqlException;

    void setCurrentValue(String name, Object val) throws HBqlException;

    Map<Long, Object> getVersionMap(final String name) throws HBqlException;

    Map<Long, Object> getKeysAsColumnsVersionMap(String name, String mapKey) throws HBqlException;

    Map<String, NavigableMap<Long, byte[]>> getFamilyDefaultVersionMap(String name) throws HBqlException;

    Map<String, byte[]> getFamilyDefaultValueMap(String name) throws HBqlException;
}
