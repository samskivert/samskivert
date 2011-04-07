//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2011 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

public class HashIntSetTest extends IntSetTestBase
{
    @Override
    protected AbstractIntSet createSet ()
    {
        return new HashIntSet();
    }

    @Override
    protected AbstractIntSet createSet (int[] values)
    {
        return new HashIntSet(values);
    }
}
