//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

/**
 * A building-block for writing an Interator.
 */
public abstract class AbstractInterator
    implements Interator
{
    // from super interface Iterator<Integer>
    public Integer next ()
    {
        return Integer.valueOf(nextInt());
    }

    // from super interface Iterator<Integer>
    public void remove ()
    {
        throw new UnsupportedOperationException();
    }
}
