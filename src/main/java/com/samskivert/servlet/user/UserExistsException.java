//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2011 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.servlet.user;

import com.samskivert.io.PersistenceException;

/**
 * Thrown during user account creation when a user with the requested
 * username already exists.
 */
public class UserExistsException extends PersistenceException
{
    public UserExistsException (String message)
    {
        super(message);
    }
}
