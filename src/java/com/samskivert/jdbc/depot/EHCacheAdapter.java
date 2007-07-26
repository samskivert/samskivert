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

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

/**
 * An implementation of {@link CacheAdapter} for ehcache.
 */
public class EHCacheAdapter
    implements CacheAdapter
{
    public EHCacheAdapter ()
    {
        _cachemgr = CacheManager.getInstance();
    }

    public <T> CacheBin<T> getCache (String id)
    {
        return new EHCacheBin<T>(id);
    }

    /**
     * The main ehcache-bridging class, a {@link CacheBin} interface against {@link Cache}.
     */
    protected class EHCacheBin<T> implements CacheBin<T>
    {
        // from CacheBin
        public CachedValue<T> lookup (Serializable key)
        {
            Element hit = _cache.get(key);
            if (hit == null) {
                return null;
            }

            @SuppressWarnings("unchecked") final T value = (T) hit.getValue();
            return new CachedValue<T>() {
                public T getValue () {
                    return value;
                }
            };
        }

        // from CacheBin
        public void store (Serializable key, T value)
        {
            _cache.put(new Element(key, value));
        }

        // from CacheBin
        public void remove (Serializable key)
        {
            _cache.remove(key);
        }

        // from CacheBin
        public Iterable<Serializable> enumerateKeys ()
        {
            @SuppressWarnings("unchecked") Iterable<Serializable> keys = _cache.getKeys();
            return keys;
        }

        protected EHCacheBin (String id)
        {
            _cache = _cachemgr.getCache(id);
            if (_cache == null) {
                _cache = new Cache(id, 5000, false, false, 600, 60);
                _cachemgr.addCache(_cache);
            }
        }

        protected Cache _cache;
    }

    // from CacheAdapter
    public void shutdown ()
    {
        CacheManager.getInstance().shutdown();
    }

    protected CacheManager _cachemgr;
}
