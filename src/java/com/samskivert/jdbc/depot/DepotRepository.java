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
import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.samskivert.util.ArrayUtil;

import com.samskivert.jdbc.ConnectionProvider;
import com.samskivert.jdbc.DatabaseLiaison;
import com.samskivert.jdbc.JDBCUtil;

import com.samskivert.jdbc.depot.Modifier.*;
import com.samskivert.jdbc.depot.clause.DeleteClause;
import com.samskivert.jdbc.depot.clause.FieldOverride;
import com.samskivert.jdbc.depot.clause.InsertClause;
import com.samskivert.jdbc.depot.clause.QueryClause;
import com.samskivert.jdbc.depot.clause.SelectClause;
import com.samskivert.jdbc.depot.clause.UpdateClause;
import com.samskivert.jdbc.depot.expression.SQLExpression;
import com.samskivert.jdbc.depot.expression.ValueExp;

import static com.samskivert.jdbc.depot.Log.log;

/**
 * Provides a base for classes that provide access to persistent objects. Also defines the
 * mechanism by which all persistent queries and updates are routed through the distributed cache.
 */
public abstract class DepotRepository
{
    /**
     * Creates a repository with the supplied persistence context. Any schema migrations needed by
     * this repository should be registered in its constructor. A repository should <em>not</em>
     * perform any actual database operations in its constructor, only register schema
     * migrations. Initialization related database operations should be performed in {@link #init}.
     */
    protected DepotRepository (PersistenceContext context)
    {
        _ctx = context;
        _ctx.repositoryCreated(this);
    }

    /**
     * Creates a repository with the supplied connection provider and its own private persistence
     * context. This should generally not be used for new systems, and is only included to
     * facilitate the integration of small numbers of Depot-based repositories into systems using
     * the older samskivert SimpleRepository system.
     */
    protected DepotRepository (ConnectionProvider conprov)
    {
        _ctx = new PersistenceContext();
        _ctx.init(getClass().getName(), conprov, null);
        _ctx.repositoryCreated(this);
    }

    /**
     * Resolves all persistent records registered to this repository (via {@link
     * #getManagedRecords}. This will be done before the repository is initialized via {@link
     * #init}.
     */
    protected void resolveRecords ()
        throws DatabaseException
    {
        Set<Class<? extends PersistentRecord>> classes =
            new HashSet<Class<? extends PersistentRecord>>();
        getManagedRecords(classes);
        for (Class<? extends PersistentRecord> rclass : classes) {
            _ctx.getMarshaller(rclass);
        }
    }

    /**
     * Provides a place where a repository can perform any initialization that requires database
     * operations.
     */
    protected void init ()
        throws DatabaseException
    {
        // run any registered data migrations
        for (DataMigration migration : _dataMigs) {
            runMigration(migration);
        }
        _dataMigs = null; // note that we've been initialized
    }

    /**
     * Registers a data migration for this repository. This migration will only be run once and its
     * unique identifier will be stored persistently to ensure that it is never run again on the
     * same database. Nonetheless, migrations should strive to be idempotent because someone might
     * come along and create a brand new system installation and all registered migrations will be
     * run once on the freshly created database. As with all database migrations, understand
     * clearly how the process works and think about edge cases when creating a migration.
     *
     * <p> See {@link PersistenceContext#registerMigration} for details on how schema migrations
     * operate and how they might interact with data migrations.
     */
    protected void registerMigration (DataMigration migration)
    {
        if (_dataMigs == null) {
            // we've already been initialized, so we have to run this migration immediately
            runMigration(migration);
        } else {
            _dataMigs.add(migration);
        }
    }

    /**
     * Adds the persistent classes used by this repository to the supplied set.
     */
    protected abstract void getManagedRecords (Set<Class<? extends PersistentRecord>> classes);

    /**
     * Loads the persistent object that matches the specified primary key.
     */
    protected <T extends PersistentRecord> T load (Class<T> type, Comparable<?> primaryKey,
                                                   QueryClause... clauses)
        throws DatabaseException
    {
        clauses = ArrayUtil.append(clauses, _ctx.getMarshaller(type).makePrimaryKey(primaryKey));
        return load(type, clauses);
    }

    /**
     * Loads the persistent object that matches the specified primary key.
     */
    protected <T extends PersistentRecord> T load (Class<T> type, String ix, Comparable<?> val,
                                                   QueryClause... clauses)
        throws DatabaseException
    {
        clauses = ArrayUtil.append(clauses, new Key<T>(type, ix, val));
        return load(type, clauses);
    }

