//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2006 Michael Bayne, Pär Winzell
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.persistence.TableGenerator;

import com.samskivert.jdbc.JDBCUtil;

/**
 * Generates primary keys using an external table 
 */
public class TableKeyGenerator implements KeyGenerator
{
    public TableKeyGenerator (TableGenerator annotation)
    {
        _table = defStr(annotation.table(), "IdSequences");
        _pkColumnName = defStr(annotation.pkColumnName(), "sequence");
        _pkColumnValue = defStr(annotation.pkColumnValue(), "default");
        _valueColumnName = defStr(annotation.valueColumnName(), "value");
        _allocationSize = annotation.allocationSize();
        _initialValue = _allocationSize > 0 ? _allocationSize : 1;
    }

    // from interface KeyGenerator
    public boolean isPostFactum ()
    {
        return false;
    }

    // from interface KeyGenerator
    public void init (Connection conn)
        throws SQLException
    {
        // make sure our table exists
        JDBCUtil.createTableIfMissing(conn, _table, new String[] {
            _pkColumnName + " VARCHAR(255) PRIMARY KEY",
            _valueColumnName + " INTEGER NOT NULL"
        }, "");

        // and also that there's a row in it for us
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(
                "SELECT * FROM " + _table + " WHERE " + _pkColumnName + " = ?");
            stmt.setString(1, _pkColumnValue);
            if (stmt.executeQuery().next()) {
                return;
            }

            JDBCUtil.close(stmt);
            stmt = null;
            stmt = conn.prepareStatement(
                "INSERT INTO " + _table + " SET " + _pkColumnName + " = ?, " +
                _valueColumnName + " = ? ");
            stmt.setString(1, _pkColumnValue);
            stmt.setInt(2, _initialValue);
            stmt.executeUpdate();

        } finally {
            JDBCUtil.close(stmt);
        }
    }

    // from interface KeyGenerator
    public int nextGeneratedValue (Connection conn)
        throws SQLException
    {
        PreparedStatement stmt = null;
        try {
            String query = "SELECT " + _valueColumnName + " FROM " + _table +
                " WHERE " + _pkColumnName + "= ? FOR UPDATE";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, _pkColumnValue);

            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                throw new SQLException("Failed to find next primary key value [table=" + _table +
                                       ", column=" + _valueColumnName +
                                       ", where=" + _pkColumnName + "=" + _pkColumnValue + "]");
            }
            int val = rs.getInt(1);
            JDBCUtil.close(stmt);

            stmt = conn.prepareStatement("UPDATE " + _table + " SET " + _valueColumnName + " = ? " +
                                         "WHERE " + _pkColumnName + " = ?");
            stmt.setInt(1, val + _allocationSize);
            stmt.setString(2, _pkColumnValue);
            stmt.executeUpdate();
            return val;

        } finally {
            JDBCUtil.close(stmt);
        }
    }

    /**
     * Convenience function to return a value or a default fallback.
     */
    protected static String defStr (String value, String def)
    {
        if (value == null || value.trim().length() == 0) {
            return def;
        }
        return value;
    }

    protected String _table;
    protected String _pkColumnName;
    protected String _pkColumnValue;
    protected String _valueColumnName;
    protected int _initialValue;
    protected int _allocationSize;
}
