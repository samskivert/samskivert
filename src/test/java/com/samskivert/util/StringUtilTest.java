//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import org.junit.*;
import static org.junit.Assert.*;

public class StringUtilTest
{
    @Test
    public void runTest ()
    {
        String source = "mary, had, a,, little, lamb, and, a, comma,,";

        // split the source string into tokens
        String[] tokens = StringUtil.parseStringArray(source);
        assertTrue("tokens.length == 7", tokens.length == 7);

        // now join them back together
        String joined = StringUtil.joinEscaped(tokens);
        assertTrue("joined.equals(source)", joined.equals(source));

        // make sure null to empty string works
        tokens = new String[] { "this", null, "is", null, "a", null, "test" };
        joined = StringUtil.joinEscaped(tokens);
        assertTrue("null elements work", joined.equals("this, , is, , a, , test"));
    }
}
