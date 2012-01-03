//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * This class manages arrays of ints. Some of those routines mimic the
 * behavior of array lists, others provide other more specialized
 * (generally faster but making requirements of the caller) list behavior.
 *
 * <p> An example is probably in order:
 *
 * <pre>
 * int[] list = null;
 *
 * // add our ints to a list
 * list = ListUtil.add(list, 2);
 * list = ListUtil.add(list, 5);
 *
 * // remove 5 from the list (does so by clearing out that index, but it
 * // doesn't slide subsequent elements down)
 * ListUtil.clear(list, 5);
 *
 * // append our objects to the end of the list letting list util know
 * // that we're tracking the list size
 * list = ListUtil.add(list, 0, 2);
 * list = ListUtil.add(list, 1, 5);
 *
 * // remove the elements from the list, compacting it to preserve
 * // element continuity
 * ListUtil.removeAt(list, 0);
 * ListUtil.remove(list, 5);
 * </pre>
 *
 * The array is initially assumed to be populated with zeros and zero is
 * assumed to be an emty slot.
 *
 * <p> See the documentation for the individual functions for their exact
 * behavior.
 */
public class IntListUtil
{
    /**
     * Adds the specified value to the first empty slot in the specified
     * list. Begins searching for empty slots at zeroth index.
     *
     * @param list the list to which to add the value. Can be null.
     * @param value the value to add.
     *
     * @return a reference to the list with value added (might not be the
     * list you passed in due to expansion, or allocation).
     */
    public static int[] add (int[] list, int value)
    {
        return add(list, 0, value);
    }

    /**
     * Adds the specified value to the next empty slot in the specified
     * list. Begins searching for empty slots at the specified index. This
     * can be used to quickly add values to a list that preserves
     * consecutivity by calling it with the size of the list as the first
     * index to check.
     *
     * @param list the list to which to add the value. Can be null.
     * @param startIdx the index at which to start looking for a spot.
     * @param value the value to add.
     *
     * @return a reference to the list with the value added (might not be
     * the list you passed in due to expansion, or allocation).
     */
    public static int[] add (int[] list, int startIdx, int value)
    {
        // make sure we've got a list to work with
        if (list == null) {
            list = new int[DEFAULT_LIST_SIZE];
        }

        // search for a spot to insert yon value; assuming we'll insert
        // it at the end of the list if we don't find one
        int llength = list.length;
        int index = llength;
        for (int i = startIdx; i < llength; i++) {
            if (list[i] == 0) {
                index = i;
                break;
            }
        }

        // expand the list if necessary
        if (index >= list.length) {
            list = accomodate(list, index);
        }

        // stick the value on in
        list[index] = value;

        return list;
    }

    /**
     * Searches through the list checking to see if the value supplied is
     * already in the list and adds it if it is not.
     *
     * @param list the list to which to add the value. Can be null.
     * @param value the value to test and add.
     *
     * @return a reference to the list with value added (might not be
     * the list you passed in due to expansion, or allocation) or null if
     * the value was already in the original array.
     */
    public static int[] testAndAdd (int[] list, int value)
    {
        // make sure we've got a list to work with
        if (list == null) {
            list = new int[DEFAULT_LIST_SIZE];
        }

        // search for a spot to insert yon value; we'll insert it at the
        // end of the list if we don't find a spot
        int llength = list.length;
        int index = llength;
        for (int i = 0; i < llength; i++) {
            int val = list[i];
            if (val == 0) {
                // only update our target index if we haven't already
                // found a spot to put the value
                if (index == llength) {
                    index = i;
                }

            } else if (val == value) {
                // oops, it's already in the list
                return null;
            }
        }

        // expand the list if necessary
        if (index >= list.length) {
            list = accomodate(list, index);
        }

        // stick the value on in
        list[index] = value;

        return list;
    }

    /**
     * Looks for an element that is equal to the supplied value. Passing a
     * zero <code>value</code> to this function will cleverly tell you
     * whether or not there are any empty elements in the array which is
     * probably not very useful.
     *
     * @return true if a matching value was found, false otherwise.
     */
    public static boolean contains (int[] list, int value)
    {
        return (-1 != indexOf(list, value));
    }

    /**
     * Looks for an element that is equal to the supplied value and
     * returns its index in the array. Passing a zero <code>value</code>
     * to this function will cleverly tell you whether or not there are
     * any empty elements in the array which is probably not very useful.
     *
     * @return the index of the first matching value if one was found,
     * -1 otherwise.
     */
    public static int indexOf (int[] list, int value)
    {
        return ArrayUtil.indexOf(list, value);
    }

