//
// $Id: IntListUtilTest.java,v 1.1 2001/11/06 02:13:29 mdb Exp $
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

package com.samskivert.util;

import junit.framework.Test;
import junit.framework.TestCase;

import com.samskivert.Log;
import com.samskivert.test.TestUtil;

public class IntListUtilTest extends TestCase
{
    public IntListUtilTest ()
    {
        super(IntListUtilTest.class.getName());
    }

    public void runTest ()
    {
        int[] list = null;

        list = IntListUtil.add(list, 2);
//          System.out.println("add(2): " + StringUtil.toString(list));
        assert("add(2)", arraysEqual(list, new int[] { 2, 0, 0, 0 }));

        list = IntListUtil.add(list, 5);
//          System.out.println("add(5): " + StringUtil.toString(list));
        assert("add(5)", arraysEqual(list, new int[] { 2, 5, 0, 0 }));

        IntListUtil.clear(list, 2);
//          System.out.println("clear(2): " + StringUtil.toString(list));
        assert("clear(2)", arraysEqual(list, new int[] { 0, 5, 0, 0 }));

        boolean contains5 = IntListUtil.contains(list, 5);
//          System.out.println("contains(newBar): " + contains5);
        assert("contains(5)", contains5);

        IntListUtil.removeAt(list, 1);
//          System.out.println("removeAt(1): " + StringUtil.toString(list));
        assert("removeAt(1)", arraysEqual(list, new int[] { 0, 0, 0, 0 }));

        list = IntListUtil.add(list, 0, 2);
        list = IntListUtil.add(list, 1, 5);
//          System.out.println("add(0, 2) + add(1, 5): " +
//                             StringUtil.toString(list));
        assert("add(0, 2) + add(1, 5))",
               arraysEqual(list, new int[] { 2, 5, 0, 0 }));

        IntListUtil.remove(list, 2);
//          System.out.println("remove(2): " + StringUtil.toString(list));
        assert("removeAt(2)", arraysEqual(list, new int[] { 5, 0, 0, 0 }));

        list = IntListUtil.add(list, 0, 2);
        list = IntListUtil.add(list, 1, 5);
        list = IntListUtil.add(list, 2, 6);
//          System.out.println("add(0, 2) + add(1, 5) + add(2, 6): " +
//                             StringUtil.toString(list));
        assert("add(0, 2) + add(1, 5) + add(2, 6)",
               arraysEqual(list, new int[] { 5, 2, 5, 6 }));

        IntListUtil.removeAt(list, 0);
//          System.out.println("removeAt(0): " + StringUtil.toString(list));
        assert("removeAt(0)", arraysEqual(list, new int[] { 2, 5, 6, 0 }));

        int[] tl = IntListUtil.testAndAdd(list, 5);
//          if (tl == null) {
//              System.out.println("testAndAdd(5): failed: " +
//                                 StringUtil.toString(list));
//          } else {
//              list = tl;
//              System.out.println("testAndAdd(5): added: " +
//                                 StringUtil.toString(list));
//          }
        assert("testAndAdd(5)", tl == null);

        tl = IntListUtil.testAndAdd(list, 7);
//          if (tl == null) {
//              System.out.println("testAndAdd(7): failed: " +
//                                 StringUtil.toString(list));
//          } else {
//              list = tl;
//              System.out.println("testAndAdd(7): added: " +
//                                 StringUtil.toString(list));
//          }
        assert("testAndAdd(7)", tl != null);

        IntListUtil.removeAt(list, 0);
//          System.out.println("removeAt(0): " + StringUtil.toString(list));
        assert("removeAt(0)", arraysEqual(list, new int[] { 5, 6, 7, 0 }));
    }

    protected boolean arraysEqual (int[] a1, int[] a2)
    {
        if (a1.length != a2.length) {
            return false;
        }

        for (int i = 0; i < a1.length; i++) {
            if (a1[i] != a2[i]) {
                return false;
            }
        }

        return true;
    }

    public static Test suite ()
    {
        return new IntListUtilTest();
    }
}
