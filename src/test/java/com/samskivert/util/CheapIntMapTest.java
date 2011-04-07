//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2011 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import org.junit.*;
import static org.junit.Assert.*;

/**
 * Tests the {@link CheapIntMap} class.
 */
public class CheapIntMapTest
{
    @Test
    public void runTest ()
    {
        CheapIntMap map = new CheapIntMap(10);
        for (int ii = 0; ii < 100; ii += 20) {
            map.put(ii, ii);
        }
        for (int ii = 0; ii < 5; ii++) {
            map.put(ii, ii);
        }

        for (int ii = 0; ii < 100; ii++) {
            Object val = map.get(ii);
            assertTrue(val == null || (ii < 5) || (ii%20 == 0));
            if (val != null) {
                assertEquals(ii, val);
            }
        }

        for (int ii = 0; ii < 100; ii += 20) {
            map.remove(ii);
        }
        for (int ii = 10; ii > 0; ii--) {
            map.put(ii, ii);
        }

        for (int ii = 0; ii < 100; ii++) {
            Object val = map.get(ii);
            assertTrue(((ii == 0 || ii > 10) && val == null) ||
                       ((ii <= 10) && val != null));
            if (val != null) {
                assertEquals(ii, val);
            }
        }
    }
}
