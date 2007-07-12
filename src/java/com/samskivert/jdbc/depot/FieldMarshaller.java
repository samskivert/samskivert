//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2006-2007 Michael Bayne, Pär Winzell
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.samskivert.jdbc.depot;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;

import com.samskivert.jdbc.depot.annotation.Column;
import com.samskivert.jdbc.depot.annotation.Computed;
import com.samskivert.jdbc.depot.annotation.GeneratedValue;
import com.samskivert.jdbc.depot.annotation.Id;

import com.samskivert.util.StringUtil;

/**
 * Handles the marshalling and unmarshalling of a particular field of a persistent object.
 *
 * @see DepotMarshaller
 */
public abstract class FieldMarshaller<T>
{
    /**
     * Creates and returns a field marshaller for the specified field. Throws an exception if the
     * field in question cannot be marshalled.
     */
    public static FieldMarshaller createMarshaller (Field field)
    {
        Class<?> ftype = field.getType();
        FieldMarshaller marshaller;

        // primitive types
        if (ftype.equals(Boolean.TYPE)) {
            marshaller = new BooleanMarshaller();
        } else if (ftype.equals(Byte.TYPE)) {
            marshaller = new ByteMarshaller();
        } else if (ftype.equals(Short.TYPE)) {
            marshaller = new ShortMarshaller();
        } else if (ftype.equals(Integer.TYPE)) {
            marshaller = new IntMarshaller();
        } else if (ftype.equals(Long.TYPE)) {
            marshaller = new LongMarshaller();
        } else if (ftype.equals(Float.TYPE)) {
            marshaller = new FloatMarshaller();
        } else if (ftype.equals(Double.TYPE)) {
            marshaller = new DoubleMarshaller();

        // "natural" types
        } else if (ftype.equals(Byte.class) ||
            ftype.equals(Short.class) ||
            ftype.equals(Integer.class) ||
            ftype.equals(Long.class) ||
            ftype.equals(Float.class) ||
            ftype.equals(Double.class) ||
            ftype.equals(String.class)) {
            marshaller = new ObjectMarshaller();

        // some primitive array types
        } else if (ftype.equals(byte[].class)) {
            marshaller = new ByteArrayMarshaller();

        } else if (ftype.equals(int[].class)) {
            marshaller = new IntArrayMarshaller();

        // SQL types
        } else if (ftype.equals(Date.class) ||
            ftype.equals(Time.class) ||
            ftype.equals(Timestamp.class) ||
            ftype.equals(Blob.class) ||
            ftype.equals(Clob.class)) {
            marshaller = new ObjectMarshaller();

        // special Enum types
        } else if (ByteEnum.class.isAssignableFrom(ftype)) {
            marshaller = new ByteEnumMarshaller(ftype);

        } else {
            throw new IllegalArgumentException(
                "Cannot marshall field of type '" + ftype.getName() + "'.");
        }

        marshaller.init(field);
        return marshaller;
    }

    /**
     * Returns the {@link Field} handled by this marshaller.
     */
    public Field getField ()
    {
        return _field;
    }

    /**
     * Returns the Computed annotation on this field, if any.
     */
    public Computed getComputed ()
    {
        return _computed;
    }

    /**
     * Returns the GeneratedValue annotation on this field, if any.
     */
    public GeneratedValue getGeneratedValue ()
    {
        return _generatedValue;
    }

    /**
     * Returns the name of the table column used to store this field.
     */
    public String getColumnName ()
    {
        return _columnName;
    }

    /**
     * Returns the SQL used to define this field's column.
     */
    public String getColumnDefinition ()
    {
        return _columnDefinition;
    }

    /**
     * Reads and returns this field from the result set.
     */
    public abstract T getFromSet (ResultSet rs)
        throws SQLException;

    /**
     * Sets the specified column of the given prepared statement to the given value.
     */
    public abstract void writeToStatement (PreparedStatement ps, int column, T value)
        throws SQLException;

    /**
     * Reads this field from the given persistent object.
     */
    public abstract T getFromObject (Object po)
        throws IllegalArgumentException, IllegalAccessException;

    /**
     * Writes the given value to the given persistent value.
     */
    public abstract void writeToObject (Object po, T value)
        throws IllegalArgumentException, IllegalAccessException;

