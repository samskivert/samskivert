//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.samskivert.annotation.ReplacedBy;

/**
 * A CountMap maps keys to non-null Integers and provides methods for efficiently adding
 * to the count.
 */
@ReplacedBy("com.google.common.collect.HashMultiset")
public class CountMap<K> extends AbstractMap<K, Integer>
{
    /**
     * Create a new CountMap backed by a HashMap.
     */
    public CountMap ()
    {
        this(new HashMap<K, CountEntry<K>>());
    }

    /**
     * For subclassing, etc. Not yet public.
     */
    protected CountMap (Map<K, CountEntry<K>> backing)
    {
        if (!backing.isEmpty()) {
            throw new IllegalArgumentException("Map is non-empty");
        }
        _backing = backing;
    }

    /**
     * Add 1 to the count for the specified key.
     */
    public int increment (K key)
    {
        return add(key, 1);
    }

    /**
     * Subtract 1 from the count for the specified key.
     */
    public int decrement (K key)
    {
        return add(key, -1);
    }

    /**
     * Add the specified amount to the count for the specified key, return the new count.
     * Adding 0 will ensure that a Map.Entry is created for the specified key.
     */
    public int add (K key, int amount)
    {
        CountEntry<K> entry = _backing.get(key);
        if (entry == null) {
            _backing.put(key, new CountEntry<K>(key, amount));
            return amount;
        }
        return (entry.count += amount);
    }

    /**
     * Get the count for the specified key. If the key is not present, 0 is returned.
     */
    public int getCount (K key)
    {
        CountEntry<K> entry = _backing.get(key);
        return (entry == null) ? 0 : entry.count;
    }

    /**
     * Remove any keys for which the count is currently 0.
     */
    public void compress ()
    {
        for (Iterator<CountEntry<K>> it = _backing.values().iterator(); it.hasNext(); ) {
            if (it.next().count == 0) {
                it.remove();
            }
        }
    }

    @Override
    public Set<Map.Entry<K, Integer>> entrySet ()
    {
        Set<Map.Entry<K, Integer>> es = _entrySet;
        return (es != null) ? es : (_entrySet = new EntrySet());
    }

    @Override
    public Integer put (K key, Integer value)
    {
        return integer(_backing.put(key, new CountEntry<K>(key, value.intValue())));
    }

    @Override
    public boolean containsKey (Object key)
    {
        return _backing.containsKey(key);
    }

    @Override
    public Integer get (Object key)
    {
        return integer(_backing.get(key));
    }

    @Override
    public Integer remove (Object key)
    {
        return integer(_backing.remove(key));
    }

    @Override
    public void clear ()
    {
        _backing.clear();
    }

    @Override
    public int size ()
    {
        return _backing.size();
    }

    @Override
    public boolean isEmpty ()
    {
        return _backing.isEmpty();
    }

    /**
     * Our EntrySet.
     */
    protected class EntrySet extends AbstractSet<Map.Entry<K, Integer>>
    {
        @SuppressWarnings("unchecked")
        @Override
        public Iterator<Map.Entry<K, Integer>> iterator ()
        {
            // fuck if I know why I gotta jump through this hoop
            Iterator<?> it = _backing.values().iterator();
            return (Iterator<Map.Entry<K, Integer>>) it;
        }

        @Override
        public boolean contains (Object o) {
            if (!(o instanceof Map.Entry<?, ?>)) {
                return false;
            }
            Map.Entry<?, ?> oentry = (Map.Entry<?, ?>) o;
            CountEntry<K> entry = _backing.get(oentry.getKey());
            return (entry != null) && entry.getValue().equals(oentry.getValue());
        }

        @Override
        public boolean remove (Object o) {
            if (contains(o)) {
                CountMap.this.remove(((Map.Entry<?, ?>) o).getKey());
                return true;
            }
            return false;
        }

        @Override
        public int size () {
            return CountMap.this.size();
        }

        @Override
        public void clear () {
            CountMap.this.clear();
        }
    }

    /**
     * Return null or the boxed value contained in the count.
     */
    protected static final Integer integer (CountEntry<?> val)
    {
        return (val == null) ? null : val.count;
    }

    protected static class CountEntry<K>
        implements Map.Entry<K, Integer>
    {
        public CountEntry (K key, int initialCount)
        {
            this.key = key;
            this.count = initialCount;
        }

        public final K getKey ()
        {
            return key;
        }

        public Integer getValue ()
        {
            return count;
        }

        public Integer setValue (Integer newValue)
        {
            int oldVal = count;
            count = newValue;
            return oldVal;
        }

        @Override
        public int hashCode ()
        {
            return ((key == null) ? 0 : key.hashCode()) ^ count;
        }

        @Override
        public boolean equals (Object o)
        {
            if (o == this) {
                return true;
            }
            if (!(o instanceof Map.Entry<?, ?>)) {
                return false;
            }
            Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
            Object okey = e.getKey();
            return ((key == null) ? (okey == null) : key.equals(okey)) &&
                getValue().equals(e.getValue());
        }

        @Override
        public String toString ()
        {
            return key + "=" + count;
        }

        /** The key. */
        protected final K key;

        /** The current count. */
        protected int count;
    }

    /** Our backing map */
    protected Map<K, CountEntry<K>> _backing;

    /** The entrySet, if created. */
    protected transient Set<Map.Entry<K, Integer>> _entrySet = null;
}