    /**
     * Clears out the first value that is equal to the supplied
     * value. Passing a zero <code>value</code> to this function will
     * cleverly tell you the index of the first empty element in the array
     * which it will have kindly overwritten with zero just for good
     * measure.
     *
     * @return the value that was removed or zero if it was not found.
     */
    public static int clear (int[] list, int value)
    {
        // nothing to clear from an empty list
        if (list == null) {
            return 0;
        }

        int llength = list.length; // no optimizing bastards
        for (int i = 0; i < llength; i++) {
            int val = list[i];
            if (val == value) {
                list[i] = 0;
                return val;
            }
        }
        return 0;
    }

    /**
     * Removes the first value that is equal to the supplied value. The
     * values after the removed value will be slid down the array one spot
     * to fill the place of the removed value.
     *
     * @return the value that was removed from the array or zero if no
     * matching object was found.
     */
    public static int remove (int[] list, int value)
    {
        // nothing to remove from an empty list
        if (list == null) {
            return 0;
        }

        int llength = list.length; // no optimizing bastards
        for (int i = 0; i < llength; i++) {
            int val = list[i];
            if (val == value) {
                System.arraycopy(list, i+1, list, i, llength-(i+1));
                list[llength-1] = 0;
                return val;
            }
        }
        return 0;
    }

    /**
     * Removes the value at the specified index. The values after the
     * removed value will be slid down the array one spot to fill the
     * place of the removed value. If a null array is supplied or one that
     * is not large enough to accomodate this index, zero is returned.
     *
     * @return the value that was removed from the array or zero if no
     * value existed at that location.
     */
    public static int removeAt (int[] list, int index)
    {
        int llength = list.length;
        if (llength <= index) {
            return 0;
        }

        int val = list[index];
        System.arraycopy(list, index+1, list, index, llength-(index+1));
        list[llength-1] = 0;
        return val;
    }

    /**
     * Converts a sparse array (with zero-valued entries) into a compact
     * array (where all elements contain non-zero values) with ordering
     * preserved.
     */
    public static int[] compact (int[] list)
    {
        int llength = list.length, lcount = 0;
        // count up the non-zero entries
        for (int i = 0; i < llength; i++) {
            if (list[i] != 0) {
                lcount++;
            }
        }

        int[] nlist = new int[lcount];
        int n = 0;
        for (int i = 0; i < llength; i++) {
            if (list[i] != 0) {
                nlist[n++] = list[i];
            }
        }

        return nlist;
    }

    /**
     * Returns the total of all of the values in the list.
     */
    public static int sum (int[] list)
    {
        int total = 0, lsize = list.length;
        for (int ii = 0; ii < lsize; ii++) {
            total += list[ii];
        }
        return total;
    }

    /**
     * Returns the maximum value in the given array of values, or {@link
     * Integer#MIN_VALUE} if the array is null or zero-length.
     */
    public static int getMaxValue (int[] values)
    {
        int max = Integer.MIN_VALUE;
        int vcount = (values == null) ? 0 : values.length;
        for (int ii = 0; ii < vcount; ii++) {
            if (values[ii] > max) {
                // new max
                max = values[ii];
            }
        }
        return max;
    }

    /**
     * Returns the minimum value in the given array of values, or {@link
     * Integer#MAX_VALUE} if the array is null or zero-length.
     */
    public static int getMinValue (int[] values)
    {
        int min = Integer.MAX_VALUE;
        int vcount = (values == null) ? 0 : values.length;
        for (int ii = 0; ii < vcount; ii++) {
            if (values[ii] < min) {
                // new min
                min = values[ii];
            }
        }
        return min;
    }

    /**
     * Returns the index of the maximum value in the given array of
     * values, or <code>-1</code> if the array is <code>null</code> or
     * zero-length.
     */
    public static int getMaxValueIndex (int[] values)
    {
        if (values == null || values.length == 0) {
            return -1;
        }

        int idx = 0;
        int max = values[idx];
        for (int ii = 1; ii < values.length; ii++) {
            if (values[ii] > max) {
                max = values[ii];
                idx = ii;
            }
        }
        return idx;
    }

