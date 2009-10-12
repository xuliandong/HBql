package org.apache.hadoop.hbase.hbql.query.stmt.select;

import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.hbql.client.HBqlException;
import org.apache.hadoop.hbase.hbql.client.HConnection;
import org.apache.hadoop.hbase.hbql.client.HRecord;
import org.apache.hadoop.hbase.hbql.query.expr.ExprContext;
import org.apache.hadoop.hbase.hbql.query.expr.node.GenericValue;
import org.apache.hadoop.hbase.hbql.query.expr.value.var.DelegateColumn;
import org.apache.hadoop.hbase.hbql.query.schema.ColumnAttrib;
import org.apache.hadoop.hbase.hbql.query.schema.HBaseSchema;
import org.apache.hadoop.hbase.hbql.query.util.HUtil;
import org.apache.hadoop.hbase.hbql.query.util.Maps;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

public class ExprSelectElement extends ExprContext implements SelectElement {

    private final String asName;

    private ColumnAttrib columnAttrib = null;
    private String familyName = null;
    private String columnName = null;
    private byte[] familyNameBytes = null;
    private byte[] columnNameBytes = null;

    public ExprSelectElement(final GenericValue genericValue, final String asName) {
        super(null, genericValue);
        this.asName = (asName != null) ? asName : genericValue.asString();
    }

    public static ExprSelectElement newExprElement(final GenericValue expr, final String as) {
        return new ExprSelectElement(expr, as);
    }

    public String getAsName() {
        return this.asName;
    }

    public boolean isSimpleColumnReference() {
        return this.getGenericValue(0) instanceof DelegateColumn;
    }

    public ColumnAttrib getColumnAttrib() {
        return this.columnAttrib;
    }

    public String getFamilyName() {
        return this.familyName;
    }

    public String getColumnName() {
        return this.columnName;
    }

    public byte[] getFamilyNameBytes() {
        return this.familyNameBytes;
    }

    public byte[] getColumnNameBytes() {
        return this.columnNameBytes;
    }

    public void validate(final HConnection connection,
                         final HBaseSchema schema,
                         final List<ColumnAttrib> selectAttribList) throws HBqlException {

        this.setSchema(schema);

        selectAttribList.addAll(this.getAttribsUsedInExpr());

        // Look up stuff for simple column references
        if (this.getGenericValue(0) instanceof DelegateColumn) {

            final String name = ((DelegateColumn)this.getGenericValue(0)).getVariableName();
            this.columnAttrib = this.getSchema().getAttribByVariableName(name);

            if (this.getColumnAttrib() != null) {
                this.familyName = this.getColumnAttrib().getFamilyName();
                this.columnName = this.getColumnAttrib().getColumnName();
            }
            else {
                if (!name.contains(":"))
                    throw new HBqlException("Invalid select value: " + name);
                final String[] strs = name.split(":");
                this.familyName = strs[0];
                this.columnName = strs[1];
                final Collection<String> families = this.getSchema().getAllSchemaFamilyNames(connection);
                if (!families.contains(this.getFamilyName()))
                    throw new HBqlException("Unknown famioy name: " + this.getFamilyName());
            }
            this.familyNameBytes = HUtil.ser.getStringAsBytes(this.getFamilyName());
            this.columnNameBytes = HUtil.ser.getStringAsBytes(this.getColumnName());
        }
    }

    private Map getMapKeysAsColumnsValue(final Result result) throws HBqlException {

        final NavigableMap<byte[], byte[]> columnMap = result.getFamilyMap(this.getFamilyNameBytes());

        Map mapval = Maps.newHashMap();

        for (final byte[] columnBytes : columnMap.keySet()) {

            final String columnName = HUtil.ser.getStringFromBytes(columnBytes);

            if (columnName.endsWith("]")) {
                final int lbrace = columnName.indexOf("[");
                final String mapcolumn = columnName.substring(0, lbrace);

                if (mapcolumn.equals(this.getColumnName())) {
                    final String mapKey = columnName.substring(lbrace + 1, columnName.length() - 1);

                    final byte[] b = columnMap.get(columnBytes);
                    final Object val = this.getColumnAttrib().getValueFromBytes(null, b);

                    mapval.put(mapKey, val);
                }
            }
        }
        return mapval;
    }

