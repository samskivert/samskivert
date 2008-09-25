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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;

import com.samskivert.io.PersistenceException;
import com.samskivert.util.StringUtil;

import com.samskivert.jdbc.ConnectionProvider;
import com.samskivert.jdbc.DatabaseLiaison;
import com.samskivert.jdbc.LiaisonRegistry;
import com.samskivert.jdbc.MySQLLiaison;
import com.samskivert.jdbc.PostgreSQLLiaison;

import com.samskivert.jdbc.depot.annotation.TableGenerator;

import static com.samskivert.jdbc.depot.Log.log;

/**
 * Defines a scope in which global annotations are shared.
 */
public class PersistenceContext
{
    /** Allow toggling of query logging and other debug output via a system property. */
    public static final boolean DEBUG = Boolean.getBoolean("com.samskivert.jdbc.depot.debug");

    /** Map {@link TableGenerator} instances by name. */
    public HashMap<String, TableGenerator> tableGenerators = new HashMap<String, TableGenerator>();

    /**
     * A cache listener is notified when cache entries change. Its purpose is typically to do
     * further invalidation of dependent entries in other caches.
     */
    public static interface CacheListener<T>
    {
        /**
         * The given entry (which is never null) has just been evicted from the cache slot
         * indicated by the given key.
         *
         * This method is most commonly used to trigger custom cache invalidation of records that
         * depend on the one that was just invalidated.
         */
        public void entryInvalidated (CacheKey key, T oldEntry);

        /**
         * The given entry, which may be an explicit null, has just been placed into the cache
         * under the given key. The previous cache entry, if any, is also supplied.
         *
         * This method is most likely used by repositories to index entries by attribute for quick
         * cache invalidation when brute force is unrealistically time consuming.
         */
        public void entryCached (CacheKey key, T newEntry, T oldEntry);
    }

    /**
     * The callback for {@link #cacheTraverse}; this is called for each entry in a given cache.
     */
    public static interface CacheTraverser<T extends Serializable>
    {
        /**
         * Performs whatever cache-related tasks need doing for this cache entry. This method
         * is called for each cache entry in a full-cache enumeration.
         */
        public void visitCacheEntry (
            PersistenceContext ctx, String cacheId, Serializable key, T record);
    }

    /**
     * A simple implementation of {@link CacheTraverser} that selectively deletes entries in
     * a cache depending on the return value of {@link #testCacheEntry}.
     */
    public static abstract class CacheEvictionFilter<T extends Serializable>
        implements CacheTraverser<T>
    {
        // from CacheTraverser
        public void visitCacheEntry (
            PersistenceContext ctx, String cacheId, Serializable key, T record)
        {
            if (testForEviction(key, record)) {
                ctx.cacheInvalidate(cacheId, key);
            }
        }

        /**
         * Decides whether or not this entry should be evicted and returns true if yes, false if
         * no.
         */
        protected abstract boolean testForEviction (Serializable key, T record);
    }

    /**
     * Returns the cache adapter used by this context or null if caching is disabled.
     */
    public CacheAdapter getCacheAdapter ()
    {
        return _cache;
    }

    /**
     * Creates an uninitialized persistence context. {@link #init} must later be called on this
     * context to prepare it for operation.
     */
    public PersistenceContext ()
    {
    }

    /**
     * Creates and initializes a persistence context. See {@link #init}.
     */
    public PersistenceContext (String ident, ConnectionProvider conprov, CacheAdapter adapter)
    {
        init(ident, conprov, adapter);
    }

    /**
     * Initializes this context with its connection provider and cache adapter.
     *
     * @param ident the identifier to provide to the connection provider when requesting a
     * connection.
     * @param conprov provides JDBC {@link Connection} instances.
     * @param adapter an optional adapter to a cache management system.
     */
    public void init (String ident, ConnectionProvider conprov, CacheAdapter adapter)
    {
        _ident = ident;
        _conprov = conprov;
        _liaison = LiaisonRegistry.getLiaison(conprov.getURL(ident));
        _cache = adapter;
    }

