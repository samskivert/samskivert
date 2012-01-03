//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import java.util.Map;
import java.util.Set;

import com.samskivert.annotation.ReplacedBy;

/**
 * An int map is a map that uses integers as keys and provides accessors
 * that eliminate the need to create and manipulate superfluous
 * <code>Integer</code> objects. It extends the <code>Map</code> interface
 * and therefore provides all of the standard accessors (for which
 * <code>Integer</code> objects should be supplied as keys).
 */
@ReplacedBy(value="java.util.Map",
            reason="Boxing shouldn't be a major concern. It's probably better to stick to " +
            "standard classes rather than worry about a tiny memory or performance gain.")
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
