//
// $Id: IntIntMap.java,v 1.7 2003/06/25 22:03:55 mdb Exp $
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

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * An int int map is like an int map, but with integers as values as well
 * as keys. Note that in situations where null would normally be returned
 * to indicate no value, -1 is returned instead. This means that you must
 * be careful if you intend to store -1 as a valid value in the table.
 */
// 
// TODO: make this a proper map, perhaps even an extension of HashIntMap
// or at least share a common abstract ancestor.
public class IntIntMap
{
    public interface IntIntEntry extends IntMap.IntEntry<Integer>
    {
        public int getIntValue ();

        public int setIntValue (int value);
    }

    public final static int DEFAULT_BUCKETS = 16;

    /**
     * The default load factor.
     */
    public final static float DEFAULT_LOAD_FACTOR = 1.75f;


    public IntIntMap (int buckets, float loadFactor)
    {
	_buckets = new Record[buckets];
        _loadFactor = loadFactor;
    }

    public IntIntMap (int buckets)
    {
        this(buckets, DEFAULT_LOAD_FACTOR);
    }

    public IntIntMap ()
    {
	this(DEFAULT_BUCKETS, DEFAULT_LOAD_FACTOR);
    }

    public int size ()
    {
	return _size;
    }

    public void put (int key, int value)
    {
        _modCount++;

        // check to see if we've passed our load factor, if so: resize
        ensureCapacity(_size + 1);

        int index = Math.abs(key)%_buckets.length;
	Record rec = _buckets[index];

	// either we start a new chain
	if (rec == null) {
	    _buckets[index] = new Record(key, value);
	    _size++; // we're bigger
	    return;
	}

	// or we replace an element in an existing chain
	Record prev = rec;
	for (; rec != null; rec = rec.next) {
	    if (rec.key == key) {
		rec.value = value; // we're not bigger
		return;
	    }
	    prev = rec;
	}

	// or we append it to this chain
	prev.next = new Record(key, value);
	_size++; // we're bigger
    }

    public int get (int key)
    {
        Record rec = locateRecord(key);
        return (rec == null) ? -1 : rec.value;
    }

    /**
     * Increments the value associated with the specified key by the
     * specified amount. If the key has no previously assigned value, it
     * will be set to the amount specified (as if incrementing from zero).
     *
     * @return the incremented value now stored for the key
     */
    public int increment (int key, int amount)
    {
        Record rec = locateRecord(key);
        if (rec == null) {
            put(key, amount);
            return amount;
        } else {
            return (rec.value += amount);
        }
    }

    public boolean contains (int key)
    {
        return (null != locateRecord(key));
    }

    /**
     * Internal method to locate the record for the specified key.
     */
    protected Record locateRecord (int key)
    {
	int index = Math.abs(key)%_buckets.length;
	for (Record rec = _buckets[index]; rec != null; rec = rec.next) {
	    if (rec.key == key) {
                return rec;
	    }
	}
        return null;
    }

    public int remove (int key)
    {
        _modCount++;
        int removed = removeImpl(key);
        checkShrink();
        return removed;
    }

    protected int removeImpl (int key)
    {
	int index = Math.abs(key)%_buckets.length;
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

        return -1; // not found
    }

