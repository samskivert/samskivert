//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * A HashMap with LRU functionality and rudimentary performance tracking
 * facilities.
 */
public class LRUHashMap<K,V> implements Map<K,V>
{
    /**
     * Used to return the "size" of a cache item for systems that wish to
     * differentiate cache items based on memory footprint or other
     * metric. The size will be used to scale up the size of a cache entry
     * such that when the specified number of units is exceeded, the least
     * recently used items will be flushed until the cache is back below
     * its target size.
     */
    public static interface ItemSizer<V>
    {
        /** Returns the "size" of the specified object. */
        public int computeSize (V item);
    }

    /**
     * An observer may be registered with a LRU hash map to be notified
     * when items are removed from the table (either explicitly or by
     * being replaced with another value or due to being flushed).
     */
    public static interface RemovalObserver<K,V>
    {
        /** Informs the observer that this item was removed from the map. */
        public void removedFromMap (LRUHashMap<K,V> map, V item);
    }

    /**
     * Construct a LRUHashMap with the specified maximum size. All items
     * in the cache will be considered to have a size of one.
     */
    public LRUHashMap (int maxSize)
    {
        this(maxSize, null);
    }

    /**
     * Construct a LRUHashMap with the specified maximum total size and
     * the supplied item sizer which will be used to compute the size of
     * each item.
     */
    public LRUHashMap (int maxSize, ItemSizer<V> sizer)
    {
        _delegate = new LinkedHashMap<K,V>(
            Math.min(1024, Math.max(16, maxSize)), .75f, true);
        _maxSize = maxSize;
        _sizer = (sizer == null) ? new ItemSizer<V>() {
            public int computeSize (V item) {
                return 1;
            }
        } : sizer;
    }

    /**
     * Updates the cache's maximum size, flushing elements from the cache
     * if necessary.
     */
    public void setMaxSize (int maxSize)
    {
        // configure our new maximum size
        _maxSize = maxSize;

        // boot enough people to get below said size
        flush();
    }

    /**
     * Returns this cache's maximum size.
     */
    public int getMaxSize ()
    {
        return _maxSize;
    }

    /**
     * Configures this hash map with a removal observer.
     */
    public void setRemovalObserver (RemovalObserver<K,V> obs)
    {
        _remobs = obs;
    }

    /**
     * Used to temporarily disable flushing elements from the
     * cache. Generally this is only used to avoid undesired garbage
     * collection until such time as it is acceptable. Beware the risks of
     * leaving flushing disabled for too long.
     */
    public void setCanFlush (boolean canFlush)
    {
        _canFlush = canFlush;
        if (_canFlush) {
            // if we just reenabled flushing, flush
            flush();
        }
    }

    /**
     * Turn performance tracking on/off.
     */
    public void setTracking (boolean track)
    {
        if (track != _tracking) {
            _tracking = track;
            if (track) {
                _seenKeys = new HashSet<K>();
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

    // documentation inherited from interface
    public int size ()
    {
        return _delegate.size();
    }

    /**
     * Update the overall size of the cache if an already added item changes size.
     *
     * @param sizeDifference the amount to adjust the size by.
     */
    public void adjustSize (int sizeDifference)
    {
        _size += sizeDifference;
        flush();
    }

    // documentation inherited from interface
    public boolean isEmpty ()
    {
        return _delegate.isEmpty();
    }

    // documentation inherited from interface
    public boolean containsKey (Object key)
    {
        return _delegate.containsKey(key);
    }

    // documentation inherited from interface
    public boolean containsValue (Object value)
    {
        return _delegate.containsValue(value);
    }

    // documentation inherited from interface
    public V get (Object key)
    {
        V result = _delegate.get(key);

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

    // documentation inherited from interface
    public V put (K key, V value)
    {
        V result = _delegate.put(key, value);

        if (_tracking) {
            _seenKeys.add(key);
        }

        // avoid fruitless NOOPs
        if (result != value) {
            // updated our computed "size"
            _size += _sizer.computeSize(value);
            entryRemoved(result);
//         System.out.println("Added " + value + ": " + _size);
        }

        // flush if needed
        flush();

        return result;
    }

    /**
     * Flushes entries from the cache until we're back under our desired
     * cache size.
     */
    protected void flush ()
    {
        if (!_canFlush) {
            return;
        }

        // If we've exceeded our size, remove things until we're back
        // under the required size.
        if (_size > _maxSize) {
            // This works because the entrySet iterator of a LinkedHashMap
            // returns the entries in LRU order
            Iterator<Map.Entry<K,V>> iter = _delegate.entrySet().iterator();
            // don't remove the last entry, even if it's too big, because
            // a cache with nothing in it sucks
            for (int ii = size(); (ii > 1) && (_size > _maxSize); ii--) {
                Map.Entry<K,V> entry = iter.next();
                entryRemoved(entry.getValue());
                iter.remove();
            }
        }
    }

    /**
     * Adjust our size to reflect the removal of the specified entry.
     */
    protected void entryRemoved (V entry)
    {
        if (entry != null) {
            _size -= _sizer.computeSize(entry);
            if (_remobs != null) {
                _remobs.removedFromMap(this, entry);
            }
        }
    }

    // documentation inherited from interface
    public V remove (Object key)
    {
        V removed = _delegate.remove(key);
        entryRemoved(removed);
        return removed;
    }

    // documentation inherited from interface
    public void putAll (Map<? extends K,? extends V> t)
    {
        for (Map.Entry<? extends K,? extends V> entry : t.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    // documentation inherited from interface
    public void clear ()
    {
        // notify our removal observer if we have one
        if (_remobs != null) {
            for (Iterator<V> iter = _delegate.values().iterator();
                 iter.hasNext(); ) {
                _remobs.removedFromMap(this, iter.next());
            }
        }

        // then clear everything out
        _delegate.clear();
        _size = 0;
    }

    // documentation inherited from interface
    public Set<K> keySet ()
    {
        // no modifying except through put() and remove()
        return Collections.unmodifiableSet(_delegate.keySet());
    }

    // documentation inherited from interface
    public Collection<V> values ()
    {
        // no modifying except through put() and remove()
        return Collections.unmodifiableCollection(_delegate.values());
    }

    // documentation inherited from interface
    public Set<Map.Entry<K,V>> entrySet ()
    {
        // no modifying except through put() and remove()
        return Collections.unmodifiableSet(_delegate.entrySet());
    }

    @Override
    public boolean equals (Object o)
    {
        return _delegate.equals(o);
    }

    @Override
    public int hashCode ()
    {
        return _delegate.hashCode();
    }

    /** Since we can't override addEntry and removeEntryForKey in Sun's
     * lovely collection classes, we have to delegate to a HashMap and
     * reimplement a crapload of stuff so that we can provide our required
     * size tracking support. Yay! I wish we could support code reuse as
     * well as Sun does. */
    protected LinkedHashMap<K,V> _delegate;

    /** The maximum size of this cache. */
    protected int _maxSize;

    /** The current size of this cache. */
    protected int _size;

    /** Used to temporarily disable flushing. */
    protected boolean _canFlush = true;

    /** Notified when items are removed from the map, if non-null. */
    protected RemovalObserver<K,V> _remobs;

    /** Used to compute the size of items in this cache. */
    protected ItemSizer<V> _sizer;

    /** Tracking info. */
    protected boolean _tracking;
    protected HashSet<K> _seenKeys;
    protected int _hits, _misses;
}
