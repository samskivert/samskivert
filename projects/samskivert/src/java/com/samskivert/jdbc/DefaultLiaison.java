//
// $Id: DefaultLiaison.java,v 1.1 2001/09/20 02:09:09 mdb Exp $
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

import java.sql.Connection;
import java.sql.SQLException;

/**
 * The default liaison is used if no other liaison could be matched for a
 * particular database connection. It isn't very smart or useful but we
 * need something.
 */
public class DefaultLiaison implements DatabaseLiaison
{
    // documentation inherited
    public boolean matchesURL (String url)
    {
        return true;
    }

    // documentation inherited
    public boolean isDuplicateRowException (SQLException sqe)
    {
        return false;
    }

    // documentation inherited
    public boolean isTransientException (SQLException sqe)
    {
        return false;
    }

    // documentation inherited
    public int lastInsertedId (Connection conn) throws SQLException
    {
        return -1;
    }
}