    /**
     * Loads the persistent object that matches the specified two-column primary key.
     */
    protected <T extends PersistentRecord> T load (Class<T> type, String ix1, Comparable<?> val1,
                                                   String ix2, Comparable<?> val2,
                                                   QueryClause... clauses)
        throws DatabaseException
    {
        clauses = ArrayUtil.append(clauses, new Key<T>(type, ix1, val1, ix2, val2));
        return load(type, clauses);
    }

    /**
     * Loads the persistent object that matches the specified three-column primary key.
     */
    protected <T extends PersistentRecord> T load (Class<T> type, String ix1, Comparable<?> val1,
                                                   String ix2, Comparable<?> val2, String ix3,
                                                   Comparable<?> val3, QueryClause... clauses)
        throws DatabaseException
    {
        clauses = ArrayUtil.append(clauses, new Key<T>(type, ix1, val1, ix2, val2, ix3, val3));
        return load(type, clauses);
    }

    /**
     * Loads the first persistent object that matches the supplied query clauses.
     */
    protected <T extends PersistentRecord> T load (
        Class<T> type, Collection<? extends QueryClause> clauses)
        throws DatabaseException
    {
        return load(type, clauses.toArray(new QueryClause[clauses.size()]));
    }

    /**
     * Loads the first persistent object that matches the supplied query clauses.
     */
    protected <T extends PersistentRecord> T load (Class<T> type, QueryClause... clauses)
        throws DatabaseException
    {
        return _ctx.invoke(new FindOneQuery<T>(_ctx, type, clauses));
    }

    /**
     * Loads up all persistent records that match the supplied set of raw primary keys.
     */
    protected <T extends PersistentRecord> List<T> loadAll (
        Class<T> type, Collection<? extends Comparable<?>> primaryKeys)
        throws DatabaseException
    {
        // convert the raw keys into real key records
        DepotMarshaller<T> marsh = _ctx.getMarshaller(type);
        List<Key<T>> keys = new ArrayList<Key<T>>();
        for (Comparable<?> key : primaryKeys) {
            keys.add(marsh.makePrimaryKey(key));
        }
        return loadAll(keys);
    }

    /**
     * Loads up all persistent records that match the supplied set of primary keys.
     */
    protected <T extends PersistentRecord> List<T> loadAll (Collection<Key<T>> keys)
        throws DatabaseException
    {
        return (keys.size() == 0) ? Collections.<T>emptyList() :
            _ctx.invoke(new FindAllQuery.WithKeys<T>(_ctx, keys));
    }

    /**
     * A varargs version of {@link #findAll(Class,Collection)}.
     */
    protected <T extends PersistentRecord> List<T> findAll (Class<T> type, QueryClause... clauses)
        throws DatabaseException
    {
        return findAll(type, Arrays.asList(clauses));
    }

    /**
     * Loads all persistent objects that match the specified clauses.
     *
     * We have two strategies for doing this: one performs the query as-is, the second executes two
     * passes: first fetching only key columns and consulting the cache for each such key; then, in
     * the second pass, fetching the full entity only for keys that were not found in the cache.
     *
     * The more complex strategy could save a lot of data shuffling. On the other hand, its
     * complexity is an inherent drawback, and it does execute two separate database queries for
     * what the simple method does in one.
     */
    protected <T extends PersistentRecord> List<T> findAll (
        Class<T> type, Collection<? extends QueryClause> clauses)
        throws DatabaseException
    {
        return findAll(type, false, clauses);
    }

    /**
     * Loads all persistent objects that match the specified clauses.
     *
     * @param skipCache if true, our normal mixed select strategy that allows cached records to be
     * loaded from the cache will not be used even if it otherwise could. See {@link
     * #findAll(Class,Collection)} for details on the mixed strategy.
     */
    protected <T extends PersistentRecord> List<T> findAll (
        Class<T> type, boolean skipCache, Collection<? extends QueryClause> clauses)
        throws DatabaseException
    {
        DepotMarshaller<T> marsh = _ctx.getMarshaller(type);
        boolean useExplicit = skipCache || (marsh.getTableName() == null) ||
            !marsh.hasPrimaryKey() || !_ctx.isUsingCache();

        // queries on @Computed records or the presence of FieldOverrides use the simple algorithm
        for (QueryClause clause : clauses) {
            useExplicit |= (clause instanceof FieldOverride);
        }

        return _ctx.invoke(useExplicit ? new FindAllQuery.Explicitly<T>(_ctx, type, clauses) :
                           new FindAllQuery.WithCache<T>(_ctx, type, clauses));
    }

