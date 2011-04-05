//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2011 Michael Bayne, et al.
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

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
