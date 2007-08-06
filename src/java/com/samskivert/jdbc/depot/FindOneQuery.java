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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.samskivert.io.PersistenceException;

import com.samskivert.jdbc.DatabaseLiaison;
import com.samskivert.jdbc.JDBCUtil;
import com.samskivert.jdbc.depot.clause.QueryClause;
import com.samskivert.jdbc.depot.clause.SelectClause;

/**
 * The implementation of {@link DepotRepository#find) functionality.
 */
public class FindOneQuery<T extends PersistentRecord>
    implements Query<T>
{
    public FindOneQuery (PersistenceContext ctx, Class<T> type, QueryClause[] clauses)
        throws PersistenceException
    {
        _marsh = ctx.getMarshaller(type);
        _select = new SelectClause<T>(type, _marsh.getFieldNames(), clauses);
        _builder = ctx.getSQLBuilder(DepotTypes.getDepotTypes(ctx, _select));
        _builder.newQuery(_select);
    }

    // from Query
    public CacheKey getCacheKey ()
    {
        WhereClause where = _select.getWhereClause();
        if (where != null && where instanceof CacheKey) {
            return (CacheKey) where;
        }
        return null;
    }

    // from Query
    public T invoke (Connection conn, DatabaseLiaison liaison) throws SQLException {
        PreparedStatement stmt = _builder.prepare(conn);
        try {
            T result = null;
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                result = _marsh.createObject(rs);
            }
            // TODO: if (rs.next()) issue warning?
            rs.close();
            return result;

        } finally {
            JDBCUtil.close(stmt);
        }
    }

    // from Query
    public void updateCache (PersistenceContext ctx, T result) {
        CacheKey key = getCacheKey();
        if (key == null) {
            // no row-specific cache key was given
            if (result == null || !_marsh.hasPrimaryKey()) {
                return;
            }
            // if we can, create a key from what was actually returned
            key = _marsh.getPrimaryKey(result);
        }
        ctx.cacheStore(key, (result != null) ? result.clone() : null);
    }

    // from Query
    public T transformCacheHit (CacheKey key, T value)
    {
        if (value == null) {
            return null;
        }
        // we do not want to return a reference to the actual cached entity so we clone it
        @SuppressWarnings("unchecked") T cvalue = (T) value.clone();
        return cvalue;
    }

    protected DepotMarshaller<T> _marsh;
    protected SelectClause<T> _select;
    protected SQLBuilder _builder;
}