    /**
     * Looks up and returns {@link Key} records for all rows that match the supplied query clauses.
     *
     * @param forUpdate if true, the query will be run using a read-write connection to ensure that
     * it talks to the master database, if false, the query will be run on a read-only connection
     * and may load keys from a slave. For performance reasons, you should always pass false unless
     * you know you will be modifying the database as a result of this query and absolutely need
     * the latest data.
     */
    protected <T extends PersistentRecord> List<Key<T>> findAllKeys (
        Class<T> type, boolean forUpdate, QueryClause... clause)
        throws DatabaseException
    {
        return findAllKeys(type, forUpdate, Arrays.asList(clause));
    }

    /**
     * Looks up and returns {@link Key} records for all rows that match the supplied query clauses.
     *
     * @param forUpdate if true, the query will be run using a read-write connection to ensure that
     * it talks to the master database, if false, the query will be run on a read-only connection
     * and may load keys from a slave. For performance reasons, you should always pass false unless
     * you know you will be modifying the database as a result of this query and absolutely need
     * the latest data.
     */
    protected <T extends PersistentRecord> List<Key<T>> findAllKeys (
        Class<T> type, boolean forUpdate, Collection<? extends QueryClause> clauses)
        throws DatabaseException
    {
        final List<Key<T>> keys = new ArrayList<Key<T>>();
        final DepotMarshaller<T> marsh = _ctx.getMarshaller(type);
        SelectClause<T> select = new SelectClause<T>(type, marsh.getPrimaryKeyFields(), clauses);
        final SQLBuilder builder = _ctx.getSQLBuilder(DepotTypes.getDepotTypes(_ctx, select));
        builder.newQuery(select);

        if (forUpdate) {
            _ctx.invoke(new Modifier(null) {
                @Override public Integer invoke (Connection conn, DatabaseLiaison liaison)
                    throws SQLException {
                    PreparedStatement stmt = builder.prepare(conn);
                    try {
                        ResultSet rs = stmt.executeQuery();
                        while (rs.next()) {
                            keys.add(marsh.makePrimaryKey(rs));
                        }
                        return 0;
                    } finally {
                        JDBCUtil.close(stmt);
                    }
                }
            });

        } else {
            _ctx.invoke(new Query.Trivial<Void>() {
                @Override
                public Void invoke (Connection conn, DatabaseLiaison liaison) throws SQLException {
                    PreparedStatement stmt = builder.prepare(conn);
                    try {
                        ResultSet rs = stmt.executeQuery();
                        while (rs.next()) {
                            keys.add(marsh.makePrimaryKey(rs));
                        }
                        return null;
                    } finally {
                        JDBCUtil.close(stmt);
                    }
                }
            });
        }

        return keys;
    }

    /**
     * Inserts the supplied persistent object into the database, assigning its primary key (if it
     * has one) in the process.
     *
     * @return the number of rows modified by this action, this should always be one.
     */
    protected <T extends PersistentRecord> int insert (T record)
        throws DatabaseException
    {
        @SuppressWarnings("unchecked") final Class<T> pClass = (Class<T>) record.getClass();
        final DepotMarshaller<T> marsh = _ctx.getMarshaller(pClass);
        Key<T> key = marsh.getPrimaryKey(record, false);

        DepotTypes types = DepotTypes.getDepotTypes(_ctx);
        types.addClass(_ctx, pClass);
        final SQLBuilder builder = _ctx.getSQLBuilder(types);

        // key will be null if record was supplied without a primary key
        return _ctx.invoke(new CachingModifier<T>(record, key, key) {
            @Override
            public Integer invoke (Connection conn, DatabaseLiaison liaison) throws SQLException {
                // if needed, update our modifier's key so that it can cache our results
                Set<String> identityFields = Collections.emptySet();
                if (_key == null) {
                    // set any auto-generated column values
                    identityFields = marsh.generateFieldValues(conn, liaison, _result, false);
                    updateKey(marsh.getPrimaryKey(_result, false));
                }

                builder.newQuery(new InsertClause<T>(pClass, _result, identityFields));

                PreparedStatement stmt = builder.prepare(conn);
                try {
                    int mods = stmt.executeUpdate();
                    // run any post-factum value generators and potentially generate our key
                    if (_key == null) {
                        marsh.generateFieldValues(conn, liaison, _result, true);
                        updateKey(marsh.getPrimaryKey(_result, false));
                    }
                    return mods;

                } finally {
                    JDBCUtil.close(stmt);
                }
            }
        });
    }

