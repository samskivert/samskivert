//
// $Id: ArrayUtil.java,v 1.17 2002/09/19 23:37:40 shaper Exp $
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001 Walter Korman
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;

import com.samskivert.Log;

/**
 * Miscellaneous utility routines for working with arrays.
 */
public class ArrayUtil
{
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
     * Reverses the elements in the given array.
     *
     * @param values the array to reverse.
     */
    public static void reverse (byte[] values)
    {
        reverse(values, 0, values.length);
    }

    /**
     * Reverses a subset of elements within the specified array.
     *
     * @param values the array containing elements to reverse.
     * @param offset the index at which to start reversing elements.
     * @param length the number of elements to reverse.
     */
    public static void reverse (byte[] values, int offset, int length)
    {
        int aidx = offset;
        int bidx = offset + length - 1;
        while (bidx > aidx) {
            byte value = values[aidx];
            values[aidx] = values[bidx];
            values[bidx] = value;
            aidx++;
            bidx--;
        }
    }

    /**
     * Reverses the elements in the given array.
     *
     * @param values the array to reverse.
     */
    public static void reverse (int[] values)
    {
        reverse(values, 0, values.length);
    }

    /**
     * Reverses a subset of elements within the specified array.
     *
     * @param values the array containing elements to reverse.
     * @param offset the index at which to start reversing elements.
     * @param length the number of elements to reverse.
     */
    public static void reverse (int[] values, int offset, int length)
    {
        int aidx = offset;
        int bidx = offset + length - 1;
        while (bidx > aidx) {
            int value = values[aidx];
            values[aidx] = values[bidx];
            values[bidx] = value;
            aidx++;
            bidx--;
        }
    }

    /**
     * Shuffles the elements in the given array into a random sequence.
     *
     * @param values the array to shuffle.
     */
    public static void shuffle (byte[] values)
    {
        shuffle(values, 0, values.length);
    }

    /**
     * Shuffles a subset of elements within the specified array into a
     * random sequence.
     *
     * @param values the array containing elements to shuffle.
     * @param offset the index at which to start shuffling elements.
     * @param length the number of elements to shuffle.
     */
    public static void shuffle (byte[] values, int offset, int length)
    {
        // starting from the end of the specified region, repeatedly swap
        // the element in question with a random element previous to it
        // (in the specified region) up to and including itself
        for (int ii = offset + length - 1; ii > offset; ii--) {
            int idx = offset + _rnd.nextInt(ii - offset + 1);
            byte tmp = values[ii];
            values[ii] = values[idx];
            values[idx] = tmp;
        }
    }

    /**
     * Shuffles the elements in the given array into a random sequence.
     *
     * @param values the array to shuffle.
     */
    public static void shuffle (int[] values)
    {
        shuffle(values, 0, values.length);
    }

    /**
     * Shuffles a subset of elements within the specified array into a
     * random sequence.
     *
     * @param values the array containing elements to shuffle.
     * @param offset the index at which to start shuffling elements.
     * @param length the number of elements to shuffle.
     */
    public static void shuffle (int[] values, int offset, int length)
    {
        // starting from the end of the specified region, repeatedly swap
        // the element in question with a random element previous to it
        // (in the specified region) up to and including itself
        for (int ii = offset + length - 1; ii > offset; ii--) {
            int idx = offset + _rnd.nextInt(ii - offset + 1);
            int tmp = values[ii];
            values[ii] = values[idx];
            values[idx] = tmp;
        }
    }

    /**
     * Shuffles the elements in the given array into a random sequence.
     *
     * @param values the array to shuffle.
     */
    public static void shuffle (Object[] values)
    {
        shuffle(values, 0, values.length);
    }

    /**
     * Shuffles a subset of elements within the specified array into a
     * random sequence.
     *
     * @param values the array containing elements to shuffle.
     * @param offset the index at which to start shuffling elements.
     * @param length the number of elements to shuffle.
     */
    public static void shuffle (Object[] values, int offset, int length)
    {
        // starting from the end of the specified region, repeatedly swap
        // the element in question with a random element previous to it
        // (in the specified region) up to and including itself
        for (int ii = offset + length - 1; ii > offset; ii--) {
            int idx = offset + _rnd.nextInt(ii - offset + 1);
            Object tmp = values[ii];
            values[ii] = values[idx];
            values[idx] = tmp;
        }
    }

