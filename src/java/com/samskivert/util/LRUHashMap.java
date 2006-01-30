//
// $Id: LRUHashMap.java,v 1.7 2003/06/05 18:18:12 ray Exp $

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
public class LRUHashMap implements Map
{
    /**
     * Used to return the "size" of a cache item for systems that wish to
     * differentiate cache items based on memory footprint or other
     * metric. The size will be used to scale up the size of a cache entry
     * such that when the specified number of units is exceeded, the least
     * recently used items will be flushed until the cache is back below
     * its target size.
     */
    public static interface ItemSizer
    {
        /** Returns the "size" of the specified object. */
        public int computeSize (Object item);
    }

    /**
     * An observer may be registered with a LRU hash map to be notified
     * when items are removed from the table (either explicitly or by
     * being replaced with another value or due to being flushed).
     */
    public static interface RemovalObserver
    {
        /** Informs the observer that this item was removed from the map. */
        public void removedFromMap (LRUHashMap map, Object item);
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
    public LRUHashMap (int maxSize, ItemSizer sizer)
    {
        _delegate = new LinkedHashMap(
            Math.min(1024, Math.max(16, maxSize)), .75f, true);
        _maxSize = maxSize;
        _sizer = (sizer == null) ? _unitSizer : sizer;
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
    public void setRemovalObserver (RemovalObserver obs)
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
        if (_canFlush = canFlush) {
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

    // documentation inherited from interface
    public int size ()
    {
        return _delegate.size();
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
    public Object get (Object key)
    {
        Object result = _delegate.get(key);

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
    public Object put (Object key, Object value)
    {
        Object result = _delegate.put(key, value);

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
            Iterator iter = _delegate.entrySet().iterator();
            // don't remove the last entry, even if it's too big, because
            // a cache with nothing in it sucks
            for (int ii = size(); (ii > 1) && (_size > _maxSize); ii--) {
                Map.Entry entry = (Map.Entry) iter.next();
                entryRemoved(entry.getValue());
                iter.remove();
            }
        }
    }

    /**
     * Adjust our size to reflect the removal of the specified entry.
     */
    protected void entryRemoved (Object entry)
    {
        if (entry != null) {
            _size -= _sizer.computeSize(entry);
            if (_remobs != null) {
                _remobs.removedFromMap(this, entry);
            }
        }
    }

    // documentation inherited from interface
    public Object remove (Object key)
    {
        Object removed = _delegate.remove(key);
        entryRemoved(removed);
        return removed;
    }

    // documentation inherited from interface
    public void putAll (Map t)
    {
        for (Iterator iter = t.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry entry = (Map.Entry) iter.next();
            put(entry.getKey(), entry.getValue());
        }
    }

    // documentation inherited from interface
    public void clear ()
    {
        // notify our removal observer if we have one
        if (_remobs != null) {
            for (Iterator iter = _delegate.values().iterator();
                 iter.hasNext(); ) {
                _remobs.removedFromMap(this, iter.next());
            }
        }

        // then clear everything out
        _delegate.clear();
        _size = 0;
    }

    // documentation inherited from interface
    public Set keySet ()
    {
        // no modifying except through put() and remove()
        return Collections.unmodifiableSet(_delegate.keySet());
    }

    // documentation inherited from interface
    public Collection values ()
    {
        // no modifying except through put() and remove()
        return Collections.unmodifiableCollection(_delegate.values());
    }

    // documentation inherited from interface
    public Set entrySet ()
    {
        // no modifying except through put() and remove()
        return Collections.unmodifiableSet(_delegate.entrySet());
    }

    // documentation inherited from interface
    public boolean equals (Object o)
    {
        return _delegate.equals(o);
    }

    // documentation inherited from interface
    public int hashCode ()
    {
        return _delegate.hashCode();
    }

    /** Since we can't override addEntry and removeEntryForKey in Sun's
     * lovely collection classes, we have to delegate to a HashMap and
     * reimplement a crapload of stuff so that we can provide our required
     * size tracking support. Yay! I wish we could support code reuse as
     * well as Sun does. */
    protected LinkedHashMap _delegate;

    /** The maximum size of this cache. */
    protected int _maxSize;

    /** The current size of this cache. */
    protected int _size;

    /** Used to temporarily disable flushing. */
    protected boolean _canFlush = true;

    /** Notified when items are removed from the map, if non-null. */
    protected RemovalObserver _remobs;

    /** Used to compute the size of items in this cache. */
    protected ItemSizer _sizer;

    /** Tracking info. */
    protected boolean _tracking;
    protected HashSet _seenKeys;
    protected int _hits, _misses;

    /** Used for caches with no item sizer. */
    protected static final ItemSizer _unitSizer = new ItemSizer() {
        public int computeSize (Object item) {
            return 1;
        }
    };
}
