//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2004 Michael Bayne
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

import java.util.AbstractSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A hashmap that maintains a count for each key.
 *
 * This implementation should change so that we extend AbstractMap, do our
 * own hashing, and can use our own Entry class.
 */
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
     * Increment the value associated with the specified key, return
     * the new value.
     */
    public int incrementCount (K key, int amount)
    {
        int[] val = get(key);
        if (val == null) {
            put(key, val = new int[1]);
        }
        val[0] += amount;
        return val[0];

        /* Alternate implementation, less hashing on the first increment
         * but more garbage created every other time.
         * (this whole method would be more optimal if this class were
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
        for (Iterator<int[]> itr = values().iterator(); itr.hasNext(); ) {
            count += itr.next()[0];
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

    @SuppressWarnings("unchecked") // documentation inherited
    public Set<Map.Entry<K, int[]>> entrySet ()
    {
        // a giant mess of hoop-jumpery so that we can convert each Map.Entry
        // returned by the iterator to be a CountEntryImpl
        return new CountEntrySet(super.entrySet());
    }

    protected static class CountEntryImpl<K>
        implements Entry<K>
    {
        public CountEntryImpl (Map.Entry<K, int[]> entry)
        {
            _entry = entry;
        }

        public boolean equals (Object o)
        {
            return _entry.equals(o);
        }

        public K getKey ()
        {
            return _entry.getKey();
        }

        public int[] getValue ()
        {
            return _entry.getValue();
        }
        
        public int hashCode ()
        {
            return _entry.hashCode();
        }

        public int[] setValue (int[] value)
        {
            return _entry.setValue(value);
        }

        public int getCount ()
        {
            return getValue()[0];
        }

        public int setCount (int count)
        {
            int[] val = getValue();
            int oldVal = val[0];
            val[0] = count;
            return oldVal;
        }

        protected Map.Entry<K, int[]> _entry;
    }

    protected class CountEntrySet<K>
        extends AbstractSet<Entry<K>>
    {
        public CountEntrySet (Set superset)
        {
            _superset = superset;
        }

        public Iterator<Entry<K>> iterator ()
        {
            @SuppressWarnings("unchecked")
            final Iterator<Map.Entry<K, int[]>> itr = _superset.iterator();
            return new Iterator<Entry<K>>() {
                public boolean hasNext ()
                {
                    return itr.hasNext();
                }

                @SuppressWarnings("unchecked")
                public Entry<K> next ()
                {
                    return new CountEntryImpl(itr.next());
                }

                public void remove ()
                {
                    itr.remove();
                }
            };
        }

        public boolean contains (Object o)
        {
            return _superset.contains(o);
        }

        public boolean remove (Object o)
        {
            return _superset.remove(o);
        }

        public int size ()
        {
            return CountHashMap.this.size();
        }
        
        public void clear ()
        {
            CountHashMap.this.clear();
        }

        protected Set _superset;
    }
}
