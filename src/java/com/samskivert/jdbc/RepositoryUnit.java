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
import com.samskivert.util.Invoker;

/**
 * Extends the {@link Invoker#Unit} and specializes it for doing database
 * repository manipulation.
 */
public abstract class RepositoryUnit extends Invoker.Unit
{
    // from abstract Invoker.Unit
    public boolean invoke ()
    {
        try {
            invokePersist();
        } catch (Exception pe) {
            _error = pe;
        }
        return true;
    }

    @Override // from Invoker.Unit
    public void handleResult ()
    {
        if (_error != null) {
            handleFailure(_error);
        } else {
            handleSuccess();
        }
    }

    /**
     * Called to perform our persistent actions.
     */
    public abstract void invokePersist ()
        throws PersistenceException;

    /**
     * Called if our persistent actions have succeeded, back on the non-invoker
     * thread.
     */
    public abstract void handleSuccess ();

    /**
     * Called if our persistent actions failed, back on the non-invoker thread.
     * Note that this may be either a {@link PersistenceException} thrown by
     * {@link #invokePersist} or a {@link RuntimeException} thrown by same.
     */
    public abstract void handleFailure (Exception pe);

    protected Exception _error;
}
