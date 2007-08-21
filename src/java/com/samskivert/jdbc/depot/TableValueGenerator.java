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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.samskivert.jdbc.depot.annotation.GeneratedValue;
import com.samskivert.jdbc.depot.annotation.TableGenerator;

import com.samskivert.jdbc.DatabaseLiaison;
import com.samskivert.jdbc.JDBCUtil;

/**
 * Generates primary keys using an external table .
 */
public class TableValueGenerator extends ValueGenerator
{
    public TableValueGenerator (TableGenerator tg, GeneratedValue gv, DepotMarshaller dm, FieldMarshaller fm)
    {
        super(gv, dm, fm);
        _valueTable = defStr(tg.table(), "IdSequences");
        _pkColumnName = defStr(tg.pkColumnName(), "sequence");
        _pkColumnValue = defStr(tg.pkColumnValue(), "default");
        _valueColumnName = defStr(tg.valueColumnName(), "value");
    }

    // from interface KeyGenerator
    public boolean isPostFactum ()
    {
        return false;
    }

    // from interface KeyGenerator
    public void init (Connection conn, DatabaseLiaison liaison)
        throws SQLException
    {
        // make sure our table exists
        liaison.createTableIfMissing(
            conn, _valueTable,
            new String[] { _pkColumnName, _valueColumnName },
            new String[] { "VARCHAR(255)", "INTEGER NOT NULL" },
            null,
            new String[] { _pkColumnName });

        // and also that there's a row in it for us
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(
                " SELECT * FROM " + liaison.tableSQL(_valueTable) +
                "  WHERE " + liaison.columnSQL(_pkColumnName) + " = ?");
            stmt.setString(1, _pkColumnValue);
            if (stmt.executeQuery().next()) {
                return;
            }

            JDBCUtil.close(stmt);
            stmt = null;
            stmt = conn.prepareStatement(
                " INSERT INTO " + liaison.tableSQL(_valueTable) + " (" +
                liaison.columnSQL(_pkColumnName) + ", " + liaison.columnSQL(_valueColumnName) +
                ") VALUES (?, ?)");
            stmt.setString(1, _pkColumnValue);
            stmt.setInt(2, _initialValue);
            stmt.executeUpdate();

        } finally {
            JDBCUtil.close(stmt);
        }
    }

    // from interface KeyGenerator
    public int nextGeneratedValue (Connection conn, DatabaseLiaison liaison)
        throws SQLException
    {
        PreparedStatement stmt = null;
        try {
            // TODO: Make this lockless!
            String query =
                " SELECT " + liaison.columnSQL(_valueColumnName) +
                "   FROM " + liaison.tableSQL(_valueTable) +
                "  WHERE " + liaison.columnSQL(_pkColumnName) + " = ? FOR UPDATE";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, _pkColumnValue);

            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                throw new SQLException("Failed to find next primary key value " +
                                       "[table=" + _valueTable + ", column=" + _valueColumnName +
                                       ", where=" + _pkColumnName + "=" + _pkColumnValue + "]");
            }
            int val = rs.getInt(1);
            JDBCUtil.close(stmt);

            stmt = conn.prepareStatement(
                " UPDATE " + liaison.tableSQL(_valueTable) +
                "    SET " + liaison.columnSQL(_valueColumnName) + " = ? " +
                "  WHERE " + liaison.columnSQL(_pkColumnName) + " = ?");
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

    protected String _valueTable;
    protected String _pkColumnName;
    protected String _pkColumnValue;
    protected String _valueColumnName;
}
