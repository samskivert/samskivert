//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.jdbc;

import com.samskivert.util.Invoker;

import static com.samskivert.jdbc.Log.log;

/**
 * Extends {@link com.samskivert.util.Invoker.Unit} and specializes it for writing to a database
 * repository.
 */
public abstract class WriteOnlyUnit extends Invoker.Unit
{
    /**
     * Creates a unit which will report the supplied name in {@link #toString} and in the event of
     * failure.
     */
    public WriteOnlyUnit (String name)
    {
        super(name);
    }

    @Override // from abstract Invoker.Unit
    public boolean invoke ()
    {
        try {
            invokePersist();
            return false;
        } catch (Exception pe) {
            _error = pe;
            return true;
        }
    }

    @Override // from Invoker.Unit
    public void handleResult ()
    {
        handleFailure(_error);
    }

    /**
     * Called to perform our persistent actions.
     */
    public abstract void invokePersist ()
        throws Exception;

    /**
     * Called if our persistent actions failed, back on the non-invoker thread. The default
     * implementation logs an error message and a stack trace.
     */
    public void handleFailure (Exception e)
    {
        log.warning(getFailureMessage(), e);
    }

    /**
     * Returns the error message to be logged if {@link #invokePersist} throws an exception.
     */
    protected String getFailureMessage ()
    {
        return this + " failed.";
    }

    protected Exception _error;
}