    /**
     * Reads the value of our field from the persistent object and sets that
     * value into the specified column of the supplied prepared statement.
     */
    public void readFromObject (Object po, PreparedStatement ps, int column)
        throws SQLException, IllegalAccessException
    {
        writeToStatement(ps, column, getFromObject(po));
    }

    /**
     * Reads the specified column from the supplied result set and writes it to
     * the appropriate field of the persistent object.
     */
    public void writeToObject (ResultSet rset, Object po)
        throws SQLException, IllegalAccessException
    {
        writeToObject(po, getFromSet(rset));
    }

    /**
     * Returns the type used in the SQL column definition for this field.
     */
    public abstract String getColumnType ();

    protected void init (Field field)
    {
        _field = field;
        _columnName = field.getName();

        // read our column metadata from the annotation (if it exists); annoyingly we can't create
        // a Column instance to read the defaults so we have to duplicate them here
        int length = 255;
        boolean nullable = false;
        boolean unique = false;
        String defval = "";
        Column column = _field.getAnnotation(Column.class);
        if (column != null) {
            nullable = column.nullable();
            unique = column.unique();
            length = column.length();
            defval = column.defaultValue();
            if (!StringUtil.isBlank(column.name())) {
                _columnName = column.name();
            }
        }

        _computed = field.getAnnotation(Computed.class);
        // if this field is @Computed, it has no SQL definition
        if (_computed != null) {
            _columnDefinition = null;
            return;
        }

        // create our SQL column definition
        StringBuilder builder = new StringBuilder();
        if (column != null && !StringUtil.isBlank(column.columnDefinition())) {
            builder.append(column.columnDefinition());

        } else {
            builder.append(getColumnName());
            String type = getColumnType();
            builder.append(" ").append(type);

            // if this is a VARCHAR field, add the length
            if (type.equals("VARCHAR") || type.equals("VARBINARY")) {
                builder.append("(").append(length).append(")");
            }

            // TODO: handle precision and scale

            // handle nullability and uniqueness
            if (!nullable) {
                builder.append(" NOT NULL");
            }
            if (unique) {
                builder.append(" UNIQUE");
            }

            // append the default value if one was specified
            if (defval.length() > 0) {
                builder.append(" DEFAULT ").append(defval);
            }
        }

        // handle primary keyness
        if (field.getAnnotation(Id.class) != null) {
            // figure out how we're going to generate our primary key values
            _generatedValue = field.getAnnotation(GeneratedValue.class);
            if (_generatedValue != null) {
                switch (_generatedValue.strategy()) {
                case AUTO:
                case IDENTITY:
                    builder.append(" AUTO_INCREMENT");
                    break;
                case SEQUENCE: // TODO
                    throw new IllegalArgumentException(
                        "SEQUENCE key generation strategy not yet supported.");
                case TABLE:
                    // nothing to do here, it'll be handled later
                    break;
                }
            }
        }

        _columnDefinition = builder.toString();
    }

    protected static class BooleanMarshaller extends FieldMarshaller<Boolean> {
        public String getColumnType () {
            return "TINYINT";
        }
        public Boolean getFromObject (Object po)
            throws IllegalArgumentException, IllegalAccessException {
            return _field.getBoolean(po);
        }
        public Boolean getFromSet (ResultSet rs)
            throws SQLException {
            return rs.getBoolean(getColumnName());
        }
        public void writeToObject (Object po, Boolean value)
            throws IllegalArgumentException, IllegalAccessException {
            _field.setBoolean(po, value);
        }
        public void writeToStatement (PreparedStatement ps, int column, Boolean value)
            throws SQLException {
            ps.setBoolean(column, value);
        }
    }

    protected static class ByteMarshaller extends FieldMarshaller<Byte> {
        public String getColumnType () {
            return "TINYINT";
        }
        public Byte getFromObject (Object po)
            throws IllegalArgumentException, IllegalAccessException {
            return _field.getByte(po);
        }
        public Byte getFromSet (ResultSet rs)
            throws SQLException {
            return rs.getByte(getColumnName());
        }
        public void writeToObject (Object po, Byte value)
            throws IllegalArgumentException, IllegalAccessException {
            _field.setByte(po, value);
        }
        public void writeToStatement (PreparedStatement ps, int column, Byte value)
            throws SQLException {
            ps.setByte(column, value);
        }
    }