    /**
     * Performs a binary search, attempting to locate the specified
     * object. The array must be sorted for this to operate correctly and
     * the contents of the array must all implement {@link Comparable}
     * (and actually be comparable to one another).
     *
     * @param array the array of {@link Comparable}s to be searched.
     * @param offset the index of the first element in the array to be
     * considered.
     * @param length the number of elements including and following the
     * element at <code>offset</code> to consider when searching.
     * @param key the object to be located.
     *
     * @return the index of the object in question or
     * <code>(-(<i>insertion point</i>) - 1)</code> (always a negative
     * value) if the object was not found in the list.
     */
    public static int binarySearch (
        Object[] array, int offset, int length, Object key)
    {
	int low = offset, high = offset+length-1;
	while (low <= high) {
	    int mid = (low + high) >> 1;
	    Comparable midVal = (Comparable)array[mid];
	    int cmp = midVal.compareTo(key);
	    if (cmp < 0) {
		low = mid + 1;
	    } else if (cmp > 0) {
		high = mid - 1;
	    } else {
		return mid; // key found
            }
	}
	return -(low + 1); // key not found.
    }

    /**
     * Performs a binary search, attempting to locate the specified
     * object. The array must be in the sort order defined by the supplied
     * {@link Comparator} for this to operate correctly.
     *
     * @param array the array of objects to be searched.
     * @param offset the index of the first element in the array to be
     * considered.
     * @param length the number of elements including and following the
     * element at <code>offset</code> to consider when searching.
     * @param key the object to be located.
     * @param comp the comparator to use when searching.
     *
     * @return the index of the object in question or
     * <code>(-(<i>insertion point</i>) - 1)</code> (always a negative
     * value) if the object was not found in the list.
     */
    public static int binarySearch (
        Object[] array, int offset, int length, Object key, Comparator comp)
    {
	int low = offset, high = offset+length-1;
	while (low <= high) {
	    int mid = (low + high) >> 1;
	    Object midVal = array[mid];
	    int cmp = comp.compare(midVal, key);
	    if (cmp < 0) {
		low = mid + 1;
	    } else if (cmp > 0) {
		high = mid - 1;
	    } else {
		return mid; // key found
            }
	}
	return -(low + 1); // key not found.
    }

    /**
     * Creates and returns a new array sized to fit and populated with the
     * subset of values from indexes <code>0</code> to </code>offset -
     * 1</code> (inclusive) in the supplied array.
     *
     * @param values the array of values to splice.
     * @param offset the index within the <code>values</code> array after
     * which all subsequent values are to be removed.  This must be a
     * valid index within the <code>values</code> array.
     */
    public static byte[] splice (byte[] values, int offset)
    {
        int length = (values == null) ? 0 : values.length - offset;
        return splice(values, offset, length);
    }

    /**
     * Creates and returns a new array sized to fit and populated with the
     * concatenated subset of values from indexes <code>0</code> to
     * </code>offset - 1</code>, and <code>offset + length</code> to
     * <code>values.length</code> (inclusive) in the supplied array.
     *
     * @param values the array of values to splice.
     * @param offset the index within the <code>values</code> array at
     * which the first element will be removed.  This must be a valid
     * index within the <code>values</code> array.
     * @param length the number of elements to be removed.  Note that
     * <code>offset + length</code> must be a valid index within the
     * <code>values</code> array.
     */
    public static byte[] splice (byte[] values, int offset, int length)
    {
        // make sure we've something to work with
        if (values == null) {
            throw new IllegalArgumentException("Can't splice a null array.");
        }

        // require that the entire range to remove be within the array bounds
        int size = values.length;
        int tstart = offset + length;
        if (offset < 0 || tstart > size) {
            throw new ArrayIndexOutOfBoundsException(
                "Splice range out of bounds [offset=" + offset +
                ", length=" + length + ", size=" + size + "].");
        }

        // create a new array and populate it with the spliced-in values
        byte[] nvalues = new byte[size - length];
        System.arraycopy(values, 0, nvalues, 0, offset);
        System.arraycopy(values, tstart, nvalues, offset, size - tstart);
        return nvalues;
    }

