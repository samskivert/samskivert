//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import java.lang.reflect.Array;

import java.util.Collection;
import java.util.Comparator;
import java.util.Random;

/**
 * Miscellaneous utility routines for working with arrays.
 */
public class ArrayUtil
{
    /** An empty (and thus immutable) int[] that can be shared by anyone. */
    public static final int[] EMPTY_INT = new int[0];

    /** An empty (and thus immutable) byte[] that can be shared by anyone. */
    public static final byte[] EMPTY_BYTE = new byte[0];

    /** An empty (and thus immutable) short[] that can be shared by anyone. */
    public static final short[] EMPTY_SHORT = new short[0];

    /** An empty (and thus immutable) float[] that can be shared by anyone. */
    public static final float[] EMPTY_FLOAT = new float[0];

    /** An empty (and thus immutable) double[] that can be shared by anyone. */
    public static final double[] EMPTY_DOUBLE = new double[0];

    /** An empty (and thus immutable) long[] that can be shared by anyone. */
    public static final long[] EMPTY_LONG = new long[0];

    /** An empty (and thus immutable) Object[] that can be shared by anyone. */
    public static final Object[] EMPTY_OBJECT = new Object[0];

    /** An empty (and thus immutable) String[] that can be shared by anyone. */
    public static final String[] EMPTY_STRING = new String[0];

    /**
     * Looks for an element that tests true for Object equality with the supplied value and
     * returns its index in the array.
     *
     * @return the index of the first matching value if one was found, -1 otherwise.
     */
    public static <T> int indexOf (T[] values, T value)
    {
        int count = (values == null) ? 0 : values.length;
        for (int ii = 0; ii < count; ii++) {
            if (ObjectUtil.equals(values[ii], value)) {
                return ii;
            }
        }
        return -1;
    }

