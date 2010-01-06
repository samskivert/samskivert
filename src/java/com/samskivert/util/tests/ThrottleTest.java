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

package com.samskivert.util.tests;

import junit.framework.Test;
import junit.framework.TestCase;

import com.samskivert.util.Throttle;

/**
 * A test case for {@link Throttle}.
 */
public class ThrottleTest extends TestCase
{
    public static class TestThrottle extends Throttle
    {
        public TestThrottle (int maxOps, long period)
        {
            super(maxOps, period);
        }

        public String opsToString ()
        {
            String hist = String.valueOf(_ops[_lastOp]);
            for (int ii = 1; ii < _ops.length; ++ii) {
                long tn = _ops[(_lastOp + ii) % _ops.length];
                hist += ", " + String.valueOf(tn);
            }
            return hist;
        }
    }

    public static Test suite ()
    {
        return new ThrottleTest();
    }

    public static void main (String[] args)
    {
        ThrottleTest test = new ThrottleTest();
        test.runTest();
    }

    public ThrottleTest ()
    {
        super(ThrottleTest.class.getName());
    }

    @Override
    public void runTest ()
    {
        testUpdate(4);
        testUpdate(5);
        testUpdate(6);
        testUpdate(7);
        testUpdate(8);
    }

    protected void testUpdate (int opCount)
    {
        // set up a throttle for 5 ops per millisecond
        TestThrottle throttle = new TestThrottle(5, 1);
        System.out.println("Testing updates with " + opCount + " operations");
        long time = 0;
        for (int ii = 0; ii < opCount; ++ii) {
            throttle.throttleOp(time += 1);
        }
        System.out.println("    " + throttle.opsToString());
        throttle.reinit(10, 1);
        for (int ii = 0; ii < opCount; ++ii) {
            throttle.throttleOp(time += 1);
        }
        System.out.println("    " + throttle.opsToString());
        throttle.reinit(5, 1);
        System.out.println("    " + throttle.opsToString());
        for (int ii = 0; ii < opCount; ++ii) {
            throttle.throttleOp(time += 1);
        }
        System.out.println("    " + throttle.opsToString());
        throttle.reinit(10, 1);
        System.out.println("    " + throttle.opsToString());
    }
}