    /**
     * Updates all fields of the supplied persistent object, using its primary key to identify the
     * row to be updated.
     *
     * @return the number of rows modified by this action.
     */
    protected <T extends PersistentRecord> int update (T record)
        throws DatabaseException
    {
        @SuppressWarnings("unchecked") Class<T> pClass = (Class<T>) record.getClass();
        requireNotComputed(pClass, "update");

        DepotMarshaller<T> marsh = _ctx.getMarshaller(pClass);
        Key<T> key = marsh.getPrimaryKey(record);
        if (key == null) {
            throw new IllegalArgumentException("Can't update record with null primary key.");
        }

        UpdateClause<T> update = new UpdateClause<T>(pClass, key, marsh._columnFields, record);
        final SQLBuilder builder = _ctx.getSQLBuilder(DepotTypes.getDepotTypes(_ctx, update));
        builder.newQuery(update);

        return _ctx.invoke(new CachingModifier<T>(record, key, key) {
            @Override
            public Integer invoke (Connection conn, DatabaseLiaison liaison) throws SQLException {
                PreparedStatement stmt = builder.prepare(conn);
                try {
                    return stmt.executeUpdate();
                } finally {
                    JDBCUtil.close(stmt);
                }
            }
        });
    }

    /**
     * Updates just the specified fields of the supplied persistent object, using its primary key
     * to identify the row to be updated. This method currently flushes the associated record from
     * the cache, but in the future it should be modified to update the modified fields in the
     * cached value iff the record exists in the cache.
     *
     * @return the number of rows modified by this action.
     */
    protected <T extends PersistentRecord> int update (T record, final String... modifiedFields)
        throws DatabaseException
    {
        @SuppressWarnings("unchecked") Class<T> pClass = (Class<T>) record.getClass();
        requireNotComputed(pClass, "updatePartial");

        DepotMarshaller<T> marsh = _ctx.getMarshaller(pClass);
        Key<T> key = marsh.getPrimaryKey(record);
        if (key == null) {
            throw new IllegalArgumentException("Can't update record with null primary key.");
        }

        UpdateClause<T> update = new UpdateClause<T>(pClass, key, modifiedFields, record);
        final SQLBuilder builder = _ctx.getSQLBuilder(DepotTypes.getDepotTypes(_ctx, update));
        builder.newQuery(update);

        return _ctx.invoke(new CachingModifier<T>(record, key, key) {
            @Override
            public Integer invoke (Connection conn, DatabaseLiaison liaison) throws SQLException {
                PreparedStatement stmt = builder.prepare(conn);
                // clear out _result so that we don't rewrite this partial record to the cache
                _result = null;
                try {
                    return stmt.executeUpdate();
                } finally {
                    JDBCUtil.close(stmt);
                }
            }
        });
    }

    /**
     * Updates the specified columns for all persistent objects matching the supplied primary key.
     *
     * @param type the type of the persistent object to be modified.
     * @param primaryKey the primary key to match in the update.
     * @param updates an mapping from the names of the fields/columns ti the values to be assigned.
     *
     * @return the number of rows modified by this action.
     */
    protected <T extends PersistentRecord> int updatePartial (
        Class<T> type, Comparable<?> primaryKey, Map<String,Object> updates)
        throws DatabaseException
    {
        Object[] fieldsValues = new Object[updates.size()*2];
        int idx = 0;
        for (Map.Entry<String,Object> entry : updates.entrySet()) {
            fieldsValues[idx++] = entry.getKey();
            fieldsValues[idx++] = entry.getValue();
        }
        return updatePartial(type, primaryKey, fieldsValues);
    }

