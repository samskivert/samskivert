//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2006 Michael Bayne
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

import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.samskivert.util.StringUtil;

/**
 * Handles the marshalling and unmarshalling of a particular field of a
 * persistent object.
 *
 * @see DepotMarshaller
 */
public abstract class FieldMarshaller
{
    /**
     * Creates and returns a field marshaller for the specified field. Throws
     * an exception if the field in question cannot be marshalled.
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

        // SQL types
        } else if (ftype.equals(Date.class) ||
            ftype.equals(Time.class) ||
            ftype.equals(Timestamp.class) ||
            ftype.equals(Blob.class) ||
            ftype.equals(Clob.class)) {
            marshaller = new ObjectMarshaller();

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
     * Reads the value of our field from the persistent object and sets that
     * value into the specified column of the supplied prepared statement.
     */
    public abstract void setValue (Object po, PreparedStatement ps, int column)
        throws SQLException, IllegalAccessException;

    /**
     * Reads the specified column from the supplied result set and writes it to
     * the appropriate field of the persistent object.
     */
    public abstract void getValue (ResultSet rset, Object po)
        throws SQLException, IllegalAccessException;

    /**
     * Returns the type used in the SQL column definition for this field.
     */
    public abstract String getColumnType ();

    protected void init (Field field)
    {
        _field = field;
        _columnName = field.getName();

        // read our column metadata from the annotation (if it exists);
        // annoyingly we can't create a Column instance to read the defaults so
        // we have to duplicate them here
        int length = 255;
        boolean nullable = true;
        boolean unique = false;
        Column column = _field.getAnnotation(Column.class);
        if (column != null) {
            nullable = column.nullable();
            unique = column.unique();
            length = column.length();
            if (!StringUtil.isBlank(column.name())) {
                _columnName = column.name();
            }
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
        }

        // handle primary keyness
        if (field.getAnnotation(Id.class) != null) {
            builder.append(" PRIMARY KEY");

            // figure out how we're going to generate our primary key values
            GeneratedValue gv = field.getAnnotation(GeneratedValue.class);
            if (gv != null) {
                switch (gv.strategy()) {
                case AUTO:
                case IDENTITY:
                    builder.append(" AUTO_INCREMENT");
                    break;
                case SEQUENCE: // TODO
                    throw new IllegalArgumentException(
                        "TABLE key generation strategy not yet supported.");
                case TABLE: // TODO
                    throw new IllegalArgumentException(
                        "TABLE key generation strategy not yet supported.");
                }
            }
        }

        _columnDefinition = builder.toString();
    }

    protected static class BooleanMarshaller extends FieldMarshaller {
        public void setValue (Object po, PreparedStatement ps, int column)
            throws SQLException, IllegalAccessException {
            ps.setBoolean(column, _field.getBoolean(po));
        }
        public void getValue (ResultSet rs, Object po)
            throws SQLException, IllegalAccessException {
            _field.setBoolean(po, rs.getBoolean(getColumnName()));
        }
        public String getColumnType () {
            return "TINYINT";
        }
    }

    protected static class ByteMarshaller extends FieldMarshaller {
        public void setValue (Object po, PreparedStatement ps, int column)
            throws SQLException, IllegalAccessException {
            ps.setByte(column, _field.getByte(po));
        }
        public void getValue (ResultSet rs, Object po)
            throws SQLException, IllegalAccessException {
            _field.setByte(po, rs.getByte(getColumnName()));
        }
        public String getColumnType () {
            return "TINYINT";
        }
    }

    protected static class ShortMarshaller extends FieldMarshaller {
        public void setValue (Object po, PreparedStatement ps, int column)
            throws SQLException, IllegalAccessException {
            ps.setShort(column, _field.getShort(po));
        }
        public void getValue (ResultSet rs, Object po)
            throws SQLException, IllegalAccessException {
            _field.setShort(po, rs.getShort(getColumnName()));
        }
        public String getColumnType () {
            return "SMALLINT";
        }
    }

    protected static class IntMarshaller extends FieldMarshaller {
        public void setValue (Object po, PreparedStatement ps, int column)
            throws SQLException, IllegalAccessException {
            ps.setInt(column, _field.getInt(po));
        }
        public void getValue (ResultSet rs, Object po)
            throws SQLException, IllegalAccessException {
            _field.setInt(po, rs.getInt(getColumnName()));
        }
        public String getColumnType () {
            return "INTEGER";
        }
    }

    protected static class LongMarshaller extends FieldMarshaller {
        public void setValue (Object po, PreparedStatement ps, int column)
            throws SQLException, IllegalAccessException {
            ps.setLong(column, _field.getLong(po));
        }
        public void getValue (ResultSet rs, Object po)
            throws SQLException, IllegalAccessException {
            _field.setLong(po, rs.getLong(getColumnName()));
        }
        public String getColumnType () {
            return "BIGINT";
        }
    }

    protected static class FloatMarshaller extends FieldMarshaller {
        public void setValue (Object po, PreparedStatement ps, int column)
            throws SQLException, IllegalAccessException {
            ps.setFloat(column, _field.getFloat(po));
        }
        public void getValue (ResultSet rs, Object po)
            throws SQLException, IllegalAccessException {
            _field.setFloat(po, rs.getFloat(getColumnName()));
        }
        public String getColumnType () {
            return "FLOAT";
        }
    }

    protected static class DoubleMarshaller extends FieldMarshaller {
        public void setValue (Object po, PreparedStatement ps, int column)
            throws SQLException, IllegalAccessException {
            ps.setDouble(column, _field.getDouble(po));
        }
        public void getValue (ResultSet rs, Object po)
            throws SQLException, IllegalAccessException {
            _field.setDouble(po, rs.getDouble(getColumnName()));
        }
        public String getColumnType () {
            return "DOUBLE";
        }
    }

    protected static class ObjectMarshaller extends FieldMarshaller {
        public void setValue (Object po, PreparedStatement ps, int column)
            throws SQLException, IllegalAccessException {
            ps.setObject(column, _field.get(po));
        }
        public void getValue (ResultSet rs, Object po)
            throws SQLException, IllegalAccessException {
            _field.set(po, rs.getObject(getColumnName()));
        }
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
    }

    protected static class ByteArrayMarshaller extends FieldMarshaller {
        public void setValue (Object po, PreparedStatement ps, int column)
            throws SQLException, IllegalAccessException {
            ps.setBytes(column, (byte[])_field.get(po));
        }
        public void getValue (ResultSet rs, Object po)
            throws SQLException, IllegalAccessException {
            _field.set(po, rs.getBytes(getColumnName()));
        }
        public String getColumnType () {
            return "VARBINARY";
        }
    }

    protected Field _field;
    protected String _columnName, _columnDefinition;
}