    protected static class ShortMarshaller extends FieldMarshaller<Short> {
        public String getColumnType () {
            return "SMALLINT";
        }
        public Short getFromObject (Object po)
            throws IllegalArgumentException, IllegalAccessException {
            return _field.getShort(po);
        }
        public Short getFromSet (ResultSet rs)
            throws SQLException {
            return rs.getShort(getColumnName());
        }
        public void writeToObject (Object po, Short value)
            throws IllegalArgumentException, IllegalAccessException {
            _field.setShort(po, value);
        }
        public void writeToStatement (PreparedStatement ps, int column, Short value)
            throws SQLException {
            ps.setShort(column, value);
        }
    }

    protected static class IntMarshaller extends FieldMarshaller<Integer> {
        public String getColumnType () {
            return "INTEGER";
        }
        public Integer getFromObject (Object po)
            throws IllegalArgumentException, IllegalAccessException {
            return _field.getInt(po);
        }
        public Integer getFromSet (ResultSet rs)
            throws SQLException {
            return rs.getInt(getColumnName());
        }
        public void writeToObject (Object po, Integer value)
            throws IllegalArgumentException, IllegalAccessException {
            _field.setInt(po, value);
        }
        public void writeToStatement (PreparedStatement ps, int column, Integer value)
            throws SQLException {
            ps.setInt(column, value);
        }
    }

    protected static class LongMarshaller extends FieldMarshaller<Long> {
        public String getColumnType () {
            return "BIGINT";
        }
        public Long getFromObject (Object po)
            throws IllegalArgumentException, IllegalAccessException {
            return _field.getLong(po);
        }
        public Long getFromSet (ResultSet rs)
            throws SQLException {
            return rs.getLong(getColumnName());
        }
        public void writeToObject (Object po, Long value)
            throws IllegalArgumentException, IllegalAccessException {
            _field.setLong(po, value);
        }
        public void writeToStatement (PreparedStatement ps, int column, Long value)
            throws SQLException {
            ps.setLong(column, value);
        }
    }

    protected static class FloatMarshaller extends FieldMarshaller<Float> {
        public String getColumnType () {
            return "FLOAT";
        }
        public Float getFromObject (Object po)
            throws IllegalArgumentException, IllegalAccessException {
            return _field.getFloat(po);
        }
        public Float getFromSet (ResultSet rs)
            throws SQLException {
            return rs.getFloat(getColumnName());
        }
        public void writeToObject (Object po, Float value)
            throws IllegalArgumentException, IllegalAccessException {
            _field.setFloat(po, value);
        }
        public void writeToStatement (PreparedStatement ps, int column, Float value)
            throws SQLException {
            ps.setFloat(column, value);
        }
    }

    protected static class DoubleMarshaller extends FieldMarshaller<Double> {
        public String getColumnType () {
            return "DOUBLE";
        }
        public Double getFromObject (Object po)
            throws IllegalArgumentException, IllegalAccessException {
            return _field.getDouble(po);
        }
        public Double getFromSet (ResultSet rs)
            throws SQLException {
            return rs.getDouble(getColumnName());
        }
        public void writeToObject (Object po, Double value)
            throws IllegalArgumentException, IllegalAccessException {
            _field.setDouble(po, value);
        }
        public void writeToStatement (PreparedStatement ps, int column, Double value)
            throws SQLException {
            ps.setDouble(column, value);
        }
    }