    /**
     * Shuts this persistence context down, shutting down any caching system in use and shutting
     * down the JDBC connection pool.
     */
    public void shutdown ()
    {
        try {
            if (_cache != null) {
                _cache.shutdown();
            }
        } catch (Throwable t) {
            log.warning("Failure shutting down Depot cache.", t);
        }
        _conprov.shutdown();
    }

    /**
     * Create and return a new {@link SQLBuilder} for the appropriate dialect.
     *
     * TODO: At some point perhaps use a more elegant way of discerning our dialect.
     */
    public SQLBuilder getSQLBuilder (DepotTypes types)
    {
        if (_liaison instanceof PostgreSQLLiaison) {
            return new PostgreSQLBuilder(types);
        }
        if (_liaison instanceof MySQLLiaison) {
            return new MySQLBuilder(types);
        }
        throw new IllegalArgumentException("Unknown liaison type: " + _liaison.getClass());
    }

    /**
     * Registers a schema migration for the specified entity class.
     *
     * <p> This method must be called <b>before</b> an Entity is used by any repository. Thus you
     * should register all migrations in the constructor of the repository that declares them in
     * its {@link DepotRepository#getManagedRecords} method.
     *
     * <p> Note that the migration process is as follows:
     *
     * <ul><li> Note the difference between the entity's declared version and the version recorded
     * in the database.
     * <li> Run all registered pre-migrations
     * <li> Perform all default migrations (column and index additions, index removals)
     * <li> Run all registered post-migrations </ul>
     *
     * Thus you must either be prepared for the entity to be at <b>any</b> version prior to your
     * migration target version because we may start up, find the schema at version 1 and the
     * Entity class at version 8 and do all "standard" migrations in one fell swoop. So if a column
     * got added in version 2 and renamed in version 6 and your migration was registered for
     * version 6 to do that migration, it must be prepared for the column not to exist at all.
     *
     * <p> If you want a completely predictable migration process, never use the default migrations
     * and register a pre-migration for every single schema migration and they will then be
     * guaranteed to be run in registration order and with predictable pre- and post-conditions.
     */
    public <T extends PersistentRecord> void registerMigration (
        Class<T> type, SchemaMigration migration)
    {
        getRawMarshaller(type).registerMigration(migration);
    }

    /**
     * Returns the marshaller for the specified persistent object class, creating and initializing
     * it if necessary.
     */
    public <T extends PersistentRecord> DepotMarshaller<T> getMarshaller (Class<T> type)
        throws DatabaseException
    {
        checkAreInitialized(); // le check du sanity
        DepotMarshaller<T> marshaller = getRawMarshaller(type);
        try {
            if (!marshaller.isInitialized()) {
                // initialize the marshaller which may create or migrate the table for its
                // underlying persistent object
                marshaller.init(this);
                if (marshaller.getTableName() != null && _warnOnLazyInit) {
                    log.warning("Record initialized lazily", "type", type.getName(),
                                new Exception());
                }
            }
        } catch (DatabaseException pe) {
            throw (DatabaseException)new DatabaseException(
                "Failed to initialize marshaller [type=" + type + "].").initCause(pe);
        }
        return marshaller;
    }

    /**
     * Invokes a non-modifying query and returns its result.
     */
    public <T> T invoke (Query<T> query)
        throws DatabaseException
    {
        CacheKey key = query.getCacheKey();
        // if there is a cache key, check the cache
        if (key != null && _cache != null) {
            CacheAdapter.CachedValue<T> cacheHit = cacheLookup(key);
            if (cacheHit != null) {
                log.debug("cache hit [key=" + key + ", hit=" + cacheHit + "]");
                T value = cacheHit.getValue();
                value = query.transformCacheHit(key, value);
                if (value != null) {
                    return value;
                }
                log.debug("transformCacheHit returned null; rejecting cached value.");
            }
            log.debug("cache miss [key=" + key + "]");
        }
        // otherwise, perform the query
        @SuppressWarnings("unchecked") T result = (T) invoke(query, null, true);
        // and let the caller figure out if it wants to cache itself somehow
        query.updateCache(this, result);
        return result;
    }

