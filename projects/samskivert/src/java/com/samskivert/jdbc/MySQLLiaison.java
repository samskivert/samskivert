//
// $Id: MySQLLiaison.java,v 1.4 2001/09/28 22:41:40 mdb Exp $
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
 * A database liaison for the MySQL database.
 */
public class MySQLLiaison implements DatabaseLiaison
{
    // documentation inherited
    public boolean matchesURL (String url)
    {
        return url.startsWith("jdbc:mysql");
    }

    // documentation inherited
    public boolean isDuplicateRowException (SQLException sqe)
    {
	String msg = sqe.getMessage();
	return (msg != null && msg.indexOf("Duplicate entry") != -1);
    }

    // documentation inherited
    public boolean isTransientException (SQLException sqe)
    {
	String msg = sqe.getMessage();
	return (msg != null &&
                (msg.indexOf("Lost connection") != -1 ||
                 msg.indexOf("Broken pipe") != -1));
    }

    // documentation inherited
    public int lastInsertedId (Connection conn) throws SQLException
    {
        Statement stmt = null;

	// we have to do this by hand. alas all is not roses.
        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("select LAST_INSERT_ID()");
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                return -1;
            }

        } finally {
            JDBCUtil.close(stmt);
        }
    }
}
