//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2010 Michael Bayne, et al.
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
import java.sql.SQLException;

/**
 * The default liaison is used if no other liaison could be matched for a particular database
 * connection. It isn't very smart or useful but we need something.
 */
public class DefaultLiaison extends BaseLiaison
{
    // from DatabaseLiaison
    public boolean matchesURL (String url)
    {
        return true;
    }

    // from DatabaseLiaison
    public boolean isDuplicateRowException (SQLException sqe)
    {
        return false;
    }

    // from DatabaseLiaison
    public boolean isTransientException (SQLException sqe)
    {
        return false;
    }

    // from DatabaseLiaison
    public void createGenerator (Connection conn, String tableName, String columnName, int initValue)
        throws SQLException
    {
        // nothing doing
    }

    // from DatabaseLiaison
    public void deleteGenerator (Connection conn, String tableName, String columnName)
        throws SQLException
    {
        // nothing doing
    }

    // from DatabaseLiaison
    public int lastInsertedId (Connection conn, String table, String column)
        throws SQLException
    {
        return -1;
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
