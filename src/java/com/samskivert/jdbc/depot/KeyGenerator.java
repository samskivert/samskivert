//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2006-2007 Michael Bayne, Pär Winzell
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

package com.samskivert.jdbc.depot;

import java.sql.Connection;
import java.sql.SQLException;

import com.samskivert.jdbc.DatabaseLiaison;
import com.samskivert.jdbc.depot.annotation.GeneratedValue;

/**
 * Defines the interface to our primary key generators.
 */
public abstract class KeyGenerator
{
    public KeyGenerator (GeneratedValue gv, String table, String column)
    {
        _allocationSize = gv.allocationSize();
        _initialValue = gv.initialValue();
        _table = table;
        _column = column;
    }

    /**
     * If true, this key generator will be run after the insert statement, if false, it will be run
     * before.
     */
    public abstract boolean isPostFactum ();

    /**
     * Prepares the generator for operation.
     */
    public abstract void init (Connection conn, DatabaseLiaison liaison)
        throws SQLException;

    /**
     * Fetch/generate the next primary key value.
     */
    public abstract int nextGeneratedValue (Connection conn, DatabaseLiaison liaison)
        throws SQLException;

    /**
     * Returns the name of the table for which we're generating a key value.
     */
    public String getTable ()
    {
        return _table;
    }

    /**
     * Returns the name of the column for which we're generating a key value.
     */
    public String getColumn ()
    {
        return _column;
    }

    protected int _initialValue;
    protected int _allocationSize;
    protected String _table;
    protected String _column;
}
