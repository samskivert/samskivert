//
// $Id: SortableArrayList.java,v 1.11 2003/03/30 02:23:39 mdb Exp $
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

import java.io.Serializable;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.RandomAccess;

import java.lang.reflect.Array;

/**
 * Extends the standard Java {@link ArrayList} functionality (which we'd
 * do with normal object extension if those pig fuckers hadn't made the
 * instance variables private) and provides a mechanism ({@link #sort})
 * for sorting the contents of the list that doesn't involve creating two
 * object arrays. Two copies of the elements array are made if you called
 * {@link java.util.Collections#sort}</code> (the first is when {@link
 * #toArray} is called on the collection and the second is when {@link
 * Arrays#sort} clones the supplied array so that it can do a merge sort).
 */
public class SortableArrayList extends AbstractList
    implements List, RandomAccess, Cloneable, Serializable
{
    /**
     * Sorts the elements in this list using the quick sort algorithm
     * (which does not involve any object allocation). The elements must
     * implement {@link Comparable} and all be mutually comparable.
     */
    public void sort ()
    {
        sort(Comparators.COMPARABLE);
    }

    /**
     * Sorts the elements in this list with the supplied element
     * comparator using the quick sort algorithm (which does not involve
     * any object allocation). The elements must all be mutually
     * comparable.
     */
    public void sort (Comparator comp)
    {
        if (_size > 1) {
            QuickSort.csort(_elements, 0, _size-1, comp);
        }
    }

    /**
     * Inserts the specified item into the list into a position that
     * preserves the sorting of the list. The list must be sorted prior to
     * the call to this method (an empty list built up entirely via calls
     * to {@link #insertSorted} will be properly sorted). The list must be
     * entirely comprised of elements that implement {@link Comparable}
     * and the element being added must implement {@link Comparable} as
     * well.
     *
     * @return the index at which the element was inserted.
     */
    public int insertSorted (Object value)
    {
        return insertSorted(value, Comparators.COMPARABLE);
    }

    /**
     * Inserts the specified item into the list into a position that
     * preserves the sorting of the list according to the supplied {@link
     * Comparator}. The list must be sorted (via the supplied comparator)
     * prior to the call to this method (an empty list built up entirely
     * via calls to {@link #insertSorted} will be properly sorted).
     *
     * @return the index at which the element was inserted.
     */
    public int insertSorted (Object value, Comparator comp)
    {
        int ipos = binarySearch(value, comp);
        if (ipos < 0) {
            ipos = -(ipos+1);
        }
        _elements = ListUtil.insert(_elements, ipos, value);
        _size++;
        return ipos;
    }

    /**
     * Performs a binary search, attempting to locate the specified
     * object. The array must be sorted for this to operate correctly and
     * the contents of the array must all implement {@link Comparable}
     * (and actually be comparable to one another).
     *
     * @return the index of the object in question or
     * <code>(-(<i>insertion point</i>) - 1)</code> (always a negative
     * value) if the object was not found in the list.
     */
    public int binarySearch (Object key)
    {
        return ArrayUtil.binarySearch(_elements, 0, _size, key);
    }

    /**
     * Performs a binary search, attempting to locate the specified
     * object. The array must be in the sort order defined by the supplied
     * {@link Comparator} for this to operate correctly.
     *
     * @return the index of the object in question or
     * <code>(-(<i>insertion point</i>) - 1)</code> (always a negative
     * value) if the object was not found in the list.
     */
    public int binarySearch (Object key, Comparator comp)
    {
        return ArrayUtil.binarySearch(_elements, 0, _size, key, comp);
    }

    // documentation inherited from interface
    public int size ()
    {
        return _size;
    }

    // documentation inherited from interface
    public boolean isEmpty ()
    {
        return (_size == 0);
    }

    // documentation inherited from interface
    public boolean contains (Object o)
    {
        return ListUtil.contains(_elements, o);
    }

    // documentation inherited from interface
    public Object[] toArray ()
    {
        return toArray(new Object[_size]);
    }

    // documentation inherited from interface
    public Object[] toArray (Object[] target)
    {
        // create the target array if necessary
        if (target.length < _size) {
            target = (Object[])Array.newInstance(
                target.getClass().getComponentType(), _size);
        }

        // copy the elements
        if (_elements != null) {
            System.arraycopy(_elements, 0, target, 0, _size);
        }

        return target;
    }

    // documentation inherited from interface
    public boolean add (Object o)
    {
        _elements = ListUtil.add(_elements, _size, o);
        _size++;
        return true;
    }

    // documentation inherited from interface
    public boolean remove (Object o)
    {
        if (ListUtil.removeEqual(_elements, o) != null) {
            _size--;
            return true;
        }
        return false;
    }

    // documentation inherited from interface
    public Object get (int index)
    {
        if (index >= 0 && index < _size) {
            return _elements[index];
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    // documentation inherited from interface
    public Object set (int index, Object element)
    {
        if (index >= 0 && index < _size) {
            Object old = _elements[index];
            _elements[index] = element;
            return old;
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    // documentation inherited from interface
    public void add (int index, Object element)
    {
        if (index == _size) {
            add(element);
        } else if (index >= 0 && index < _size) {
            _elements = ListUtil.insert(_elements, index, element);
            _size++;
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    // documentation inherited from interface
    public Object remove (int index)
    {
        Object removed = ListUtil.remove(_elements, index);
        if (removed != null) {
            _size--;
        }
        return removed;
    }

    // documentation inherited from interface
    public int indexOf  (Object o)
    {
        return ListUtil.indexOfEqual(_elements, o);
    }

//     // documentation inherited from interface
//     public int lastIndexOf (Object o)
//     {
//     }

    /** The array of elements in our list. */
    protected Object[] _elements;

    /** The number of elements in our list. */
    protected int _size;

    /** Change this if the fields or inheritance hierarchy ever changes
     * (which is extremely unlikely). We override this because I'm tired
     * of serialized crap not working depending on whether I compiled with
     * jikes or javac. */
    private static final long serialVersionUID = 1;
}
