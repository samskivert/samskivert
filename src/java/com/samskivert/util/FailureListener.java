package com.samskivert.util;

/**
 * The pessimist's dream.  This ResultListener silently eats requestCompleted but makes subclasses
 * handle requestFailed.
 */
public abstract class FailureListener
    implements ResultListener
{
    // documentation inherited from interface ResultListener
    public void requestCompleted (Object result)
    {
        // Yeah, yeah, yeah. You did something. Good for you.
    }
}
