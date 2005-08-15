//
// $Id: HashIntMap.java,v 1.15 2004/03/15 18:13:15 ray Exp $
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001 Michael Bayne
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
public class HashIntMap
    extends AbstractMap implements IntMap, Cloneable, Serializable
{
    public interface Entry extends AbstractMap.Entry
    {
        public int getIntKey ();
    }

    /**
     * The default number of buckets to use for the hash table.
     */
    public final static int DEFAULT_BUCKETS = 64;

    /**
     * The default load factor.
     */
    public final static float DEFAULT_LOAD_FACTOR = 1.75f;

    /**
     * The shrink factor.
     * When the number of elements multiplied by this number is less
     * than the size of the array, we shrink the array by half.
     */
    protected static final int SHRINK_FACTOR = 8;

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

        _buckets = new Record[capacity];
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

    // documentation inherited
    public int size ()
    {
        return _size;
    }

    // documentation inherited
    public boolean containsKey (Object key)
    {
        return containsKey(((Integer)key).intValue());
    }

    // documentation inherited
    public boolean containsKey (int key)
    {
        return get(key) != null;
    }

    // documentation inherited
    public boolean containsValue (Object o)
    {
        for (int i = 0; i < _buckets.length; i++) {
            for (Record r = _buckets[i]; r != null; r = r.next) {
                if (ObjectUtil.equals(r.value, o)) {
                    return true;
                }
            }
        }
        return false;
    }

    // documentation inherited
    public Object get (Object key)
    {
        return get(((Integer)key).intValue());
    }

    // documentation inherited
    public Object get (int key)
    {
        int index = keyToIndex(key);
        for (Record rec = _buckets[index]; rec != null; rec = rec.next) {
            if (rec.key == key) {
                return rec.value;
            }
        }
        return null;
    }

    // documentation inherited
    public Object put (Object key, Object value)
    {
        return put(((Integer)key).intValue(), value);
    }

    // documentation inherited
    public Object put (int key, Object value)
    {
        // check to see if we've passed our load factor, if so: resize
        checkGrow();

        int index = keyToIndex(key);
        Record rec = _buckets[index];

        // either we start a new chain
        if (rec == null) {
            _buckets[index] = new Record(key, value);
            _size++; // we're bigger
            return null;
        }

        // or we replace an element in an existing chain
        Record prev = rec;
        for (; rec != null; rec = rec.next) {
            if (rec.key == key) {
                Object ovalue = rec.value;
                rec.value = value; // we're not bigger
                return ovalue;
            }
            prev = rec;
        }

        // or we append it to this chain
        prev.next = new Record(key, value);
        _size++; // we're bigger
        return null;
    }

    // documentation inherited
    public Object remove (Object key)
    {
        return remove(((Integer)key).intValue());
    }
                
    // documentation inherited
    public Object remove (int key)
    {
        Object removed = removeImpl(key);
        if (removed != null) {
            checkShrink();
        }
        return removed;
    }

    /**
     * Remove an element with no checking to see if we should shrink.
     */
    protected Object removeImpl (int key)
    {
        int index = keyToIndex(key);
        Record prev = null;

        // go through the chain looking for a match
        for (Record rec = _buckets[index]; rec != null; rec = rec.next) {
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
    public void clear ()
    {
        // abandon all of our hash chains (the joy of garbage collection)
        for (int i = 0; i < _buckets.length; i++) {
            _buckets[i] = null;
        }
        // zero out our size
        _size = 0;
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
            (_size * SHRINK_FACTOR < _buckets.length)) {
            resizeBuckets(_buckets.length >> 1);
        }
    }

    /**
     * Check to see if we want to grow the table.
     */
    protected void checkGrow ()
    {
        if (_size > (int) (_buckets.length * _loadFactor)) {
            resizeBuckets(_buckets.length << 1);
        }
    }

    /**
     * Resize the hashtable.
     *
     * @param newsize MUST be a power of 2.
     */
    protected void resizeBuckets (int newsize)
    {
        Record[] oldbuckets = _buckets;
        _buckets = new Record[newsize];

        // we shuffle the records around without allocating new ones
        int index = oldbuckets.length;
        while (index-- > 0) {
            Record oldrec = oldbuckets[index];
            while (oldrec != null) {
                Record newrec = oldrec;
                oldrec = oldrec.next;

                // always put the newrec at the start of a chain
                int newdex = keyToIndex(newrec.key);
                newrec.next = _buckets[newdex];
                _buckets[newdex] = newrec;
            }
        }
    }

    // documentation inherited
    public Set entrySet ()
    {
        return new AbstractSet() {
            public int size ()
            {
                return _size;
            }

            public Iterator iterator ()
            {
                return new EntryIterator();
            }
        };
    }

    protected class EntryIterator implements Iterator
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

        public Object next ()
        {
            // if we're not pointing to an entry, search for the next
            // non-empty hash chain
            if (_record == null) {
                while ((_index-- > 0) &&
                       ((_record = _buckets[_index]) == null));
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
        protected Record _record, _last;
    }

    /**
     * Returns an iteration over the keys of this hash int map. The keys
     * are returned as <code>Integer</code> objects.
     */
    public Iterator keys ()
    {
        return keySet().iterator();
    }

    /**
     * Returns an iteration over the elements (values) of this hash int
     * map.
     */
    public Iterator elements ()
    {
        return values().iterator();
    }

    // documentation inherited from interface cloneable
    public Object clone ()
    {
        HashIntMap copy = new HashIntMap(_buckets.length, _loadFactor);

        for (Iterator itr = new EntryIterator(); itr.hasNext(); ) {
            Entry entry = (Entry) itr.next();
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
        for (Iterator i = entrySet().iterator(); i.hasNext(); ) {
            Entry e = (Entry)i.next();
            s.writeInt(e.getIntKey());
            s.writeObject(e.getValue());
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
        _buckets = new Record[s.readInt()];
        _loadFactor = s.readFloat();

        // read in size (number of mappings)
        int size = s.readInt();

        // read the keys and values
        for (int i=0; i<size; i++) {
            int key = s.readInt();
            Object value = s.readObject();
            put(key, value);
        }
    }

    protected static class Record implements Entry
    {
        public Record next;
        public int key;
        public Object value;

        public Record (int key, Object value)
        {
            this.key = key;
            this.value = value;
        }

        public Object getKey ()
        {
            return new Integer(key);
        }

        public int getIntKey ()
        {
            return key;
        }

        public Object getValue ()
        {
            return value;
        }

        public Object setValue (Object value)
        {
            Object ovalue = this.value;
            this.value = value;
            return ovalue;
        }

        public boolean equals (Object o)
        {
            if (o instanceof Record) {
                Record or = (Record)o;
                return (key == or.key) && ObjectUtil.equals(value, or.value);
            } else {
                return false;
            }
        }

        public int hashCode ()
        {
            return key ^ ((value == null) ? 0 : value.hashCode());
        }

        public String toString ()
        {
            return key + "=" + StringUtil.toString(value);
        }
    }

    protected Record[] _buckets;
    protected int _size;
    protected float _loadFactor;

    /** Change this if the fields or inheritance hierarchy ever changes
     * (which is extremely unlikely). We override this because I'm tired
     * of serialized crap not working depending on whether I compiled with
     * jikes or javac. */
    private static final long serialVersionUID = 1;
}
