//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import java.util.Iterator;

/**
 * Can be used as an Iterator, and all Objects returned should be Integer objects, but can also can
 * avoid boxing by calling {@link #nextInt}.
 */
public interface Interator extends Iterator<Integer>
{
    /**
     * @return the next int value from this Iterator.
     */
    public int nextInt ();
}
