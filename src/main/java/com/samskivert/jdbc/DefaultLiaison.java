//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * The default liaison is used if no other liaison could be matched for a particular database
 * connection. It isn't very smart or useful but we need something.
 */
public class DefaultLiaison extends BaseLiaison
{
    @Override // from DatabaseLiaison
    public boolean matchesURL (String url)
    {
        return true;
    }

    @Override // from DatabaseLiaison
    public boolean isDuplicateRowException (SQLException sqe)
    {
        return false;
    }

    @Override // from DatabaseLiaison
    public boolean isTransientException (SQLException sqe)
    {
        return false;
    }

    @Override // from DatabaseLiaison
    public void createGenerator (Connection conn, String tableName, String columnName, int initValue)
        throws SQLException
    {
        // nothing doing
    }

    @Override // from DatabaseLiaison
    public void deleteGenerator (Connection conn, String tableName, String columnName)
        throws SQLException
    {
        // nothing doing
    }

    @Override // from DatabaseLiaison
    public String columnSQL (String column)
    {
        return column;
    }

    @Override // from DatabaseLiaison
    public String tableSQL (String table)
    {
        return table;
    }

    @Override // from DatabaseLiaison
    public String indexSQL (String index)
    {
        return index;
    }
}
