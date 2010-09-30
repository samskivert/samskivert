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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.*;
import static org.junit.Assert.*;

/**
 * Tests the {@link Folds} class.
 */
public class FoldsTest
{
    public static List<Integer> INTS = Arrays.asList(5, 4, 3, 6, 1);
    public static List<Integer> EMPTY_INTS = Collections.<Integer>emptyList();

    public static Folds.R<Integer> INT_MAX = new Folds.R<Integer>() {
        public Integer apply (Integer zero, Integer elem) {
            return Math.max(zero, elem);
        }
    };

    @Test public void testFold () {
        assertTrue(6 == Folds.foldLeft(INT_MAX, 0, INTS));
    }

    @Test public void testReduce () {
        assertTrue(6 == Folds.reduceLeft(INT_MAX, INTS));
    }

    @Test(expected=NoSuchElementException.class)
    public void testEmptyReduce () {
        Folds.reduceLeft(INT_MAX, EMPTY_INTS);
    }

    @Test public void testPromotingFold ()
    {
        assertTrue(6L + Integer.MAX_VALUE == Folds.foldLeft(new Folds.F<Long,Integer>() {
            public Long apply (Long zero, Integer elem) {
                return zero + elem;
            }
        }, 0L, Arrays.asList(Integer.MAX_VALUE, 1, 2, 3)));
    }

    @Test public void testIntSum () {
        assertEquals(19, Folds.sum(0, INTS));
    }
    @Test public void testEmptyIntSum () {
        assertEquals(0, Folds.sum(0, EMPTY_INTS));
    }

    public static List<Long> LONGS = Arrays.asList(5L, 4L, 3L, 6L, 1L);
    public static List<Long> EMPTY_LONGS = Collections.<Long>emptyList();

    @Test public void testLongSum () {
        assertTrue(19L == Folds.sum(0, LONGS));
    }
    @Test public void testEmptyLongSum () {
        assertTrue(0L == Folds.sum(0, EMPTY_LONGS));
    }
}
