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

import java.util.Properties;

import org.junit.*;
import static org.junit.Assert.*;

/**
 * Our test properties files:
 *
 * <pre>
 * lib/test-c.jar:
 * _package = testC
 * _overrides = testAL, testBR
 *
 * three = testC - three
 *
 * lib/test-br.jar:
 * _package = testBR
 * _overrides = testAR
 *
 * two = testBR - two
 * four = testBR - four
 *
 * lib/test-ar.jar:
 * _package = testAR
 * _overrides = test
 *
 * one = testAR - one
 * two = testAR - two
 * four = testAR - four
 *
 * lib/test-al.jar:
 * _package = testAL
 * _overrides = test
 *
 * one = testAL - one
 *
 * lib/test.jar:
 * _package = test
 *
 * one = test - one
 * two = test - two
 * three = test - three
 * </pre>
 */
public class ConfigUtilTest
{
    @Test
    public void runTest ()
    {
        try {
            String path = "util/child.properties";
            Properties props = ConfigUtil.loadInheritedProperties(path);
            assertTrue("props valid", props.toString().equals(DUMP));

            path = "test/test.properties";
            props = ConfigUtil.loadInheritedProperties(path);
            assertTrue("props valid", props.toString().equals(IDUMP));

        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    protected static final String DUMP =
        "{prop4=one, two, three,, and a half, four, " +
        "prop3=9, 8, 7, 6, prop2=twenty five, prop1=25, " +
        "sub.sub2=whee!, sub.sub1=5}";

    protected static final String IDUMP = "{two=testBR - two, " +
        "one=testAR - one, three=testC - three, four=testBR - four}";
}
