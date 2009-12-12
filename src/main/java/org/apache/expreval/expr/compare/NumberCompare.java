/*
 * Copyright (c) 2009.  The Apache Software Foundation
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

package org.apache.expreval.expr.compare;

import org.apache.expreval.client.ResultMissingColumnException;
import org.apache.expreval.expr.Operator;
import org.apache.expreval.expr.node.GenericValue;
import org.apache.expreval.expr.node.NumberValue;
import org.apache.expreval.expr.var.DelegateColumn;
import org.apache.expreval.expr.var.GenericColumn;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.WritableByteArrayComparable;
import org.apache.hadoop.hbase.hbql.client.HBqlException;
import org.apache.hadoop.hbase.hbql.impl.HConnectionImpl;
import org.apache.hadoop.hbase.hbql.io.IO;
import org.apache.hadoop.hbase.hbql.mapping.FieldType;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class NumberCompare extends GenericCompare {

    public NumberCompare(final GenericValue arg0, final Operator operator, final GenericValue arg1) {
        super(arg0, operator, arg1);
    }

    public Class<? extends GenericValue> validateTypes(final GenericValue parentExpr,
                                                       final boolean allowCollections) throws HBqlException {
        return this.validateType(NumberValue.class);
    }

    public Boolean getValue(final HConnectionImpl connection,
                            final Object object) throws HBqlException, ResultMissingColumnException {

        final Object obj0 = this.getValue(0, connection, object);
        final Object obj1 = this.getValue(1, connection, object);

        this.validateNumericArgTypes(obj0, obj1);

        if (!this.useDecimal()) {

            final long val0 = ((Number)obj0).longValue();
            final long val1 = ((Number)obj1).longValue();

            switch (this.getOperator()) {
                case EQ:
                    return val0 == val1;
                case GT:
                    return val0 > val1;
                case GTEQ:
                    return val0 >= val1;
                case LT:
                    return val0 < val1;
                case LTEQ:
                    return val0 <= val1;
                case NOTEQ:
                    return val0 != val1;
                default:
                    throw new HBqlException("Invalid operator: " + this.getOperator());
            }
        }
        else {

            final double val0 = ((Number)obj0).doubleValue();
            final double val1 = ((Number)obj1).doubleValue();

            switch (this.getOperator()) {
                case EQ:
                    return val0 == val1;
                case GT:
                    return val0 > val1;
                case GTEQ:
                    return val0 >= val1;
                case LT:
                    return val0 < val1;
                case LTEQ:
                    return val0 <= val1;
                case NOTEQ:
                    return val0 != val1;
                default:
                    throw new HBqlException("Invalid operator: " + this.getOperator());
            }
        }
    }

    public Filter getFilter() throws HBqlException {

        final GenericColumn<? extends GenericValue> column;
        final Object constant;
        final CompareFilter.CompareOp compareOp;
        final WritableByteArrayComparable comparator;

        this.validateArgsForColumnConstant();

        if (this.getExprArg(0).isAColumnReference()) {
            column = ((DelegateColumn)this.getExprArg(0)).getTypedColumn();
            constant = this.getConstantValue(1);
            compareOp = this.getOperator().getCompareOpLeft();
        }
        else {
            column = ((DelegateColumn)this.getExprArg(1)).getTypedColumn();
            constant = this.getConstantValue(0);
            compareOp = this.getOperator().getCompareOpRight();
        }

        this.validateNumericArgTypes(constant);

        if (!this.useDecimal()) {
            final long val = ((Number)constant).longValue();
            comparator = new LongComparable(column.getColumnAttrib().getFieldType(), val);
        }
        else {
            final double val = ((Number)constant).doubleValue();
            comparator = new DoubleComparable(column.getColumnAttrib().getFieldType(), val);
        }

        return this.newSingleColumnValueFilter(column, compareOp, comparator);
    }

    public static abstract class NumberComparable<T> extends GenericComparable<T> {

        private FieldType fieldType;

        protected void setFieldType(final FieldType fieldType) {
            this.fieldType = fieldType;
        }

        protected FieldType getFieldType() {
            return this.fieldType;
        }

        protected void setValueInBytes(final Number val) throws IOException {
            try {
                this.setValueInBytes(IO.getSerialization().getNumbeEqualityBytes(this.getFieldType(), val));
            }
            catch (HBqlException e) {
                throw new IOException(e.getMessage());
            }
        }
    }

    public static class LongComparable extends NumberComparable<Long> {


        public LongComparable() {
        }

        public LongComparable(final FieldType fieldType, final long value) {
            this.setFieldType(fieldType);
            this.setValue(value);
        }

        public int compareTo(final byte[] bytes) {

            if (this.equalValues(bytes))
                return 0;

            try {
                long columnValue = IO.getSerialization().getNumberFromBytes(this.getFieldType(), bytes).longValue();
                return (columnValue > this.getValue()) ? -1 : 1;
            }
            catch (HBqlException e) {
                e.printStackTrace();
                return 0;
            }
        }

        public void write(final DataOutput dataOutput) throws IOException {
            dataOutput.writeUTF(this.getFieldType().name());
            dataOutput.writeLong(this.getValue());
        }

        public void readFields(final DataInput dataInput) throws IOException {
            this.setFieldType(FieldType.valueOf(dataInput.readUTF()));
            this.setValue(dataInput.readLong());

            this.setValueInBytes(this.getValue());
        }
    }

    public static class DoubleComparable extends NumberComparable<Double> {

        public DoubleComparable() {
        }

        public DoubleComparable(final FieldType fieldType, final double value) {
            this.setFieldType(fieldType);
            this.setValue(value);
        }

        public int compareTo(final byte[] bytes) {

            if (this.equalValues(bytes))
                return 0;

            try {
                double columnValue = IO.getSerialization().getNumberFromBytes(this.getFieldType(), bytes).doubleValue();
                return (columnValue > this.getValue()) ? -1 : 1;
            }
            catch (HBqlException e) {
                e.printStackTrace();
                return 0;
            }
        }

        public void write(final DataOutput dataOutput) throws IOException {
            dataOutput.writeUTF(this.getFieldType().name());
            dataOutput.writeDouble(this.getValue());
        }

        public void readFields(final DataInput dataInput) throws IOException {
            this.setFieldType(FieldType.valueOf(dataInput.readUTF()));
            this.setValue(dataInput.readDouble());

            this.setValueInBytes(this.getValue());
        }
    }
}