    public void clear ()
    {
        _modCount++;

	// abandon all of our hash chains (the joy of garbage collection)
	for (int i = 0; i < _buckets.length; i++) {
	    _buckets[i] = null;
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
     * @param newsize The new number of buckets to allocate.
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
                int newdex = Math.abs(newrec.key)%_buckets.length;
                newrec.next = _buckets[newdex];
                _buckets[newdex] = newrec;
            }
        }
    }

    public Interator keys ()
    {
        return new KeyValueInterator(true, new IntEntryIterator());
    }

    public Interator values ()
    {
        return new KeyValueInterator(false, new IntEntryIterator());
    }

    /**
     * Get an array of the unique keys in this map.
     */
    public int[] getKeys ()
    {
        return toIntArray(true);
    }

    /**
     * Get an array of the values that may be in this map.
     * There may be duplicates.
     */
    public int[] getValues ()
    {
        return toIntArray(false);
    }

    /**
     * Returns a string representation of this instance.
     */
    public String toString ()
    {
        StringBuffer buf = new StringBuffer("[");
        int[] keys = getKeys();
        for (int ii = 0; ii < keys.length; ii++) {
            if (ii > 0) {
                buf.append(", ");
            }
            buf.append(keys[ii]).append("->").append(get(keys[ii]));
        }
        return buf.append("]").toString();
    }

    protected int[] toIntArray (boolean keys)
    {
        int[] ret = new int[_size];

        int dex = 0;
        for (int ii=0, nn=_buckets.length; ii < nn; ii++) {
            Record r = _buckets[ii];
            while (r != null) {
                ret[dex++] = keys ? r.key : r.value;
                r = r.next;
            }
        }
        return ret;
    }

    /**
     * Get a set of all the entries in this map.
     */
    public Set<IntIntEntry> entrySet ()
    {
        return new AbstractSet<IntIntEntry>() {
            public int size () {
                return _size;
            }

            public Iterator<IntIntEntry> iterator() {
                return new IntEntryIterator();
            }
        };
    }

    class Record implements IntIntEntry
    {
	public Record next;
	public int key;
	public int value;

	public Record (int key, int value)
	{
	    this.key = key;
	    this.value = value;
	}

        public Integer getKey () {
            return Integer.valueOf(key);
        }

        public int getIntKey () {
            return key;
        }

        public Integer getValue () {
            return Integer.valueOf(value);
        }

        public int getIntValue () {
            return value;
        }

        public Integer setValue (Integer v) {
            return Integer.valueOf(setIntValue(v.intValue()));
        }

        public int setIntValue (int v) {
            int oldVal = value;
            value = v;
            return oldVal;
        }

        public boolean equals (Object o) {
            if (o instanceof IntIntEntry) {
                IntIntEntry that = (IntIntEntry) o;
                return (this.key == that.getIntKey()) &&
                    (this.value == that.getIntValue());
            }
            return false;
        }

        public int hashCode () {
            return key;
        }
    }

    class IntEntryIterator implements Iterator<IntIntEntry>
    {
	public IntEntryIterator ()
	{
            this._modCount = IntIntMap.this._modCount;
	    _index = _buckets.length;
	}

        /**
         * Check for concurrent modifications.
         */
        protected void checkMods ()
        {
            if (this._modCount != IntIntMap.this._modCount) {
                throw new ConcurrentModificationException("IntIntMapIterator");
            }
        }

	public boolean hasNext ()
	{
            checkMods();

	    // if we're pointing to an entry, we've got more entries
	    if (_next != null) {
		return true;
	    }

	    // search backward through the buckets looking for the next
	    // non-empty hash chain
	    while (_index-- > 0) {
		if ((_next = _buckets[_index]) != null) {
		    return true;
		}
	    }

	    // found no non-empty hash chains, we're done
	    return false;
	}

	public IntIntEntry next ()
	{
	    // if we're not pointing to an entry, search for the next
	    // non-empty hash chain
            if (hasNext()) {
                _prev = _next;
                _next = _next.next;
                return _prev;

            } else {
                throw new NoSuchElementException("IntIntMapIterator");
            }
        }

        public void remove ()
        {
            checkMods();

            if (_prev == null) {
                throw new IllegalStateException("IntIntMapIterator");
            }

            // otherwise remove the hard way
            removeImpl(_prev.key);
            _prev = null;
        }

	private int _index;
	private Record _next, _prev;
        private int _modCount;
    }

    protected class KeyValueInterator
        implements Interator
    {
        public KeyValueInterator (boolean keys,  IntEntryIterator eiter)
        {
            _keys = keys;
            _eiter = eiter;
        }

        public int nextInt ()
        {
            IntIntEntry entry = _eiter.next();
            return _keys ? entry.getIntKey() : entry.getIntValue();
        }

        public boolean hasNext ()
        {
            return _eiter.hasNext();
        }

        public Integer next ()
        {
            return Integer.valueOf(nextInt());
        }

        public void remove ()
        {
            _eiter.remove();
        }

        protected boolean _keys;
        protected IntEntryIterator _eiter;
    }

//     public static void main (String[] args)
//     {
//         IntIntMap table = new IntIntMap();

//         System.out.print("Inserting: ");
//         for (int i = 10; i < 100; i++) {
//             table.put(i, i);
//             System.out.print("(" + i + "," + i + ")");
//         }
//         System.out.println("");

//         System.out.print("Looking up: ");
//         for (int i = 10; i < 100; i++) {
//             System.out.print("(" + i + "," + table.get(i) + ")");
//         }
//         System.out.println("");

//         System.out.print("Removing: ");
//         for (int i = 10; i < 100; i++) {
//             System.out.print("(" + i + "," + table.remove(i) + ")");
//         }
//         System.out.println("");
//     }

    private Record[] _buckets;
    private int _size;

    protected float _loadFactor;

    protected int _modCount = 0;
}
