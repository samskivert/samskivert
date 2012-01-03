//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import java.util.AbstractSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.samskivert.annotation.ReplacedBy;

/**
 * A hashmap that maintains a count for each key.
 *
 * This implementation should change so that we extend AbstractMap, do our own hashing, and can use
 * our own Entry class.
 */
@ReplacedBy("com.google.common.collect.HashMultiset")
public class CountHashMap<K> extends HashMap<K, int[]>
{
    public interface Entry<K> extends Map.Entry<K, int[]>
    {
        /**
         * Get the value of the entry as an int.
         */
        public int getCount ();

        /**
         * Set the value of this entry as an int.
         * @return the old count
         */
        public int setCount (int count);
    }

    /**
     * Increment the value associated with the specified key, return the new value.
     */
    public int incrementCount (K key, int amount)
    {
        int[] val = get(key);
        if (val == null) {
            put(key, val = new int[1]);
        }
        val[0] += amount;
        return val[0];

        /* Alternate implementation, less hashing on the first increment but more garbage created
         * every other time.  (this whole method would be more optimal if this class were
         * rewritten)
         *
        int[] newVal = new int[] { amount };
        int[] oldVal = put(key, newVal);
        if (oldVal != null) {
            newVal[0] += oldVal[0];
            return oldVal[0];
        }
        return 0;
        */
    }

    /**
     * Get the count associated with the specified key.
     */
    public int getCount (K key)
    {
        int[] val = get(key);
        return (val == null) ? 0 : val[0];
    }

    /**
     * Set the count for the specified key.
     *
     * @return the old count.
     */
    public int setCount (K key, int count)
    {
        int[] val = get(key);
        if (val == null) {
            put(key, new int[] { count });
            return 0; // old value
        }
        int oldVal = val[0];
        val[0] = count;
        return oldVal;
    }

    /**
     * Get the total count for all keys in the map.
     */
    public int getTotalCount ()
    {
        int count = 0;
        for (int[] name : values()) {
            count += name[0];
        }
        return count;
    }

    /**
     * Compress the count map- remove entries for which the value is 0.
     */
    public void compress ()
    {
        for (Iterator<int[]> itr = values().iterator(); itr.hasNext(); ) {
            if (itr.next()[0] == 0) {
                itr.remove();
            }
        }
    }

    @Override
    public Set<Map.Entry<K, int[]>> entrySet ()
    {
        // a giant mess of hoop-jumpery so that we can convert each Map.Entry returned by the
        // iterator to be a CountEntryImpl
        Set<?> eset = new CountEntrySet<K>(super.entrySet());
        @SuppressWarnings("unchecked") Set<Map.Entry<K, int[]>> r = (Set<Map.Entry<K, int[]>>)eset;
        return r;
    }

    /**
     * Returns a set of {@link Entry} records which can be used to easily obtain our count.
     */
    public Set<Entry<K>> countEntrySet ()
    {
        return new CountEntrySet<K>(super.entrySet());
    }

    protected static class CountEntryImpl<K>
        implements Entry<K>
    {
        public CountEntryImpl (Map.Entry<K, int[]> entry) {
            _entry = entry;
        }

        public K getKey () {
            return _entry.getKey();
        }

        public int[] getValue () {
            return _entry.getValue();
        }

        public int[] setValue (int[] value) {
            return _entry.setValue(value);
        }

        public int getCount () {
            return getValue()[0];
        }

        public int setCount (int count) {
            int[] val = getValue();
            int oldVal = val[0];
            val[0] = count;
            return oldVal;
        }

        @Override public int hashCode () {
            return _entry.hashCode();
        }

        @Override public boolean equals (Object o) {
            if (!(o instanceof CountEntryImpl<?>)) {
                return false;
            }
            @SuppressWarnings("unchecked") CountEntryImpl<K> other = (CountEntryImpl<K>)o;
            return _entry.equals(other._entry);
        }

        @Override public String toString () {
            return _entry.toString();
        }

        protected Map.Entry<K, int[]> _entry;
    }

    protected class CountEntrySet<E> extends AbstractSet<Entry<E>>
    {
        public CountEntrySet (Set<Map.Entry<E,int[]>> superset) {
            _superset = superset;
        }

        @Override public Iterator<Entry<E>> iterator () {
            final Iterator<Map.Entry<E, int[]>> itr = _superset.iterator();
            return new Iterator<Entry<E>>() {
                public boolean hasNext () {
                    return itr.hasNext();
                }
                public Entry<E> next () {
                    return new CountEntryImpl<E>(itr.next());
                }
                public void remove () {
                    itr.remove();
                }
            };
        }

        @Override public boolean contains (Object o) {
            return _superset.contains(o);
        }

        @Override public boolean remove (Object o) {
            return _superset.remove(o);
        }

        @Override public int size () {
            return CountHashMap.this.size();
        }

        @Override public void clear () {
            CountHashMap.this.clear();
        }

        protected Set<Map.Entry<E,int[]>> _superset;
    }
}
