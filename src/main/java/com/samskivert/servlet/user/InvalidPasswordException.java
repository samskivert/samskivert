//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.servlet.user;

/**
 * Thrown during authentication when an invalid password is supplied.
 */
public class InvalidPasswordException extends AuthenticationFailedException
{
    public InvalidPasswordException (String message)
    {
        super(message);
    }
}
