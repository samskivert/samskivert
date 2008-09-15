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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.samskivert.jdbc.DatabaseLiaison;
import com.samskivert.jdbc.JDBCUtil;

import com.samskivert.jdbc.depot.clause.FieldOverride;
import com.samskivert.jdbc.depot.clause.QueryClause;
import com.samskivert.jdbc.depot.clause.SelectClause;
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
        public WithCache (PersistenceContext ctx, Class<T> type,
                          Collection<? extends QueryClause> clauses)
            throws DatabaseException
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

            SelectClause<T> select =
                new SelectClause<T>(_type, _marsh.getPrimaryKeyFields(), clauses);
            _builder = _ctx.getSQLBuilder(DepotTypes.getDepotTypes(ctx, select));
            _builder.newQuery(select);
        }

        public List<T> invoke (Connection conn, DatabaseLiaison liaison)
            throws SQLException
        {
            Map<Key<T>, T> entities = new HashMap<Key<T>, T>();
            List<Key<T>> allKeys = new ArrayList<Key<T>>();
            Set<Key<T>> fetchKeys = new HashSet<Key<T>>();

            PreparedStatement stmt = _builder.prepare(conn);
            String stmtString = stmt.toString(); // for debugging
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

            return loadAndResolve(conn, allKeys, fetchKeys, entities, stmtString);
        }
    }

    /**
     * The two-pass collection query implementation. {@see DepotRepository#findAll} for details.
     */
    public static class WithKeys<T extends PersistentRecord> extends FindAllQuery<T>
    {
        public WithKeys (PersistenceContext ctx, Collection<Key<T>> keys)
            throws DatabaseException
        {
            super(ctx, keys.iterator().next().getPersistentClass());
            _keys = keys;
            _builder = ctx.getSQLBuilder(new DepotTypes(ctx, _type));
        }

        public List<T> invoke (Connection conn, DatabaseLiaison liaison)
            throws SQLException
        {
            Map<Key<T>, T> entities = new HashMap<Key<T>, T>();
            Set<Key<T>> fetchKeys = new HashSet<Key<T>>();
            for (Key<T> key : _keys) {
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

            return loadAndResolve(conn, _keys, fetchKeys, entities, null);
        }

        protected Collection<Key<T>> _keys;
    }

    /**
     * The single-pass collection query implementation. {@see DepotRepository#findAll} for details.
     */
    public static class Explicitly<T extends PersistentRecord> extends FindAllQuery<T>
    {
        public Explicitly (PersistenceContext ctx, Class<T> type,
                           Collection<? extends QueryClause> clauses)
            throws DatabaseException
        {
            super(ctx, type);
            SelectClause<T> select = new SelectClause<T>(type, _marsh.getFieldNames(), clauses);
            _builder = ctx.getSQLBuilder(DepotTypes.getDepotTypes(ctx, select));
            _builder.newQuery(select);
        }

        public List<T> invoke (Connection conn, DatabaseLiaison liaison)
            throws SQLException
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
        throws DatabaseException
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

    protected List<T> loadAndResolve (Connection conn, Collection<Key<T>> allKeys,
                                      Set<Key<T>> fetchKeys, Map<Key<T>, T> entities,
                                      String origStmt)
        throws SQLException
    {
        // if we're fetching a huge number of records, we have to do it in multiple queries
        if (fetchKeys.size() > In.MAX_KEYS) {
            int keyCount = fetchKeys.size();
            do {
                Set<Key<T>> keys = new HashSet<Key<T>>();
                Iterator<Key<T>> iter = fetchKeys.iterator();
                for (int ii = 0; ii < Math.min(keyCount, In.MAX_KEYS); ii++) {
                    keys.add(iter.next());
                    iter.remove();
                }
                keyCount -= keys.size();
                loadRecords(conn, keys, entities, origStmt);
            } while (keyCount > 0);

        } else if (fetchKeys.size() > 0) {
            loadRecords(conn, fetchKeys, entities, origStmt);
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

    protected void loadRecords (Connection conn, Set<Key<T>> keys, Map<Key<T>, T> entities,
                                String origStmt)
        throws SQLException
    {
        _builder.newQuery(new SelectClause<T>(_type, _marsh.getFieldNames(),
                                              new KeySet<T>(_type, keys)));
        PreparedStatement stmt = _builder.prepare(conn);
        try {
            Set<Key<T>> got = new HashSet<Key<T>>();
            ResultSet rs = stmt.executeQuery();
            int cnt = 0, dups = 0;
            while (rs.next()) {
                T obj = _marsh.createObject(rs);
                Key<T> key = _marsh.getPrimaryKey(obj);
                if (entities.put(key, obj) != null) {
                    dups++;
                }
                got.add(key);
                cnt++;
            }
            // if we get more results than we planned, or if we're doing a two-phase query and got
            // fewer, then complain
            if (cnt > keys.size() || (origStmt != null && cnt < keys.size())) {
                log.warning("Row count mismatch in second pass", "origQuery", origStmt,
                            "wanted", keys, "got", got, "dups", dups, new Exception());
            }

        } finally {
            JDBCUtil.close(stmt);
        }
    }

    protected PersistenceContext _ctx;
    protected SQLBuilder _builder;
    protected DepotMarshaller<T> _marsh;
    protected Class<T> _type;
}
