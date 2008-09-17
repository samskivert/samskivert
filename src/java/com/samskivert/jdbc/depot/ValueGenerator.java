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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.samskivert.jdbc.DatabaseLiaison;
import com.samskivert.jdbc.JDBCUtil;
import com.samskivert.jdbc.depot.annotation.GeneratedValue;

import static com.samskivert.jdbc.depot.Log.log;

/**
 * Defines the interface to our value generators.
 */
public abstract class ValueGenerator
{
    public ValueGenerator (GeneratedValue gv, DepotMarshaller<?> dm, FieldMarshaller<?> fm)
    {
        _allocationSize = gv.allocationSize();
        _initialValue = gv.initialValue();
        _migrateIfExists = gv.migrateIfExists();
        _dm = dm;
        _fm = fm;
    }

    /**
     * If true, this key generator will be run after the insert statement, if false, it will be run
     * before.
     */
    public abstract boolean isPostFactum ();

    /**
     * Ensures the generator is prepared for operation, creating it if necessary. Care is taken to
     * only run this the first time a column is created. However, if a column with a value
     * generator is renamed, we can't distinguish that from a newly created column and we call this
     * method again on the renamed column. If it's possible to not fail in that circumstance, try
     * to avoid doing so.
     */
    public abstract void create (Connection conn, DatabaseLiaison liaison)
        throws SQLException;

    /**
     * Fetch/generate the next primary key value.
     */
    public abstract int nextGeneratedValue (Connection conn, DatabaseLiaison liaison)
        throws SQLException;

    /**
     * Delete all database entities associated with this value generator.
     */
    public abstract void delete (Connection conn, DatabaseLiaison liaison)
        throws SQLException;

    /**
     * Scans the table associated with this {@link ValueGenerator} and returns either null, if
     * there are no rows, or an {@link Integer} containing the largest numerical value our
     * field attains.
     */
    protected Integer getFieldMaximum (Connection conn, DatabaseLiaison liaison)
        throws SQLException
    {
        String column = _fm.getColumnName();
        String table = _dm.getTableName();

        Statement stmt = conn.createStatement();
        try {
            ResultSet rs = stmt.executeQuery(
                " SELECT COUNT(*), MAX(" + liaison.columnSQL(column) + ") " +
                "   FROM " + liaison.tableSQL(table));
            if (!rs.next()) {
                log.warning("Query on count()/max() bizarrely returned no rows.");
                return null;
            }

            int cnt = rs.getInt(1);
            if (cnt > 0) {
                return Integer.valueOf(rs.getInt(2));
            }
            return null;

        } finally {
            JDBCUtil.close(stmt);
        }
    }

    public DepotMarshaller<?> getDepotMarshaller ()
    {
        return _dm;
    }

    public FieldMarshaller<?> getFieldMarshaller ()
    {
        return _fm;
    }

    protected int _initialValue;
    protected int _allocationSize;
    protected boolean _migrateIfExists;

    protected DepotMarshaller<?> _dm;
    protected FieldMarshaller<?> _fm;
}
