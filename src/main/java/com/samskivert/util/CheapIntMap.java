//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import java.util.Arrays;

/**
 * A low overhead hash map using positive integers as keys that is useful
 * for fast storage and lookup of a small and bounded number of items.
 */
public class CheapIntMap
{
    /**
     * Constructs a map that can hold the specified number of items.
     */
    public CheapIntMap (int maxSize)
    {
        _keys = new int[maxSize];
        _values = new Object[maxSize];
        clear();
    }

    /**
     * Inserts the specified value into the map. <em>Note:</em> the key
     * must be a positive integer.
     */
    public void put (int key, Object value)
    {
        int size = _keys.length, start = key % size, iidx = -1;
        for (int ii = 0; ii < size; ii++) {
            int idx = (ii + start) % size;
            if (_keys[idx] == key) {
                _values[idx] = value;
                return;
            } else if (iidx == -1 && _keys[idx] == -1) {
                iidx = idx;
            }
        }

        if (iidx != -1) {
            _keys[iidx] = key;
            _values[iidx] = value;
            return;
        }

        // they booched it!
        String errmsg = "You fool! You've filled up your cheap int map! " +
            "[keys=" + StringUtil.toString(_keys) +
            ", values=" + StringUtil.toString(_values) + "]";
        throw new RuntimeException(errmsg);
    }

    /**
     * Returns the object with the specified key, null if no object exists
     * in the table with that key.
     */
    public Object get (int key)
    {
        int size = _keys.length, start = key % size;
        for (int ii = 0; ii < size; ii++) {
            int idx = (ii + start) % size;
            if (_keys[idx] == key) {
                return _values[idx];
            }
        }
        return null;
    }

    /**
     * Removes the mapping associated with the specified key. The previous
     * value of the mapping will be returned.
     */
    public Object remove (int key)
    {
        int size = _keys.length, start = key % size;
        for (int ii = 0; ii < size; ii++) {
            int idx = (ii + start) % size;
            if (_keys[idx] == key) {
                Object value = _values[idx];
                _keys[idx] = -1;
                _values[idx] = null;
                return value;
            }
        }
        return null;
    }

    /**
     * Clears out all mappings from the table.
     */
    public void clear ()
    {
        Arrays.fill(_keys, -1);
        Arrays.fill(_values, null);
    }

    /**
     * Returns the number of mappings in this table.
     */
    public int size ()
    {
        int size = 0;
        for (int ii = 0, ll = _keys.length; ii < ll; ii++) {
            if (_keys[ii] != -1) {
                size++;
            }
        }
        return size;
    }

    /**
     * Returns the key with the specified index or -1 if no key exists at
     * that index.
     */
    public int getKey (int index)
    {
        return _keys[index];
    }

    /**
     * Returns the value with the specified index or null if no value
     * exists at that index.
     */
    public Object getValue (int index)
    {
        return _values[index];
    }

    protected int[] _keys;
    protected Object[] _values;
}