    private String getSelectName() {
        if (this.getAsName() == null || this.getAsName().length() == 0)
            return this.getFamilyName() + ":" + this.getColumnName();
        else
            return this.getAsName();
    }

    private byte[] getResultCurrentValue(final Result result) {

        final NavigableMap<byte[], byte[]> columnMap = result.getFamilyMap(this.getFamilyNameBytes());

        // ColumnMap should not be null at this point, but check just in case
        if (columnMap == null)
            return null;
        else
            return columnMap.get(this.getColumnNameBytes());
    }

    private void assignCalculation(final Object newobj, final Result result) throws HBqlException {
        // If it is a calculation, then assign according to the AS name
        final String name = this.getAsName();
        final ColumnAttrib attrib = this.getSchema().getAttribByVariableName(name);

        if (attrib == null) {
            // Find value in results and assign the byte[] value to HRecord, but bail on Annotated object
            if (!(newobj instanceof HRecord))
                return;

            final HRecord hrecord = (HRecord)newobj;
            hrecord.setCurrentFamilyDefaultValue(this.getFamilyName(),
                                                 this.getSelectName(),
                                                 0,
                                                 result.getValue(this.getFamilyNameBytes(), this.getColumnNameBytes()));
        }
        else {
            final Object elementValue = this.getValue(result);
            attrib.setCurrentValue(newobj, 0, elementValue);
        }
    }

    public void assignValues(final Object newobj,
                             final List<ColumnAttrib> selectAttribList,
                             final int maxVerions,
                             final Result result) throws HBqlException {

        // If it is a calculation, take care of it and then bail since calculations have no history
        if (!this.isSimpleColumnReference()) {
            this.assignCalculation(newobj, result);
            return;
        }

        // Column reference is not known to schema, so just assign byte[] value
        if (this.getColumnAttrib() == null) {

            // Find value in results and assign the byte[] value to HRecord, but bail on Annotated object
            if (!(newobj instanceof HRecord))
                return;

            ((HRecord)newobj).setCurrentFamilyDefaultValue(this.getFamilyName(),
                                                           this.getSelectName(),
                                                           0,
                                                           result.getValue(this.getFamilyNameBytes(),
                                                                           this.getColumnNameBytes()));
        }
        else {
            // Do not process if it is an annotation history value
            if (this.getColumnAttrib().isACurrentValue()) {

                // If this is a mapKeysAsColumns, then we need to build the map from all the related columns in the family
                if (this.getColumnAttrib().isMapKeysAsColumnsColumn()) {
                    final Map mapval = this.getMapKeysAsColumnsValue(result);
                    this.getColumnAttrib().setCurrentValue(newobj, 0, mapval);
                }
                else {
                    final byte[] b = result.getValue(this.getFamilyNameBytes(), this.getColumnNameBytes());
                    this.getColumnAttrib().setCurrentValue(newobj, 0, b);
                }
            }
        }

        // Now assign versions
        // Do not process if it doesn't support version values
        if (maxVerions > 1 && this.getColumnAttrib().isAVersionValue()) {

            final NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> familyMap = result.getMap();
            final NavigableMap<byte[], NavigableMap<Long, byte[]>> columnMap = familyMap.get(this.getFamilyNameBytes());

            if (columnMap == null)
                return;

            final NavigableMap<Long, byte[]> timeStampMap = columnMap.get(this.getColumnNameBytes());

            if (this.getColumnAttrib() == null) {

                // Find value in results and assign the byte[] value to HRecord, but bail on Annotated object
                if (!(newobj instanceof HRecord))
                    return;

                ((HRecord)newobj).setVersionFamilyDefaultMap(this.getFamilyName(), this.getSelectName(), timeStampMap);
            }
            else {
                final Map<Long, Object> mapval = this.getColumnAttrib().getVersionObjectValueMap(newobj);

                for (final Long timestamp : timeStampMap.keySet()) {
                    final byte[] b = timeStampMap.get(timestamp);
                    final Object val = this.getColumnAttrib().getValueFromBytes(newobj, b);
                    mapval.put(timestamp, val);
                }
            }
        }
    }

    public Object getValue(final Result result) throws HBqlException {
        return this.evaluate(0, true, false, result);
    }

    public String asString() {
        return this.getGenericValue(0).asString();
    }

    public boolean useHBaseResult() {
        return true;
    }
}
