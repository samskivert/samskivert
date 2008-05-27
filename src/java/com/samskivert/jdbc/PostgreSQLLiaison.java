//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001 Michael Bayne
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

import static com.samskivert.Log.log;

/**
 * A database liaison for the MySQL database.
 */
public class PostgreSQLLiaison extends BaseLiaison
{
    // from DatabaseLiaison
    public boolean matchesURL (String url)
    {
        return url.startsWith("jdbc:postgresql");
    }

    // from DatabaseLiaison
    public boolean isDuplicateRowException (SQLException sqe)
    {
        String msg = sqe.getMessage();
        return (msg != null && msg.indexOf("duplicate key") != -1);
    }

    // from DatabaseLiaison
    public boolean isTransientException (SQLException sqe)
    {
        // TODO: Add more error messages here as we encounter them.
        String msg = sqe.getMessage();
        return (msg != null &&
                msg.indexOf("An I/O error occured while sending to the backend") != -1);
    }

    // from DatabaseLiaison
    public int lastInsertedId (Connection conn, String table, String column) throws SQLException
    {
        // PostgreSQL's support for auto-generated ID's comes in the form of appropriately named
        // sequences and DEFAULT nextval(sequence) modifiers in the ID columns. To get the next ID,
        // we use the currval() method which is set in a database sessions when any given sequence
        // is incremented.
        Statement stmt = null;

        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                "select currval('\"" + table + "_" + column + "_seq\"')");
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                return -1;
            }

        } finally {
            JDBCUtil.close(stmt);
        }
    }

    // from DatabaseLiaison
    public void deleteGenerator (Connection conn, String table, String column)
        throws SQLException
    {
        executeQuery(conn, "drop sequence if exists \"" + table + "_" + column + "_seq\"");
    }

    @Override // from DatabaseLiaison
    public boolean changeColumn (Connection conn, String table, String column, String type,
                                 Boolean nullable, Boolean unique, String defaultValue)
        throws SQLException
    {
        StringBuilder lbuf = new StringBuilder();
        if (type != null) {
            executeQuery(
                conn, "ALTER TABLE " + tableSQL(table) + " ALTER COLUMN " + columnSQL(column) +
                " TYPE " + type);
            lbuf.append("type=" + type);
        }
        if (nullable != null) {
            executeQuery(
                conn, "ALTER TABLE " + tableSQL(table) + " ALTER COLUMN " + columnSQL(column) +
                " " + (nullable.booleanValue() ? "SET NOT NULL" : "DROP NOT NULL"));
            if (lbuf.length() > 0) lbuf.append(", ");
            lbuf.append("nullable=" + nullable);
        }
        if (unique != null) {
            // TODO: I think this requires ALTER TABLE DROP CONSTRAINT and so on
            if (lbuf.length() > 0) lbuf.append(", ");
            lbuf.append("unique=" + unique + " (not implemented yet)");
        }
        if (defaultValue != null) {
            executeQuery(
                conn, "ALTER TABLE " + tableSQL(table) + " ALTER COLUMN " + columnSQL(column) +
                " " + (defaultValue.length() > 0 ? "SET DEFAULT " + defaultValue : "DROP DEFAULT"));
            if (lbuf.length() > 0) lbuf.append(", ");
            lbuf.append("defaultValue=" + defaultValue);
        }
        log.info("Database column '" + column + "' of table '" + table + "' modified to have " +
                 "definition [" + lbuf + "].");
        return true;
    }

    // from DatabaseLiaison
    public String columnSQL (String column)
    {
        return "\"" + column + "\"";
    }

    // from DatabaseLiaison
    public String tableSQL (String table)
    {
        return "\"" + table + "\"";
    }

    // from DatabaseLiaison
    public String indexSQL (String index)
    {
        return "\"" + index + "\"";
    }
}
