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

import java.util.Iterator;
import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;

/**
 * An int int map is like an int map, but with integers as values as well
 * as keys. Note that in situations where null would normally be returned
 * to indicate no value, -1 is returned instead. This means that you must
 * be careful if you intend to store -1 as a valid value in the table.
 */
public class IntIntMap
{
    public final static int DEFAULT_BUCKETS = 64;

    public IntIntMap (int buckets)
    {
	_buckets = new Record[buckets];
    }

    public IntIntMap ()
    {
	this(DEFAULT_BUCKETS);
    }

    public int size ()
    {
	return _size;
    }

    public void put (int key, int value)
    {
        _modCount++;

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
     */
    public void increment (int key, int amount)
    {
        Record rec = locateRecord(key);
        if (rec == null) {
            put(key, amount);
        } else {
            rec.value += amount;
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
        return removeImpl(key);
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

    public Iterator keys ()
    {
	return new Interator(true);
    }

    public Iterator values ()
    {
	return new Interator(false);
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

    class Record
    {
	public Record next;
	public int key;
	public int value;

	public Record (int key, int value)
	{
	    this.key = key;
	    this.value = value;
	}
    }

    class Interator implements Iterator
    {
	public Interator (boolean returnKeys)
	{
            this._modCount = IntIntMap.this._modCount;
	    _index = _buckets.length;
	    _returnKeys = returnKeys;
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

	public Object next ()
	{
	    // if we're not pointing to an entry, search for the next
	    // non-empty hash chain
            if (hasNext()) {
                _prev = _next;
                _next = _next.next;
                return new Integer(_returnKeys ? _prev.key : _prev.value);

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
	private boolean _returnKeys;
        private int _modCount;
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

    protected int _modCount = 0;
}
