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