    /**
     * Returns an array of the indexes in the given array of values that
     * have the maximum value in the array, or a zero-length array if the
     * supplied array of values is <code>null</code> or zero-length.
     */
    public static int[] getMaxIndexes (int[] values)
    {
        int max = Integer.MIN_VALUE;
        int num = 0;
        int vcount = (values == null) ? 0 : values.length;

        for (int ii=0; ii < vcount; ii++) {
            int value = values[ii];

            if (value < max) {
                // common case- stop checking things..
                continue;

            } else if (value > max) {
                // new max
                max = value;
                num = 1;

            } else {
                // another sighting of max
                num++;
            }
        }

        // now find the indexes that have max
        int[] maxes = new int[num];
        for (int ii=0, pos=0; pos < num; ii++) {
            if (values[ii] == max) {
                maxes[pos++] = ii;
            }
        }

        return maxes;
    }

    /**
     * Returns an array of the indexes in the given array of values that
     * have the minimum value in the array, or a zero-length array if the
     * supplied array of values is <code>null</code> or zero-length.
     */
    public static int[] getMinIndexes (int[] values)
    {
        int min = Integer.MAX_VALUE;
        int num = 0;
        int vcount = (values == null) ? 0 : values.length;

        for (int ii=0; ii < vcount; ii++) {
            int value = values[ii];

            if (value > min) {
                // common case- stop checking things..
                continue;

            } else if (value < min) {
                // new min
                min = value;
                num = 1;

            } else {
                // another sighting of min
                num++;
            }
        }

        // now find the indexes that have min
        int[] mins = new int[num];
        for (int ii=0, pos=0; pos < num; ii++) {
            if (values[ii] == min) {
                mins[pos++] = ii;
            }
        }

        return mins;
    }

    /**
     * Normalizes an array of integers from the bounding [min,max] to
     * [0.0, 1.0]. If min == max, all elements in the returned array
     * will be 1f.
     */
    public static float[] normalize (int[] values)
    {
        // Allocate storage for the normalized array
        float[] normalized = new float[values.length];

        // Determine the minimum and maximum
        int min = getMinValue(values);
        int max = getMaxValue(values);
        int spread = max - min;

        // If there is no spread, return a flat normalization
        if (spread == 0) {
            Arrays.fill(normalized, 1f);
            return normalized;
        }

        // Normalize each value in the input array
        for (int i = 0; i < values.length; i++) {
            normalized[i] = (values[i] - min) / (float) spread;
        }
        return normalized;
    }

    /**
     * Creates a new list that will accomodate the specified index and
     * copies the contents of the old list to the first.
     */
    protected static int[] accomodate (int[] list, int index)
    {
        int size = list.length;
        // expand size by powers of two until we're big enough
        while (size <= index) {
            size = Math.max(size * 2, DEFAULT_LIST_SIZE);
        }

        // create a new list and copy the contents
        int[] newlist = new int[size];
        System.arraycopy(list, 0, newlist, 0, list.length);
        return newlist;
    }

    /**
     * Covnerts an array of primitives to an array of objects.
     */
    public static Integer[] box (int[] list)
    {
        if (list == null) {
            return null;
        }
        Integer[] boxed = new Integer[list.length];
        for (int ii = 0; ii < list.length; ii++) {
            boxed[ii] = list[ii];
        }
        return boxed;
    }

    /**
     * Converts an array of primitives to a list of Integers.
     */
    public static List<Integer> asList (int[] list)
    {
        if (list == null) {
            return null;
        }
        List<Integer> ilist = new ArrayList<Integer>(list.length);
        for (int ii = 0; ii < list.length; ii++) {
            ilist.add(list[ii]);
        }
        return ilist;
    }

    /**
     * Converts an array of Integer objects to an array of primitives.
     */
    public static int[] unbox (Integer[] list)
    {
        if (list == null) {
            return null;
        }
        int[] unboxed = new int[list.length];
        for (int ii = 0; ii < list.length; ii++) {
            unboxed[ii] = list[ii];
        }
        return unboxed;
    }

    /**
     * Converts an array of Integer objects to an array of primitives.
     */
    public static int[] unbox (Collection<Integer> list)
    {
        if (list == null) {
            return null;
        }
        int[] unboxed = new int[list.size()];
        int ii = 0;
        for (Integer value : list) {
            unboxed[ii++] = value;
        }
        return unboxed;
    }

    /**
     * The size of a list to create if we have to create one entirely
     * from scratch rather than just expand it.
     */
    protected static final int DEFAULT_LIST_SIZE = 4;
}
