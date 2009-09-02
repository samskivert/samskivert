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
    public abstract T getResult ()
        throws Exception;

    /**
     * Operates on the result from <code>getResult</code> back on the main thread, if
     * <code>getResult</code> succeeded.
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
        _result = getResult();
    }

    protected T _result;
}