    /**
     * Updates the specified columns for all persistent objects matching the supplied primary key.
     *
     * @param type the type of the persistent object to be modified.
     * @param primaryKey the primary key to match in the update.
     * @param fieldsValues an array containing the names of the fields/columns and the values to be
     * assigned, in key, value, key, value, etc. order.
     *
     * @return the number of rows modified by this action.
     */
    protected <T extends PersistentRecord> int updatePartial (
        Class<T> type, Comparable<?> primaryKey, Object... fieldsValues)
        throws DatabaseException
    {
        return updatePartial(_ctx.getMarshaller(type).makePrimaryKey(primaryKey), fieldsValues);
    }

    /**
     * Updates the specified columns for all persistent objects matching the supplied two-column
     * primary key.
     *
     * @param type the type of the persistent object to be modified.
     * @param fieldsValues an array containing the names of the fields/columns and the values to be
     * assigned, in key, value, key, value, etc. order.
     *
     * @return the number of rows modified by this action.
     */
    protected <T extends PersistentRecord> int updatePartial (
        Class<T> type, String ix1, Comparable<?> val1, String ix2, Comparable<?> val2,
        Object... fieldsValues)
        throws DatabaseException
    {
        return updatePartial(new Key<T>(type, ix1, val1, ix2, val2), fieldsValues);
    }

    /**
     * Updates the specified columns for all persistent objects matching the supplied three-column
     * primary key.
     *
     * @param type the type of the persistent object to be modified.
     * @param fieldsValues an array containing the names of the fields/columns and the values to be
     * assigned, in key, value, key, value, etc. order.
     *
     * @return the number of rows modified by this action.
     */
    protected <T extends PersistentRecord> int updatePartial (
        Class<T> type, String ix1, Comparable<?> val1, String ix2, Comparable<?> val2,
        String ix3, Comparable<?> val3, Object... fieldsValues)
        throws DatabaseException
    {
        return updatePartial(new Key<T>(type, ix1, val1, ix2, val2, ix3, val3), fieldsValues);
    }

    /**
     * Updates the specified columns for all persistent objects matching the supplied key.
     *
     * @param key the key for the persistent objects to be modified.
     * @param fieldsValues an array containing the names of the fields/columns and the values to be
     * assigned, in key, value, key, value, etc. order.
     *
     * @return the number of rows modified by this action.
     */
    protected <T extends PersistentRecord> int updatePartial (Key<T> key, Object... fieldsValues)
        throws DatabaseException
    {
        return updatePartial(key.getPersistentClass(), key, key, fieldsValues);
    }

    /**
     * Updates the specified columns for all persistent objects matching the supplied key. This
     * method currently flushes the associated record from the cache, but in the future it should
     * be modified to update the modified fields in the cached value iff the record exists in the
     * cache.
     *
     * @param type the type of the persistent object to be modified.
     * @param key the key to match in the update.
     * @param invalidator a cache invalidator that will be run prior to the update to flush the
     * relevant persistent objects from the cache, or null if no invalidation is needed.
     * @param fieldsValues an array containing the names of the fields/columns and the values to be
     * assigned, in key, value, key, value, etc. order.
     *
     * @return the number of rows modified by this action.
     */
    protected <T extends PersistentRecord> int updatePartial (
        Class<T> type, final WhereClause key, CacheInvalidator invalidator, Object... fieldsValues)
        throws DatabaseException
    {
        if (invalidator instanceof ValidatingCacheInvalidator) {
            ((ValidatingCacheInvalidator)invalidator).validateFlushType(type); // sanity check
        }
        key.validateQueryType(type); // and another

        // separate the arguments into keys and values
        final String[] fields = new String[fieldsValues.length/2];
        final SQLExpression[] values = new SQLExpression[fields.length];
        for (int ii = 0, idx = 0; ii < fields.length; ii++) {
            fields[ii] = (String)fieldsValues[idx++];
            values[ii] = new ValueExp(fieldsValues[idx++]);
        }
        UpdateClause<T> update = new UpdateClause<T>(type, key, fields, values);
        final SQLBuilder builder = _ctx.getSQLBuilder(DepotTypes.getDepotTypes(_ctx, update));
        builder.newQuery(update);

        return _ctx.invoke(new Modifier(invalidator) {
            @Override
            public Integer invoke (Connection conn, DatabaseLiaison liaison) throws SQLException {
                PreparedStatement stmt = builder.prepare(conn);
                try {
                    return stmt.executeUpdate();
                } finally {
                    JDBCUtil.close(stmt);
                }
            }
        });
    }

