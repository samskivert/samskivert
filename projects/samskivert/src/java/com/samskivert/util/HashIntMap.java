//
// $Id: HashIntMap.java,v 1.3 2001/08/11 22:43:29 mdb Exp $
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

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * An int map is like a hashmap, but with integers as keys. We avoid the
 * annoyance of having to create integer objects every time we want to
 * lookup or insert values.
 */
public class IntMap
{
    public final static int DEFAULT_BUCKETS = 64;

    public IntMap (int buckets)
    {
	_buckets = new Record[buckets];
    }

    public IntMap ()
    {
	this(DEFAULT_BUCKETS);
    }

    public synchronized int size ()
    {
	return _size;
    }

    public synchronized void put (int key, Object value)
    {
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

    public synchronized Object get (int key)
    {
	int index = Math.abs(key)%_buckets.length;
	for (Record rec = _buckets[index]; rec != null; rec = rec.next) {
	    if (rec.key == key) {
		return rec.value;
	    }
	}
	return null;
    }

    public boolean contains (int key)
    {
	return get(key) != null;
    }

    public synchronized Object remove (int key)
    {
	int index = Math.abs(key)%_buckets.length;
	Record rec = _buckets[index];

	// if there's no chain, there's no object
	if (rec == null) {
	    return null;
	}

	// maybe it's the first one in this chain
	if (rec.key == key) {
	    _buckets[index] = rec.next;
	    _size--;
	    return rec.value;
	}

	// or maybe it's an element further down the chain
	for (Record prev = rec; rec != null; rec = rec.next) {
	    if (rec.key == key) {
		prev.next = rec.next;
		_size--;
		return rec.value;
	    }
	    prev = rec;
	}

	return null;
    }

    public synchronized void clear ()
    {
	// abandon all of our hash chains (the joy of garbage collection)
	for (int i = 0; i < _buckets.length; i++) {
	    _buckets[i] = null;
	}
	// zero out our size
	_size = 0;
    }

    public Enumeration keys ()
    {
	return new Enumerator(_buckets, true);
    }

    public Enumeration elements ()
    {
	return new Enumerator(_buckets, false);
    }

    protected static class Record
    {
	public Record next;
	public int key;
	public Object value;

	public Record (int key, Object value)
	{
	    this.key = key;
	    this.value = value;
	}
    }

    class Enumerator implements Enumeration
    {
	public Enumerator (Record[] buckets, boolean returnKeys)
	{
	    _buckets = buckets;
	    _index = buckets.length;
	    _returnKeys = returnKeys;
	}

	public boolean hasMoreElements ()
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

	public Object nextElement ()
	{
	    // if we're not pointing to an entry, search for the next
	    // non-empty hash chain
	    if (_record == null) {
		while ((_index-- > 0) &&
		       ((_record = _buckets[_index]) == null));
	    }

	    // if we found a record, return it's value and move our record
	    // reference to it's successor
	    if (_record != null) {
		Record r = _record;
		_record = r.next;
		return _returnKeys ? new Integer(r.key) : r.value;
	    }

	    throw new NoSuchElementException("IntMapEnumerator");
	}

	private int _index;
	private Record _record;
	private Record[] _buckets;
	private boolean _returnKeys;
    }

//     public static void main (String[] args)
//     {
//         IntMap table = new IntMap();

//         System.out.print("Inserting: ");
//         for (int i = 10; i < 100; i++) {
//             Integer value = new Integer(i);
//             table.put(i, value);
//             System.out.print("(" + i + "," + value + ")");
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
}
