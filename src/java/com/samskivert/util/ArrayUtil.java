//
// $Id: ArrayUtil.java,v 1.27 2004/02/25 13:20:44 mdb Exp $
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

import java.lang.reflect.Array;

import java.util.Comparator;
import java.util.Random;

/**
 * Miscellaneous utility routines for working with arrays.
 */
public class ArrayUtil
{
    /**
     * Looks for an element that is equal to the supplied value and
     * returns its index in the array.
     *
     * @return the index of the first matching value if one was found, -1
     * otherwise.
     */
    public static int indexOf (byte[] values, byte value)
    {
        int count = (values == null) ? 0 : values.length;
        for (int ii = 0; ii < count; ii++) {
            if (values[ii] == value) {
                return ii;
            }
        }
        return -1;
    }

    /**
     * Looks for an element that is equal to the supplied value and
     * returns its index in the array.
     *
     * @return the index of the first matching value if one was found, -1
     * otherwise.
     */
    public static int indexOf (float[] values, float value)
    {
        int count = (values == null) ? 0 : values.length;
        for (int ii = 0; ii < count; ii++) {
            if (values[ii] == value) {
                return ii;
            }
        }
        return -1;
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
        shuffle(values, _rnd);
    }

    /**
     * Shuffles the elements in the given array into a random sequence.
     *
     * @param values the array to shuffle.
     * @param rnd the source from which random values for shuffling the
     * array are obtained.
     */
    public static void shuffle (byte[] values, Random rnd)
    {
        shuffle(values, 0, values.length, rnd);
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
        shuffle(values, offset, length, _rnd);
    }

    /**
     * Shuffles a subset of elements within the specified array into a
     * random sequence.
     *
     * @param values the array containing elements to shuffle.
     * @param offset the index at which to start shuffling elements.
     * @param length the number of elements to shuffle.
     * @param rnd the source from which random values for shuffling the
     * array are obtained.
     */
    public static void shuffle (
        byte[] values, int offset, int length, Random rnd)
    {
        // starting from the end of the specified region, repeatedly swap
        // the element in question with a random element previous to it
        // (in the specified region) up to and including itself
        for (int ii = offset + length - 1; ii > offset; ii--) {
            int idx = offset + rnd.nextInt(ii - offset + 1);
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
        shuffle(values, _rnd);
    }

    /**
     * Shuffles the elements in the given array into a random sequence.
     *
     * @param values the array to shuffle.
     * @param rnd the source from which random values for shuffling the
     * array are obtained.
     */
    public static void shuffle (int[] values, Random rnd)
    {
        shuffle(values, 0, values.length, rnd);
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
        shuffle(values, offset, length, _rnd);
    }

    /**
     * Shuffles a subset of elements within the specified array into a
     * random sequence.
     *
     * @param values the array containing elements to shuffle.
     * @param offset the index at which to start shuffling elements.
     * @param length the number of elements to shuffle.
     * @param rnd the source from which random values for shuffling the
     * array are obtained.
     */
    public static void shuffle (
        int[] values, int offset, int length, Random rnd)
    {
        // starting from the end of the specified region, repeatedly swap
        // the element in question with a random element previous to it
        // (in the specified region) up to and including itself
        for (int ii = offset + length - 1; ii > offset; ii--) {
            int idx = offset + rnd.nextInt(ii - offset + 1);
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
        shuffle(values, _rnd);
    }

    /**
     * Shuffles the elements in the given array into a random sequence.
     *
     * @param values the array to shuffle.
     * @param rnd the source from which random values for shuffling the
     * array are obtained.
     */
    public static void shuffle (Object[] values, Random rnd)
    {
        shuffle(values, 0, values.length, rnd);
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
        shuffle(values, offset, length, _rnd);
    }

    /**
     * Shuffles a subset of elements within the specified array into a
     * random sequence.
     *
     * @param values the array containing elements to shuffle.
     * @param offset the index at which to start shuffling elements.
     * @param length the number of elements to shuffle.
     * @param rnd the source from which random values for shuffling the
     * array are obtained.
     */
    public static void shuffle (
        Object[] values, int offset, int length, Random rnd)
    {
        // starting from the end of the specified region, repeatedly swap
        // the element in question with a random element previous to it
        // (in the specified region) up to and including itself
        for (int ii = offset + length - 1; ii > offset; ii--) {
            int idx = offset + rnd.nextInt(ii - offset + 1);
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
    public static <T extends Comparable<? super T>> int binarySearch (
        T[] array, int offset, int length, T key)
    {
	int low = offset, high = offset+length-1;
	while (low <= high) {
	    int mid = (low + high) >> 1;
	    T midVal = array[mid];
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
    public static <T> int binarySearch (
        T[] array, int offset, int length, T key, Comparator<T> comp)
    {
	int low = offset, high = offset+length-1;
	while (low <= high) {
	    int mid = (low + high) >> 1;
	    T midVal = array[mid];
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

        } else if (length == 0) {
            // we're not splicing anything!
            return values;
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
    public static short[] splice (short[] values, int offset)
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
    public static short[] splice (short[] values, int offset, int length)
    {
        // make sure we've something to work with
        if (values == null) {
            throw new IllegalArgumentException("Can't splice a null array.");

        } else if (length == 0) {
            // we're not splicing anything!
            return values;
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
        short[] nvalues = new short[size - length];
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

        } else if (length == 0) {
            // we're not splicing anything!
            return values;
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

        } else if (length == 0) {
            // we're not splicing anything!
            return values;
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
     * <code>values.length</code> (inclusive) in the supplied array. The
     * type of the array is preserved.
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

        } else if (length == 0) {
            // we're not splicing anything!
            return values;
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
        Object[] nvalues = (Object[])Array.newInstance(
            values.getClass().getComponentType(), size - length);
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

        } else if (length == 0) {
            // we're not splicing anything!
            return values;
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

    /**
     * Creates a new array one larger than the supplied array and with the
     * specified value inserted into the last slot.
     */
    public static byte[] append (byte[] values, byte value)
    {
        byte[] nvalues = new byte[values.length+1];
        System.arraycopy(values, 0, nvalues, 0, values.length);
        nvalues[values.length] = value;
        return nvalues;
    }

    /**
     * Creates a new array one larger than the supplied array and with the
     * specified value inserted into the last slot.
     */
    public static short[] append (short[] values, short value)
    {
        short[] nvalues = new short[values.length+1];
        System.arraycopy(values, 0, nvalues, 0, values.length);
        nvalues[values.length] = value;
        return nvalues;
    }

    /**
     * Creates a new array one larger than the supplied array and with the
     * specified value inserted into the last slot.
     */
    public static int[] append (int[] values, int value)
    {
        int[] nvalues = new int[values.length+1];
        System.arraycopy(values, 0, nvalues, 0, values.length);
        nvalues[values.length] = value;
        return nvalues;
    }

    /**
     * Creates a new array one larger than the supplied array and with the
     * specified value inserted into the last slot. The type of the values
     * array will be preserved.
     */
    public static Object[] append (Object[] values, Object value)
    {
        Object[] nvalues = (Object[])Array.newInstance(
            values.getClass().getComponentType(), values.length+1);
        System.arraycopy(values, 0, nvalues, 0, values.length);
        nvalues[values.length] = value;
        return nvalues;
    }

    /** The default random object used when shuffling an array. */
    protected static Random _rnd = new Random();
}