    /**
     * Updates the specified columns for all persistent objects matching the supplied primary
     * key. The values in this case must be literal SQL to be inserted into the update statement.
     * In general this is used when you want to do something like the following:
     *
     * <pre>
     * update FOO set BAR = BAR + 1;
     * update BAZ set BIF = NOW();
     * </pre>
     *
     * @param type the type of the persistent object to be modified.
     * @param primaryKey the key to match in the update.
     * @param fieldsValues an array containing the names of the fields/columns and the values to be
     * assigned, in key, literal value, key, literal value, etc. order.
     *
     * @return the number of rows modified by this action.
     */
    protected <T extends PersistentRecord> int updateLiteral (
        Class<T> type, Comparable<?> primaryKey, Map<String, SQLExpression> fieldsValues)
        throws DatabaseException
    {
        Key<T> key = _ctx.getMarshaller(type).makePrimaryKey(primaryKey);
        return updateLiteral(type, key, key, fieldsValues);
    }

    /**
     * Updates the specified columns for all persistent objects matching the supplied two-column
     * primary key. The values in this case must be literal SQL to be inserted into the update
     * statement. In general this is used when you want to do something like the following:
     *
     * <pre>
     * update FOO set BAR = BAR + 1;
     * update BAZ set BIF = NOW();
     * </pre>
     *
     * @param type the type of the persistent object to be modified.
     * @param fieldsValues an array containing the names of the fields/columns and the values to be
     * assigned, in key, literal value, key, literal value, etc. order.
     *
     * @return the number of rows modified by this action.
     */
    protected <T extends PersistentRecord> int updateLiteral (
        Class<T> type, String ix1, Comparable<?> val1, String ix2, Comparable<?> val2,
        Map<String, SQLExpression> fieldsValues)
        throws DatabaseException
    {
        Key<T> key = new Key<T>(type, ix1, val1, ix2, val2);
        return updateLiteral(type, key, key, fieldsValues);
    }

    /**
     * Updates the specified columns for all persistent objects matching the supplied three-column
     * primary key. The values in this case must be literal SQL to be inserted into the update
     * statement. In general this is used when you want to do something like the following:
     *
     * <pre>
     * update FOO set BAR = BAR + 1;
     * update BAZ set BIF = NOW();
     * </pre>
     *
     * @param type the type of the persistent object to be modified.
     * @param fieldsValues an array containing the names of the fields/columns and the values to be
     * assigned, in key, literal value, key, literal value, etc. order.
     *
     * @return the number of rows modified by this action.
     */
    protected <T extends PersistentRecord> int updateLiteral (
        Class<T> type, String ix1, Comparable<?> val1, String ix2, Comparable<?> val2,
        String ix3, Comparable<?> val3, Map<String, SQLExpression> fieldsValues)
        throws DatabaseException
    {
        Key<T> key = new Key<T>(type, ix1, val1, ix2, val2, ix3, val3);
        return updateLiteral(type, key, key, fieldsValues);
    }

    /**
     * Updates the specified columns for all persistent objects matching the supplied primary
     * key. The values in this case must be literal SQL to be inserted into the update statement.
     * In general this is used when you want to do something like the following:
     *
     * <pre>
     * update FOO set BAR = BAR + 1;
     * update BAZ set BIF = NOW();
     * </pre>
     *
     * @param type the type of the persistent object to be modified.
     * @param key the key to match in the update.
     * @param fieldsValues an array containing the names of the fields/columns and the values to be
     * assigned, in key, literal value, key, literal value, etc. order.
     *
     * @return the number of rows modified by this action.
     */
    protected <T extends PersistentRecord> int updateLiteral (
        Class<T> type, final WhereClause key, CacheInvalidator invalidator,
        Map<String, SQLExpression> fieldsValues)
        throws DatabaseException
    {
        requireNotComputed(type, "updateLiteral");

        if (invalidator instanceof ValidatingCacheInvalidator) {
            ((ValidatingCacheInvalidator)invalidator).validateFlushType(type); // sanity check
        }
        key.validateQueryType(type); // and another

        // separate the arguments into keys and values
        final String[] fields = new String[fieldsValues.size()];
        final SQLExpression[] values = new SQLExpression[fields.length];
        int ii = 0;
        for (Map.Entry<String, SQLExpression> entry : fieldsValues.entrySet()) {
            fields[ii] = entry.getKey();
            values[ii] = entry.getValue();
            ii ++;
        }

        UpdateClause<T> update = new UpdateClause<T>(type, key, fields, values);
        final SQLBuilder builder = _ctx.getSQLBuilder(DepotTypes.getDepotTypes(_ctx, update));
        builder.newQuery(update);

        return _ctx.invoke(new Modifier(invalidator) {
            @Override
            public Integer invoke (Connection conn, DatabaseLiaison liaison) throws SQLException {
                PreparedStatement stmt = builder.prepare(conn);
                try {
                    return stmt.executeUpdate();
                } finally {
                    JDBCUtil.close(stmt);
                }
            }
        });
    }

