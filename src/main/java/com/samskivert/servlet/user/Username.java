//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.servlet.user;

/**
 * Allows us to require a valid username as a parameter without having to
 * do the checking ourselves.
 */
public class Username
{
    /** The minimum allowable length of a username. */
    public static final int MINIMUM_USERNAME_LENGTH = 3;

    /** The maximum allowable length of a username. */
    public static final int MAXIMUM_USERNAME_LENGTH = 12;

    /** The regular expression defining valid names. */
    public static final String NAME_REGEX = "^[_A-Za-z0-9]*$";

    /**
     * Creates a username instance.
     */
    public Username (String username)
        throws InvalidUsernameException
    {
        validateName(username);
        _username = username;
    }

    /** Returns the text of this username. */
    public String getUsername ()
    {
        return _username;
    }

    @Override
    public String toString ()
    {
        return _username;
    }

    /**
     * Validates our username. The default implementation requires that usernames consist only of
     * characters that match the {@link #NAME_REGEX} regular expression and be between {@link
     * #MINIMUM_USERNAME_LENGTH} and {@link #MAXIMUM_USERNAME_LENGTH} characters.
     */
    protected void validateName (String username)
        throws InvalidUsernameException
    {
        // check length
        if (username.length() < MINIMUM_USERNAME_LENGTH) {
            throw new InvalidUsernameException("error.username_too_short");
        }
        if (username.length() > MAXIMUM_USERNAME_LENGTH) {
            throw new InvalidUsernameException("error.username_too_long");
        }

        // check that it's only valid characters
        if (!username.matches(NAME_REGEX)) {
            throw new InvalidUsernameException("error.invalid_username");
        }
    }

    protected String _username;
}
