//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.jdbc;

/**
 * A specialized invoker unit that does one or more database operations and then sends the results
 * back to the event processing thread. Override {@link #handleSuccess} to process your result on
 * the event thread and {@link #handleFailure} to handle a failure on the event thread. For
 * example:
 *
 * <pre>
 * final int userId = 123;
 * _invoker.postUnit(new RepositoryUnit("loadUser") {
 *     public void invokePersist () throws Exception {
 *         _user = _userRepo.loadUser(userId);
 *     }
 *     public void handleSuccess () {
 *         // do something with _user
 *     }
 *     public void handleFailure (Exception cause) {
 *         // report failure to load user
 *     }
 *     protected UserRecord _user;
 * });
 * </pre>
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