    /**
     * Stores the supplied persisent object in the database. If it has no primary key assigned (it
     * is null or zero), it will be inserted directly. Otherwise an update will first be attempted
     * and if that matches zero rows, the object will be inserted.
     *
     * @return true if the record was created, false if it was updated.
     */
    protected <T extends PersistentRecord> boolean store (T record)
        throws DatabaseException
    {
        @SuppressWarnings("unchecked") final Class<T> pClass = (Class<T>) record.getClass();
        requireNotComputed(pClass, "store");

        final DepotMarshaller<T> marsh = _ctx.getMarshaller(pClass);
        Key<T> key = marsh.hasPrimaryKey() ? marsh.getPrimaryKey(record) : null;
        final UpdateClause<T> update =
            new UpdateClause<T>(pClass, key, marsh._columnFields, record);
        final SQLBuilder builder = _ctx.getSQLBuilder(DepotTypes.getDepotTypes(_ctx, update));

        // if our primary key isn't null, we start by trying to update rather than insert
        if (key != null) {
            builder.newQuery(update);
        }

        final boolean[] created = new boolean[1];
        _ctx.invoke(new CachingModifier<T>(record, key, key) {
            @Override
            public Integer invoke (Connection conn, DatabaseLiaison liaison) throws SQLException {
                PreparedStatement stmt = null;
                try {
                    if (_key != null) {
                        // run the update
                        stmt = builder.prepare(conn);
                        int mods = stmt.executeUpdate();
                        if (mods > 0) {
                            // if it succeeded, we're done
                            return mods;
                        }
                        JDBCUtil.close(stmt);
                    }

                    // if the update modified zero rows or the primary key was unset, insert
                    Set<String> identityFields = Collections.emptySet();
                    if (_key == null) {
                        // first, set any auto-generated column values
                        identityFields = marsh.generateFieldValues(conn, liaison, _result, false);
                        // update our modifier's key so that it can cache our results
                        updateKey(marsh.getPrimaryKey(_result, false));
                    }

                    builder.newQuery(new InsertClause<T>(pClass, _result, identityFields));

                    stmt = builder.prepare(conn);
                    int mods = stmt.executeUpdate();

                    // run any post-factum value generators and potentially generate our key
                    if (_key == null) {
                        marsh.generateFieldValues(conn, liaison, _result, true);
                        updateKey(marsh.getPrimaryKey(_result, false));
                    }
                    created[0] = true;
                    return mods;

                } finally {
                    JDBCUtil.close(stmt);
                }
            }
        });
        return created[0];
    }

    /**
     * Deletes all persistent objects from the database with a primary key matching the primary key
     * of the supplied object.
     *
     * @return the number of rows deleted by this action.
     */
    protected <T extends PersistentRecord> int delete (T record)
        throws DatabaseException
    {
        @SuppressWarnings("unchecked") Class<T> type = (Class<T>)record.getClass();
        Key<T> primaryKey = _ctx.getMarshaller(type).getPrimaryKey(record);
        if (primaryKey == null) {
            throw new IllegalArgumentException("Can't delete record with null primary key.");
        }
        return delete(type, primaryKey);
    }

    /**
     * Deletes all persistent objects from the database with a primary key matching the supplied
     * primary key.
     *
     * @return the number of rows deleted by this action.
     */
    protected <T extends PersistentRecord> int delete (Class<T> type, Comparable<?> primaryKeyValue)
        throws DatabaseException
    {
        return delete(type, _ctx.getMarshaller(type).makePrimaryKey(primaryKeyValue));
    }

    /**
     * Deletes all persistent objects from the database with a primary key matching the supplied
     * primary key.
     *
     * @return the number of rows deleted by this action.
     */
    protected <T extends PersistentRecord> int delete (Class<T> type, Key<T> primaryKey)
        throws DatabaseException
    {
        return deleteAll(type, primaryKey, primaryKey);
    }

