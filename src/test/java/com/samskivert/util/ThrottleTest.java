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

import org.junit.*;
import static org.junit.Assert.*;

/**
 * A test case for {@link Throttle}.
 */
public class ThrottleTest
{
    public static class TestThrottle extends Throttle
    {
        public TestThrottle (int maxOps, long period)
        {
            super(maxOps, period);
        }

        public String opsToString ()
        {
            StringBuilder hist = new StringBuilder().append(_ops[_lastOp]);
            for (int ii = 1; ii < _ops.length; ++ii) {
                long tn = _ops[(_lastOp + ii) % _ops.length];
                hist.append(tn);
            }
            return hist.toString();
        }
    }

    @Test
    public void runTest ()
    {
        testUpdate(4, "01234", "0012345678", "00123", "39101112", "0000039101112");
        testUpdate(5, "12345", "12345678910", "12345", "1112131415", "000001112131415");
        testUpdate(6, "23456", "3456789101112", "34567", "1415161718", "000001415161718");
        testUpdate(7, "34567", "567891011121314", "56789", "1718192021", "000001718192021");
        testUpdate(8, "45678", "78910111213141516", "7891011", "2021222324", "000002021222324");
    }

    protected void testUpdate (int opCount, String... results)
    {
        // set up a throttle for 5 ops per millisecond
        TestThrottle throttle = new TestThrottle(5, 1);
        // System.out.println("Testing updates with " + opCount + " operations");
        long time = 0;
        for (int ii = 0; ii < opCount; ++ii) {
            throttle.throttleOp(time += 1);
        }
        assertEquals(results[0], throttle.opsToString());
        throttle.reinit(10, 1);
        for (int ii = 0; ii < opCount; ++ii) {
            throttle.throttleOp(time += 1);
        }
        assertEquals(results[1], throttle.opsToString());
        throttle.reinit(5, 1);
        assertEquals(results[2], throttle.opsToString());
        for (int ii = 0; ii < opCount; ++ii) {
            throttle.throttleOp(time += 1);
        }
        assertEquals(results[3], throttle.opsToString());
        throttle.reinit(10, 1);
        assertEquals(results[4], throttle.opsToString());
    }
}