    /**
     * Invokes a modifying query and returns the number of rows modified.
     */
    public int invoke (Modifier modifier)
        throws DatabaseException
    {
        modifier.cacheInvalidation(this);
        int rows = (Integer) invoke(null, modifier, true);
        if (rows > 0) {
            modifier.cacheUpdate(this);
        }
        return rows;
    }

    /**
     * Returns true if there is a {@link CacheAdapter} configured, false otherwise.
     */
    public boolean isUsingCache ()
    {
        return _cache != null;
    }

    /**
     * Looks up an entry in the cache by the given key.
     */
    public <T> CacheAdapter.CachedValue<T> cacheLookup (CacheKey key)
    {
        if (_cache == null) {
            return null;
        }
        CacheAdapter.CacheBin<T> bin = _cache.getCache(key.getCacheId());
        return bin.lookup(key.getCacheKey());
    }

    /**
     * Stores a new entry indexed by the given key.
     */
    public <T> void cacheStore (CacheKey key, T entry)
    {
        if (_cache == null) {
            return;
        }
        if (key == null) {
            log.warning("Cache key must not be null [entry=" + entry + "]");
            Thread.dumpStack();
            return;
        }
        log.debug("storing [key=" + key + ", value=" + entry + "]");

        CacheAdapter.CacheBin<T> bin = _cache.getCache(key.getCacheId());
        CacheAdapter.CachedValue<T> element = bin.lookup(key.getCacheKey());
        T oldEntry = (element != null ? element.getValue() : null);

        // update the cache
        bin.store(key.getCacheKey(), entry);

        // then do cache invalidations
        Set<CacheListener<?>> listeners = _listenerSets.get(key.getCacheId());
        if (listeners != null && listeners.size() > 0) {
            for (CacheListener<?> listener : listeners) {
                log.debug("cascading [listener=" + listener + "]");
                @SuppressWarnings("unchecked")
                    CacheListener<T> casted = (CacheListener<T>)listener;
                casted.entryCached(key, entry, oldEntry);
            }
        }
    }

    /**
     * Evicts the cache entry indexed under the given key, if there is one.  The eviction may
     * trigger further cache invalidations.
     */
    public void cacheInvalidate (CacheKey key)
    {
        if (key == null) {
            log.warning("Cache key to invalidate must not be null.");
            Thread.dumpStack();
        } else {
            cacheInvalidate(key.getCacheId(), key.getCacheKey());
        }
    }

    /**
     * Evicts the cache entry indexed under the given class and cache key, if there is one.  The
     * eviction may trigger further cache invalidations.
     */
    public void cacheInvalidate (Class<? extends PersistentRecord> pClass, Serializable cacheKey)
    {
        cacheInvalidate(pClass.getName(), cacheKey);
    }

    /**
     * Evicts the cache entry indexed under the given cache id and cache key, if there is one.  The
     * eviction may trigger further cache invalidations.
     */
    public <T extends Serializable> void cacheInvalidate (String cacheId, Serializable cacheKey)
    {
        if (_cache == null) {
            return;
        }
        log.debug("invalidating [cacheId=" + cacheId + ", cacheKey=" + cacheKey + "]");

        CacheAdapter.CacheBin<T> bin = _cache.getCache(cacheId);
        CacheAdapter.CachedValue<T> element = bin.lookup(cacheKey);
        if (element != null) {
            // find the old entry, if any
            T oldEntry = element.getValue();
            if (oldEntry != null) {
                // if there was one, do (possibly cascading) cache invalidations
                Set<CacheListener<?>> listeners = _listenerSets.get(cacheId);
                if (listeners != null && listeners.size() > 0) {
                    CacheKey key = new SimpleCacheKey(cacheId, cacheKey);
                    for (CacheListener<?> listener : listeners) {
                        log.debug("cascading [listener=" + listener + "]");
                        @SuppressWarnings("unchecked") CacheListener<T> casted =
                            (CacheListener<T>)listener;
                        casted.entryInvalidated(key, oldEntry);
                    }
                }
            }
        }

        // then remove the keyed entry from the cache system
        bin.remove(cacheKey);
    }

