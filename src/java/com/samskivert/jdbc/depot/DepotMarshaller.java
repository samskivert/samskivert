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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;

import javax.persistence.Id;

/**
 * Handles the marshalling and unmarshalling of persistent instances to JDBC
 * primitives ({@link PreparedStatement} and {@link ResultSet}).
 */
public class DepotMarshaller<T>
{
    /**
     * Creates a marshaller for the specified persistent object class.
     */
    public DepotMarshaller (Class<T> pclass)
    {
        _pclass = pclass;

        // determine our table name
        _tableName = _pclass.getName();
        _tableName = _tableName.substring(_tableName.lastIndexOf(".")+1);

        // introspect on the class and create field marshallers for all of its
        // fields
        ArrayList<FieldMarshaller> flist = new ArrayList<FieldMarshaller>();
        for (Field field : _pclass.getFields()) {
            // the field must be public, non-static and non-transient
            int mods = field.getModifiers();
            if (((mods & Modifier.PUBLIC) == 0) ||
                ((mods & Modifier.STATIC) != 0) ||
                ((mods & Modifier.TRANSIENT) != 0)) {
                continue;
            }

            FieldMarshaller fm = FieldMarshaller.createMarshaller(field);
            flist.add(fm);

            // check to see if this is our primary key
            for (Annotation ann : field.getDeclaredAnnotations()) {
                System.err.println(
                    "Annotation " + ann + " (" + ann.getClass() + ")");
            }
        }
        _fields = flist.toArray(new FieldMarshaller[flist.size()]);

        // generate our full list of columns for use in queries
        StringBuilder columns = new StringBuilder();
        for (FieldMarshaller fm : _fields) {
            if (columns.length() > 0) {
                columns.append(",");
            }
            columns.append(fm.getColumnName());
        }
        _fullColumnList = columns.toString();
    }

    /**
     * Returns the name of the table in which persistence instances of this
     * class are stored. By default this is the classname of the persistent
     * object without the package.
     */
    public String getTableName ()
    {
        return _tableName;
    }

    /**
     * Returns the field name of the primary key for this persistent object
     * class or null if it did not declare a primary key.
     */
    public String getPrimaryKey ()
    {
        return _primaryKey == null ? null : _primaryKey.getColumnName();
    }

    /**
     * Creates a query for instances of this persistent object type using the
     * supplied key.
     */
    public PreparedStatement createQuery (
        Connection conn, DepotRepository.Key key)
        throws SQLException
    {
        String query = "select " + _fullColumnList + " from " + getTableName() +
            " where " + key.toWhereClause();
        PreparedStatement pstmt = conn.prepareStatement(query);
        for (int ii = 0; ii < key.indices.length; ii++) {
            pstmt.setObject(ii+1, key.values[ii]);
        }
        return pstmt;
    }

    /**
     * Creates a persistent object from the supplied result set.
     */
    public T createObject (ResultSet rs)
        throws SQLException
    {
        try {
            T po = (T)_pclass.newInstance();
            for (int ii = 0; ii < _fields.length; ii++) {
                _fields[ii].getValue(rs, ii+1, po);
            }
            return po;
        } catch (Exception e) {
            String errmsg = "Failed to unmarshall persistent object " +
                "[pclass=" + _pclass.getName() + "]";
            throw (SQLException)new SQLException(errmsg).initCause(e);
        }
    }

    /**
     * Creates a statement that will insert the supplied persistent object into
     * the database.
     */
    public PreparedStatement createInsert (Connection conn, Object po)
        throws SQLException
    {
        return null;
    }

    /**
     * Creates a statement that will update the supplied persistent object
     * using the supplied key.
     */
    public PreparedStatement createUpdate (
        Connection conn, Object po, DepotRepository.Key key)
        throws SQLException
    {
        return null;
    }

    /**
     * Binds the specified object to the specified prepared statement.
     */
    public void bindObject (PreparedStatement stmt, Object po)
    {
    }

    protected Class<T> _pclass;
    protected FieldMarshaller[] _fields;

    protected String _tableName;
    protected FieldMarshaller _primaryKey;
    protected String _fullColumnList;
}
