//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2008 Michael Bayne
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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Handles liaison for HSQLDB. NOTE: incomplete and doesn't work yet!
 */
public class HsqldbLiaison extends BaseLiaison
{
    // from DatabaseLiaison
    public boolean matchesURL (String url)
    {
        return url.startsWith("jdbc:hsqldb");
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

    // from DatabaseLiaison
    public void createGenerator (Connection conn, String tableName, String columnName, int initValue)
        throws SQLException
    {
        // TODO: is there any way we can set the initial AUTO_INCREMENT value?
    }

    // from DatabaseLiaison
    public void deleteGenerator (Connection conn, String tableName, String columnName)
        throws SQLException
    {
        // AUTO_INCREMENT does not create any database entities that we need to delete
    }

    // from DatabaseLiaison
    public int lastInsertedId (Connection conn, String table, String column) throws SQLException
    {
        throw new NotImplementedException();
//         // MySQL does not keep track of per-table-and-column insertion data, so we are pretty much
//         // going on blind faith here that we're fetching the right ID. In the overwhelming number
//         // of cases that will be so, but it's still not pretty.
//         Statement stmt = null;
//         try {
//             stmt = conn.createStatement();
//             ResultSet rs = stmt.executeQuery("select LAST_INSERT_ID()");
//             return rs.next() ? rs.getInt(1) : -1;
//         } finally {
//             JDBCUtil.close(stmt);
//         }
    }

    // from DatabaseLiaison
    public boolean isTransientException (SQLException sqe)
    {
        return false; // no known transient exceptions for HSQLDB
    }

    // from DatabaseLiaison
    public boolean isDuplicateRowException (SQLException sqe)
    {
        throw new NotImplementedException();
//         String msg = sqe.getMessage(); // TODO: not sure
//         return (msg != null && msg.indexOf("Duplicate entry") != -1);
    }
}
