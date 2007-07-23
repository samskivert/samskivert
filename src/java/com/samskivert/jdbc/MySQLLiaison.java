//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2007 Michael Bayne
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

package com.samskivert.jdbc;

import java.sql.*;

import com.samskivert.Log;
import com.samskivert.util.StringUtil;

/**
 * A database liaison for the MySQL database.
 */
public class MySQLLiaison extends BaseLiaison
{
    // from DatabaseLiaison
    public boolean matchesURL (String url)
    {
        return url.startsWith("jdbc:mysql");
    }

    // from DatabaseLiaison
    public boolean isDuplicateRowException (SQLException sqe)
    {
	String msg = sqe.getMessage();
	return (msg != null && msg.indexOf("Duplicate entry") != -1);
    }

    // from DatabaseLiaison
    public boolean isTransientException (SQLException sqe)
    {
	String msg = sqe.getMessage();
	return (msg != null &&
                (msg.indexOf("Lost connection") != -1 ||
                 msg.indexOf("link failure") != -1 ||
                 msg.indexOf("Broken pipe") != -1));
    }

    // from DatabaseLiaison
    public int lastInsertedId (Connection conn) throws SQLException
    {
        return lastInsertedId(conn, null, null);
    }

    // from DatabaseLiaison
    public void initializeGenerator (
        Connection conn, String table, String column, int first, int step)
        throws SQLException
    {
        // AUTO_INCREMENT does not allow this kind of control
    }

    // from DatabaseLiaison
    public int lastInsertedId (Connection conn, String table, String column) throws SQLException
    {
        // MySQL does not keep track of per-table-and-column insertion data, so we are pretty much
        // going on blind faith here that we're fetching the right ID. In the overwhelming number
        // of cases that will be so, but it's still not pretty.
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("select LAST_INSERT_ID()");
            return rs.next() ? rs.getInt(1) : -1;
        } finally {
            JDBCUtil.close(stmt);
        }
    }

    @Override // from BaseLiaison
    public boolean addIndexToTable (Connection conn, String table, String[] columns, String ixName)
        throws SQLException
    {
        if (tableContainsIndex(conn, table, ixName)) {
            return false;
        }
        ixName = (ixName != null ? ixName : StringUtil.join(columns, "_"));

        // MySQL's "CREATE INDEX" is buggy, it actually changes the case of the table names you're
        // working with. Luckily (?) ALTER TABLE ADD INDEX works, so we do that here.
        StringBuilder update = new StringBuilder("ALTER TABLE ").append(table).
            append(" ADD INDEX ").append(indexSQL(ixName)).append(" (");
        for (int ii = 0; ii < columns.length; ii ++) {
            if (ii > 0) {
                update.append(", ");
            }
            update.append(columnSQL(columns[ii]));
        }
        update.append(")");

        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(update.toString());
            stmt.executeUpdate();
        } finally {
            JDBCUtil.close(stmt);
        }

        Log.info("Database index '" + ixName + "' added to table '" + table + "'");
        return true;
    }

    // from DatabaseLiaison
    public String columnSQL (String column)
    {
        return column;
    }

    // from DatabaseLiaison
    public String tableSQL (String table)
    {
        return table;
    }

    // from DatabaseLiaison
    public String indexSQL (String index)
    {
        return index;
    }
}
