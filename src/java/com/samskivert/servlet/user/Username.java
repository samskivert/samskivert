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

    /** Returns a string representation of this instance. */
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
