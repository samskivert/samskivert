//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2011 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.servlet.user;

/**
 * Thrown during user account creation if an invalid username is supplied.
 */
public class InvalidUsernameException extends Exception
{
    public InvalidUsernameException (String message)
    {
        super(message);
    }
}
