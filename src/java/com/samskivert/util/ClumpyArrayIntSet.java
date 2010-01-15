//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2010 Michael Bayne, et al.
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

import java.io.Serializable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Efficiently stores ints that are "clumped" together.
 */
public class ClumpyArrayIntSet extends AbstractIntSet
    implements Cloneable, Serializable
{
    /**
     */
    public ClumpyArrayIntSet ()
    {
        _anchors = new int[DEFAULT_CAPACITY];
        _masks = new int[DEFAULT_CAPACITY];
    }

    @Override // from interface IntSet
    public boolean contains (int value)
    {
        int index = binarySearch(value >>> 5);
        return (index >= 0) && (0 != (_masks[index] & (1 << (value & 31))));
    }

    @Override // from interface IntSet
    public boolean add (int value)
    {
        int anchor = value >>> 5;
        int mask = 1 << (value & 31);
        int index = binarySearch(anchor);
        if (index >= 0) {
            if (0 != (_masks[index] & mask)) {
                return false; // already contained
            }
            _masks[index] |= mask;
            _size++;
            return true;
        }

        // convert the return value into the insertion point
        index += 1;
        index *= -1;

        // expand the values array if necessary, leaving room for the newly added element
        int alen = _anchors.length;
        int[] sourceAnchors = _anchors;
        int[] sourceMask = _masks;
        if (alen == _anchorSize) {
            int newLen = Math.max(DEFAULT_CAPACITY, alen*2);
            _anchors = new int[newLen];
            System.arraycopy(sourceAnchors, 0, _anchors, 0, index);
            _masks = new int[newLen];
            System.arraycopy(sourceMask, 0, _masks, 0, index);
        }

        // shift and insert
        if (_anchorSize > index) {
            System.arraycopy(sourceAnchors, index, _anchors, index+1, _anchorSize-index);
            System.arraycopy(sourceMask, index, _masks, index+1, _anchorSize-index);
        }
        _anchors[index] = anchor;
        _masks[index] = mask;

        // increment our size
        _anchorSize++;
        _size++;

        return true;
    }

//    @Override // from interface IntSet
//    public boolean remove (int value)
//    {
//        int anchor = value >>> 5;
//        int mask = 1 << (value & 31);
//        int index = binarySearch(anchor);
//        if (index < 0) {
//            return false;
//        }
//
//        // TODO!!!!!!! HERE
//
////        _size--;
////        if ((_values.length > DEFAULT_CAPACITY) && (_size < _values.length/8)) {
////            // if we're using less than 1/8 of our capacity, shrink by half
////            int[] newVals = new int[_values.length/2];
////            System.arraycopy(_values, 0, newVals, 0, index);
////            System.arraycopy(_values, index+1, newVals, index, _size-index);
////            _values = newVals;
////
////        } else {
////            // shift entries past the removed one downwards
////            System.arraycopy(_values, index+1, _values, index, _size-index);
////            //_values[_size] = 0;
////        }
//        return true;
//    }

    // from interface IntSet
    public Interator interator ()
    {
        // TODO: rewrite, use FindingInterator
        return new AbstractInterator() {
            public boolean hasNext () {
                if (!_hasNext) {
                    _hasNext = findNext();
                }
                return _hasNext;
            }

            public int nextInt () {
                if (!_hasNext && !hasNext()) {
                    throw new NoSuchElementException();
                }
                int mask = Integer.lowestOneBit(_curMask);
                int retval = (_anchors[_pos] << 5) + Integer.numberOfTrailingZeros(mask);
                _curMask &= ~mask;
                _hasNext = (_curMask != 0);
                return retval;
            }

//            @Override public void remove () {
//                if (!_canRemove) {
//                    throw new IllegalStateException();
//                }
//                System.arraycopy(_values, _pos, _values, _pos - 1, _size - _pos);
//                _pos--;
//                _size--; //_values[--_size] = 0;
//                _canRemove = false;
//            }

            protected boolean findNext ()
            {
                if (_pos + 1 < _anchorSize) {
                    _curMask = _masks[++_pos];
                    return true;
                }
                return false;
            }

            protected int _pos = -1;
            protected int _curMask;
            protected boolean _hasNext;
        };
    }

    @Override // from AbstractSet<Integer>
    public int size ()
    {
        return (_size > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int)_size;
    }

    @Override // from AbstractSet<Integer>
    public void clear ()
    {
        _anchorSize = 0;
        _size = 0;
        Arrays.fill(_masks, 0);
    }

    /**
     * Performs a binary search on our values array, looking for the specified value. Swiped from
     * <code>java.util.Arrays</code> because those wankers didn't provide a means by which to
     * perform a binary search on a subset of an array.
     */
    protected int binarySearch (int key)
    {
	int low = 0;
	int high = _anchorSize-1;

	while (low <= high) {
	    int mid = (low + high) >> 1;
	    int midVal = _anchors[mid];

	    if (midVal < key) {
		low = mid + 1;
	    } else if (midVal > key) {
		high = mid - 1;
	    } else {
		return mid; // key found
            }
	}

	return -(low + 1);  // key not found.
    }

    /** An array containing the values in this set. */
    protected int[] _anchors;

    protected int[] _masks;

    protected int _anchorSize;

    /** The number of elements in this set. */
    protected long _size;

    /** The default initial capacity of this set. */
    protected static final int DEFAULT_CAPACITY = 16;

    /** Change this if the fields or inheritance hierarchy ever changes (extremely unlikely). */
    private static final long serialVersionUID = 1;
}
