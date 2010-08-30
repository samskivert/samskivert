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
