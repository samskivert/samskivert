//
// $Id: JDBCUtil.java,v 1.10 2004/05/28 01:54:47 eric Exp $
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

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.io.IOUtils;

import com.samskivert.Log;
import com.samskivert.io.PersistenceException;
import com.samskivert.util.StringUtil;

/**
 * A repository for JDBC related utility functions.
 */
public class JDBCUtil
{
    /**
     * Closes the supplied JDBC statement and gracefully handles being
     * passed null (by doing nothing).
     */
    public static void close (Statement stmt)
	throws SQLException
    {
	if (stmt != null) {
	    stmt.close();
	}
    }

    /**
     * Closes the supplied JDBC connection and gracefully handles being
     * passed null (by doing nothing).
     */
    public static void close (Connection conn)
	throws SQLException
    {
	if (conn != null) {
	    conn.close();
	}
    }

    /**
     * Calls <code>stmt.executeUpdate()</code> on the supplied statement,
     * checking to see that it returns the expected update count and
     * throwing a persistence exception if it does not.
     */
    public static void checkedUpdate (
        PreparedStatement stmt, int expectedCount)
        throws SQLException, PersistenceException
    {
        int modified = stmt.executeUpdate();
        if (modified != expectedCount) {
            String err = "Statement did not modify expected number of rows " +
                "[stmt=" + stmt + ", expected=" + expectedCount +
                ", modified=" + modified + "]";
            throw new PersistenceException(err);
        }
    }

    /**
     * Calls <code>stmt.executeUpdate()</code> on the supplied statement
     * with the supplied query, checking to see that it returns the
     * expected update count and throwing a persistence exception if it
     * does not.
     */
    public static void checkedUpdate (
        Statement stmt, String query, int expectedCount)
        throws SQLException, PersistenceException
    {
        int modified = stmt.executeUpdate(query);
        if (modified != expectedCount) {
            String err = "Statement did not modify expected number of rows " +
                "[stmt=" + stmt + ", expected=" + expectedCount +
                ", modified=" + modified + "]";
            throw new PersistenceException(err);
        }
    }

    /**
     * Calls <code>stmt.executeUpdate()</code> on the supplied statement,
     * checking to see that it returns the expected update count and
     * logging a warning if it does not.
     */
    public static void warnedUpdate (
        PreparedStatement stmt, int expectedCount)
        throws SQLException
    {
        int modified = stmt.executeUpdate();
        if (modified != expectedCount) {
            Log.warning("Statement did not modify expected number of rows " +
                        "[stmt=" + stmt + ", expected=" + expectedCount +
                        ", modified=" + modified + "]");
        }
    }

    /**
     * Calls <code>stmt.executeUpdate()</code> on the supplied statement
     * with the supplied query, checking to see that it returns the
     * expected update count and logging a warning if it does not.
     */
    public static void warnedUpdate (
        Statement stmt, String query, int expectedCount)
        throws SQLException
    {
        int modified = stmt.executeUpdate(query);
        if (modified != expectedCount) {
            Log.warning("Statement did not modify expected number of rows " +
                        "[stmt=" + stmt + ", expected=" + expectedCount +
                        ", modified=" + modified + "]");
        }
    }

    /**
     * Escapes any single quotes in the supplied text and wraps it in
     * single quotes to make it safe for embedding into a database query.
     */
    public static String escape (String text)
    {
        text = StringUtil.replace(text, "\\", "\\\\");
        return "'" + StringUtil.replace(text, "'", "\\'") + "'";
    }

    /**
     * Escapes a list of values, separating the escaped values by
     * commas. See {@link #escape(String)}.
     */
    public static String escape (Object[] values)
    {
        StringBuffer buf = new StringBuffer();
        for (int ii = 0; ii < values.length; ii++) {
            if (ii > 0) {
                buf.append(", ");
            }
            buf.append(escape(String.valueOf(values[ii])));
        }
        return buf.toString();
    }

    /**
     * Many databases simply fail to handle Unicode text properly and this
     * routine provides a common workaround which is to represent a UTF-8
     * string as an ISO-8895-1 string. If you don't need to use the
     * database's collation routines, this allows you to do pretty much
     * exactly what you want at the expense of having to jigger and
     * dejigger every goddamned string that might contain multibyte
     * characters every time you access the database. Three cheers for
     * progress!
     */
    public static String jigger (String text)
    {
        if (text == null) {
            return null;
        }
        try {
            return new String(text.getBytes("UTF8"), "8859_1");
        } catch (UnsupportedEncodingException uee) {
            Log.logStackTrace(uee);
            return text;
        }
    }

