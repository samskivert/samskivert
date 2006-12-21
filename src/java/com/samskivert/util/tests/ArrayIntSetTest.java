//
// $Id: ArrayIntSetTest.java,v 1.5 2004/02/25 13:21:08 mdb Exp $
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

package com.samskivert.util.tests;

import java.util.Arrays;
import java.util.Random;

import junit.framework.Test;
import junit.framework.TestCase;

import com.samskivert.util.ArrayIntSet;

public class ArrayIntSetTest extends TestCase
{
    public ArrayIntSetTest ()
    {
        super(ArrayIntSetTest.class.getName());
    }

    public void runTest ()
    {
        ArrayIntSet set = new ArrayIntSet();
        set.add(new int[] { 3, 5, 5, 9, 5, 7, 1 });

        int[] values = { 1, 3, 5, 7, 9 };
        int[] setvals = set.toIntArray();

        assertTrue("values equal", Arrays.equals(values, setvals));

        ArrayIntSet set1 = new ArrayIntSet();
        set1.add(new int[] { 1, 2, 3, 5, 7, 12, 19, 35 });
        ArrayIntSet set2 = new ArrayIntSet();
        set2.add(new int[] { 3, 4, 5, 11, 13, 17, 19, 25 });

        ArrayIntSet set3 = new ArrayIntSet();
        set3.add(new int[] { 3, 5, 19 });

        // intersect sets 1 and 2; making sure that retainAll() returns
        // true to indicate that set 1 was modified
        assertTrue("retain modifies", set1.retainAll(set2));

        // make sure the intersections were correct
        assertTrue("intersection", set1.equals(set3));

        // make sure that retainAll returns false if we do something that
        // doesn't modify the set
        assertTrue("retain didn't modify", !set1.retainAll(set3));

        Random rando = new Random();
        for (int i = 0; i < 1000; i++) {
            ArrayIntSet s1 = new ArrayIntSet();
            ArrayIntSet s2 = new ArrayIntSet();
            ArrayIntSet s3 = new ArrayIntSet();

            // add some odd numbers to all three sets
            for (int c = 0; c < 100; c++) {
                int r = rando.nextInt(5000) * 2 + 1;
                s1.add(r);
                s2.add(r);
                s3.add(r);
            }

            // now add some even numbers to each of the first two sets in
            // non-overlapping ranges
            for (int c = 0; c < 100; c++) {
                s1.add(rando.nextInt(5000)*2);
                s2.add(rando.nextInt(5000)*2 + 15000);
            }

            // now ensure that s1.retainAll(s2) equals s3
            s1.retainAll(s2);
            assertTrue("random intersection", s1.equals(s3));
        }
    }

    public static Test suite ()
    {
        return new ArrayIntSetTest();
    }

    public static void main (String[] args)
    {
        ArrayIntSetTest test = new ArrayIntSetTest();
        test.runTest();
    }
}
