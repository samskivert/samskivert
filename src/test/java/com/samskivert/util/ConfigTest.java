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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.junit.*;
import static org.junit.Assert.*;

/**
 * Tests the {@link Config} class.
 */
public class ConfigTest
{
    @Test
    public void runTest ()
    {
        // util/test.properties contains:
        // prop1 = 25
        // prop2 = twenty five
        // prop3 = 9, 8, 7, 6
        // prop4 = one, two, three,, and a half, four
        // sub.sub1 = 5
        // sub.sub2 = whee!

        Config config = new Config("util/test");

        assertEquals(25, config.getValue("prop1", 1));
        assertEquals("twenty five", config.getValue("prop2", "two"));
        assertArrayEquals(new int[] { 9, 8, 7, 6 }, config.getValue("prop3", (int[])null));
        assertArrayEquals(new String[] { "one", "two", "three, and a half", "four" },
                          config.getValue("prop4", (String[])null));
        assertEquals("undefined", config.getValue("prop5", "undefined"));

        // now set some properties
        PrefsConfig pconfig = new PrefsConfig("util/test");
        pconfig.setValue("prop1", 15);
        assertEquals(15, pconfig.getValue("prop1", 1));
        pconfig.setValue("prop2", "three");
        assertEquals("three", pconfig.getValue("prop2", "two"));

        // fiddly with sub-properties
        pconfig.setValue("sub.sub3", "three");
        Properties subprops = pconfig.getSubProperties("sub");
        assertEquals("three", subprops.getProperty("sub3"));
        // oh Java, you're so awesome
        List<String> slist = new ArrayList<String>();
        for (Enumeration<?> iter = subprops.propertyNames(); iter.hasMoreElements(); ) {
            slist.add(iter.nextElement().toString());
        }
        Collections.sort(slist);
        assertEquals("(sub1, sub2, sub3)", StringUtil.toString(slist));

        // check the whole shebang
        List<String> list = CollectionUtil.addAll(new ArrayList<String>(), pconfig.keys());
        Collections.sort(list);
        assertEquals("(prop1, prop2, prop3, prop4, sub.sub1, sub.sub2, sub.sub3)",
                     StringUtil.toString(list));
    }
}
