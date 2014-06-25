//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import java.util.Arrays;
import java.util.Comparator;

import com.samskivert.annotation.ReplacedBy;

/**
 * Provides a mechanism ({@link #sort}) for sorting the contents of the list that doesn't involve
 * creating two object arrays. Two copies of the elements array are made if you called {@link
 * java.util.Collections#sort} (the first is when {@link #toArray} is called on the collection and
 * the second is when {@link Arrays#sort} clones the supplied array so that it can do a merge sort).
 */
@ReplacedBy(
    value="java.util.ArrayList, java.util.TreeSet, or com.google.common.collect.TreeMultiset",
    reason="It depends on whether you want to add elements then sort them, or ensure things are always sorted; and whether duplicates are OK. See the documentation for each to understand the differences.")
public class SortableArrayList<T> extends BaseArrayList<T>
{
    /**
     * Sorts the elements in this list with the supplied element
     * comparator using the quick sort algorithm (which does not involve
     * any object allocation). The elements must all be mutually
     * comparable.
     */
    public void sort (Comparator<? super T> comp)
    {
        if (_size > 1) {
            QuickSort.sort(_elements, 0, _size-1, comp);
        }
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
    @SuppressWarnings("unchecked")
    public int insertSorted (T value, Comparator<? super T> comp)
    {
        int ipos = binarySearch(value, comp);
        if (ipos < 0) {
            ipos = -(ipos+1);
        }
        _elements = (T[])ListUtil.insert(_elements, ipos, value);
        _size++;
        return ipos;
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
    public int binarySearch (T key, Comparator<? super T> comp)
    {
        return ArrayUtil.binarySearch(_elements, 0, _size, key, comp);
    }

    /**
     * Search for an element in the List, using the specified key. Note that the key
     * does not need to be of the same type as the element type, it just needs to be able
     * to compare itself to them.
     */
    public int binarySearch (Comparable<? super T> key)
    {
        return ArrayUtil.binarySearch(_elements, 0, _size, key);
    }

    /** Change this if the fields or inheritance hierarchy ever changes
     * (which is extremely unlikely). We override this because I'm tired
     * of serialized crap not working depending on whether I compiled with
     * jikes or javac. */
    private static final long serialVersionUID = 1;
}
