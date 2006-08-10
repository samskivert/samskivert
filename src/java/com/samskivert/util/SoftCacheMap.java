//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2006 Michael Bayne
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

package com.samskivert.util;

import java.lang.ref.SoftReference;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Implements a {@link SoftReference} cache wherein the values in the hashmap
 * are not prevented from being garbage collected.
 *
 * <p><em>Beware!</em> if you iterate over the contents of this map, some
 * values may be null. Also note that if you try to store null values in this
 * map, those entries will be pruned because the map cannot distinguish between
 * an expired soft reference and a null value. Why would you cache null anyway?
 */
public class SoftCacheMap<K,V> extends AbstractMap<K,V>
{
    public SoftCacheMap (int initialCapacity, float loadFactor)
    {
        _map = new HashMap<K,SoftReference<V>>(initialCapacity, loadFactor);
    }

    public SoftCacheMap (int initialCapacity)
    {
        _map = new HashMap<K,SoftReference<V>>(initialCapacity);
    }

    public SoftCacheMap ()
    {
        _map = new HashMap<K,SoftReference<V>>();
    }

    // from interface Map
    public int size ()
    {
        return _map.size();
    }

    // from interface Map
    public boolean containsKey (Object key)
    {
        return (get(key) != null);
    }

    // from interface Map
    public V get (Object key)
    {
        V value = null;
        SoftReference<V> ref = _map.get(key);
        if (ref != null) {
            value = ref.get();
            if (value == null) {
                _map.remove(key);
            }
        }
        return value;
    }

    // from interface Map
    public V put (K key, V value)
    {
        SoftReference<V> old = _map.put(key, new SoftReference<V>(value));
        return (old == null) ? null : old.get();
    }

    // from interface Map
    public V remove (Object key)
    {
        SoftReference<V> ref = _map.remove(key);
        return (ref == null) ? null : ref.get();
    }

    // from interface Map
    public void clear ()
    {
        _map.clear();
    }

    // from interface Map
    public Set<K> keySet ()
    {
        return _map.keySet();
    }

    // from interface Map
    public Set<Entry<K,V>> entrySet ()
    {
        return new EntrySet();
    }

    /**
     * Used by {@link EntrySet}.
     */
    protected Entry<K,V> getEntry (Object key)
    {
        SoftReference<V> value = _map.get(key);
        @SuppressWarnings("unchecked") K ckey = (K)key;
        return (value == null) ? null : new MapEntry<K,V>(ckey, value.get());
    }

    protected class EntrySet extends AbstractSet<Entry<K,V>> {
        public Iterator<Entry<K,V>> iterator () {
            final Iterator<Entry<K,SoftReference<V>>> iter =
                _map.entrySet().iterator();
            return new Iterator<Entry<K,V>>() {
                public boolean hasNext () {
                    return iter.hasNext();
                }
                public Entry<K,V> next () {
                    Entry<K,SoftReference<V>> ent = iter.next();
                    return (ent == null) ? null :
                        new MapEntry<K,V>(ent.getKey(), ent.getValue().get());
                }
                public void remove () {
                    iter.remove();
                }
            };
        }

        public boolean contains (Object o) {
            if (!(o instanceof Entry)) {
                return false;
            }
            @SuppressWarnings("unchecked") Entry<K,V> entry = (Entry<K,V>)o;
            return containsKey(entry.getKey());
        }

        public boolean remove (Object o) {
            if (!(o instanceof Entry)) {
                return false;
            }
            @SuppressWarnings("unchecked") Entry<K,V> entry = (Entry<K,V>)o;
            return remove(entry.getKey());
        }

        public int size () {
            return _map.size();
        }

        public void clear () {
            _map.clear();
        }
    }

    protected HashMap<K,SoftReference<V>> _map;
}