    /**
     * Deletes all persistent objects from the database that match the supplied where clause.
     *
     * @return the number of rows deleted by this action.
     */
    protected <T extends PersistentRecord> int deleteAll (Class<T> type, final WhereClause where)
        throws DatabaseException
    {
        if (_ctx.getMarshaller(type).hasPrimaryKey()) {
            // look up the primary keys for all rows matching our where clause and delete using those
            KeySet<T> pwhere = new KeySet<T>(type, findAllKeys(type, true, where));
            return deleteAll(type, pwhere, pwhere);
        } else {
            // otherwise just do the delete directly as we can't have cached a record that has no
            // primary key in the first place
            return deleteAll(type, where, null);
        }
    }

    /**
     * Deletes all persistent objects from the database that match the supplied key.
     *
     * @return the number of rows deleted by this action.
     */
    protected <T extends PersistentRecord> int deleteAll (
        Class<T> type, final WhereClause where, CacheInvalidator invalidator)
        throws DatabaseException
    {
        if (invalidator instanceof ValidatingCacheInvalidator) {
            ((ValidatingCacheInvalidator)invalidator).validateFlushType(type); // sanity check
        }
        where.validateQueryType(type); // and another

        DeleteClause<T> delete = new DeleteClause<T>(type, where);
        final SQLBuilder builder = _ctx.getSQLBuilder(DepotTypes.getDepotTypes(_ctx, delete));
        builder.newQuery(delete);

        return _ctx.invoke(new Modifier(invalidator) {
            @Override
            public Integer invoke (Connection conn, DatabaseLiaison liaison) throws SQLException {
                PreparedStatement stmt = builder.prepare(conn);
                try {
                    return stmt.executeUpdate();
                } finally {
                    JDBCUtil.close(stmt);
                }
            }
        });
    }

    // make sure the given type corresponds to a concrete class
    protected void requireNotComputed (Class<? extends PersistentRecord> type, String action)
        throws DatabaseException
    {
        DepotMarshaller<?> marsh = _ctx.getMarshaller(type);
        if (marsh == null) {
            throw new DatabaseException("Unknown persistent type [class=" + type + "]");
        }
        if (marsh.getTableName() == null) {
            throw new DatabaseException(
                "Can't " + action + " computed entities [class=" + type + "]");
        }
    }

    /**
     * If the supplied migration has not already been run, it will be run and if it completes, we
     * will note in the DepotMigrationHistory table that it has been run.
     */
    protected void runMigration (DataMigration migration)
        throws DatabaseException
    {
        // attempt to get a lock to run this migration (or detect that it has already been run)
        DepotMigrationHistoryRecord record;
        while (true) {
            // check to see if the migration has already been completed
            record = load(DepotMigrationHistoryRecord.class, migration.getIdent());
            if (record != null && record.whenCompleted != null) {
                return; // great, no need to do anything
            }

            // if no record exists at all, try to insert one and thereby obtain the migration lock
            if (record == null) {
                try {
                    record = new DepotMigrationHistoryRecord();
                    record.ident = migration.getIdent();
                    insert(record);
                    break; // we got the lock, break out of this loop and run the migration
                } catch (DuplicateKeyException dke) {
                    // someone beat us to the punch, so we have to wait for them to finish
                }
            }

            // we didn't get the lock, so wait 5 seconds and then check to see if the other process
            // finished the update or failed in which case we'll try to grab the lock ourselves
            try {
                log.info("Waiting on migration lock for " + migration.getIdent() + ".");
                Thread.sleep(5000);
            } catch (InterruptedException ie) {
                throw new DatabaseException("Interrupted while waiting on migration lock.");
            }
        }

        log.info("Running data migration", "ident", migration.getIdent());
        try {
            // run the migration
            migration.invoke();

            // report to the world that we've done so
            record.whenCompleted = new Timestamp(System.currentTimeMillis());
            update(record);

        } finally {
            // clear out our migration history record if we failed to get the job done
            if (record.whenCompleted == null) {
                try {
                    delete(record);
                } catch (Throwable dt) {
                    log.warning("Oh noez! Failed to delete history record for failed migration. " +
                                "All clients will loop forever waiting for the lock.",
                                "ident", migration.getIdent(), dt);
                }
            }
        }
    }

    protected PersistenceContext _ctx;
    protected List<DataMigration> _dataMigs = new ArrayList<DataMigration>();
}
