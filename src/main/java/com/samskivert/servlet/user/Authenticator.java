//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.servlet.user;

/**
 * Provides a means for applications to use their own application-specific
 * authentication schemes for validating a user by constructing their own
 * authenticator and passing it to {@link UserManager#login}.
 */
public interface Authenticator
{
    /**
     * Checks whether the user should be authenticated based on the
     * supplied user record and the specified user information.  Throws an
     * {@link AuthenticationFailedException} if the user fails to pass the
     * authentication check for some reason.
     *
     * @param user the definitive user record loaded from the persistent
     * repository against which the user-supplied data is to be checked.
     * @param username the username supplied by the user.
     * @param password the plaintext password supplied by the user.
     *
     * @throws AuthenticationFailedException if the user failed to pass
     * the authentication check.
     */
    public void authenticateUser (User user, String username, Password password)
        throws AuthenticationFailedException;
}
