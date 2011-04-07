//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2011 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

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
