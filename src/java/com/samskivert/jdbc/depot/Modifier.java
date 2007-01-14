//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2006 Michael Bayne
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
import java.sql.Statement;

/**
 * Encapsulates a modification of persistent objects.
 */
public abstract class Modifier
{
    /**
     * A simple modifier that executes a single SQL statement. No cache flushing is done as a
     * result of this operation.
     */
    public static class Simple extends Modifier
    {
        public Simple (String query) {
            super(null);
            _query = query;
        }

        public int invoke (Connection conn) throws SQLException {
            Statement stmt = conn.createStatement();
            try {
                return stmt.executeUpdate(_query);
            } finally {
                stmt.close();
            }
        }

        protected String _query;
    }

    /**
     * A convenience modifier that can perform cache updates in addition to invalidation:
     * - Before {@link #invoke(Connection)}, the {@link CacheInvalidator} is run, if given.
     * - After {@link #invoke(Connection)}, the cache is updated with the modified object,
     * presuming both _key and _result are non-null. These variables may be set or modified
     * during execution in addition to being supplied to the constructor.
     */
    public static abstract class CachingModifier<T> extends Modifier
    {
        /**
         * Construct a new CachingModifier with the given result, cache key, and invalidator,
         * all of which are optional, and may also be set during execution.
         */
        protected CachingModifier (T result, CacheKey key, CacheInvalidator invalidator)
        {
            super(invalidator);
            _result = result;
            _key = key;
        }

        /**
         * Update this {@link CachingModifier}'s cache key, e.g. during insertion when a
         * persistent object first receives a generated key.
         */
        protected void updateKey (CacheKey key)
        {
            if (key != null) {
                _key = key;
            }
        }

        @Override // from Modifier
        public void cacheUpdate (PersistenceContext ctx)
        {
            super.cacheUpdate(ctx);
            // if we have both a key and a record, cache
            if (_key != null && _result != null) {
                ctx.cacheStore(_key, _result);
            }
        }

        protected CacheKey _key;
        protected T _result;
    }

    /**
     * Overriden to perform the actual database modifications represented by this object;
     * should return the number of modified rows.
     */
    public abstract int invoke (Connection conn) throws SQLException;

    /**
     * Constructs a {@link Modifier} without a cache invalidator.
     */
    public Modifier ()
    {
        this(null);
    }

    /**
     * Constructs a {@link Modifier} with the given cache invalidator.
     */
    public Modifier (CacheInvalidator invalidator)
    {
        _invalidator = invalidator;
    }

    /**
     * Do any cache invalidation needed for this modification. This method is called just
     * before the database statement is executed.
     */
    public void cacheInvalidation (PersistenceContext ctx)
    {
        if (_invalidator != null) {
            _invalidator.invalidate(ctx);
        }
    }

    /**
     * Do any cache updates needed for this modification. This method is called just after
     * the successful execution of the database statement.
     */
    public void cacheUpdate (PersistenceContext ctx)
    {
    }

    protected CacheInvalidator _invalidator;
}
