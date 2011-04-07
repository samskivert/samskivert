//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2011 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.*;
import static org.junit.Assert.*;

/**
 * Tests the {@link CollectionUtil} class.
 */
public class CollectionUtilTest
{
    @Test public void testSelectRandomSubset ()
    {
        ArrayList<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < 100; i++) {
            list.add(i);
        }

        for (int i = 0; i < 10; i++) {
            List<Integer> subset = CollectionUtil.selectRandomSubset(list, 10);
            // System.out.println(StringUtil.toString(subset));
            assertTrue("length == 10", subset.size() == 10);
        }
    }

    @Test public void testInsertSorted ()
    {
        // test comparable array list insertion
        Random rand = new Random();
        ComparableArrayList<Integer> slist = new ComparableArrayList<Integer>();
        for (int ii = 0; ii < 25; ii++) {
            slist.insertSorted(rand.nextInt(100));
        }
        for (int ii = 0; ii < slist.size()-1; ii++) {
            assertTrue(slist.get(ii) <= slist.get(ii+1));
        }
    }
}
