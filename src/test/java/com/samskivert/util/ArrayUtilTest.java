//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2011 Michael Bayne, et al.
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

import java.util.Arrays;

import org.junit.*;
import static org.junit.Assert.*;

/**
 * Tests the {@link ArrayUtil} class.
 */
public class ArrayUtilTest
{
    @Test
    public void runTest ()
    {
        // test reversing an array
        int[] values = new int[] { 0 };
        int[] work = values.clone();
        ArrayUtil.reverse(work);
        assertEquals("(0)", StringUtil.toString(work));

        values = new int[] { 0, 1, 2 };
        work = values.clone();
        ArrayUtil.reverse(work);
        assertEquals("(2, 1, 0)", StringUtil.toString(work));

        work = values.clone();
        ArrayUtil.reverse(work, 0, 2);
        assertEquals("(1, 0, 2)", StringUtil.toString(work));

        work = values.clone();
        ArrayUtil.reverse(work, 1, 2);
        assertEquals("(0, 2, 1)", StringUtil.toString(work));

        values = new int[] { 0, 1, 2, 3, 4 };
        work = values.clone();
        ArrayUtil.reverse(work, 1, 3);
        assertEquals("(0, 3, 2, 1, 4)", StringUtil.toString(work));

        values = new int[] { 0, 1, 2, 3 };
        work = values.clone();
        ArrayUtil.reverse(work);
        assertEquals("(3, 2, 1, 0)", StringUtil.toString(work));

        // test shuffling two elements
        values = new int[] { 0, 1 };
        work = values.clone();
        ArrayUtil.shuffle(work, 0, 1);
        assertEquals("(0, 1)", StringUtil.toString(work));

        work = values.clone();
        ArrayUtil.shuffle(work, 1, 1);
        assertEquals("(0, 1)", StringUtil.toString(work));

        work = values.clone();
        ArrayUtil.shuffle(work);
        Arrays.sort(work); // return them to canonical order
        assertEquals("(0, 1)", StringUtil.toString(work));

        // test shuffling three elements
        values = new int[] { 0, 1, 2 };
        work = values.clone();
        ArrayUtil.shuffle(work, 0, 2);
        assertTrue(StringUtil.toString(work).endsWith(", 2)"));

        work = values.clone();
        ArrayUtil.shuffle(work, 1, 2);
        assertTrue(StringUtil.toString(work).startsWith("(0, "));

        work = values.clone();
        ArrayUtil.shuffle(work);
        Arrays.sort(work); // return them to canonical order
        assertEquals("(0, 1, 2)", StringUtil.toString(work));

        // test shuffling ten elements
        values = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        work = values.clone();
        ArrayUtil.shuffle(work, 0, 5);
        assertTrue(StringUtil.toString(work).endsWith(", 5, 6, 7, 8, 9)"));

        work = values.clone();
        ArrayUtil.shuffle(work, 5, 5);
        assertTrue(StringUtil.toString(work).startsWith("(0, 1, 2, 3, 4, "));

        work = values.clone();
        ArrayUtil.shuffle(work);
        Arrays.sort(work); // return them to canonical order
        assertEquals("(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)", StringUtil.toString(work));

        // test splicing with simple truncate beyond offset
        values = new int[] { 0, 1, 2 };
        work = values.clone();
        work = ArrayUtil.splice(work, 0);
        assertEquals("()", StringUtil.toString(work));

        work = values.clone();
        work = ArrayUtil.splice(work, 1);
        assertEquals("(0)", StringUtil.toString(work));

        work = values.clone();
        work = ArrayUtil.splice(work, 2);
        assertEquals("(0, 1)", StringUtil.toString(work));

        values = new int[] { 0 };
        work = values.clone();
        work = ArrayUtil.splice(work, 0);
        assertEquals("()", StringUtil.toString(work));

        // test splicing out a single element
        values = new int[] { 0, 1, 2 };
        work = values.clone();
        work = ArrayUtil.splice(work, 0, 1);
        assertEquals("(1, 2)", StringUtil.toString(work));

        work = values.clone();
        work = ArrayUtil.splice(work, 1, 1);
        assertEquals("(0, 2)", StringUtil.toString(work));

        work = values.clone();
        work = ArrayUtil.splice(work, 2, 1);
        assertEquals("(0, 1)", StringUtil.toString(work));

        // test splicing out two elements
        values = new int[] { 0, 1, 2, 3 };
        work = values.clone();
        work = ArrayUtil.splice(work, 0, 2);
        assertEquals("(2, 3)", StringUtil.toString(work));

        work = values.clone();
        work = ArrayUtil.splice(work, 1, 2);
        assertEquals("(0, 3)", StringUtil.toString(work));

        work = values.clone();
        work = ArrayUtil.splice(work, 2, 2);
        assertEquals("(0, 1)", StringUtil.toString(work));
    }
}
