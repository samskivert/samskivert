//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.swing.util;

import com.samskivert.util.StringUtil;

/**
 * A mechanism for registering a set of objects with x and y screen
 * coordinates and then efficiently determining which of those objects is
 * closest to a given screen coordinate. Useful for highlighting the
 * object closest to the mouse and that sort of thing. The data structure
 * and algorithm for tracking and looking up the closest object are highly
 * optimized so that one can track large numbers of objects and obtain the
 * most proximous one with frequencies on par with the frequencies of
 * mouse moved events.
 */
public class ProximityTracker
{
    /**
     * Creates a proximity tracker with the default initial capacity.
     */
    public ProximityTracker ()
    {
        this(DEFAULT_INIT_CAPACITY);
    }

    /**
     * Creates a proximity tracker with the specified initial capacity.
     */
    public ProximityTracker (int initialCapacity)
    {
        _records = new Record[initialCapacity];
    }

    /**
     * Adds an object to the tracker.
     */
    public void addObject (int x, int y, Object object)
    {
        Record record = new Record(x, y, object);

        // if this is the very first element, we have to insert it
        // straight away because our binary search algorithm doesn't work
        // on empty arrays
        if (_size == 0) {
            _records[_size++] = record;
            return;
        }

        // figure out where to insert it
        int ipoint = binarySearch(x);

        // expand the records array if necessary
        if (_size >= _records.length) {
            int nsize = _size*2;
            Record[] records = new Record[nsize];
            System.arraycopy(_records, 0, records, 0, _size);
            _records = records;
        }

        // shift everything down
        if (ipoint < _size) {
            System.arraycopy(_records, ipoint, _records, ipoint+1,
                             _size-ipoint);
        }

        // insert the record
        _records[ipoint] = record;
        _size++;
    }

    /**
     * Removes from the tracker the object that is referentially equal to
     * (<code>o1 == object</code>) the specified object.
     *
     * @return true if an object was located and removed, false if not.
     */
    public boolean removeObject (Object object)
    {
        for (int i = 0; i < _size; i++) {
            if (_records[i].object == object) {
                // shift everything down
                System.arraycopy(_records, i+1, _records, i, _size-(i+1));
                // clear out the trailing reference
                _records[--_size] = null;
                return true;
            }
        }

        return false;
    }

    /**
     * Removes from the tracker the object that is equal to
     * (<code>o1.equals(object)</code>) the specified object.
     *
     * @return true if an object was located and removed, false if not.
     */
    public boolean removeObjectEquals (Object object)
    {
        for (int i = 0; i < _size; i++) {
            if (_records[i].object.equals(object)) {
                // shift everything down
                System.arraycopy(_records, i+1, _records, i, _size-(i+1));
                // clear out the trailing reference
                _records[--_size] = null;
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the object nearest to the supplied coordinates. If
     * <code>distance</code> is non-null and at least one element in
     * length, the actual between the supplied coordinates and the object
     * coordinates will be filled into the first element of the array.
     *
     * @return the object nearest the supplied coordinates or null if the
     * tracker contains no objects.
     */
    public Object findClosestObject (int x, int y, int[] distance)
    {
        // make sure we're tracking at least one object
        if (_size == 0) {
            return null;
        }

        // locate the object nearest the x coordinate
        int sr = binarySearch(x), sl = sr-1;
        int mindist = Integer.MAX_VALUE, minidx = -1;

        // we search outward from the nearest x coordinate, looking for
        // the nearest object and refining the bounds of our search each
        // time we find a nearer object
        for (boolean expanded = true; expanded;) {
            expanded = false;

            // look to the right
            if (sr < _size) {
                Record rec = _records[sr];
                // we can stop searching in this direction when the
                // distance in the x direction becomes larger than the
                // known shortest distance
                if (rec.x-x < mindist) {
                    int dist = distance(rec.x, rec.y, x, y);
                    if (dist < mindist) {
                        minidx = sr;
                        mindist = dist;
                    }
                    // move to the next element
                    sr += 1;
                    expanded = true;
                }
            }

            // look to the left
            if (sl >= 0) {
                Record rec = _records[sl];
                // we can stop searching in this direction when the
                // distance in the x direction becomes larger than the
                // known shortest distance
                if (x-rec.x < mindist) {
                    int dist = distance(rec.x, rec.y, x, y);
                    if (dist < mindist) {
                        minidx = sl;
                        mindist = dist;
                    }
                    // move to the next element
                    sl -= 1;
                    expanded = true;
                }
            }
        }

        // as we ensured above that there was at least one element in our
        // array, we are required to have found some element as the
        // closest element to the given point. if we didn't, we're hosed!
        if (minidx == -1) {
            throw new RuntimeException("Proximity algorithm failed!");
        }

        // communicate the minimum distance back to the caller
        if (distance != null && distance.length > 0) {
            distance[0] = mindist;
        }

        return _records[minidx].object;
    }

    @Override
    public String toString ()
    {
        return "[size=" + _size +
            ", elems=" + StringUtil.toString(_records) + "]";
    }

    /**
     * Computes the geometric distance between the supplied two points.
     */
    public static int distance (int x1, int y1, int x2, int y2)
    {
        int dx = x1-x2, dy = y1-y2;
        return (int)Math.sqrt(dx*dx+dy*dy);
    }

    /**
     * Returns the index of a record with the specified x coordinate, or a
     * value representing the index where such a record would be inserted
     * while preserving the sort order of the array. Doesn't work if the
     * records array contains zero elements.
     */
    protected int binarySearch (int x)
    {
        // copied from java.util.Arrays which I wouldn't have to have done had the provided a
        // means by which to binarySearch in a subset of an array. alas.
        int low = 0;
        int high = _size-1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            int cmp = (_records[mid].x - x);

            if (cmp < 0) {
                low = mid + 1;
            } else if (cmp > 0) {
                high = mid - 1;
            } else {
                return mid; // key found
            }
        }

        return low;  // key not found
    }

    /**
     * This is used to track our object records.
     */
    protected static class Record
    {
        /** The x coordinate of the object. */
        public int x;

        /** The y coordinate of the object. */
        public int y;

        /** The object itself. */
        public Object object;

        public Record (int x, int y, Object object)
        {
            this.x = x;
            this.y = y;
            this.object = object;
        }

        @Override public String toString ()
        {
            return "[x=" + x + ", y=" + y + "]"; // + ", object=" + object + "]";
        }
    }

    /** The object records. */
    protected Record[] _records;

    /** The number of records being tracked. */
    protected int _size;

    /** Assume a non-trivial number of objects will be tracked. */
    protected static final int DEFAULT_INIT_CAPACITY = 16;
}
