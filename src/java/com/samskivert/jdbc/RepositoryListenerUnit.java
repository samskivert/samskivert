//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2006 Michael Bayne
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

import com.samskivert.io.PersistenceException;
import com.samskivert.util.ResultListener;

/**
 * Extends the {@link RepositoryUnit} and integrates with a {@link
 * ResultListener}.
 */
public abstract class RepositoryListenerUnit<T> extends RepositoryUnit
{
    /**
     * Creates a repository listener unit that will report its results to the
     * supplied result listener.
     */
    public RepositoryListenerUnit (ResultListener<T> listener)
    {
        _listener = listener;
    }

    /**
     * Called to perform our persistent action and generate our result.
     */
    public abstract T invokePersistResult ()
        throws PersistenceException;

    @Override // from RepositoryUnit
    public void invokePersist ()
        throws PersistenceException
    {
        _result = invokePersistResult();
    }

    @Override // from RepositoryUnit
    public void handleSuccess ()
    {
        _listener.requestCompleted(_result);
    }

    @Override // from RepositoryUnit
    public void handleFailure (PersistenceException pe)
    {
        _listener.requestFailed(pe);
    }

    protected ResultListener<T> _listener;
    protected T _result;
}
