//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2007 Michael Bayne
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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import junit.framework.Test;
import junit.framework.TestCase;

import com.samskivert.util.CollectionUtil;
import com.samskivert.util.ComparableArrayList;
import com.samskivert.util.StringUtil;

/**
 * Tests the {@link CollectionUtil} class.
 */
public class CollectionUtilTest extends TestCase
{
    public CollectionUtilTest ()
    {
        super(CollectionUtil.class.getName());
    }

    @Override
    public void runTest ()
    {
        ArrayList<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < 100; i++) {
            list.add(new Integer(i));
        }

        for (int i = 0; i < 10; i++) {
            List<Integer> subset = CollectionUtil.selectRandomSubset(list, 10);
            // System.out.println(StringUtil.toString(subset));
            assertTrue("length == 10", subset.size() == 10);
        }

        // test comparable array list insertion
        Random rand = new Random();
        ComparableArrayList<Integer> slist = new ComparableArrayList<Integer>();
        for (int ii = 0; ii < 25; ii++) {
            Integer value = new Integer(rand.nextInt(100));
            slist.insertSorted(value);
        }
        System.out.println(StringUtil.toString(slist));
    }

    public static Test suite ()
    {
        return new CollectionUtilTest();
    }

    public static void main (String[] args)
    {
        CollectionUtilTest test = new CollectionUtilTest();
        test.runTest();
    }
}
