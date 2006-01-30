//
// $Id: Username.java,v 1.3 2004/01/31 13:03:39 mdb Exp $

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
     * Creates a username instance. Usernames must consist only of
     * characters that match the following regular expression:
     * <code>[_A-Za-z0-9]+</code> and be longer than two characters.
     */
    public Username (String username)
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

        _username = username;
    }

    /** Returns the text of this username. */
    public String getUsername ()
    {
        return _username;
    }

    /** Returns a string representation of this instance. */
    public String toString ()
    {
        return _username;
    }

    protected String _username;
}
