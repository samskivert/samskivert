//
// $Id: ConfigUtilTest.java,v 1.3 2002/04/11 04:07:42 mdb Exp $
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

import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestCase;

import com.samskivert.Log;

public class ConfigUtilTest extends TestCase
{
    public ConfigUtilTest ()
    {
        super(ConfigUtilTest.class.getName());
    }

    public void runTest ()
    {
        try {
            String path = "/rsrc/util/test.properties";
            Properties props = ConfigUtil.loadInheritedProperties(path);
            assertTrue("props valid", props.toString().equals(DUMP));

        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    public static Test suite ()
    {
        return new ConfigUtilTest();
    }

    protected static final String DUMP =
        "{prop4=one, two, three,, and a half, four, " +
        "prop3=9, 8, 7, 6, prop2=twenty five, prop1=25, " +
        "sub.sub2=whee!, sub.sub1=5}";
}