    /**
     * Creates and returns a new array sized to fit and populated with the
     * subset of values from indexes <code>0</code> to </code>offset -
     * 1</code> (inclusive) in the supplied array.
     *
     * @param values the array of values to splice.
     * @param offset the index within the <code>values</code> array after
     * which all subsequent values are to be removed.  This must be a
     * valid index within the <code>values</code> array.
     */
    public static int[] splice (int[] values, int offset)
    {
        int length = (values == null) ? 0 : values.length - offset;
        return splice(values, offset, length);
    }

    /**
     * Creates and returns a new array sized to fit and populated with the
     * concatenated subset of values from indexes <code>0</code> to
     * </code>offset - 1</code>, and <code>offset + length</code> to
     * <code>values.length</code> (inclusive) in the supplied array.
     *
     * @param values the array of values to splice.
     * @param offset the index within the <code>values</code> array at
     * which the first element will be removed.  This must be a valid
     * index within the <code>values</code> array.
     * @param length the number of elements to be removed.  Note that
     * <code>offset + length</code> must be a valid index within the
     * <code>values</code> array.
     */
    public static int[] splice (int[] values, int offset, int length)
    {
        // make sure we've something to work with
        if (values == null) {
            throw new IllegalArgumentException("Can't splice a null array.");
        }

        // require that the entire range to remove be within the array bounds
        int size = values.length;
        int tstart = offset + length;
        if (offset < 0 || tstart > size) {
            throw new ArrayIndexOutOfBoundsException(
                "Splice range out of bounds [offset=" + offset +
                ", length=" + length + ", size=" + size + "].");
        }

        // create a new array and populate it with the spliced-in values
        int[] nvalues = new int[size - length];
        System.arraycopy(values, 0, nvalues, 0, offset);
        System.arraycopy(values, tstart, nvalues, offset, size - tstart);
        return nvalues;
    }

    /**
     * Creates and returns a new array sized to fit and populated with the
     * subset of values from indexes <code>0</code> to </code>offset -
     * 1</code> (inclusive) in the supplied array.
     *
     * @param values the array of values to splice.
     * @param offset the index within the <code>values</code> array after
     * which all subsequent values are to be removed.  This must be a
     * valid index within the <code>values</code> array.
     */
    public static String[] splice (String[] values, int offset)
    {
        int length = (values == null) ? 0 : values.length - offset;
        return splice(values, offset, length);
    }

    /**
     * Creates and returns a new array sized to fit and populated with the
     * concatenated subset of values from indexes <code>0</code> to
     * </code>offset - 1</code>, and <code>offset + length</code> to
     * <code>values.length</code> (inclusive) in the supplied array.
     *
     * @param values the array of values to splice.
     * @param offset the index within the <code>values</code> array at
     * which the first element will be removed.  This must be a valid
     * index within the <code>values</code> array.
     * @param length the number of elements to be removed.  Note that
     * <code>offset + length</code> must be a valid index within the
     * <code>values</code> array.
     */
    public static String[] splice (String[] values, int offset, int length)
    {
        // make sure we've something to work with
        if (values == null) {
            throw new IllegalArgumentException("Can't splice a null array.");
        }

        // require that the entire range to remove be within the array bounds
        int size = values.length;
        int tstart = offset + length;
        if (offset < 0 || tstart > size) {
            throw new ArrayIndexOutOfBoundsException(
                "Splice range out of bounds [offset=" + offset +
                ", length=" + length + ", size=" + size + "].");
        }

        // create a new array and populate it with the spliced-in values
        String[] nvalues = new String[size - length];
        System.arraycopy(values, 0, nvalues, 0, offset);
        System.arraycopy(values, tstart, nvalues, offset, size - tstart);
        return nvalues;
    }

