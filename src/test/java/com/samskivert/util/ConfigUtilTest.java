//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import java.util.HashMap;
import java.util.Map;
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
            assertEquals("25", props.get("prop1"));
            assertEquals("twenty five", props.get("prop2"));
            assertEquals("9, 8, 7, 6", props.get("prop3"));
            assertEquals("one, two, three,, and a half, four", props.get("prop4"));
            assertEquals("5", props.get("sub.sub1"));
            assertEquals("whee!", props.get("sub.sub2"));

            path = "test/test.properties";
            props = ConfigUtil.loadInheritedProperties(path);
            assertEquals("testAR - one", props.get("one"));
            assertEquals("testBR - two", props.get("two"));
            assertEquals("testC - three", props.get("three"));
            assertEquals("testBR - four", props.get("four"));

        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}
