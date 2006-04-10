//
// $Id: IntMap.java,v 1.1 2001/09/15 17:22:11 mdb Exp $
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001 Michael Bayne
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

import java.util.Map;
import java.util.Set;

/**
 * An int map is a map that uses integers as keys and provides accessors
 * that eliminate the need to create and manipulate superfluous
 * <code>Integer</code> objects. It extends the <code>Map</code> interface
 * and therefore provides all of the standard accessors (for which
 * <code>Integer</code> objects should be supplied as keys).
 */
public interface IntMap<V> extends Map<Integer,V>
{
    /**
     * An IntMap entry (key-value pair). The int key may be retrieved directly,
     * avoiding the creation of an Integer object.
     */
    public interface IntEntry<V> extends Entry<Integer,V>
    {
        public int getIntKey ();
    }

    /**
     * Returns <tt>true</tt> if this map contains a mapping for the
     * specified key.
     *
     * @param key key whose presence in this map is to be tested.
     *
     * @return <tt>true</tt> if this map contains a mapping for the
     * specified key.
     */
    public boolean containsKey (int key);

    /**
     * Returns the value to which this map maps the specified key.  Returns
     * <tt>null</tt> if the map contains no mapping for this key.
     *
     * @param key key whose associated value is to be returned.
     *
     * @return the value to which this map maps the specified key, or
     * <tt>null</tt> if the map contains no mapping for this key.
     */
    public V get (int key);

    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for this key, the old
     * value is replaced.
     *
     * @param key key with which the specified value is to be associated.
     * @param value value to be associated with the specified key.
     *
     * @return previous value associated with specified key, or
     * <tt>null</tt> if there was no mapping for key.
     */
    public V put (int key, V value);

    /**
     * Removes the mapping for this key from this map if present.
     *
     * @param key key whose mapping is to be removed from the map.
     *
     * @return previous value associated with specified key, or
     * <tt>null</tt> if there was no mapping for key.
     */
    public V remove (int key);

    /**
     * Get a set of all the keys, as an IntSet.
     */
    public IntSet intKeySet ();

    /**
     * Returns a set of all the map entries.
     */
    public Set<IntEntry<V>> intEntrySet ();
}
