//
// $Id: LRUHashMap.java,v 1.1 2002/10/18 01:43:46 ray Exp $

package com.samskivert.util;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A HashMap with LRU functionality and rudimentary performance tracking
 * facilities.
 */
public class LRUHashMap extends LinkedHashMap
{
    /**
     * Construct a LRUHashMap with the specified maximum size.
     */
    public LRUHashMap (int maxSize)
    {
        super(Math.min(1024, Math.max(16, maxSize)), .75f, true);
        _maxSize = maxSize;
    }

    /**
     * Turn performance tracking on/off.
     */
    public void setTracking (boolean track)
    {
        if (track != _tracking) {
            _tracking = track;
            if (track) {
                _seenKeys = new HashSet();
                _misses = _hits = 0;

                // oh boy, but to properly track we need to clear the hash
                clear();
            } else {
                _seenKeys = null;
            }
        }
    }

    /**
     * Return a measure of the effectiveness of this cache, the ratio of
     * hits to misses.
     *
     * @return an array containing {hits, misses}
     */
    public int[] getTrackedEffectiveness ()
    {
        return new int[] {_hits, _misses};
    }

    // documentation inherited
    public Object get (Object key)
    {
        Object result = super.get(key);

        if (_tracking) {
            if (result == null) {
                if (_seenKeys.contains(key)) {
                    // only count a miss if we've seen the key before
                    _misses++;
                }
            } else {
                _hits++;
            }
        }

        return result;
    }

    // documentation inherited
    public Object put (Object key, Object value)
    {
        Object result = super.put(key, value);

        if (_tracking) {
            _seenKeys.add(key);
        }

        return result;
    }

    // documentation inherited
    protected boolean removeEldestEntry (Map.Entry eldest)
    {
        return size() > _maxSize;
    }

    /** The maximum size of this cache. */
    protected int _maxSize;

    /** Tracking info. */
    protected boolean _tracking;
    protected HashSet _seenKeys;
    protected int _hits, _misses;
}
