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

import java.io.Serializable;

/**
 * Implementations of this interface are responsible for all the caching needs of Depot.
 *
 * The cache consists of many {@link CacheBin}s. Each bin its own key space. Look-ups and storage
 * occur on a per-bin per-key basis.
 */
public interface CacheAdapter
{
    /** The encapsulated result of a cache lookup. */
    public interface CachedValue<T>
    {
        /** Returns the cached value, which can be null. */
        public T getValue ();
    }

    /**
     * A reference to a specific bin within the cache; this is the type where most of the actual
     * caching functionality occurs.
     */
    public interface CacheBin<T>
    {
        /**
         * Searches this bin using the given key and returns the resulting {@link CachedValue}, or
         * null if nothing exists in the cache for this key.
         */
        public CachedValue<T> lookup (Serializable key);

        /**
         * Stores a new value in this cache bin under the given key.
         */
        public void store (Serializable key, T value);

        /**
         * Removes the cache entry, if any, associated with the given key.
         */
        public void remove (Serializable key);

        /**
         * Provides a way to enumerate the currently cached entries in this bin.
         */
        public Iterable<Serializable> enumerateKeys ();
    }

    /**
     * Fetch the {@link CacheBin} associated with the given ID, creating one on the fly if needed.
     */
    public <T> CacheBin<T> getCache (String id);

    /**
     * Shut down all operations, e.g. persisting memory contents to disk.
     */
    public void shutdown ();
}