    /**
     * Reverses {@link #jigger}.
     */
    public static String unjigger (String text)
    {
        if (text == null) {
            return null;
        }
        try {
            return new String(text.getBytes("8859_1"), "UTF8");
        } catch (UnsupportedEncodingException uee) {
            Log.logStackTrace(uee);
            return text;
        }
    }

    /**
     * Utility method to jigger the specified string so that it's safe
     * to use in a regular Statement.
     */
    public static String safeJigger (String text)
    {
        return StringUtil.replace(jigger(text), "'", "\\'");
    }

    /**
     * Used to programatically create a database table. Does nothing if the
     * table already exists.
     */
    public static void createTableIfMissing (
        Connection conn, String table, String[] definition, String postamble)
        throws SQLException
    {
        if (tableExists(conn, table)) {
            return;
        }

        Statement stmt = conn.createStatement();
        try {
            stmt.executeUpdate("create table " + table + "(" +
                               StringUtil.join(definition, ",") + ") " +
                               postamble);
        } finally {
            close(stmt);
        }

        Log.info("Database table '" + table + "' created.");
    }

    /**
     * Returns true if the table with the specified name exists, false if
     * it does not. <em>Note:</em> the table name is case sensitive.
     */
    public static boolean tableExists (Connection conn, String name)
        throws SQLException
    {
        boolean matched = false;
        ResultSet rs = conn.getMetaData().getTables("", "", name, null);
        while (rs.next()) {
            String tname = rs.getString("TABLE_NAME");
            if (name.equals(tname)) {
                matched = true;
            }
        }
        return matched;
    }

    /**
     * Returns true if the table with the specified name exists and
     * contains a column with the specified name, false if either
     * condition does not hold true. <em>Note:</em> the names are case
     * sensitive.
     */
    public static boolean tableContainsColumn (
        Connection conn, String table, String column)
        throws SQLException
    {
        boolean matched = false;
        ResultSet rs = conn.getMetaData().getColumns("", "", table, column);
        while (rs.next()) {
            String tname = rs.getString("TABLE_NAME");
            String cname = rs.getString("COLUMN_NAME");
            if (tname.equals(table) && cname.equals(column)) {
                matched = true;
            }
        }
        return matched;
    }

    /**
     * Returns true if the index on the specified column exists for the
     * specified table, false if it does not. Optionally you can specifiy
     * a non null index name, and the table will be checked to see if it
     * contains that specifically named index. <em>Note:</em> the names
     * are case sensitive.
     */
    public static boolean tableContainsIndex (Connection conn, String table,
                                              String column, String index)
        throws SQLException
    {
        boolean matched = false;
        ResultSet rs = conn.getMetaData().getIndexInfo("", "", table, false,
                                                       true);
        while (rs.next()) {
            String tname = rs.getString("TABLE_NAME");
            String cname = rs.getString("COLUMN_NAME");
            String iname = rs.getString("INDEX_NAME");

            if (index == null) {
                if (tname.equals(table) && cname.equals(column)) {
                    matched = true;
                }
            } else if (index.equals(iname)) {
                matched = true;
            }
        }
        return matched;
    }

    /**
     * Returns true if the specified table contains a primary key on the
     * specified column.
     */
    public static boolean tableContainsPrimaryKey (Connection conn,
                                                   String table, String column)
        throws SQLException
    {
        boolean matched = false;
        ResultSet rs = conn.getMetaData().getPrimaryKeys("", "", table);
        while (rs.next()) {
            String tname = rs.getString("TABLE_NAME");
            String cname = rs.getString("COLUMN_NAME");

            if (tname.equals(table) && cname.equals(column)) {
                matched = true;
            }
        }
        return matched;
    }

    /**
     * Returns the name of the index for the specified column in the
     * specified table.
     */
    public static String getIndexName (Connection conn, String table,
                                       String column)
        throws SQLException
    {
        boolean matched = false;
        ResultSet rs = conn.getMetaData().getIndexInfo("", "", table, false,
                                                       true);
        while (rs.next()) {
            String tname = rs.getString("TABLE_NAME");
            String cname = rs.getString("COLUMN_NAME");
            String iname = rs.getString("INDEX_NAME");
            if (tname.equals(table) && cname.equals(column)) {
                return iname;
            }
        }
        return null;
    }