    protected static class ObjectMarshaller extends FieldMarshaller<Object> {
        public String getColumnType () {
            Class<?> ftype = _field.getType();
            if (ftype.equals(Byte.class)) {
                return "TINYINT";
            } else if (ftype.equals(Short.class)) {
                return "SMALLINT";
            } else if (ftype.equals(Integer.class)) {
                return "INTEGER";
            } else if (ftype.equals(Long.class)) {
                return "BIGINT";
            } else if (ftype.equals(Float.class)) {
                return "FLOAT";
            } else if (ftype.equals(Double.class)) {
                return "DOUBLE";
            } else if (ftype.equals(String.class)) {
                return "VARCHAR";
            } else if (ftype.equals(Date.class)) {
                return "DATE";
            } else if (ftype.equals(Time.class)) {
                return "DATETIME";
            } else if (ftype.equals(Timestamp.class)) {
                return "TIMESTAMP";
            } else if (ftype.equals(Blob.class)) {
                return "BLOB";
            } else if (ftype.equals(Clob.class)) {
                return "CLOB";
            } else {
                throw new IllegalArgumentException(
                    "Don't know how to create SQL for " + ftype + ".");
            }
        }
        public Object getFromObject (Object po)
            throws IllegalArgumentException, IllegalAccessException {
            return _field.get(po);
        }
        public Object getFromSet (ResultSet rs)
            throws SQLException {
            return rs.getObject(getColumnName());
        }
        public void writeToObject (Object po, Object value)
            throws IllegalArgumentException, IllegalAccessException {
            _field.set(po, value);
        }
        public void writeToStatement (PreparedStatement ps, int column, Object value)
            throws SQLException {
            ps.setObject(column, value);
        }
    }

    protected static class ByteArrayMarshaller extends FieldMarshaller<byte[]> {
        public byte[] getFromObject (Object po)
            throws IllegalArgumentException, IllegalAccessException {
            return (byte[]) _field.get(po);
        }
        public byte[] getFromSet (ResultSet rs)
            throws SQLException {
            return rs.getBytes(getColumnName());
        }
        public void writeToObject (Object po, byte[] value)
            throws IllegalArgumentException, IllegalAccessException {
            _field.set(po, value);
        }
        public void writeToStatement (PreparedStatement ps, int column, byte[] value)
            throws SQLException {
            ps.setBytes(column, value);
        }
        public String getColumnType () {
            return "VARBINARY";
        }
    }

    protected static class IntArrayMarshaller extends FieldMarshaller<int[]> {
        public int[] getFromObject (Object po)
            throws IllegalArgumentException, IllegalAccessException {
            return (int[]) _field.get(po);
        }
        public int[] getFromSet (ResultSet rs)
            throws SQLException {
            return (int[]) rs.getObject(getColumnName());
        }
        public void writeToObject (Object po, int[] value)
            throws IllegalArgumentException, IllegalAccessException {
            _field.set(po, value);
        }
        public void writeToStatement (PreparedStatement ps, int column, int[] value)
            throws SQLException {
            ps.setObject(column, value);
        }
        public String getColumnType () {
            return "BLOB";
        }
    }

    protected static class ByteEnumMarshaller extends FieldMarshaller<ByteEnum> {
        public ByteEnumMarshaller (Class clazz) {
            try {
                _factmeth = clazz.getMethod("fromByte", new Class[] { Byte.TYPE });
            } catch (Exception e) {
                throw new IllegalArgumentException(
                    "Could not locate fromByte() method on enum field " + _field.getType() + ".");
            }
            if (!Modifier.isPublic(_factmeth.getModifiers()) ||
                !Modifier.isStatic(_factmeth.getModifiers())) {
                throw new IllegalArgumentException(
                    _field.getType() + ".fromByte() must be public and static.");
            }
        }

        public String getColumnType () {
            return "TINYINT";
        }

        public ByteEnum getFromObject (Object po)
            throws IllegalArgumentException, IllegalAccessException {
            return (ByteEnum) _field.get(po);
        }
        public ByteEnum getFromSet (ResultSet rs)
            throws SQLException {
            try {
                return (ByteEnum) _factmeth.invoke(null, rs.getByte(getColumnName()));
            } catch (SQLException se) {
                throw se;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        public void writeToObject (Object po, ByteEnum value)
            throws IllegalArgumentException, IllegalAccessException {
            _field.set(po, value);
        }
        public void writeToStatement (PreparedStatement ps, int column, ByteEnum value)
            throws SQLException {
            ps.setByte(column, value.toByte());
        }

        protected Method _factmeth;
    }

    protected Field _field;
    protected String _columnName, _columnDefinition;
    protected Computed _computed;
    protected GeneratedValue _generatedValue;
}
