//
// $Id: ArrayUtil.java,v 1.6 2002/07/11 01:33:54 shaper Exp $
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

import java.util.Random;

import com.samskivert.Log;

/**
 * Miscellaneous utility routines for working with arrays.
 */
public class ArrayUtil
{
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

    public static void main (String[] args)
    {
        // test reversing an array
        int[] values = new int[] { 0 };
        int[] work = (int[])values.clone();
        reverse(work);
        Log.info("reverse: " + StringUtil.toString(work));

        values = new int[] { 0, 1, 2 };
        work = (int[])values.clone();
        reverse(work);
        Log.info("reverse: " + StringUtil.toString(work));

        work = (int[])values.clone();
        reverse(work, 0, 2);
        Log.info("reverse first-half: " + StringUtil.toString(work));

        work = (int[])values.clone();
        reverse(work, 1, 2);
        Log.info("reverse second-half: " + StringUtil.toString(work));

        values = new int[] { 0, 1, 2, 3, 4 };
        work = (int[])values.clone();
        reverse(work, 1, 3);
        Log.info("reverse middle: " + StringUtil.toString(work));

        values = new int[] { 0, 1, 2, 3 };
        work = (int[])values.clone();
        reverse(work);
        Log.info("reverse even: " + StringUtil.toString(work));

        // test shuffling two elements
        values = new int[] { 0, 1 };
        work = (int[])values.clone();
        shuffle(work, 0, 1);
        Log.info("first-half shuffle: " + StringUtil.toString(work));

        work = (int[])values.clone();
        shuffle(work, 1, 1);
        Log.info("second-half shuffle: " + StringUtil.toString(work));

        work = (int[])values.clone();
        shuffle(work);
        Log.info("full shuffle: " + StringUtil.toString(work));

        // test shuffling three elements
        values = new int[] { 0, 1, 2 };
        work = (int[])values.clone();
        shuffle(work, 0, 2);
        Log.info("first-half shuffle: " + StringUtil.toString(work));

        work = (int[])values.clone();
        shuffle(work, 1, 2);
        Log.info("second-half shuffle: " + StringUtil.toString(work));

        work = (int[])values.clone();
        shuffle(work);
        Log.info("full shuffle: " + StringUtil.toString(work));

        // test shuffling ten elements
        values = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        work = (int[])values.clone();
        shuffle(work, 0, 5);
        Log.info("first-half shuffle: " + StringUtil.toString(work));

        work = (int[])values.clone();
        shuffle(work, 5, 5);
        Log.info("second-half shuffle: " + StringUtil.toString(work));

        work = (int[])values.clone();
        shuffle(work);
        Log.info("full shuffle: " + StringUtil.toString(work));
    }

    /** The random object used when shuffling an array. */
    protected static Random _rnd = new Random();
}
