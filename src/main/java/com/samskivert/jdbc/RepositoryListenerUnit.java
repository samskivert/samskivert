//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.jdbc;

import com.samskivert.util.ResultListener;

/**
 * Extends the {@link RepositoryUnit} and integrates with a {@link ResultListener}.
 */
public abstract class RepositoryListenerUnit<T> extends RepositoryUnit
{
    /**
     * Creates a repository listener unit that will report its results to the supplied result
     * listener.
     */
    public RepositoryListenerUnit (ResultListener<T> listener)
    {
        super(String.valueOf(listener));
        _listener = listener;
    }

    /**
     * Creates a repository listener unit that will report its results to the supplied result
     * listener and report the supplied name in {@link #toString}.
     */
    public RepositoryListenerUnit (String name, ResultListener<T> listener)
    {
        super(name);
        _listener = listener;
    }

    /**
     * Called to perform our persistent action and generate our result.
     */
    public abstract T invokePersistResult ()
        throws Exception;

    @Override // from RepositoryUnit
    public void invokePersist ()
        throws Exception
    {
        _result = invokePersistResult();
    }

    @Override // from RepositoryUnit
    public void handleSuccess ()
    {
        _listener.requestCompleted(_result);
    }

    @Override // from RepositoryUnit
    public void handleFailure (Exception pe)
    {
        _listener.requestFailed(pe);
    }

    protected ResultListener<T> _listener;
    protected T _result;
}
