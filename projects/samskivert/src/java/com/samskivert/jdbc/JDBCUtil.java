//
// $Id: JDBCUtil.java,v 1.1 2000/12/06 00:27:30 mdb Exp $

package com.samskivert.jdbc;

import java.sql.*;

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
}
