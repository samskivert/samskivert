//
// $Id: LockableLRUHashMap.java,v 1.2 2002/11/26 21:14:36 ray Exp $

package com.samskivert.util;

import java.util.HashSet;
import java.util.Map;

/**
 * A LRU HashMap that allows specified keys to be locked such that they
 * cannot be removed.
 */
public class LockableLRUHashMap extends LRUHashMap
{
    /**
     * Construct a LockableLRUHashMap with the specified maximum number
     * of <em>unlocked</em> elements.
     */
    public LockableLRUHashMap (int baseMaxSize)
    {
        super(baseMaxSize);
    }

    /**
     * Clear all entries but those that are currently locked.
     */
    public void clear ()
    {
        // unfortunately there's not a better way to do this because all the
        // appropriate variables have package access
        Object[] keys = keySet().toArray();
        for (int ii=0, nn=keys.length; ii < nn; ii++) {
            if (! _locks.contains(keys[ii])) {
                remove(keys[ii]);
            }
        }
    }

    /**
     * Lock the specified key from being removed.
     * This has the side effect of increasing the maximum size by 1.
     *
     * @return false if the key was already locked and the max size was
     * not altered.
     */
    public boolean lock (Object key)
    {
        if (_locks.add(key)) {
            _maxSize++;
            return true;
        }
        return false;
    }

    /**
     * Unlock the specified key so that it can be removed.
     * This has the side effect of decreasing the maximum size by 1.
     * If the map is bigger than the new maximum size, the object with that
     * key will be immediately removed.
     *
     * @return false if the key was not even locked in the first place.
     */
    public boolean unlock (Object key) 
    {
        if (_locks.remove(key)) {
            _maxSize--;
            if (size() > _maxSize) {
                remove(key);
            }
            return true;
        }
        return false;
    }

    // documentation inherited
    protected boolean removeEldestEntry (Map.Entry eldest)
    {
        return (! _locks.contains(eldest.getKey())) &&
            super.removeEldestEntry(eldest);
    }

    /** The set of keys that are locked. */
    protected HashSet _locks = new HashSet();
}
