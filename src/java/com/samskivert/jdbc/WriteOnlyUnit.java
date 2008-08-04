//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2008 Michael Bayne
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

import com.samskivert.util.Invoker;

import static com.samskivert.Log.log;

/**
 * Extends the {@link Invoker.Unit} and specializes it for writing to a database repository.
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

    // from abstract Invoker.Unit
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
