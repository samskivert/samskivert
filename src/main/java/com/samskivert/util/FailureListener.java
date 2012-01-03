//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

/**
 * The pessimist's dream.  This ResultListener silently eats requestCompleted but makes subclasses
 * handle requestFailed.
 */
public abstract class FailureListener<T>
    implements ResultListener<T>
{
    // from interface ResultListener
    public final void requestCompleted (T result)
    {
        // Yeah, yeah, yeah. You did something. Good for you.
    }

    /**
     * Recasts us to look like we're of a different type. We can safely do this because we know
     * that requestCompleted never actually looks at the value passed in.
     */
    public <V> FailureListener<V> retype (Class<V> klass)
    {
        @SuppressWarnings("unchecked") FailureListener<V> casted = (FailureListener<V>)this;
        return casted;
    }
}
