//
// $Id: JDBCUtil.java,v 1.2 2001/08/11 22:43:28 mdb Exp $
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
