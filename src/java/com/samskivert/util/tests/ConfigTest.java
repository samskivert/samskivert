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

import java.util.Iterator;
import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestCase;

import com.samskivert.util.PrefsConfig;
import com.samskivert.util.StringUtil;

/**
 * Tests the {@link Config} class.
 */
public class ConfigTest extends TestCase
{
    public ConfigTest ()
    {
        super(ConfigTest.class.getName());
    }

    public void runTest ()
    {
        PrefsConfig config = new PrefsConfig("rsrc/util/test");

        System.out.println("prop1: " + config.getValue("prop1", 1));
        System.out.println("prop2: " + config.getValue("prop2", "two"));

        int[] ival = new int[] { 1, 2, 3 };
        ival = config.getValue("prop3", ival);
        System.out.println("prop3: " + StringUtil.toString(ival));

        String[] sval = new String[] { "one", "two", "three" };
        sval = config.getValue("prop4", sval);
        System.out.println("prop4: " + StringUtil.toString(sval));

        System.out.println("prop5: " + config.getValue("prop5", "undefined"));

        // now set some properties
        config.setValue("prop1", 15);
        System.out.println("prop1: " + config.getValue("prop1", 1));
        config.setValue("prop2", "three");
        System.out.println("prop2: " + config.getValue("prop2", "two"));

        Iterator<String> iter = config.keys();
        System.out.println("Keys: " + StringUtil.toString(iter));

        config.setValue("sub.sub3", "three");

        Properties subprops = config.getSubProperties("sub");
        System.out.println("Sub: " + StringUtil.toString(subprops.propertyNames()));
    }

    public static Test suite ()
    {
        return new ConfigTest();
    }

    public static void main (String[] args)
    {
        ConfigTest test = new ConfigTest();
        test.runTest();
    }
}
