//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2011 Michael Bayne, et al.
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
import java.util.HashMap;

/**
 * Implements a {@link SoftReference} cache wherein the values in the hashmap are not prevented
 * from being garbage collected.
 */
public class SoftCache<K,V>
{
    public SoftCache (int initialCapacity, float loadFactor)
    {
        _map = new HashMap<K,SoftReference<V>>(initialCapacity, loadFactor);
    }

    public SoftCache (int initialCapacity)
    {
        _map = new HashMap<K,SoftReference<V>>(initialCapacity);
    }

    public SoftCache ()
    {
        _map = new HashMap<K,SoftReference<V>>();
    }

    /**
     * Returns a reference to the underlying map.
     */
    public HashMap<K,SoftReference<V>> getMap ()
    {
        return _map;
    }

    /**
     * Returns true if the supplied key exists in this map and is mapped to an active value.
     */
    public boolean containsKey (K key)
    {
        return (get(key) != null);
    }

    /**
     * Looks up and returns the value associated with the supplied key.
     */
    public V get (K key)
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

    /**
     * Maps the specified key to the specified value.
     */
    public V put (K key, V value)
    {
        SoftReference<V> old = _map.put(key, createReference(value));
        return (old == null) ? null : old.get();
    }

    /**
     * Removes the specified key from the map. Returns the value to which the key was previously
     * mapped or null.
     */
    public V remove (K key)
    {
        SoftReference<V> ref = _map.remove(key);
        return (ref == null) ? null : ref.get();
    }

    /**
     * Clears all mappings.
     */
    public void clear ()
    {
        _map.clear();
    }

    /**
     * Creates and returns a {@link SoftReference} to the supplied value.  Subclasses can override
     * to return custom subclasses.
     */
    protected SoftReference<V> createReference (V value)
    {
        return new SoftReference<V>(value);
    }

    protected HashMap<K,SoftReference<V>> _map;
}