    /**
     * Looks for an element that is equal to the supplied value and returns its index in the array.
     *
     * @return the index of the first matching value if one was found, 1 otherwise.
     */
    public static int indexOf (int[] values, int value)
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
     * Looks for an element that is equal to the supplied value and returns its index in the array.
     *
     * @return the index of the first matching value if one was found, -1 otherwise.
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
     * Looks for an element that is equal to the supplied value and returns its index in the array.
     *
     * @return the index of the first matching value if one was found, -1 otherwise.
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
     * Reverses the elements in the given array.
     *
     * @param values the array to reverse.
     */
    public static void reverse (Object[] values)
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
    public static void reverse (Object[] values, int offset, int length)
    {
        int aidx = offset;
        int bidx = offset + length - 1;
        while (bidx > aidx) {
            Object value = values[aidx];
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
     * @param rnd the source from which random values for shuffling the array are obtained.
     */
    public static void shuffle (byte[] values, Random rnd)
    {
        shuffle(values, 0, values.length, rnd);
    }

    /**
     * Shuffles a subset of elements within the specified array into a random sequence.
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
     * Shuffles a subset of elements within the specified array into a random sequence.
     *
     * @param values the array containing elements to shuffle.
     * @param offset the index at which to start shuffling elements.
     * @param length the number of elements to shuffle.
     * @param rnd the source from which random values for shuffling the array are obtained.
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
     * @param rnd the source from which random values for shuffling the array are obtained.
     */
    public static void shuffle (int[] values, Random rnd)
    {
        shuffle(values, 0, values.length, rnd);
    }

    /**
     * Shuffles a subset of elements within the specified array into a random sequence.
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
     * Shuffles a subset of elements within the specified array into a random sequence.
     *
     * @param values the array containing elements to shuffle.
     * @param offset the index at which to start shuffling elements.
     * @param length the number of elements to shuffle.
     * @param rnd the source from which random values for shuffling the array are obtained.
     */
    public static void shuffle (int[] values, int offset, int length, Random rnd)
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
     * @param rnd the source from which random values for shuffling the array are obtained.
     */
    public static void shuffle (Object[] values, Random rnd)
    {
        shuffle(values, 0, values.length, rnd);
    }

    /**
     * Shuffles a subset of elements within the specified array into a random sequence.
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
     * Shuffles a subset of elements within the specified array into a random sequence.
     *
     * @param values the array containing elements to shuffle.
     * @param offset the index at which to start shuffling elements.
     * @param length the number of elements to shuffle.
     * @param rnd the source from which random values for shuffling the array are obtained.
     */
    public static void shuffle (Object[] values, int offset, int length, Random rnd)
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
     * Performs a binary search, attempting to locate the element that compares as equal
     * to the specified Comparable key. Note that the array elements can implement
     * {@link Comparable} and the key can be of the same type, or the key can be a completely
     * different class that can compare the element type.
     * <b>All comparisons will be called as <tt>key.compareTo(element)</tt></b>.
     *
     * @param array the array to be searched.
     * @param offset the index of the first element in the array to be considered.
     * @param length the number of elements including and following the
     * element at <code>offset</code> to consider when searching.
     * @param key the Comparable that will be used to search the elements of the array.
     *
     * @return the index of the object in question or
     * <code>(-(<i>insertion point</i>) - 1)</code> (always a negative
     * value) if the object was not found in the list.
     */
    public static <T> int binarySearch (
        T[] array, int offset, int length, Comparable<? super T> key)
    {
        int low = offset, high = offset+length-1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            int cmp = key.compareTo(array[mid]);
            if (cmp > 0) {
                low = mid + 1;
            } else if (cmp < 0) {
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
     * @param offset the index of the first element in the array to be* considered.
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
            int mid = (low + high) >>> 1;
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
     * subset of values from indexes <code>0</code> to <code>offset -
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
     * <code>offset - 1</code>, and <code>offset + length</code> to
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
     * subset of values from indexes <code>0</code> to <code>offset -
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
     * <code>offset - 1</code>, and <code>offset + length</code> to
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
     * subset of values from indexes <code>0</code> to <code>offset -
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
     * <code>offset - 1</code>, and <code>offset + length</code> to
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
     * subset of values from indexes <code>0</code> to <code>offset -
     * 1</code> (inclusive) in the supplied array.
     *
     * @param values the array of values to splice.
     * @param offset the index within the <code>values</code> array after
     * which all subsequent values are to be removed.  This must be a
     * valid index within the <code>values</code> array.
     */
    public static <T extends Object> T[] splice (T[] values, int offset)
    {
        int length = (values == null) ? 0 : values.length - offset;
        return splice(values, offset, length);
    }

    /**
     * Creates and returns a new array sized to fit and populated with the
     * concatenated subset of values from indexes <code>0</code> to
     * <code>offset - 1</code>, and <code>offset + length</code> to
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
    public static <T extends Object> T[] splice (T[] values, int offset, int length)
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
        @SuppressWarnings("unchecked")
        T[] nvalues = (T[])Array.newInstance(values.getClass().getComponentType(), size - length);
        System.arraycopy(values, 0, nvalues, 0, offset);
        System.arraycopy(values, tstart, nvalues, offset, size - tstart);
        return nvalues;
    }

    /**
     * Creates and returns a new array sized to fit and populated with the
     * subset of values from indexes <code>0</code> to <code>offset -
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
     * <code>offset - 1</code>, and <code>offset + length</code> to
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
     * specified value inserted into the specified slot.
     */
    public static byte[] insert (byte[] values, byte value, int index)
    {
        byte[] nvalues = new byte[values.length+1];
        if (index > 0) {
            System.arraycopy(values, 0, nvalues, 0, index);
        }
        nvalues[index] = value;
        if (index < values.length) {
            System.arraycopy(values, index, nvalues, index+1, values.length-index);
        }
        return nvalues;
    }

    /**
     * Creates a new array one larger than the supplied array and with the
     * specified value inserted into the specified slot.
     */
    public static short[] insert (short[] values, short value, int index)
    {
        short[] nvalues = new short[values.length+1];
        if (index > 0) {
            System.arraycopy(values, 0, nvalues, 0, index);
        }
        nvalues[index] = value;
        if (index < values.length) {
            System.arraycopy(values, index, nvalues, index+1, values.length-index);
        }
        return nvalues;
    }

    /**
     * Creates a new array one larger than the supplied array and with the
     * specified value inserted into the specified slot.
     */
    public static int[] insert (int[] values, int value, int index)
    {
        int[] nvalues = new int[values.length+1];
        if (index > 0) {
            System.arraycopy(values, 0, nvalues, 0, index);
        }
        nvalues[index] = value;
        if (index < values.length) {
            System.arraycopy(
                values, index, nvalues, index+1, values.length-index);
        }
        return nvalues;
    }

    /**
     * Creates a new array one larger than the supplied array and with the
     * specified value inserted into the specified slot.
     */
    public static float[] insert (float[] values, float value, int index)
    {
        float[] nvalues = new float[values.length+1];
        if (index > 0) {
            System.arraycopy(values, 0, nvalues, 0, index);
        }
        nvalues[index] = value;
        if (index < values.length) {
            System.arraycopy(values, index, nvalues, index+1, values.length-index);
        }
        return nvalues;
    }

    /**
     * Creates a new array one larger than the supplied array and with the
     * specified value inserted into the specified slot. The type of the values
     * array will be preserved.
     */
    public static <T extends Object> T[] insert (T[] values, T value, int index)
    {
        @SuppressWarnings("unchecked")
        T[] nvalues = (T[])Array.newInstance(values.getClass().getComponentType(), values.length+1);
        if (index > 0) {
            System.arraycopy(values, 0, nvalues, 0, index);
        }
        nvalues[index] = value;
        if (index < values.length) {
            System.arraycopy(values, index, nvalues, index+1, values.length-index);
        }
        return nvalues;
    }

    /**
     * Creates a new array one larger than the supplied array and with the
     * specified value inserted into the last slot.
     */
    public static byte[] append (byte[] values, byte value)
    {
        return insert(values, value, values.length);
    }

    /**
     * Creates a new array one larger than the supplied array and with the
     * specified value inserted into the last slot.
     */
    public static short[] append (short[] values, short value)
    {
        return insert(values, value, values.length);
    }

    /**
     * Creates a new array one larger than the supplied array and with the
     * specified value inserted into the last slot.
     */
    public static int[] append (int[] values, int value)
    {
        return insert(values, value, values.length);
    }

    /**
     * Creates a new array one larger than the supplied array and with the
     * specified value inserted into the last slot.
     */
    public static float[] append (float[] values, float value)
    {
        return insert(values, value, values.length);
    }

    /**
     * Creates a new array one larger than the supplied array and with the
     * specified value inserted into the last slot. The type of the values
     * array will be preserved.
     */
    public static <T extends Object> T[] append (T[] values, T value)
    {
        return insert(values, value, values.length);
    }

    /**
     * Creates a new array that contains the contents of the first parameter
     * array followed by those of the second.
     */
    public static int[] concatenate (int[] v1, int[] v2)
    {
        int[] values = new int[v1.length + v2.length];
        System.arraycopy(v1, 0, values, 0, v1.length);
        System.arraycopy(v2, 0, values, v1.length, v2.length);
        return values;
    }

    /**
     * Creates a new array that contains the contents of the first parameter
     * array followed by those of the second.
     */
    public static <T extends Object> T[] concatenate (T[] v1, T[] v2)
    {
        @SuppressWarnings("unchecked")
        T[] values = (T[])Array.newInstance(v1.getClass().getComponentType(),
            v1.length + v2.length);
        System.arraycopy(v1, 0, values, 0, v1.length);
        System.arraycopy(v2, 0, values, v1.length, v2.length);
        return values;
    }

    /**
     * Similar to {@link Collection#toArray}, this method copies the contents
     * of the first parameter to the second, creating a new array of the same
     * type if the destination array is too small to hold the contents of the
     * source.
     */
    public static <S extends Object, T extends S> T[] copy (
        S[] values, T[] store)
    {
        @SuppressWarnings("unchecked")
        T[] dest = (store.length >= values.length) ? store :
            (T[])Array.newInstance(store.getClass().getComponentType(), values.length);
        System.arraycopy(values, 0, dest, 0, values.length);
        return dest;
    }

    /** The default random object used when shuffling an array. */
    protected static final Random _rnd = new Random();
}
