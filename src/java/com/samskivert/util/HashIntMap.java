//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2007 Michael Bayne
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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * An int map is like a regular map, but with integers as keys. We avoid
 * the annoyance of having to create integer objects every time we want to
 * lookup or insert values. The hash int map is an int map that uses a
 * hashtable mechanism to store its key/value mappings.
 */
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
        return containsKey(((Integer)key).intValue());
    }

    // documentation inherited
    public boolean containsKey (int key)
    {
        return get(key) != null;
    }

    @Override
    public boolean containsValue (Object o)
    {
        for (int ii = 0, ll = _buckets.length; ii < ll; ii++) {
            for (Record<V> r = _buckets[ii]; r != null; r = r.next) {
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
        return get(((Integer)key).intValue());
    }

    // documentation inherited
    public V get (int key)
    {
        int index = keyToIndex(key);
        for (Record<V> rec = _buckets[index]; rec != null; rec = rec.next) {
            if (rec.key == key) {
                return rec.value;
            }
        }
        return null;
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
        return remove(((Integer)key).intValue());
    }

    // documentation inherited
    public V remove (int key)
    {
        V removed = removeImpl(key);
        if (removed != null) {
            checkShrink();
        }
        return removed;
    }

    /**
     * Remove an element with no checking to see if we should shrink.
     */
    protected V removeImpl (int key)
    {
        int index = keyToIndex(key);
        Record<V> prev = null;

        // go through the chain looking for a match
        for (Record<V> rec = _buckets[index]; rec != null; rec = rec.next) {
            if (rec.key == key) {
                if (prev == null) {
                    _buckets[index] = rec.next;
                } else {
                    prev.next = rec.next;
                }
                _size--;
                return rec.value;
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
        for (int ii = _buckets.length - 1; ii >= 0; ii--) {
            _buckets[ii] = null;
        }
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

    protected abstract class RecordIterator
    {
        public boolean hasNext ()
        {
            // if we're pointing to an entry, we've got more entries
            if (_record != null) {
                return true;
            }

            // search backward through the buckets looking for the next
            // non-empty hash chain
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
                while ((_index-- > 0) && ((_record = _buckets[_index]) == null)) { /* loop! */ }
            }

            // keep track of the last thing we returned
            _last = _record;

            // if we found a record, return it's value and move our record
            // reference to it's successor
            if (_record != null) {
                _record = _last.next;
                return _last;
            }

            throw new NoSuchElementException();
        }

        public void remove ()
        {
            if (_last == null) {
                throw new IllegalStateException();
            }

            // remove the record the hard way
            HashIntMap.this.removeImpl(_last.key);
            _last = null;
        }

        protected int _index = _buckets.length;
        protected Record<V> _record, _last;
    }

    protected class IntEntryIterator extends RecordIterator
        implements Iterator<IntEntry<V>>
    {
        public IntEntry<V> next () {
            return nextRecord();
        }
    }

    protected class MapEntryIterator extends RecordIterator
        implements Iterator<Entry<Integer,V>>
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
                @Override public Interator interator () {
                    return new Interator () {
                        public boolean hasNext () {
                            return i.hasNext();
                        }
                        public Integer next () {
                            return i.next().getKey();
                        }
                        public int nextInt () {
                            return i.next().getIntKey();
                        }
                        public void remove () {
                            i.remove();
                        }
                        private Iterator<IntEntry<V>> i = intEntrySet().iterator();
                    };
                }

                @Override public int size () {
                    return HashIntMap.this.size();
                }

                @Override public boolean contains (Object t) {
                    return HashIntMap.this.containsKey(t);
                }

                public boolean contains (int t) {
                    return HashIntMap.this.containsKey(t);
                }

                @Override public boolean remove (Object o) {
                    return (null != HashIntMap.this.remove(o));
                }

                public boolean remove (int value) {
                    return (null != HashIntMap.this.remove(value));
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
    public Object clone ()
    {
        HashIntMap<V> copy = new HashIntMap<V>(_buckets.length, _loadFactor);
        for (IntEntry<V> entry : intEntrySet()) {
            copy.put(entry.getIntKey(), entry.getValue());
        }
        return copy;
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
        @SuppressWarnings("unchecked") Record<V>[] recs = new Record[size];
        return recs;
    }

    protected static class Record<V> implements Entry<Integer,V>, IntEntry<V>
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
            if (!(o instanceof IntEntry<?>)) {
                return false;
            }
            IntEntry<?> that = (IntEntry<?>)o;
            return (this.key == that.getIntKey()) &&
                ObjectUtil.equals(this.value, that.getValue());
        }

        @Override public int hashCode ()
        {
            return key ^ ((value == null) ? 0 : value.hashCode());
        }

        @Override public String toString ()
        {
            return key + "=" + StringUtil.toString(value);
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
