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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.samskivert.Log;

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

    public void initializeGenerator (
        Connection conn, String table, String column, int first, int step)
        throws SQLException
    {
        executeQuery(conn, "alter sequence \"" + table + "_" + column + "_seq\" " +
                     " restart with " + first + " increment " + step);
    }

    @Override // from DatabaseLiaison
    public boolean changeColumn (Connection conn, String table, String column, String definition)
        throws SQLException
    {
        // we need to handle nullability separately; TODO: make this less of a hack
        boolean notNull = false;
        Matcher match = NOT_NULL.matcher(definition);
        if (match.find()) {
            definition = match.replaceFirst("");
            notNull = true;
        }
        executeQuery(conn, "ALTER TABLE " + tableSQL(table) + " ALTER COLUMN " +
                     columnSQL(column) + " TYPE " + definition);
        executeQuery(conn, "ALTER TABLE " + tableSQL(table) + " ALTER COLUMN " +
                     columnSQL(column) + " " + (notNull ? "SET NOT NULL" : "DROP NOT NULL"));
        Log.info("Database column '" + column + "' of table '" + table + "' modified to have " +
                 "definition '" + definition + "'.");
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

    protected static final Pattern NOT_NULL =
        Pattern.compile(" NOT NULL", Pattern.CASE_INSENSITIVE);
}
