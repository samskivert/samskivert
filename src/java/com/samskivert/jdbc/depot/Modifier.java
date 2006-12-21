//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2006 Michael Bayne
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
import java.sql.Statement;

import com.samskivert.jdbc.depot.clause.Where;

/**
 * Encapsulates a modification of persistent objects.
 */
public abstract class Modifier
{
    /** A simple modifier that executes a single SQL statement. No cache flushing is done as a
     * result of this operation. */
    public static class Simple extends Modifier
    {
        public Simple (String query) {
            super(null);
            _query = query;
        }

        public int invoke (Connection conn) throws SQLException {
            Statement stmt = conn.createStatement();
            try {
                return stmt.executeUpdate(_query);
            } finally {
                stmt.close();
            }
        }

        protected String _query;
    }

    public abstract int invoke (Connection conn) throws SQLException;

    protected Modifier (Where key)
    {
        _key = key;
    }

    protected void updateKey (Key key)
    {
        if (key != null) {
            _key = key;
        }
    }

    protected Where _key;
}