    /**
     * Returns the type (as specified in {@link java.sql.Types} for the
     * specified column in the specified table.
     */
    public static int getColumnType (Connection conn, String table,
                                     String column)
        throws SQLException
    {
        boolean matched = false;
        ResultSet rs = conn.getMetaData().getColumns("", "", table, column);

        while (rs.next()) {
            String tname = rs.getString("TABLE_NAME");
            String cname = rs.getString("COLUMN_NAME");
            int type = rs.getInt("DATA_TYPE");
            if (tname.equals(table) && cname.equals(column)) {
                return type;
            }
        }

        throw new SQLException("Table or Column not defined. [table=" + table +
                               ", col=" + column + "].");
    }

    /**
     * Adds a column (with name 'cname' and definition 'cdef') to the
     * specified table.
     *
     * @param afterCname (optional) the name of the column after which to
     * add the new column.
     */
    public static void addColumn (Connection conn, String table,
                                  String cname, String cdef, String afterCname)
        throws SQLException
    {
        if (JDBCUtil.tableContainsColumn(conn, table, cname)) {
//             Log.info("Database table '" + table + "' already has column '" +
//                      cname + "'.");
            return;
        }

        String update = "ALTER TABLE " + table + " ADD COLUMN " +
            cname + " " + cdef;
        if (afterCname != null) {
            update += " AFTER " + afterCname;
        }

        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(update);
            stmt.executeUpdate();
        } finally {
            close(stmt);
        }

        Log.info("Database column '" + cname + "' added to table '" +
                 table + "'.");
    }

    /**
     * Changes a column's definition. Takes a full column definition
     * 'cdef' (including the name of the column) with which to replace the
     * specified column 'cname'.
     *
     * NOTE: A handy thing you can do with this is to rename a column by
     * providing a column definition that has a different name, but the
     * same column type.
     */
    public static void changeColumn (Connection conn, String table,
                                     String cname, String cdef)
        throws SQLException
    {
        String update = "ALTER TABLE " + table + " CHANGE " + cname + " " +
            cdef;

        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(update);
            stmt.executeUpdate();
        } finally {
            close(stmt);
        }

        Log.info("Database column '" + cname + "' of table '" + table +
                 "' modified to have this def '" + cdef + "'.");
    }

    /**
     * Removes a column from the specified table.
     */
    public static void dropColumn (Connection conn, String table, String cname)
        throws SQLException
    {
        String update = "ALTER TABLE " + table + " DROP COLUMN " + cname;

        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(update);
            if (stmt.executeUpdate() == 1) {
                Log.info("Database index '" + cname + "' removed from " +
                         "table '" + table + "'.");
            }
        } finally {
            close(stmt);
        }
    }
    
    /**
     * Removes a named index from the specified table.
     */
    public static void dropIndex (Connection conn, String table, String iname)
        throws SQLException
    {
        String update = "ALTER TABLE " + table + " DROP INDEX " + iname;

        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(update);
            if (stmt.executeUpdate() == 1) {
                Log.info("Database index '" + iname + "' removed from " +
                         "table '" + table + "'.");
            }
        } finally {
            close(stmt);
        }
    }
    
    /**
     * Removes the primary key from the specified table.
     */
    public static void dropPrimaryKey (Connection conn, String table)
        throws SQLException
    {
        String update = "ALTER TABLE " + table + " DROP PRIMARY KEY";

        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(update);
            if (stmt.executeUpdate() == 1) {
                Log.info("Database primary key removed from '" + table + "'.");
            }
        } finally {
            close(stmt);
        }
    }

    /**
     * Adds an index on the specified column (cname) to the specified
     * table.  Optionally supply an index name, otherwise the index is
     * named after the column.
     */
    public static void addIndexToTable (Connection conn, String table,
                                        String cname, String iname)
        throws SQLException
    {
        if (JDBCUtil.tableContainsIndex(conn, table, cname, iname)) {
//             Log.info("Database table '" + table + "' already has an index " +
//                      "on column '" + cname + "'" +
//                      (iname != null ? " named '" + iname + "'." : "."));
            return;
        }

        String idx_name = (iname != null ? iname : cname);
        String update = "CREATE INDEX " + idx_name + " on " + table + "(" +
            cname + ")";

        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(update);
            stmt.executeUpdate();
        } finally {
            close(stmt);
        }

        Log.info("Database index '" + idx_name + "' added to table '" +
                 table + "'");
    }
}
