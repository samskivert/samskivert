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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.samskivert.io.PersistenceException;

import com.samskivert.jdbc.DatabaseLiaison;
import com.samskivert.jdbc.JDBCUtil;

import com.samskivert.jdbc.depot.clause.FieldOverride;
import com.samskivert.jdbc.depot.clause.QueryClause;
import com.samskivert.jdbc.depot.clause.SelectClause;
import com.samskivert.jdbc.depot.clause.Where;
import com.samskivert.jdbc.depot.expression.SQLExpression;
import com.samskivert.jdbc.depot.operator.Logic.*;
import com.samskivert.jdbc.depot.operator.Conditionals.*;

import static com.samskivert.jdbc.depot.Log.log;

/**
 * This class implements the functionality required by {@link DepotRepository#findAll): fetch
 * a collection of persistent objects using one of two included strategies.
 */
public abstract class FindAllQuery<T extends PersistentRecord>
    implements Query<List<T>>
{
    /**
     * The two-pass collection query implementation. {@see DepotRepository#findAll} for details.
     */
    public static class WithCache<T extends PersistentRecord> extends FindAllQuery<T>
    {
        public WithCache (PersistenceContext ctx, Class<T> type, QueryClause[] clauses)
            throws PersistenceException
        {
            super(ctx, type);

            if (_marsh.getComputed() != null) {
                throw new IllegalArgumentException(
                "This algorithm doesn't work on @Computed records.");
            }
            for (QueryClause clause : clauses) {
                if (clause instanceof FieldOverride) {
                    throw new IllegalArgumentException(
                        "This algorithm doesn't work with FieldOverrides.");
                }
            }

            DepotTypes types = DepotTypes.getDepotTypes(ctx, clauses);
            types.addClass(ctx, type);
            _builder = _ctx.getSQLBuilder(types);
            _clauses = clauses;
        }

        public List<T> invoke (Connection conn, DatabaseLiaison liaison) throws SQLException
        {
            Map<Key<T>, T> entities = new HashMap<Key<T>, T>();
            List<Key<T>> allKeys = new ArrayList<Key<T>>();
            List<Key<T>> fetchKeys = new ArrayList<Key<T>>();

            _builder.newQuery(new SelectClause<T>(_type, _marsh.getPrimaryKeyFields(), _clauses));
            PreparedStatement stmt = _builder.prepare(conn);
            try {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    Key<T> key = _marsh.makePrimaryKey(rs);
                    allKeys.add(key);

                    // TODO: All this cache fiddling needs to move to PersistenceContext?
                    CacheAdapter.CachedValue<T> hit = _ctx.cacheLookup(key);
                    if (hit != null) {
                        T value = hit.getValue();
                        if (value != null) {
                            @SuppressWarnings("unchecked") T newValue = (T) value.clone();
                            entities.put(key, newValue);
                            continue;
                        }
                    }

                    fetchKeys.add(key);
                }

            } finally {
                JDBCUtil.close(stmt);
            }

            if (fetchKeys.size() > 0) {
                SQLExpression condition;

                if (_marsh.getPrimaryKeyFields().length == 1) {
                    // Single-column keys result in the compact IN(keyVal1, keyVal2, ...)
                    Comparable[] keyFieldValues = new Comparable[fetchKeys.size()];
                    for (int ii = 0; ii < keyFieldValues.length; ii ++) {
                        keyFieldValues[ii] = fetchKeys.get(ii).condition.getValues().get(0);
                    }
                    condition = new In(_type, _marsh.getPrimaryKeyFields()[0], keyFieldValues);

                } else {
                    // Multi-column keys result in OR'd AND's, of unknown efficiency (TODO check).
                    SQLExpression[] keyArray = new SQLExpression[fetchKeys.size()];
                    for (int ii = 0; ii < keyArray.length; ii ++) {
                        keyArray[ii] = fetchKeys.get(ii).condition;
                    }
                    condition = new Or(keyArray);
                }

                Where keyWhere = new Where(condition);
                // finally build the new query
                _builder.newQuery(new SelectClause<T>(_type, _marsh.getFieldNames(), keyWhere));
                stmt = _builder.prepare(conn);

                // and execute it
                try {
                    ResultSet rs = stmt.executeQuery();
                    int cnt = 0, dups = 0;
                    while (rs.next()) {
                        T obj = _marsh.createObject(rs);
                        if (entities.put(_marsh.getPrimaryKey(obj), obj) != null) {
                            dups++;
                        }
                        cnt++;
                    }
                    if (cnt != fetchKeys.size()) {
                        log.warning("Row count mismatch in second pass [query=" + stmt +
                                    ", wanted=" + fetchKeys.size() + ", got=" + cnt +
                                    ", dups=" + dups + "]");
                    }

                } finally {
                    JDBCUtil.close(stmt);
                }
            }

            List<T> result = new ArrayList<T>();
            for (Key<T> key : allKeys) {
                T value = entities.get(key);
                if (value != null) {
                    result.add(value);
                }
            }
            return result;
        }

        protected QueryClause[] _clauses;
    }

    /**
     * The single-pass collection query implementation. {@see DepotRepository#findAll} for details.
     */
    public static class Explicitly<T extends PersistentRecord> extends FindAllQuery<T>
    {
        public Explicitly (PersistenceContext ctx, Class<T> type, QueryClause[] clauses)
            throws PersistenceException
        {
            super(ctx, type);
            SelectClause<T> select = new SelectClause<T>(type, _marsh.getFieldNames(), clauses);
            _builder = ctx.getSQLBuilder(DepotTypes.getDepotTypes(ctx, select));
            _builder.newQuery(select);
        }

        public List<T> invoke (Connection conn, DatabaseLiaison liaison) throws SQLException
        {
            List<T> result = new ArrayList<T>();
            PreparedStatement stmt = _builder.prepare(conn);
            try {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    result.add(_marsh.createObject(rs));
                }
            } finally {
                JDBCUtil.close(stmt);
            }
            return result;
        }
    }

    public FindAllQuery (PersistenceContext ctx, Class<T> type)
        throws PersistenceException
    {
        _ctx = ctx;
        _type = type;
        _marsh = _ctx.getMarshaller(type);
    }

    // from Query
    public CacheKey getCacheKey ()
    {
        return null;
    }

    // from Query
    public void updateCache (PersistenceContext ctx, List<T> result) {
        if (_marsh.hasPrimaryKey()) {
            for (T bit : result) {
                ctx.cacheStore(_marsh.getPrimaryKey(bit), bit.clone());
            }
        }
    }

    // from Query
    public List<T> transformCacheHit (CacheKey key, List<T> bits)
    {
        if (bits == null) {
            return bits;
        }

        List<T> result = new ArrayList<T>();
        for (T bit : bits) {
            if (bit != null) {
                @SuppressWarnings("unchecked") T cbit = (T) bit.clone();
                result.add(cbit);
            } else {
                result.add(null);
            }
        }
        return result;
    }

    protected PersistenceContext _ctx;
    protected SQLBuilder _builder;
    protected DepotMarshaller<T> _marsh;
    protected Class<T> _type;
}
