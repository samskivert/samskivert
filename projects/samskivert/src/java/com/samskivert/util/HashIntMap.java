//
// $Id: HashIntMap.java,v 1.4 2001/09/15 17:22:11 mdb Exp $
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

import java.util.*;

/**
 * An int map is like a regular map, but with integers as keys. We avoid
 * the annoyance of having to create integer objects every time we want to
 * lookup or insert values. The hash int map is an int map that uses a
 * hashtable mechanism to store its key/value mappings.
 */
public class HashIntMap
    extends AbstractMap implements IntMap
{
    /**
     * The default number of buckets to use for the hash table.
     */
    public final static int DEFAULT_BUCKETS = 64;

    /**
     * Constructs an empty hash int map with the specified number of hash
     * buckets.
     */
    public HashIntMap (int buckets)
    {
	_buckets = new Record[buckets];
    }

    /**
     * Constructs an empty hash int map with the default number of hash
     * buckets.
     */
    public HashIntMap ()
    {
	this(DEFAULT_BUCKETS);
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
                if (r.value.equals(o)) {
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
	int index = Math.abs(key) % _buckets.length;
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
        // disallow null values
        if (value == null) {
            throw new IllegalArgumentException();
        }

	int index = Math.abs(key)%_buckets.length;
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
	int index = Math.abs(key) % _buckets.length;
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
            HashIntMap.this.remove(_last.key);
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
                return (key == or.key) && value.equals(or.value);
            } else {
                return false;
            }
        }

	public int hashCode ()
        {
            return key ^ ((value == null) ? 0 : value.hashCode());
        }
    }

   public static void main (String[] args)
   {
       HashIntMap table = new HashIntMap();

       System.out.print("Inserting: ");
       for (int i = 10; i < 100; i++) {
           Integer value = new Integer(i);
           table.put(i, value);
           System.out.print("(" + i + "," + value + ")");
       }
       System.out.println("");

       System.out.print("Looking up: ");
       for (int i = 10; i < 100; i++) {
           System.out.print("(" + i + "," + table.get(i) + ")");
       }
       System.out.println("");

       System.out.println("Keys: " +
                          StringUtil.toString(table.keys()));
       System.out.println("Elems: " +
                          StringUtil.toString(table.elements()));

       System.out.print("Removing: ");
       for (int i = 10; i < 98; i++) {
           System.out.print("(" + i + "," + table.remove(i) + ")");
       }
       System.out.println("");

       System.out.println("Keys: " +
                          StringUtil.toString(table.keys()));
       System.out.println("Elems: " +
                          StringUtil.toString(table.elements()));
   }

    private Record[] _buckets;
    private int _size;
}
