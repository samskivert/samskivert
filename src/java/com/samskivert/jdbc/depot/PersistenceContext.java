//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2006 Michael Bayne, Pär Winzell
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.samskivert.jdbc.depot.annotation.TableGenerator;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import com.samskivert.io.PersistenceException;

import com.samskivert.util.StringUtil;

import com.samskivert.jdbc.ConnectionProvider;
import com.samskivert.jdbc.DuplicateKeyException;


/**
 * Defines a scope in which global annotations are shared.
 */
public class PersistenceContext
{
    /** Map {@link TableGenerator} instances by name. */
    public HashMap<String, TableGenerator> tableGenerators = new HashMap<String, TableGenerator>();

    /**
     * A cache listener is notified when cache entries are invalited through creation, deletion,
     * or modification. Its purpose is typically to do further invalidation of dependent entries
     * in other caches.
     */
    public static interface CacheListener<T>
    {
        /**
         * The given entry has just been deleted, modified or created. Do what thou wilt.
         */
        public void entryModified (CacheKey key, T entry);
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
     * Creates a persistence context that will use the supplied provider to obtain JDBC
     * connections.
     *
     * @param ident the identifier to provide to the connection provider when requesting a
     * connection.
     */
    public PersistenceContext (String ident, ConnectionProvider conprov)
    {
        _ident = ident;
        _conprov = conprov;
        _cachemgr = CacheManager.getInstance();
    }

    /**
     * Registers a migration for the specified entity class.
     *
     * <p> This method must be called <b>before</b> an Entity is used by any repository. Thus you
     * should register all migrations immediately after creating your persistence context or if you
     * are careful to ensure that your Entities are only used by a single repository, you can
     * register your migrations in the constructor for that repository.
     *
     * <p> Note that the migration process is as follows:
     *
     * <ul><li> Note the difference between the entity's declared version and the version recorded
     * in the database.
     * <li> Run all registered pre-migrations
     * <li> Perform all default migrations (column additions and removals)
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
    public <T extends PersistentRecord> void registerMigration (Class<T> type, EntityMigration migration)
    {
        getRawMarshaller(type).registerMigration(migration);
    }

    /**
     * Returns the marshaller for the specified persistent object class, creating and initializing
     * it if necessary.
     */
    public <T extends PersistentRecord> DepotMarshaller<T> getMarshaller (Class<T> type)
        throws PersistenceException
    {
        DepotMarshaller<T> marshaller = getRawMarshaller(type);
        if (!marshaller.isInitialized()) {
            // initialize the marshaller which may create or migrate the table for its underlying
            // persistent object
            marshaller.init(this);
        }
        return marshaller;
    }

