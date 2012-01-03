//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.jdbc;


/**
 * A RepositoryUnit that returns a single result from its database operations and operates on that.
 */
public abstract class ResultUnit<T> extends RepositoryUnit
{
    public ResultUnit (String name)
    {
        super(name);
    }

    /**
     * Performs actions on the database and returns exciting data.
     */
    public abstract T computeResult ()
        throws Exception;

    /**
     * Operates on the result from <code>computeResult</code> back on the main thread, if
     * <code>computeResult</code> succeeded.
     */
    public abstract void handleResult (T result);

    @Override
    public void handleSuccess ()
    {
        handleResult(_result);
    }

    @Override
    public void invokePersist ()
        throws Exception
    {
        _result = computeResult();
    }

    protected T _result;
}
