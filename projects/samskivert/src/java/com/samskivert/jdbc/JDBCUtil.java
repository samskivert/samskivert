//
// $Id: JDBCUtil.java,v 1.7 2004/04/30 02:38:10 mdb Exp $
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

import java.io.UnsupportedEncodingException;
import java.sql.*;

import com.samskivert.Log;
import com.samskivert.io.PersistenceException;

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
}
