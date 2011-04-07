//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2011 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

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
