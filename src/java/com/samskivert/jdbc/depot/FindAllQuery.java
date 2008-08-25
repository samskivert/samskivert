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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.samskivert.io.PersistenceException;
import com.samskivert.util.IntSet;

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
        public WithCache (PersistenceContext ctx, Class<T> type, List<QueryClause> clauses)
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
            Set<Key<T>> fetchKeys = new HashSet<Key<T>>();

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

            // if we're fetching a huge number of records, we have to do it in multiple queries
            if (fetchKeys.size() > MAX_IN_KEYS) {
                int keyCount = fetchKeys.size();
                do {
                    Set<Key<T>> keys = new HashSet<Key<T>>();
                    Iterator<Key<T>> iter = fetchKeys.iterator();
                    for (int ii = 0; ii < Math.min(keyCount, MAX_IN_KEYS); ii++) {
                        keys.add(iter.next());
                        iter.remove();
                    }
                    keyCount -= keys.size();
                    loadRecords(conn, keys, entities);
                } while (keyCount > 0);

            } else if (fetchKeys.size() > 0) {
                loadRecords(conn, fetchKeys, entities);
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

        protected void loadRecords (Connection conn, Set<Key<T>> keys, Map<Key<T>, T> entities)
            throws SQLException
        {
            SQLExpression condition;

            if (_marsh.getPrimaryKeyFields().length == 1) {
                // Single-column keys result in the compact IN(keyVal1, keyVal2, ...)
                Comparable<?>[] keyFieldValues = new Comparable<?>[keys.size()];
                int ii = 0;
                for (Key<T> key : keys) {
                    keyFieldValues[ii ++] = key.condition.getValues().get(0);
                }
                condition = new In(_type, _marsh.getPrimaryKeyFields()[0], keyFieldValues);

            } else {
                // Multi-column keys result in OR'd AND's, of unknown efficiency (TODO check).
                SQLExpression[] keyArray = new SQLExpression[keys.size()];
                int ii = 0;
                for (Key<T> key : keys) {
                    keyArray[ii ++] = key.condition;
                }
                condition = new Or(keyArray);
            }

            Where keyWhere = new Where(condition);
            // finally build the new query
            _builder.newQuery(new SelectClause<T>(_type, _marsh.getFieldNames(), keyWhere));
            PreparedStatement stmt = _builder.prepare(conn);

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
                if (cnt != keys.size()) {
                    log.warning("Row count mismatch in second pass [query=" + stmt +
                                ", wanted=" + keys.size() + ", got=" + cnt +
                                ", dups=" + dups + "]");
                }

            } finally {
                JDBCUtil.close(stmt);
            }
        }

        protected List<QueryClause> _clauses;
    }

    /**
     * The single-pass collection query implementation. {@see DepotRepository#findAll} for details.
     */
    public static class Explicitly<T extends PersistentRecord> extends FindAllQuery<T>
    {
        public Explicitly (PersistenceContext ctx, Class<T> type, List<QueryClause> clauses)
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

    /** The maximum number of keys allowed in an IN() clause. */
    protected static final int MAX_IN_KEYS = Short.MAX_VALUE;
}
