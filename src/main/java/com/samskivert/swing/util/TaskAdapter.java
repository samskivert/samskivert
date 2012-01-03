//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.swing.util;

/**
 * A helper class for easily instantiating {@link Task} instances.
 */
public class TaskAdapter implements Task
{
    /**
     * Always returns null by default. Override this method to implement
     * the desired functionality.
     *
     * @see Task#invoke
     */
    public Object invoke () throws Exception
    {
        return null;
    }

    /**
     * Always returns false by default. Override this method to implement
     * the desired functionality.
     *
     * @see Task#abort
     */
    public boolean abort ()
    {
        return false;
    }
}
