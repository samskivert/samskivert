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
import java.nio.ByteBuffer;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;

import com.samskivert.jdbc.ColumnDefinition;
import com.samskivert.jdbc.depot.annotation.Column;
import com.samskivert.jdbc.depot.annotation.Computed;
import com.samskivert.jdbc.depot.annotation.GeneratedValue;

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
    public static FieldMarshaller<?> createMarshaller (Field field)
    {
        Class<?> ftype = field.getType();
        FieldMarshaller<?> marshaller;

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

        marshaller.create(field);
        return marshaller;
    }

    /**
     * Initializes this field marshaller with a SQL builder which it uses to construct its column
     * definition according to the appropriate database dialect.
     */
    public void init (SQLBuilder builder)
        throws DatabaseException
    {
        _columnDefinition = builder.buildColumnDefinition(this);
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
    public ColumnDefinition getColumnDefinition ()
    {
        return _columnDefinition;
    }

    /**
     * Reads this field from the given persistent object.
     */
    public abstract T getFromObject (Object po)
        throws IllegalArgumentException, IllegalAccessException;

    /**
     * Sets the specified column of the given prepared statement to the given value.
     */
    public abstract void writeToStatement (PreparedStatement ps, int column, T value)
        throws SQLException;

    /**
     * Reads the value of our field from the supplied persistent object and sets that value into
     * the specified column of the supplied prepared statement.
     */
    public void getAndWriteToStatement (PreparedStatement ps, int column, Object po)
        throws SQLException, IllegalAccessException
    {
        writeToStatement(ps, column, getFromObject(po));
    }

    /**
     * Reads and returns this field from the result set.
     */
    public abstract T getFromSet (ResultSet rs)
        throws SQLException;

    /**
     * Writes the given value to the given persistent value.
     */
    public abstract void writeToObject (Object po, T value)
        throws IllegalArgumentException, IllegalAccessException;

    /**
     * Reads the specified column from the supplied result set and writes it to the appropriate
     * field of the persistent object.
     */
    public void getAndWriteToObject (ResultSet rset, Object po)
        throws SQLException, IllegalAccessException
    {
        writeToObject(po, getFromSet(rset));
    }

    protected void create (Field field)
    {
        _field = field;
        _columnName = field.getName();

        Column column = _field.getAnnotation(Column.class);
        if (column != null) {
            if (!StringUtil.isBlank(column.name())) {
                _columnName = column.name();
            }
        }

        _computed = field.getAnnotation(Computed.class);
        if (_computed != null) {
            return;
        }

        // figure out how we're going to generate our primary key values
        _generatedValue = field.getAnnotation(GeneratedValue.class);
    }

    protected static class BooleanMarshaller extends FieldMarshaller<Boolean> {
        @Override public Boolean getFromObject (Object po)
            throws IllegalArgumentException, IllegalAccessException {
            return _field.getBoolean(po);
        }
        @Override public Boolean getFromSet (ResultSet rs)
            throws SQLException {
            return rs.getBoolean(getColumnName());
        }
        @Override public void writeToObject (Object po, Boolean value)
            throws IllegalArgumentException, IllegalAccessException {
            _field.setBoolean(po, value);
        }
        @Override public void writeToStatement (PreparedStatement ps, int column, Boolean value)
            throws SQLException {
            ps.setBoolean(column, value);
        }
    }

    protected static class ByteMarshaller extends FieldMarshaller<Byte> {
        @Override public Byte getFromObject (Object po)
            throws IllegalArgumentException, IllegalAccessException {
            return _field.getByte(po);
        }
        @Override public Byte getFromSet (ResultSet rs)
            throws SQLException {
            return rs.getByte(getColumnName());
        }
        @Override public void writeToObject (Object po, Byte value)
            throws IllegalArgumentException, IllegalAccessException {
            _field.setByte(po, value);
        }
        @Override public void writeToStatement (PreparedStatement ps, int column, Byte value)
            throws SQLException {
            ps.setByte(column, value);
        }
    }

    protected static class ShortMarshaller extends FieldMarshaller<Short> {
        @Override public Short getFromObject (Object po)
            throws IllegalArgumentException, IllegalAccessException {
            return _field.getShort(po);
        }
        @Override public Short getFromSet (ResultSet rs)
            throws SQLException {
            return rs.getShort(getColumnName());
        }
        @Override public void writeToObject (Object po, Short value)
            throws IllegalArgumentException, IllegalAccessException {
            _field.setShort(po, value);
        }
        @Override public void writeToStatement (PreparedStatement ps, int column, Short value)
            throws SQLException {
            ps.setShort(column, value);
        }
    }

    protected static class IntMarshaller extends FieldMarshaller<Integer> {
        @Override public Integer getFromObject (Object po)
            throws IllegalArgumentException, IllegalAccessException {
            return _field.getInt(po);
        }
        @Override public Integer getFromSet (ResultSet rs)
            throws SQLException {
            return rs.getInt(getColumnName());
        }
        @Override public void writeToObject (Object po, Integer value)
            throws IllegalArgumentException, IllegalAccessException {
            _field.setInt(po, value);
        }
        @Override public void writeToStatement (PreparedStatement ps, int column, Integer value)
            throws SQLException {
            ps.setInt(column, value);
        }
    }

    protected static class LongMarshaller extends FieldMarshaller<Long> {
        @Override public Long getFromObject (Object po)
            throws IllegalArgumentException, IllegalAccessException {
            return _field.getLong(po);
        }
        @Override public Long getFromSet (ResultSet rs)
            throws SQLException {
            return rs.getLong(getColumnName());
        }
        @Override public void writeToObject (Object po, Long value)
            throws IllegalArgumentException, IllegalAccessException {
            _field.setLong(po, value);
        }
        @Override public void writeToStatement (PreparedStatement ps, int column, Long value)
            throws SQLException {
            ps.setLong(column, value);
        }
    }

    protected static class FloatMarshaller extends FieldMarshaller<Float> {
        @Override public Float getFromObject (Object po)
            throws IllegalArgumentException, IllegalAccessException {
            return _field.getFloat(po);
        }
        @Override public Float getFromSet (ResultSet rs)
            throws SQLException {
            return rs.getFloat(getColumnName());
        }
        @Override public void writeToObject (Object po, Float value)
            throws IllegalArgumentException, IllegalAccessException {
            _field.setFloat(po, value);
        }
        @Override public void writeToStatement (PreparedStatement ps, int column, Float value)
            throws SQLException {
            ps.setFloat(column, value);
        }
    }

    protected static class DoubleMarshaller extends FieldMarshaller<Double> {
        @Override public Double getFromObject (Object po)
            throws IllegalArgumentException, IllegalAccessException {
            return _field.getDouble(po);
        }
        @Override public Double getFromSet (ResultSet rs)
            throws SQLException {
            return rs.getDouble(getColumnName());
        }
        @Override public void writeToObject (Object po, Double value)
            throws IllegalArgumentException, IllegalAccessException {
            _field.setDouble(po, value);
        }
        @Override public void writeToStatement (PreparedStatement ps, int column, Double value)
            throws SQLException {
            ps.setDouble(column, value);
        }
    }

    protected static class ObjectMarshaller extends FieldMarshaller<Object> {
        @Override public Object getFromObject (Object po)
            throws IllegalArgumentException, IllegalAccessException {
            return _field.get(po);
        }
        @Override public Object getFromSet (ResultSet rs)
            throws SQLException {
            return rs.getObject(getColumnName());
        }
        @Override public void writeToObject (Object po, Object value)
            throws IllegalArgumentException, IllegalAccessException {
            _field.set(po, value);
        }
        @Override public void writeToStatement (PreparedStatement ps, int column, Object value)
            throws SQLException {
            ps.setObject(column, value);
        }
    }

    protected static class ByteArrayMarshaller extends FieldMarshaller<byte[]> {
        @Override public byte[] getFromObject (Object po)
            throws IllegalArgumentException, IllegalAccessException {
            return (byte[]) _field.get(po);
        }
        @Override public byte[] getFromSet (ResultSet rs)
            throws SQLException {
            return rs.getBytes(getColumnName());
        }
        @Override public void writeToObject (Object po, byte[] value)
            throws IllegalArgumentException, IllegalAccessException {
            _field.set(po, value);
        }
        @Override public void writeToStatement (PreparedStatement ps, int column, byte[] value)
            throws SQLException {
            ps.setBytes(column, value);
        }
    }

    protected static class IntArrayMarshaller extends FieldMarshaller<byte[]> {
        @Override public byte[] getFromObject (Object po)
            throws IllegalArgumentException, IllegalAccessException {
            int[] values = (int[]) _field.get(po);
            if (values == null) {
                return null;
            }
            ByteBuffer bbuf = ByteBuffer.allocate(values.length * 4);
            bbuf.asIntBuffer().put(values);
            return bbuf.array();
            
        }
        @Override public byte[] getFromSet (ResultSet rs)
            throws SQLException {
            return (byte[]) rs.getObject(getColumnName());
        }
        @Override public void writeToObject (Object po, byte[] data)
            throws IllegalArgumentException, IllegalAccessException {
            int[] value = null;
            if (data != null) {
                value = new int[data.length/4];
                ByteBuffer.wrap(data).asIntBuffer().get(value);
            }
            _field.set(po, value);
        }
        @Override public void writeToStatement (PreparedStatement ps, int column, byte[] value)
            throws SQLException {
            ps.setObject(column, value);
        }
    }

    protected static class ByteEnumMarshaller extends FieldMarshaller<ByteEnum> {
        public ByteEnumMarshaller (Class<?> clazz) {
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

        @Override public ByteEnum getFromObject (Object po)
            throws IllegalArgumentException, IllegalAccessException {
            return (ByteEnum) _field.get(po);
        }
        @Override public ByteEnum getFromSet (ResultSet rs)
            throws SQLException {
            try {
                return (ByteEnum) _factmeth.invoke(null, rs.getByte(getColumnName()));
            } catch (SQLException se) {
                throw se;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        @Override public void writeToObject (Object po, ByteEnum value)
            throws IllegalArgumentException, IllegalAccessException {
            _field.set(po, value);
        }
        @Override public void writeToStatement (PreparedStatement ps, int column, ByteEnum value)
            throws SQLException {
            ps.setByte(column, value.toByte());
        }

        protected Method _factmeth;
    }

    protected Field _field;
    protected String _columnName;
    protected ColumnDefinition _columnDefinition;
    protected Computed _computed;
    protected GeneratedValue _generatedValue;
}