    /**
     * Creates and returns a new array sized to fit and populated with the
     * subset of values from indexes <code>0</code> to </code>offset -
     * 1</code> (inclusive) in the supplied array.
     *
     * @param values the array of values to splice.
     * @param offset the index within the <code>values</code> array after
     * which all subsequent values are to be removed.  This must be a
     * valid index within the <code>values</code> array.
     */
    public static Object[] splice (Object[] values, int offset)
    {
        int length = (values == null) ? 0 : values.length - offset;
        return splice(values, offset, length);
    }

    /**
     * Creates and returns a new array sized to fit and populated with the
     * concatenated subset of values from indexes <code>0</code> to
     * </code>offset - 1</code>, and <code>offset + length</code> to
     * <code>values.length</code> (inclusive) in the supplied array.
     *
     * @param values the array of values to splice.
     * @param offset the index within the <code>values</code> array at
     * which the first element will be removed.  This must be a valid
     * index within the <code>values</code> array.
     * @param length the number of elements to be removed.  Note that
     * <code>offset + length</code> must be a valid index within the
     * <code>values</code> array.
     */
    public static Object[] splice (Object[] values, int offset, int length)
    {
        // make sure we've something to work with
        if (values == null) {
            throw new IllegalArgumentException("Can't splice a null array.");
        }

        // require that the entire range to remove be within the array bounds
        int size = values.length;
        int tstart = offset + length;
        if (offset < 0 || tstart > size) {
            throw new ArrayIndexOutOfBoundsException(
                "Splice range out of bounds [offset=" + offset +
                ", length=" + length + ", size=" + size + "].");
        }

        // create a new array and populate it with the spliced-in values
        Object[] nvalues = new Object[size - length];
        System.arraycopy(values, 0, nvalues, 0, offset);
        System.arraycopy(values, tstart, nvalues, offset, size - tstart);
        return nvalues;
    }

    /**
     * Creates and returns a new array sized to fit and populated with the
     * subset of values from indexes <code>0</code> to </code>offset -
     * 1</code> (inclusive) in the supplied array.
     *
     * @param values the array of values to splice.
     * @param offset the index within the <code>values</code> array after
     * which all subsequent values are to be removed.  This must be a
     * valid index within the <code>values</code> array.
     */
    public static int[][] splice (int[][] values, int offset)
    {
        int length = (values == null) ? 0 : values.length - offset;
        return splice(values, offset, length);
    }

    /**
     * Creates and returns a new array sized to fit and populated with the
     * concatenated subset of values from indexes <code>0</code> to
     * </code>offset - 1</code>, and <code>offset + length</code> to
     * <code>values.length</code> (inclusive) in the supplied array.
     *
     * @param values the array of values to splice.
     * @param offset the index within the <code>values</code> array at
     * which the first element will be removed.  This must be a valid
     * index within the <code>values</code> array.
     * @param length the number of elements to be removed.  Note that
     * <code>offset + length</code> must be a valid index within the
     * <code>values</code> array.
     */
    public static int[][] splice (int[][] values, int offset, int length)
    {
        // make sure we've something to work with
        if (values == null) {
            throw new IllegalArgumentException("Can't splice a null array.");
        }

        // require that the entire range to remove be within the array bounds
        int size = values.length;
        int tstart = offset + length;
        if (offset < 0 || tstart > size) {
            throw new ArrayIndexOutOfBoundsException(
                "Splice range out of bounds [offset=" + offset +
                ", length=" + length + ", size=" + size + "].");
        }

        // create a new array and populate it with the spliced-in values
        int[][] nvalues = new int[size - length][];
        System.arraycopy(values, 0, nvalues, 0, offset);
        System.arraycopy(values, tstart, nvalues, offset, size - tstart);
        return nvalues;
    }

    /** The random object used when shuffling an array. */
    protected static Random _rnd = new Random();
}
