//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.velocity;

/**
 * The dummy logic is used as a placeholder for URIs that match no normal
 * logic or for which the matching logic could not be instantiated. It
 * does nothing.
 */
public class DummyLogic implements Logic
{
    public void invoke (Application app, InvocationContext context)
        throws Exception
    {
        // we're such a dummy that we do absolutely nothing.
    }
}
