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

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import com.samskivert.io.PersistenceException;
import com.samskivert.jdbc.JDBCUtil;
import com.samskivert.jdbc.depot.operator.Logic.*;
import com.samskivert.jdbc.depot.clause.FieldOverride;
import com.samskivert.jdbc.depot.clause.Join;
import com.samskivert.jdbc.depot.clause.QueryClause;
import com.samskivert.jdbc.depot.clause.Where;
import com.samskivert.jdbc.depot.expression.SQLExpression;
import com.samskivert.util.ArrayUtil;

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
            _types = SQLQueryBuilder.getDepotTypes(ctx, type, clauses);
            _clauses = clauses;
        }

        public List<T> invoke (Connection conn) throws SQLException {
            SQLQueryBuilder<T> keyFieldQuery = new SQLQueryBuilder<T>(
                    _ctx, _types, _marsh.getPrimaryKeyColumns(), _clauses);
            PreparedStatement stmt = keyFieldQuery.prepare(conn);

            Map<Key<T>, T> entities = new HashMap<Key<T>, T>();
            List<Key<T>> allKeys = new ArrayList<Key<T>>();
            List<Key<T>> fetchKeys = new ArrayList<Key<T>>();
            try {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    Key<T> key = _marsh.makePrimaryKey(rs);
                    allKeys.add(key);

                    // TODO: All this cache fiddling needs to move to PersistenceContext?
                    Cache cache = _ctx.getCache(key.getCacheId());
                    Element hit = cache.get(key.getCacheKey());
                    if (hit != null) {
                        @SuppressWarnings("unchecked") T value = (T) hit.getValue();
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
                int jj = 0;
                QueryClause[] newClauses = new QueryClause[_clauses.length + 1];
                // a select subset of query clauses are preserved for the entity query
                for (int ii = 0; ii < _clauses.length; ii ++) {
                    if (_clauses[ii] instanceof Join || _clauses[ii] instanceof FieldOverride) {
                        newClauses[jj ++] = _clauses[ii];
                    }
                }
                SQLExpression[] keyArray = fetchKeys.toArray(new SQLExpression[0]);

                // add our special key-matching where clause
                newClauses[jj ++] = new Where(new Or(keyArray));
                newClauses = ArrayUtil.splice(newClauses, jj);

                // build the new query
                SQLQueryBuilder<T> entityQuery = new SQLQueryBuilder<T>(
                        _ctx, _types, _marsh.getFieldNames(), newClauses);

                // and execute it
                stmt = entityQuery.prepare(conn);

                try {
                    ResultSet rs = stmt.executeQuery();
                    for (Key<T> key : fetchKeys) {
                        if (!rs.next()) {
                            throw new SQLException("Expecting more rows in result set.");
                        }
                        entities.put(key, _marsh.createObject(rs));
                    }

                } finally {
                    JDBCUtil.close(stmt);
                }
            }

            List<T> result = new ArrayList<T>();
            for (Key<T> key : allKeys) {
                result.add(entities.get(key));
            }
            return result;
        }

        protected DepotTypes<T> _types;
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
            DepotTypes<List<T>> types = SQLQueryBuilder.getDepotTypes(ctx, type, clauses);
            _builder = new SQLQueryBuilder<List<T>>(ctx, types, _marsh.getFieldNames(), clauses);
        }

        public List<T> invoke (Connection conn) throws SQLException {
            PreparedStatement stmt = _builder.prepare(conn);

            List<T> result = new ArrayList<T>();
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

        protected SQLQueryBuilder<List<T>> _builder;
    }

    public FindAllQuery (PersistenceContext ctx, Class<T> type)
        throws PersistenceException
    {
        _ctx = ctx;
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
    protected DepotMarshaller<T> _marsh;
}
