//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

/**
 * Used to wrap a key/value pair into an object that behaves like the key
 * but holds on to the value. This might be used to maintain a list of
 * objects sorted by some unrelated key in a {@link SortableArrayList}.
 * For example:
 *
 * <pre>
 * SortableArrayList list = new SortableArrayList();
 * Integer key = new Integer(4);
 * Object value = ...;
 * KeyValue kval = new KeyValue(key, value);
 * list.insertSorted(kval);
 * // ...
 * Integer key = new Integer(4);
 * int oidx = list.indexOf(key);
 * Object value = (oidx == -1) ? null : list.get(oidx);
 * </pre>
 */
public class KeyValue<K extends Comparable<? super K>,V>
    implements Comparable<KeyValue<K,V>>
{
    /** The key in this key/value pair. */
    public K key;

    /** The value in this key/value pair. */
    public V value;

    /**
     * Creates a key/value pair with the specified key and value.
     */
    public KeyValue (K key, V value)
    {
        this.key = key;
        this.value = value;
    }

    @Override
    public String toString ()
    {
        return key + "=" + value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean equals (Object other)
    {
        return (other instanceof KeyValue<?,?>) && key.equals(((KeyValue<K,V>)other).key);
    }

    @Override
    public int hashCode ()
    {
        return key.hashCode();
    }

    // from interface Comparable<KeyValue<K,V>>
    public int compareTo (KeyValue<K,V> other)
    {
        return key.compareTo(other.key);
    }
}
