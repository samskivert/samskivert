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
            marshaller = new PBooleanMarshaller();
        } else if (ftype.equals(Byte.TYPE)) {
            marshaller = new PByteMarshaller();
        } else if (ftype.equals(Short.TYPE)) {
            marshaller = new PShortMarshaller();
        } else if (ftype.equals(Integer.TYPE)) {
            marshaller = new PIntMarshaller();
        } else if (ftype.equals(Long.TYPE)) {
            marshaller = new PLongMarshaller();
        } else if (ftype.equals(Float.TYPE)) {
            marshaller = new PFloatMarshaller();
        } else if (ftype.equals(Double.TYPE)) {
            marshaller = new PDoubleMarshaller();

        // "natural" types
        } else if (ftype.equals(Byte.class) ||
            ftype.equals(Short.class) ||
            ftype.equals(Integer.class) ||
            ftype.equals(Long.class) ||
            ftype.equals(Float.class) ||
            ftype.equals(Double.class) ||
            ftype.equals(String.class)) {
            marshaller = new ObjectMarshaller();

        // TODO: byte array, (other array types?)

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
     * Reads the value of our field from the persistent object and sets that
     * value into the specified column of the supplied prepared statement.
     */
    public abstract void setValue (Object po, PreparedStatement ps, int column)
        throws SQLException, IllegalAccessException;

    /**
     * Reads the specified column from the supplied result set and writes it to
     * the appropriate field of the persistent object.
     */
    public abstract void getValue (ResultSet rset, int column, Object po)
        throws SQLException, IllegalAccessException;

    /**
     * Returns the name of the table column used to store this field.
     */
    public String getColumnName ()
    {
        return _field.getName();
    }

    protected void init (Field field)
    {
        _field = field;
    }

    protected static class PBooleanMarshaller extends FieldMarshaller {
        public void setValue (Object po, PreparedStatement ps, int column)
            throws SQLException, IllegalAccessException {
            ps.setBoolean(column, _field.getBoolean(po));
        }
        public void getValue (ResultSet rs, int column, Object po)
            throws SQLException, IllegalAccessException {
            _field.setBoolean(po, rs.getBoolean(column));
        }
    }

    protected static class PByteMarshaller extends FieldMarshaller {
        public void setValue (Object po, PreparedStatement ps, int column)
            throws SQLException, IllegalAccessException {
            ps.setByte(column, _field.getByte(po));
        }
        public void getValue (ResultSet rs, int column, Object po)
            throws SQLException, IllegalAccessException {
            _field.setByte(po, rs.getByte(column));
        }
    }

    protected static class PShortMarshaller extends FieldMarshaller {
        public void setValue (Object po, PreparedStatement ps, int column)
            throws SQLException, IllegalAccessException {
            ps.setShort(column, _field.getShort(po));
        }
        public void getValue (ResultSet rs, int column, Object po)
            throws SQLException, IllegalAccessException {
            _field.setShort(po, rs.getShort(column));
        }
    }

    protected static class PIntMarshaller extends FieldMarshaller {
        public void setValue (Object po, PreparedStatement ps, int column)
            throws SQLException, IllegalAccessException {
            ps.setInt(column, _field.getInt(po));
        }
        public void getValue (ResultSet rs, int column, Object po)
            throws SQLException, IllegalAccessException {
            _field.setInt(po, rs.getInt(column));
        }
    }

    protected static class PLongMarshaller extends FieldMarshaller {
        public void setValue (Object po, PreparedStatement ps, int column)
            throws SQLException, IllegalAccessException {
            ps.setLong(column, _field.getLong(po));
        }
        public void getValue (ResultSet rs, int column, Object po)
            throws SQLException, IllegalAccessException {
            _field.setLong(po, rs.getLong(column));
        }
    }

    protected static class PFloatMarshaller extends FieldMarshaller {
        public void setValue (Object po, PreparedStatement ps, int column)
            throws SQLException, IllegalAccessException {
            ps.setFloat(column, _field.getFloat(po));
        }
        public void getValue (ResultSet rs, int column, Object po)
            throws SQLException, IllegalAccessException {
            _field.setFloat(po, rs.getFloat(column));
        }
    }

    protected static class PDoubleMarshaller extends FieldMarshaller {
        public void setValue (Object po, PreparedStatement ps, int column)
            throws SQLException, IllegalAccessException {
            ps.setDouble(column, _field.getDouble(po));
        }
        public void getValue (ResultSet rs, int column, Object po)
            throws SQLException, IllegalAccessException {
            _field.setDouble(po, rs.getDouble(column));
        }
    }

    protected static class ObjectMarshaller extends FieldMarshaller {
        public void setValue (Object po, PreparedStatement ps, int column)
            throws SQLException, IllegalAccessException {
            ps.setObject(column, _field.get(po));
        }
        public void getValue (ResultSet rs, int column, Object po)
            throws SQLException, IllegalAccessException {
            _field.set(po, rs.getObject(column));
        }
    }

    protected Field _field;
}
