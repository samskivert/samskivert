//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2011 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.servlet.user;

/**
 * Thrown when a user cannot be located in the user database.
 */
public class NoSuchUserException extends AuthenticationFailedException
{
    public NoSuchUserException (String message)
    {
        super(message);
    }
}