    /**
     * Brutally iterates over the entire contents of the cache associated with the given class,
     * invoking the callback for each cache entry.
     */
    public <T extends Serializable> void cacheTraverse (
        Class<? extends PersistentRecord> pClass, CacheTraverser<T> filter)
    {
        cacheTraverse(pClass.getName(), filter);
    }

    /**
     * Brutally iterates over the entire contents of the cache identified by the given cache id,
     * invoking the callback for each cache entry.
     */
    public <T extends Serializable> void cacheTraverse (String cacheId, CacheTraverser<T> filter)
    {
        if (_cache == null) {
            return;
        }
        CacheAdapter.CacheBin<T> bin = _cache.getCache(cacheId);
        if (bin != null) {
            for (Object key : bin.enumerateKeys()) {
                CacheAdapter.CachedValue<T> element = bin.lookup((Serializable) key);
                T value;
                if (element != null && (value = element.getValue()) != null) {
                    filter.visitCacheEntry(this, cacheId, (Serializable) key, value);
                }
            }
        }
    }

    /**
     * Registers a new cache listener with the cache associated with the given class.
     */
    public <T extends Serializable> void addCacheListener (
        Class<T> pClass, CacheListener<T> listener)
    {
        addCacheListener(pClass.getName(), listener);
    }

    /**
     * Registers a new cache listener with the identified cache.
     */
    public <T extends Serializable> void addCacheListener (
        String cacheId, CacheListener<T> listener)
    {
        Set<CacheListener<?>> listenerSet = _listenerSets.get(cacheId);
        if (listenerSet == null) {
            listenerSet = new HashSet<CacheListener<?>>();
            _listenerSets.put(cacheId, listenerSet);
        }
        listenerSet.add(listener);
    }

    /**
     * Initializes all repositories that have been created and registered with this persistence
     * context. Any repositories that are constructed after this call will be immediately
     * initialized at the time they are constructed (which is probably undesirable). When a
     * repository is initialized, schema migrations for all of its managed persistent records are
     * run. It is best to do all schema migrations when the system initializes which is why lazy
     * initialization of repositories is undesirable.
     *
     * @param warnOnLazyInit if true, any repositories are constructed after this method is called
     * will result in a warning so that the application developer can restructure their code to
     * ensure that those repositories are properly created prior to this call.
     */
    public void initializeRepositories (boolean warnOnLazyInit)
        throws DatabaseException
    {
        // initialize our repositories
        for (DepotRepository repo : _repositories) {
            repo.init();
        }
        // note that we've now been initialized
        _repositories = null;
        // now issue a warning if we lazily initialize any other persistent record
        _warnOnLazyInit = warnOnLazyInit;
    }

    /**
     * Called when a depot repository is created. We register all persistent record classes used by
     * the repository so that systems that desire it can force the resolution of all database
     * tables rather than allowing resolution to happen on demand.
     */
    protected void repositoryCreated (DepotRepository repo)
    {
        if (_repositories == null) {
            if (_warnOnLazyInit) {
                log.warning("Repository created lazily: " + repo.getClass().getName());
            }
            repo.init();
        } else {
            _repositories.add(repo);
        }
    }

