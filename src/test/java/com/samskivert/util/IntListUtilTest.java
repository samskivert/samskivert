//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2011 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import java.util.Arrays;

import org.junit.*;
import static org.junit.Assert.*;

public class IntListUtilTest
{
    @Test
    public void runTest ()
    {
        int[] list = null;

        list = IntListUtil.add(list, 2);
//          System.out.println("add(2): " + StringUtil.toString(list));
        assertTrue("add(2)", Arrays.equals(list, new int[] { 2, 0, 0, 0 }));

        list = IntListUtil.add(list, 5);
//          System.out.println("add(5): " + StringUtil.toString(list));
        assertTrue("add(5)", Arrays.equals(list, new int[] { 2, 5, 0, 0 }));

        IntListUtil.clear(list, 2);
//          System.out.println("clear(2): " + StringUtil.toString(list));
        assertTrue("clear(2)", Arrays.equals(list, new int[] { 0, 5, 0, 0 }));

        boolean contains5 = IntListUtil.contains(list, 5);
//          System.out.println("contains(newBar): " + contains5);
        assertTrue("contains(5)", contains5);

        IntListUtil.removeAt(list, 1);
//          System.out.println("removeAt(1): " + StringUtil.toString(list));
        assertTrue("removeAt(1)",
                   Arrays.equals(list, new int[] { 0, 0, 0, 0 }));

        list = IntListUtil.add(list, 0, 2);
        list = IntListUtil.add(list, 1, 5);
//          System.out.println("add(0, 2) + add(1, 5): " +
//                             StringUtil.toString(list));
        assertTrue("add(0, 2) + add(1, 5))",
                   Arrays.equals(list, new int[] { 2, 5, 0, 0 }));

        IntListUtil.remove(list, 2);
//          System.out.println("remove(2): " + StringUtil.toString(list));
        assertTrue("removeAt(2)",
                   Arrays.equals(list, new int[] { 5, 0, 0, 0 }));

        list = IntListUtil.add(list, 0, 2);
        list = IntListUtil.add(list, 1, 5);
        list = IntListUtil.add(list, 2, 6);
//          System.out.println("add(0, 2) + add(1, 5) + add(2, 6): " +
//                             StringUtil.toString(list));
        assertTrue("add(0, 2) + add(1, 5) + add(2, 6)",
                   Arrays.equals(list, new int[] { 5, 2, 5, 6 }));

        IntListUtil.removeAt(list, 0);
//          System.out.println("removeAt(0): " + StringUtil.toString(list));
        assertTrue("removeAt(0)",
                   Arrays.equals(list, new int[] { 2, 5, 6, 0 }));

        int[] tl = IntListUtil.testAndAdd(list, 5);
//          if (tl == null) {
//              System.out.println("testAndAdd(5): failed: " +
//                                 StringUtil.toString(list));
//          } else {
//              list = tl;
//              System.out.println("testAndAdd(5): added: " +
//                                 StringUtil.toString(list));
//          }
        assertTrue("testAndAdd(5)", tl == null);

        tl = IntListUtil.testAndAdd(list, 7);
//          if (tl == null) {
//              System.out.println("testAndAdd(7): failed: " +
//                                 StringUtil.toString(list));
//          } else {
//              list = tl;
//              System.out.println("testAndAdd(7): added: " +
//                                 StringUtil.toString(list));
//          }
        assertTrue("testAndAdd(7)", tl != null);

        IntListUtil.removeAt(list, 0);
//          System.out.println("removeAt(0): " + StringUtil.toString(list));
        assertTrue("removeAt(0)",
                   Arrays.equals(list, new int[] { 5, 6, 7, 0 }));
    }
}
