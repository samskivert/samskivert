//
// $Id: CollectionUtilTest.java,v 1.2 2002/09/23 01:45:47 mdb Exp $

package com.samskivert.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import junit.framework.Test;
import junit.framework.TestCase;

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
        ArrayList list = new ArrayList();
        for (int i = 0; i < 100; i++) {
            list.add(new Integer(i));
        }

        for (int i = 0; i < 10; i++) {
            List subset = CollectionUtil.selectRandomSubset(list, 10);
            // System.out.println(StringUtil.toString(subset));
            assertTrue("length == 10", subset.size() == 10);
        }

        // test sortable array list insertion
        Random rand = new Random();
        SortableArrayList slist = new SortableArrayList();
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
