//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2007 Michael Bayne
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
 * Extends {@link WriteOnlyUnit} and allows for processing of results back on the event processing
 * thread.
 */
public abstract class RepositoryUnit extends WriteOnlyUnit
{
    /**
     * Create a RepositoryUnit which will report the supplied name in {@link #toString}.
     */
    public RepositoryUnit (String name)
    {
        super(name);
    }

    @Override // from WriteOnlyUnit
    public boolean invoke ()
    {
        try {
            invokePersist();
        } catch (Exception pe) {
            _error = pe;
        }
        return true;
    }

    @Override // from WriteOnlyUnit
    public void handleResult ()
    {
        if (_error != null) {
            handleFailure(_error);
        } else {
            handleSuccess();
        }
    }

    /**
     * Called if our persistent actions have succeeded, back on the non-invoker thread.
     */
    public abstract void handleSuccess ();
}
