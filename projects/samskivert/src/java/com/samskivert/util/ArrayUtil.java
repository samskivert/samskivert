//
// $Id: ArrayUtil.java,v 1.1 2001/12/14 18:57:36 shaper Exp $
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

/**
 * Miscellaneous utility routines for working with arrays.
 */
public class ArrayUtil
{
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
     * Shuffles a subset of elements within the specified array.
     *
     * @param values the array containing elements to shuffle.
     * @param offset the index at which to start shuffling elements.
     * @param length the number of elements to shuffle.
     */
    public static void shuffle (int[] values, int offset, int length)
    {
        for (int ii = (offset + length); ii > offset; ii--) {
            int temp = values[ii - 1];
            int idx = _rnd.nextInt(ii);
            values[ii - 1] = values[idx];
            values[idx] = temp;
        }
    }

    /** The random object used when shuffling an array. */
    protected static Random _rnd = new Random();
}
