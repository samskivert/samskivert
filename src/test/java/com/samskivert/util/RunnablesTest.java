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

import org.junit.*;
import static org.junit.Assert.*;

/**
 * Does something extraordinary.
 */
public class RunnablesTest
{
    @Test(expected=IllegalArgumentException.class) public void testWrapStaticFail ()
    {
        Runnables.asRunnable(this, "staticMethod");
    }

    @Test(expected=IllegalArgumentException.class) public void testWrapNonStaticFail ()
    {
        Runnables.asRunnable(RunnablesTest.class, "instanceMethod");
    }

    @Test public void testWrapInstance ()
    {
        Runnables.asRunnable(this, "instanceMethod").run();
        assertTrue(_ranInstance);
    }

    @Test public void testWrapStatic ()
    {
        Runnables.asRunnable(RunnablesTest.class, "staticMethod").run();
        assertTrue(_ranStatic);
    }

    protected void instanceMethod ()
    {
        _ranInstance = true;
    }

    protected static void staticMethod ()
    {
        _ranStatic = true;
    }

    protected boolean _ranInstance;
    protected static boolean _ranStatic;
}
