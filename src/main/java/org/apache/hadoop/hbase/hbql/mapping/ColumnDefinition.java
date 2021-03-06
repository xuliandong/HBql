/*
 * Copyright (c) 2011.  The Apache Software Foundation
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.hbase.hbql.mapping;

import org.apache.expreval.expr.node.GenericValue;
import org.apache.hadoop.hbase.hbql.client.HBqlException;
import org.apache.hadoop.hbase.hbql.impl.Utils;
import org.apache.hadoop.hbase.hbql.statement.args.ColumnWidth;
import org.apache.hadoop.hbase.hbql.statement.args.KeyInfo;

import java.io.Serializable;
import java.lang.reflect.Field;


public final class ColumnDefinition implements Serializable {

    private static final long serialVersionUID = 1L;

    private           String       columnName;
    private           String       aliasName;
    private           boolean      isArray;
    private           FieldType    fieldType;
    private transient GenericValue defaultValue;
    private           ColumnWidth  columnWidth;
    private           String       getter;
    private           String       setter;

    private FamilyMapping familyMapping = null;

    public ColumnDefinition() {
    }

    private ColumnDefinition(final String familyName,
                             final String columnName,
                             final String aliasName,
                             final FieldType fieldType,
                             final boolean isArray,
                             final ColumnWidth columnWidth,
                             final GenericValue defaultValue,
                             final String getter,
                             final String setter) {

        this.columnName = columnName;
        this.fieldType = fieldType;
        this.isArray = isArray;
        this.columnWidth = columnWidth != null ? columnWidth : new ColumnWidth();
        this.aliasName = aliasName;
        this.defaultValue = defaultValue;
        this.getter = getter;
        this.setter = setter;

        if (familyName != null)
            this.setFamilyMapping(new FamilyMapping(familyName, false, null));
    }

    // For KEY attribs
    public static ColumnDefinition newKeyColumn(final KeyInfo keyInfo) {
        return new ColumnDefinition("",
                                    keyInfo.getKeyName(),
                                    keyInfo.getKeyName(),
                                    FieldType.KeyType,
                                    false,
                                    keyInfo,
                                    null,
                                    null,
                                    null);
    }

    // For Family Default attribs
    public static ColumnDefinition newUnMappedColumn(final String familyName) {
        return new ColumnDefinition(familyName, "", familyName, null, false, null, null, null, null);
    }

    // For regular attribs
    public static ColumnDefinition newMappedColumn(final String columnName,
                                                   final String typeName,
                                                   final boolean isArray,
                                                   final ColumnWidth columnWidth,
                                                   final String aliasName,
                                                   final GenericValue defaultValue) {
        final FieldType fieldType = getFieldType(typeName);
        return new ColumnDefinition(null,
                                    columnName,
                                    aliasName,
                                    fieldType,
                                    isArray,
                                    columnWidth,
                                    defaultValue,
                                    null,
                                    null);
    }

    // For FieldAttrib columns
    public static ColumnDefinition newFieldAttribColumn(final String familyName,
                                                        final String columnName,
                                                        final Field field,
                                                        final FieldType fieldType,
                                                        final String getter,
                                                        final String setter) {
        return new ColumnDefinition(familyName,
                                    Utils.isValidString(columnName) ? columnName : field.getName(),
                                    field.getName(),
                                    fieldType,
                                    field.getType().isArray(),
                                    null,
                                    null,
                                    getter,
                                    setter);
    }

    // For regular attribs
    public static ColumnDefinition newIndexedColumn(final String columnName, final String typeName) {
        final FieldType fieldType = getFieldType(typeName);
        return new ColumnDefinition(null,
                                    columnName,
                                    null,
                                    fieldType,
                                    false,
                                    null,
                                    null,
                                    null,
                                    null);
    }


    // For SelectFamilyAttrib columns
    public static ColumnDefinition newSelectFamilyAttribColumn(final String familyName) {
        return new ColumnDefinition(familyName, "", "", null, false, null, null, null, null);
    }

    private FamilyMapping getFamilyMapping() {
        return this.familyMapping;
    }

    void setFamilyMapping(final FamilyMapping familyMapping) {
        this.familyMapping = familyMapping;
    }

    private static FieldType getFieldType(final String typeName) {
        try {
            return FieldType.getFieldType(typeName);
        }
        catch (HBqlException e) {
            return null;
        }
    }

    public String getFamilyName() {
        return (this.getFamilyMapping() != null) ? this.getFamilyMapping().getFamilyName() : null;
    }

    // This is used for validating width of values
    public ColumnWidth getColumnWidth() {
        return this.columnWidth;
    }

    public String getColumnName() {
        return this.columnName;
    }

    public String getAliasName() {
        return this.aliasName;
    }

    public FieldType getFieldType() {
        return this.fieldType;
    }

    public boolean isAnArray() {
        return this.isArray;
    }

    public GenericValue getDefaultValue() {
        return this.defaultValue;
    }

    public String getGetter() {
        return this.getter;
    }

    public String getSetter() {
        return this.setter;
    }
}