    /**
     * Invokes a non-modifying query and returns its result.
     */
    public <T> T invoke (Query<T> query)
        throws PersistenceException
    {
        CacheKey key = query.getCacheKey();
        // if there is a cache key, check the cache
        if (key != null) {
            Cache cache = getCache(key.getCacheId());
            if (cache != null) {
                Element cacheHit = cache.get(key.getCacheKey());
                if (cacheHit != null) {
                    Log.debug("invoke: cache hit [hit=" + cacheHit + "]");
                    @SuppressWarnings("unchecked") T value = (T) cacheHit.getValue();
                    value = query.transformCacheHit(key, value);
                    if (value != null) {
                        return value;
                    }
                    Log.debug("invoke: transformCacheHit returned null; rejecting cached value.");
                }
            }
            Log.debug("invoke: cache miss [key=" + key + "]");
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
        throws PersistenceException
    {
        modifier.cacheInvalidation(this);
        int rows = (Integer) invoke(null, modifier, true);
        if (rows > 0) {
            modifier.cacheUpdate(this);
        }
        return rows;
    }

    /**
     * Returns the {@link Cache} for the given cache id, or creates one if necessary.
     */
    public Cache getCache (String cacheId)
    {
        Cache cache = _cachemgr.getCache(cacheId);
        if (cache == null) {
            cache = new Cache(cacheId, 5000, false, false, 600, 60);
            _cachemgr.addCache(cache);
        }
        return cache;
    }

    /**
     * Stores a new entry indexed by the given key.
     */
    public <T> void cacheStore (CacheKey key, T value)
    {
        Log.debug("cacheStore: entry [key=" + key + ", value=" + value + "]");
        getCache(key.getCacheId()).put(new Element(key.getCacheKey(), value));

        // first do cascading invalidations
        Set<CacheListener<?>> listeners = _listenerSets.get(key.getCacheId());
        if (listeners != null && listeners.size() > 0) {
            for (CacheListener<?> listener : listeners) {
                Log.debug("cacheInvalidate: cascading [listener=" + listener + "]");
                @SuppressWarnings("unchecked") CacheListener<T> casted = (CacheListener<T>)listener;
                casted.entryModified(key, value);
            }
        }
    }

    /**
     * Evicts the cache entry indexed under the given key, if there is one.
     * The eviction may trigger further cache invalidations.
     */
    public void cacheInvalidate (CacheKey key)
    {
        cacheInvalidate(key.getCacheId(), key.getCacheKey());
    }

    /**
     * Evicts the cache entry indexed under the given class and cache key, if there is one.
     * The eviction may trigger further cache invalidations.
     */
    public void cacheInvalidate (Class pClass, Serializable cacheKey)
    {
        cacheInvalidate(pClass.getName(), cacheKey);
    }

    /**
     * Evicts the cache entry indexed under the given cache id and cache key, if there is one.
     * The eviction may trigger further cache invalidations.
     */
    public <T extends Serializable> void cacheInvalidate (String cacheId, Serializable cacheKey)
    {
        Log.debug("cacheInvalidate: entry [cacheId=" + cacheId + ", cacheKey=" + cacheKey + "]");
        Cache cache = getCache(cacheId);
        Element element = cache.get(cacheKey);

        // first do cascading invalidations
        Set<CacheListener<?>> listeners = _listenerSets.get(cacheId);
        if (listeners != null && listeners.size() > 0) {
            CacheKey key = new SimpleCacheKey(cacheId, cacheKey);
            for (CacheListener<?> listener : listeners) {
                Log.debug("cacheInvalidate: cascading [listener=" + listener + "]");
                @SuppressWarnings("unchecked") CacheListener<T> casted = (CacheListener<T>)listener;
                @SuppressWarnings("unchecked") T value =
                    (element != null ? (T) element.getValue() : null);
                casted.entryModified(key, value);
            }
        }

        // then evict the keyed entry, if needed
        if (element != null) {
            Log.debug("cacheInvalidate: evicting [cacheKey=" + cacheKey + "]");
            cache.remove(cacheKey);
        }
    }

    /**
     * Brutally iterates over the entire contents of the cache associated with the given class,
     * invoking the callback for each cache entry.
     */
    public <T extends Serializable> void cacheTraverse (Class pClass, CacheTraverser<T> filter)
    {
        cacheTraverse(pClass.getName(), filter);
    }

    /**
     * Brutally iterates over the entire contents of the cache identified by the given cache id,
     * invoking the callback for each cache entry.
     */
    public <T extends Serializable> void cacheTraverse (String cacheId, CacheTraverser<T> filter)
    {
        Cache cache = getCache(cacheId);
        if (cache != null) {
            for (Object key : cache.getKeys()) {
                Serializable sKey = (Serializable) key;
                Element element = cache.get(sKey);
                if (element != null) {
                    @SuppressWarnings("unchecked") T value = (T) element.getValue();
                    filter.visitCacheEntry(this, cacheId, sKey, value);
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
     * Looks up and creates, but does not initialize, the marshaller for the specified Entity
     * type.
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
     * Internal invoke method that takes care of transient retries
     * for both queries and modifiers.
     */
    protected <T> Object invoke (
        Query<T> query, Modifier modifier, boolean retryOnTransientFailure)
        throws PersistenceException
    {
        boolean isReadOnlyQuery = (query != null);
        Connection conn = _conprov.getConnection(_ident, isReadOnlyQuery);
        try {
            if (isReadOnlyQuery) {
                // if this becomes more complex than this single statement,
                // then this should turn into a method call that contains
                // the complexity
                return query.invoke(conn);

            } else {
                // if this becomes more complex than this single statement,
                // then this should turn into a method call that contains
                // the complexity
                return modifier.invoke(conn);
            }

        } catch (SQLException sqe) {
            if (!isReadOnlyQuery) {
                // convert this exception to a DuplicateKeyException if
                // appropriate
                String msg = sqe.getMessage();
                if (msg != null && msg.indexOf("Duplicate entry") != -1) {
                    throw new DuplicateKeyException(msg);
                }
            }

            // let the provider know that the connection failed
            _conprov.connectionFailed(_ident, isReadOnlyQuery, conn, sqe);
            conn = null;

            if (retryOnTransientFailure && isTransientException(sqe)) {
                // the MySQL JDBC driver has the annoying habit of including
                // the embedded exception stack trace in the message of their
                // outer exception; if I want a fucking stack trace, I'll call
                // printStackTrace() thanksverymuch
                String msg = StringUtil.split(String.valueOf(sqe), "\n")[0];
                Log.info("Transient failure executing operation, " +
                    "retrying [error=" + msg + "].");

            } else {
                String msg = isReadOnlyQuery ? "Query failure " + query
                                             : "Modifier failure " + modifier;
                throw new PersistenceException(msg, sqe);
            }

        } finally {
            _conprov.releaseConnection(_ident, isReadOnlyQuery, conn);
        }

        // if we got here, we want to retry a transient failure
        return invoke(query, modifier, false);
    }

    /**
     * Check whether the specified exception is a transient failure that can be retried.
     */
    protected boolean isTransientException (SQLException sqe)
    {
        // TODO: this is MySQL specific. This was snarfed from MySQLLiaison.
        String msg = sqe.getMessage();
        return (msg != null && (msg.indexOf("Lost connection") != -1 ||
                                msg.indexOf("link failure") != -1 ||
                                msg.indexOf("Broken pipe") != -1));
    }

    protected String _ident;
    protected ConnectionProvider _conprov;
    protected CacheManager _cachemgr;

    protected Map<String, Set<CacheListener<?>>> _listenerSets =
        new HashMap<String, Set<CacheListener<?>>>(); 

    protected Map<Class<?>, DepotMarshaller<?>> _marshallers =
        new HashMap<Class<?>, DepotMarshaller<?>>();
}