    /**
     * Looks up and creates, but does not initialize, the marshaller for the specified Entity type.
     */
    protected <T extends PersistentRecord> DepotMarshaller<T> getRawMarshaller (Class<T> type)
    {
        @SuppressWarnings("unchecked") DepotMarshaller<T> marshaller =
            (DepotMarshaller<T>)_marshallers.get(type);
        if (marshaller == null) {
            _marshallers.put(type, marshaller = new DepotMarshaller<T>(type, this));
        }
        return marshaller;
    }

    /**
     * Internal invoke method that takes care of transient retries for both queries and modifiers.
     */
    protected <T> Object invoke (Query<T> query, Modifier modifier, boolean retryOnTransientFailure)
        throws DatabaseException
    {
        checkAreInitialized(); // le check du sanity

        boolean isReadOnlyQuery = (query != null);
        Connection conn;
        try {
            conn = _conprov.getConnection(_ident, isReadOnlyQuery);
        } catch (PersistenceException pe) {
            throw new DatabaseException(pe.getMessage(), pe.getCause());
        }

        // TEMP: we synchronize on the connection to cooperate with SimpleRepository when used in
        // conjunction with a StaticConnectionProvider; at some point we'll switch to standard JDBC
        // connection pooling which will block in getConnection() instead of returning a connection
        // that someone else may be using
        synchronized (conn) {
            try {
                if (isReadOnlyQuery) {
                    // if this becomes more complex than this single statement, then this should
                    // turn into a method call that contains the complexity
                    return query.invoke(conn, _liaison);

                } else {
                    // if this becomes more complex than this single statement, then this should
                    // turn into a method call that contains the complexity
                    return modifier.invoke(conn, _liaison);
                }

            } catch (SQLException sqe) {
                if (!isReadOnlyQuery) {
                    // convert this exception to a DuplicateKeyException if appropriate
                    if (_liaison.isDuplicateRowException(sqe)) {
                        throw new DuplicateKeyException(sqe.getMessage());
                    }
                }

                // let the provider know that the connection failed
                _conprov.connectionFailed(_ident, isReadOnlyQuery, conn, sqe);
                conn = null;

                if (retryOnTransientFailure && _liaison.isTransientException(sqe)) {
                    // the MySQL JDBC driver has the annoying habit of including the embedded
                    // exception stack trace in the message of their outer exception; if I want a
                    // fucking stack trace, I'll call printStackTrace() thanksverymuch
                    String msg = StringUtil.split(String.valueOf(sqe), "\n")[0];
                    log.info("Transient failure executing op, retrying [error=" + msg + "].");

                } else {
                    String msg = isReadOnlyQuery ?
                        "Query failure " + query : "Modifier failure " + modifier;
                    throw new DatabaseException(msg, sqe);
                }

            } finally {
                _conprov.releaseConnection(_ident, isReadOnlyQuery, conn);
            }
        }

        // if we got here, we want to retry a transient failure
        return invoke(query, modifier, false);
    }

    protected void checkAreInitialized ()
    {
        if (_conprov == null) {
            throw new IllegalStateException(
                "This persistence context has not yet been initialized. You are probably " +
                "doing something too early, like in a repository's constructor. Don't do that.");
        }
    }

    protected String _ident;
    protected ConnectionProvider _conprov;
    protected DatabaseLiaison _liaison;
    protected boolean _warnOnLazyInit;

    /** The object through which all our caching is relayed, or null, for no caching. */
    protected CacheAdapter _cache;

    /** Tracks repositories during the pre-initialization phase. */
    protected List<DepotRepository> _repositories = new ArrayList<DepotRepository>();

    /** A mapping from persistent record class to resolved marshaller. */
    protected Map<Class<?>, DepotMarshaller<?>> _marshallers =
        new HashMap<Class<?>, DepotMarshaller<?>>();

    /** A mapping of cache listeners by cache id. */
    protected Map<String, Set<CacheListener<?>>> _listenerSets =
        new HashMap<String, Set<CacheListener<?>>>();
}
