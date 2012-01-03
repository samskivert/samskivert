//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;

/**
 * A set of integers that uses hashing with linear probing to provide most of the memory usage
 * and garbage creation benefits of {@link ArrayIntSet} and the performance benefits of
 * a {@link java.util.HashSet} of {@link Integer}s (and then some, because of better spatial
 * locality).  The downside is that it requires a sentinel value ({@link Integer#MIN_VALUE} by
 * default) that cannot be stored in the set because it is used internally to represent unused
 * locations.
 */
public class HashIntSet extends AbstractIntSet
    implements Cloneable, Serializable
{
    /**
     * Construct a HashIntSet with the specified starting values.
     */
    public HashIntSet (int[] values)
    {
        this(values.length);
        add(values);
    }

    /**
     * Construct a HashIntSet with the specified starting values.
     *
     * @throws NullPointerException if the collection contains any null values.
     */
    public HashIntSet (Collection<Integer> values)
    {
        this(values.size());
        addAll(values);
    }

    /**
     * Creates a new set with the default capacity.
     */
    public HashIntSet ()
    {
        this(DEFAULT_CAPACITY);
    }

    /**
     * Creates a new set with the specified initial capacity.
     */
    public HashIntSet (int capacity)
    {
        this(capacity, Integer.MIN_VALUE);
    }

    /**
     * Creates a new set with the specified initial capacity and sentinel value.
     */
    public HashIntSet (int capacity, int sentinel)
    {
        _sentinel = sentinel;
        createBuckets(getBucketCount(capacity));
    }

    /**
     * Sets the sentinel value, which cannot itself be stored in the set because it is used
     * internally to represent an unused location.
     *
     * @exception IllegalArgumentException if the set currently contains the requested sentinel.
     */
    public void setSentinel (int sentinel)
    {
        if (_sentinel == sentinel) {
            return;
        }
        if (contains(sentinel)) {
            throw new IllegalArgumentException("Set contains sentinel value " + sentinel);
        }
        // replace every instance of the old sentinel with the new
        for (int ii = 0; ii < _buckets.length; ii++) {
            if (_buckets[ii] == _sentinel) {
                _buckets[ii] = sentinel;
            }
        }
        _sentinel = sentinel;
    }

    /**
     * Returns the sentinel value.
     */
    public int getSentinel ()
    {
        return _sentinel;
    }

    // documentation inherited from interface IntSet
    public Interator interator ()
    {
        return new AbstractInterator() {
            public boolean hasNext () {
                checkConcurrentModification();
                return _pos < _size;
            }
            public int nextInt () {
                checkConcurrentModification();
                if (_pos >= _size) {
                    throw new NoSuchElementException();
                }
                if (_idx == 0) {
                    // start after a sentinel.  if we don't and instead start in the middle of a
                    // run of filled buckets, we risk returning values that will reappear at the
                    // end of the list after being shifted over to due to a removal
                    while (_buckets[_idx++] != _sentinel);
                }
                int mask = _buckets.length - 1;
                for (; _pos < _size; _idx++) {
                    int value = _buckets[_idx & mask];
                    if (value != _sentinel) {
                        _pos++;
                        _idx++;
                        return value;
                    }
                }
                // we shouldn't get here
                throw new RuntimeException("Ran out of elements getting next");
            }
            @Override public void remove () {
                checkConcurrentModification();
                if (_idx == 0) {
                    throw new IllegalStateException("Next method not yet called");
                }
                int pidx = (--_idx) & (_buckets.length - 1);
                if (_buckets[pidx] == _sentinel) {
                    throw new IllegalStateException("No element to remove");
                }
                _buckets[pidx] = _sentinel;
                _pos--;
                _size--;
                _omodcount = ++_modcount;
                shift(pidx);
            }
            protected void checkConcurrentModification () {
                if (_modcount != _omodcount) {
                    throw new ConcurrentModificationException();
                }
            }
            protected int _pos, _idx;
            protected int _omodcount = _modcount;
        };
    }

    @Override // documentation inherited
    public boolean contains (int value)
    {
        if (value == _sentinel) {
            return false;
        }
        int mask = _buckets.length - 1;
        int start = hash(value) & mask, idx = start;
        do {
            int bvalue = _buckets[idx];
            if (bvalue == value) {
                return true;
            } else if (bvalue == _sentinel) {
                return false;
            }
        } while ((idx = idx + 1 & mask) != start);

        // we shouldn't get here
        throw new RuntimeException("Ran out of buckets looking for value " + value);
    }

    @Override // documentation inherited
    public int size ()
    {
        return _size;
    }

    @Override // documentation inherited
    public boolean isEmpty ()
    {
        return _size == 0;
    }

    @Override // documentation inherited
    public boolean add (int value)
    {
        if (value == _sentinel) {
            throw new IllegalArgumentException("Can't add sentinel value " + value);
        }
        int mask = _buckets.length - 1;
        int start = hash(value) & mask, idx = start;
        do {
            int bvalue = _buckets[idx];
            if (bvalue == value) {
                return false;

            } else if (bvalue == _sentinel) {
                _buckets[idx] = value;
                _size++;
                _modcount++;

                // if necessary to preserve our maximum load factor, increase the bucket count
                int ncount = getBucketCount(_size, MAX_LOAD_FACTOR);
                if (ncount > _buckets.length) {
                    rehash(ncount);
                }
                return true;
            }
        } while ((idx = idx + 1 & mask) != start);

        // we shouldn't get here
        throw new RuntimeException("Ran out of buckets adding value " + value);
    }

    @Override // documentation inherited
    public boolean remove (int value)
    {
        if (value == _sentinel) {
            return false;
        }
        int mask = _buckets.length - 1;
        int start = hash(value) & mask, idx = start;
        do {
            int bvalue = _buckets[idx];
            if (bvalue == value) {
                _buckets[idx] = _sentinel;
                _size--;
                _modcount++;

                // if necessary to preserve our minimum load factor, decrease the bucket count;
                // otherwise, we must shift elements over to fill the newly emptied bucket
                int ncount = getBucketCount(_size, MIN_LOAD_FACTOR);
                if (ncount < _buckets.length) {
                    rehash(ncount);
                } else {
                    shift(idx);
                }
                return true;

            } else if (bvalue == _sentinel) {
                return false;
            }
        } while ((idx = idx + 1 & mask) != start);

        // we shouldn't get here
        throw new RuntimeException("Ran out of buckets removing value " + value);
    }

    @Override // documentation inherited
    public void clear ()
    {
        if (_size > 0) {
            createBuckets(MIN_BUCKET_COUNT);
            _size = 0;
            _modcount++;
        }
    }

    @Override // documentation inherited
    public HashIntSet clone ()
    {
        try {
            HashIntSet nset = (HashIntSet)super.clone();
            nset._buckets = _buckets.clone();
            return nset;

        } catch (CloneNotSupportedException cnse) {
            throw new AssertionError(cnse); // won't happen; we're Cloneable
        }
    }

    /**
     * Recreates the bucket array with the specified new count.
     */
    protected void rehash (int ncount)
    {
        int[] obuckets = _buckets;
        createBuckets(ncount);

        for (int idx = 0, pos = 0; pos < _size; idx++) {
            int value = obuckets[idx];
            if (value != _sentinel) {
                readd(value);
                pos++;
            }
        }
    }

    /**
     * (Re)creates and initializes the bucket array.
     */
    protected void createBuckets (int count)
    {
        Arrays.fill(_buckets = new int[count], _sentinel);
    }

    /**
     * Adds a value that we know is neither equal to the sentinel nor already in the set.
     */
    protected void readd (int value)
    {
        int mask = _buckets.length - 1;
        int start = hash(value) & mask, idx = start;
        do {
            if (_buckets[idx] == _sentinel) {
                _buckets[idx] = value;
                return;
            }
        } while ((idx = idx + 1 & mask) != start);

        // we shouldn't get here
        throw new RuntimeException("Ran out of buckets readding value " + value);
    }

    /**
     * Shifts elements over to fill a newly empty slot.  Anything between the previous sentinel and
     * the empty slot (which moves as we shift elements), taking wrapping into account, needs to
     * be checked and moved if necessary to ensure that it will be found by a search beginning at
     * its hash-derived bucket index.  We stop when we encounter another sentinel.
     */
    protected void shift (int start)
    {
        // first, scan backwards to find the previous sentinel
        int mask = _buckets.length - 1;
        int sidx = start;
        while ((sidx = sidx + mask & mask) != start) {
            if (_buckets[sidx] == _sentinel) {
                break;
            }
        }

        // then forwards to shift elements into place
        int idx = start, pidx = start;
        while ((idx = idx + 1 & mask) != start) {
            int bvalue = _buckets[idx];
            if (bvalue == _sentinel) {
                _buckets[pidx] = _sentinel;
                return;
            }
            int bidx = hash(bvalue) & mask;
            if (pidx > sidx ? (bidx > sidx && bidx <= pidx) : (bidx > sidx || bidx <= pidx)) {
                _buckets[pidx] = bvalue;
                pidx = idx;
            }
        }

        // we shouldn't get here
        throw new RuntimeException("Ran out of buckets fixing empty location at " + start);
    }

    /**
     * Custom serializer.
     */
    private void writeObject (ObjectOutputStream out)
        throws IOException
    {
        out.defaultWriteObject();
        for (int idx = 0, pos = 0; pos < _size; idx++) {
            int value = _buckets[idx];
            if (value != _sentinel) {
                out.writeInt(value);
                pos++;
            }
        }
    }

    /**
     * Custom deserializer.
     */
    private void readObject (ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        createBuckets(getBucketCount(_size));
        for (int ii = 0; ii < _size; ii++) {
            readd(in.readInt());
        }
    }

    /**
     * Returns the hash of the specified value.  This is copied straight from
     * the source of {@link java.util.HashMap} and ensures that we don't rely
     * solely on the bits of lower significance.
     */
    protected static int hash (int h)
    {
        h ^= (h >>> 20) ^ (h >>> 12);
        return h ^ (h >>> 7) ^ (h >>> 4);
    }

    /**
     * Computes the number of buckets needed to provide the given capacity with a load factor
     * halfway between the minimum and the maximum.
     */
    protected static int getBucketCount (int capacity)
    {
        return getBucketCount(capacity, (MIN_LOAD_FACTOR + MAX_LOAD_FACTOR) * 0.5f);
    }

    /**
     * Computes the number of buckets needed to provide the given capacity with the specified load
     * factor.
     */
    protected static int getBucketCount (int capacity, float loadFactor)
    {
        int size = (int)(capacity / loadFactor);
        int highest = Integer.highestOneBit(size);
        return Math.max((size == highest) ? highest : (highest << 1), MIN_BUCKET_COUNT);
    }

    /** The buckets containing the contents of the set. */
    protected transient int[] _buckets;

    /** The number of elements in the set. */
    protected int _size;

    /** The value that indicates an empty location in the contents. */
    protected int _sentinel;

    /** Incremented on each set modification, used to track concurrent changes. */
    protected transient int _modcount;

    /** The default initial capacity of this set. */
    protected static final int DEFAULT_CAPACITY = 16;

    /** The minimum number of buckets to provide. */
    protected static final int MIN_BUCKET_COUNT = 8;

    /** The maximum load factor (ratio of size to length of contents array). */
    protected static final float MAX_LOAD_FACTOR = 0.7f;

    /** The minimum load factor. */
    protected static final float MIN_LOAD_FACTOR = 0.3f;

    /** Change this if the fields or inheritance hierarchy ever changes (extremely unlikely). */
    private static final long serialVersionUID = 1L;
}
