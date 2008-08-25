//
// $Id: CollectionUtilTest.java,v 1.2 2002/09/23 01:45:47 mdb Exp $

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
