//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import com.samskivert.annotation.ReplacedBy;

/**
 * An int map is like a regular map, but with integers as keys. We avoid
 * the annoyance of having to create integer objects every time we want to
 * lookup or insert values. The hash int map is an int map that uses a
 * hashtable mechanism to store its key/value mappings.
 */
@ReplacedBy(value="java.util.Map",
            reason="Boxing shouldn't be a major concern. It's probably better to stick to " +
            "standard classes rather than worry about a tiny memory or performance gain.")
public class HashIntMap<V> extends AbstractMap<Integer,V>
    implements IntMap<V>, Cloneable, Serializable
{
    /**
     * The default number of buckets to use for the hash table.
     */
    public final static int DEFAULT_BUCKETS = 16;

    /**
     * The default load factor.
     */
    public final static float DEFAULT_LOAD_FACTOR = 1.75f;

    /**
     * Constructs an empty hash int map with the specified number of hash
     * buckets.
     */
    public HashIntMap (int buckets, float loadFactor)
    {
        // force the capacity to be a power of 2
        int capacity = 1;
        while (capacity < buckets) {
            capacity <<= 1;
        }

        _buckets = createBuckets(capacity);
        _loadFactor = loadFactor;
    }

    /**
     * Constructs an empty hash int map with the default number of hash
     * buckets.
     */
    public HashIntMap ()
    {
        this(DEFAULT_BUCKETS, DEFAULT_LOAD_FACTOR);
    }

    @Override
    public int size ()
    {
        return _size;
    }

    @Override
    public boolean containsKey (Object key)
    {
        return (key instanceof Integer) && containsKey(((Integer)key).intValue());
    }

    // documentation inherited
    public boolean containsKey (int key)
    {
        return (null != getImpl(key));
    }

    @Override
    public boolean containsValue (Object o)
    {
        for (Record<V> bucket : _buckets) {
            for (Record<V> r = bucket; r != null; r = r.next) {
                if (ObjectUtil.equals(r.value, o)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public V get (Object key)
    {
        return (key instanceof Integer) ? get(((Integer)key).intValue()) : null;
    }

    // documentation inherited
    public V get (int key)
    {
        Record<V> rec = getImpl(key);
        return (rec == null) ? null : rec.value;
    }

    @Override
    public V put (Integer key, V value)
    {
        return put(key.intValue(), value);
    }

    // documentation inherited
    public V put (int key, V value)
    {
        // check to see if we've passed our load factor, if so: resize
        ensureCapacity(_size + 1);

        int index = keyToIndex(key);
        Record<V> rec = _buckets[index];

        // either we start a new chain
        if (rec == null) {
            _buckets[index] = new Record<V>(key, value);
            _size++; // we're bigger
            return null;
        }

        // or we replace an element in an existing chain
        Record<V> prev = rec;
        for (; rec != null; rec = rec.next) {
            if (rec.key == key) {
                V ovalue = rec.value;
                rec.value = value; // we're not bigger
                return ovalue;
            }
            prev = rec;
        }

        // or we append it to this chain
        prev.next = new Record<V>(key, value);
        _size++; // we're bigger
        return null;
    }

    @Override
    public V remove (Object key)
    {
        return (key instanceof Integer) ? remove(((Integer)key).intValue()) : null;
    }

    // documentation inherited
    public V remove (int key)
    {
        Record<V> removed = removeImpl(key, true);
        return (removed == null) ? null : removed.value;
    }

    /**
     * Locate the record with the specified key.
     */
    protected Record<V> getImpl (int key)
    {
        for (Record<V> rec = _buckets[keyToIndex(key)]; rec != null; rec = rec.next) {
            if (rec.key == key) {
                return rec;
            }
        }
        return null;
    }

    /**
     * Remove an element with optional checking to see if we should shrink.
     * When this is called from our iterator, checkShrink==false to avoid booching the buckets.
     */
    protected Record<V> removeImpl (int key, boolean checkShrink)
    {
        int index = keyToIndex(key);

        // go through the chain looking for a match
        for (Record<V> prev = null, rec = _buckets[index]; rec != null; rec = rec.next) {
            if (rec.key == key) {
                if (prev == null) {
                    _buckets[index] = rec.next;
                } else {
                    prev.next = rec.next;
                }
                _size--;
                if (checkShrink) {
                    checkShrink();
                }
                return rec;
            }
            prev = rec;
        }

        return null;
    }

    // documentation inherited
    public void putAll (IntMap<V> t)
    {
        // if we can, avoid creating Integer objects while copying
        for (IntEntry<V> entry : t.intEntrySet()) {
            put(entry.getIntKey(), entry.getValue());
        }
    }

    @Override
    public void clear ()
    {
        // abandon all of our hash chains (the joy of garbage collection)
        Arrays.fill(_buckets, null);
        // zero out our size
        _size = 0;
    }

    /**
     * Ensure that the hash can comfortably hold the specified number
     * of elements. Calling this method is not necessary, but can improve
     * performance if done prior to adding many elements.
     */
    public void ensureCapacity (int minCapacity)
    {
        int size = _buckets.length;
        while (minCapacity > (int) (size * _loadFactor)) {
            size *= 2;
        }
        if (size != _buckets.length) {
            resizeBuckets(size);
        }
    }

    /**
     * Turn the specified key into an index.
     */
    protected final int keyToIndex (int key)
    {
        // we lift the hash-fixing function from HashMap because Sun
        // wasn't kind enough to make it public
        key += ~(key << 9);
        key ^=  (key >>> 14);
        key +=  (key << 4);
        key ^=  (key >>> 10);
        return key & (_buckets.length - 1);
    }

    /**
     * Check to see if we want to shrink the table.
     */
    protected void checkShrink ()
    {
        if ((_buckets.length > DEFAULT_BUCKETS) &&
                (_size < (int) (_buckets.length * _loadFactor * .125))) {
            resizeBuckets(Math.max(DEFAULT_BUCKETS, _buckets.length >> 1));
        }
    }

    /**
     * Resize the hashtable.
     *
     * @param newsize MUST be a power of 2.
     */
    protected void resizeBuckets (int newsize)
    {
        Record<V>[] oldbuckets = _buckets;
        _buckets = createBuckets(newsize);

        // we shuffle the records around without allocating new ones
        int index = oldbuckets.length;
        while (index-- > 0) {
            Record<V> oldrec = oldbuckets[index];
            while (oldrec != null) {
                Record<V> newrec = oldrec;
                oldrec = oldrec.next;

                // always put the newrec at the start of a chain
                int newdex = keyToIndex(newrec.key);
                newrec.next = _buckets[newdex];
                _buckets[newdex] = newrec;
            }
        }
    }

    @Override
    public Set<Entry<Integer,V>> entrySet ()
    {
        return new AbstractSet<Entry<Integer,V>>() {
            @Override public int size () {
                return _size;
            }
            @Override public Iterator<Entry<Integer,V>> iterator () {
                return new MapEntryIterator();
            }
        };
    }

    // documentation inherited
    public Set<IntEntry<V>> intEntrySet ()
    {
        return new AbstractSet<IntEntry<V>>() {
            @Override public int size () {
                return _size;
            }
            @Override public Iterator<IntEntry<V>> iterator () {
                return new IntEntryIterator();
            }
        };
    }

    protected abstract class RecordIterator<E>
        implements Iterator<E>
    {
        public boolean hasNext ()
        {
            // if we're pointing to an entry, we're good
            if (_record != null) {
                return true;
            }

            // search backward through the buckets looking for the next non-empty hash chain
            while (_index-- > 0) {
                if ((_record = _buckets[_index]) != null) {
                    return true;
                }
            }

            // found no non-empty hash chains, we're done
            return false;
        }

        public Record<V> nextRecord ()
        {
            // if we're not pointing to an entry, search for the next
            // non-empty hash chain
            if (_record == null) {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
            }

            // keep track of the last thing we returned, our next record, and return
            _last = _record;
            _record = _record.next;
            return _last;
        }

        public void remove ()
        {
            if (_last == null) {
                throw new IllegalStateException();
            }

            // remove the record the hard way, avoiding any major changes to the buckets
            HashIntMap.this.removeImpl(_last.key, false);
            _last = null;
        }

        protected int _index = _buckets.length;
        protected Record<V> _record, _last;
    }

    protected class IntEntryIterator extends RecordIterator<IntEntry<V>>
    {
        public IntEntry<V> next () {
            return nextRecord();
        }
    }

    protected class MapEntryIterator extends RecordIterator<Entry<Integer,V>>
    {
        public Entry<Integer,V> next () {
            return nextRecord();
        }
    }

    // documentation inherited from interface IntMap
    public IntSet intKeySet ()
    {
        // damn Sun bastards made the 'keySet' variable with default access, so we can't share it
        if (_keySet == null) {
            _keySet = new AbstractIntSet() {
                public Interator interator () {
                    return new AbstractInterator () {
                        public boolean hasNext () {
                            return i.hasNext();
                        }
                        public int nextInt () {
                            return i.next().getIntKey();
                        }
                        @Override public void remove () {
                            i.remove();
                        }
                        private Iterator<IntEntry<V>> i = intEntrySet().iterator();
                    };
                }

                @Override public int size () {
                    return HashIntMap.this.size();
                }

                @Override public boolean contains (int t) {
                    return HashIntMap.this.containsKey(t);
                }

                @Override public boolean remove (int value) {
                    Record<V> removed = removeImpl(value, true);
                    return (removed != null);
                }
            };
        }
        return _keySet;
    }

    @Override
    public Set<Integer> keySet ()
    {
        return intKeySet();
    }

    /**
     * Returns an interation over the keys of this hash int map.
     */
    public Interator keys ()
    {
        return intKeySet().interator();
    }

    /**
     * Returns an iteration over the elements (values) of this hash int
     * map.
     */
    public Iterator<V> elements ()
    {
        return values().iterator();
    }

    @Override
    public HashIntMap<V> clone ()
    {
        try {
            @SuppressWarnings("unchecked")
            HashIntMap<V> result = (HashIntMap<V>) super.clone();
            result._keySet = null;
            Record<V>[] buckets = result._buckets = result._buckets.clone();
            for (int ii = buckets.length - 1; ii >= 0; ii--) {
                if (buckets[ii] != null) {
                    buckets[ii] = buckets[ii].clone();
                }
            }
            return result;

        } catch (CloneNotSupportedException cnse) {
            throw new AssertionError(cnse); // won't happen; we're Cloneable
        }
    }

    /**
     * Save the state of this instance to a stream (i.e., serialize it).
     */
    private void writeObject (ObjectOutputStream s)
        throws IOException
    {
        // write out number of buckets
        s.writeInt(_buckets.length);
        s.writeFloat(_loadFactor);

        // write out size (number of mappings)
        s.writeInt(_size);

        // write out keys and values
        for (IntEntry<V> entry : intEntrySet()) {
            s.writeInt(entry.getIntKey());
            s.writeObject(entry.getValue());
        }
    }

    /**
     * Reconstitute the <tt>HashIntMap</tt> instance from a stream (i.e.,
     * deserialize it).
     */
    private void readObject (ObjectInputStream s)
         throws IOException, ClassNotFoundException
    {
        // read in number of buckets and allocate the bucket array
        _buckets = createBuckets(s.readInt());
        _loadFactor = s.readFloat();

        // read in size (number of mappings)
        int size = s.readInt();

        // read the keys and values
        for (int i=0; i<size; i++) {
            int key = s.readInt();
            @SuppressWarnings("unchecked") V value = (V)s.readObject();
            put(key, value);
        }
    }

    protected Record<V>[] createBuckets (int size)
    {
        @SuppressWarnings("unchecked") Record<V>[] recs = (Record<V>[])new Record<?>[size];
        return recs;
    }

    protected static class Record<V>
        implements Cloneable, IntEntry<V>
    {
        public Record<V> next;
        public int key;
        public V value;

        public Record (int key, V value)
        {
            this.key = key;
            this.value = value;
        }

        public Integer getKey ()
        {
            return Integer.valueOf(key);
        }

        public int getIntKey ()
        {
            return key;
        }

        public V getValue ()
        {
            return value;
        }

        public V setValue (V value)
        {
            V ovalue = this.value;
            this.value = value;
            return ovalue;
        }

        @Override public boolean equals (Object o)
        {
            if (o instanceof IntEntry<?>) {
                IntEntry<?> that = (IntEntry<?>)o;
                return (this.key == that.getIntKey()) &&
                    ObjectUtil.equals(this.value, that.getValue());

            } else if (o instanceof Entry<?,?>) {
                Entry<?,?> that = (Entry<?,?>)o;
                return (this.getKey().equals(that.getKey())) &&
                    ObjectUtil.equals(this.value, that.getValue());

            } else {
                return false;
            }
        }

        @Override public int hashCode ()
        {
            return key ^ ((value == null) ? 0 : value.hashCode());
        }

        @Override public String toString ()
        {
            return key + "=" + StringUtil.toString(value);
        }

        @Override public Record<V> clone ()
        {
            try {
                @SuppressWarnings("unchecked")
                Record<V> result = (Record<V>) super.clone();
                // value is not cloned
                if (result.next != null) {
                    result.next = result.next.clone();
                }
                return result;

            } catch (CloneNotSupportedException cnse) {
                throw new AssertionError(cnse); // won't happen; we are Cloneable.
            }
        }
    }

    protected Record<V>[] _buckets;
    protected int _size;
    protected float _loadFactor;

    /** A stateless view of our keys, so we re-use it. */
    protected transient volatile IntSet _keySet = null;

    /** Change this if the fields or inheritance hierarchy ever changes
     * (which is extremely unlikely). We override this because I'm tired
     * of serialized crap not working depending on whether I compiled with
     * jikes or javac. */
    private static final long serialVersionUID = 1;
}
