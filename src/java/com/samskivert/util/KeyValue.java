//
// $Id: KeyValue.java,v 1.2 2003/05/25 01:49:20 mdb Exp $

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
public class KeyValue
    implements Comparable
{
    /** The key in this key/value pair. */
    public Comparable key;

    /** The value in this key/value pair. */
    public Object value;

    /**
     * Creates a key/value pair with the specified key and value.
     */
    public KeyValue (Comparable key, Object value)
    {
        this.key = key;
        this.value = value;
    }

    /**
     * Generates a string representation of this instance.
     */
    public String toString ()
    {
        return key + "=" + value;
    }

    // documentation inherited
    public boolean equals (Object other)
    {
        if (other instanceof KeyValue) {
            return key.equals(((KeyValue)other).key);
        } else {
            return false;
        }
    }

    // documentation inherited
    public int hashCode ()
    {
        return key.hashCode();
    }

    // documentation inherited
    public int compareTo (Object other)
    {
        if (other instanceof KeyValue) {
            return key.compareTo(((KeyValue)other).key);
        } else {
            return getClass().getName().compareTo(other.getClass().getName());
        }
    }
}
