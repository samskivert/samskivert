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
 * Generates primary keys using an identity column.
 */
public class IdentityValueGenerator extends ValueGenerator
{
    public IdentityValueGenerator (GeneratedValue gv, DepotMarshaller<?> dm, FieldMarshaller<?> fm)
    {
        super(gv, dm, fm);
    }

    @Override // from ValueGenerator
    public boolean isPostFactum ()
    {
        return true;
    }

    @Override // from ValueGenerator
    public void init (Connection conn, DatabaseLiaison liaison)
        throws SQLException
    {
        // identity value generators are auto-created by the database
    }

    @Override // from ValueGenerator
    public int nextGeneratedValue (Connection conn, DatabaseLiaison liaison)
        throws SQLException
    {
        return liaison.lastInsertedId(conn, _dm.getTableName(), _fm.getColumnName());
    }

    @Override // from ValueGenerator
    public void delete (Connection conn, DatabaseLiaison liaison)
        throws SQLException
    {
        liaison.deleteGenerator(conn, _dm.getTableName(), _fm.getColumnName());
    }
}
