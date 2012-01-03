//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.servlet.user;

/**
 * Thrown when a user authentication attempt failed.
 */
public class AuthenticationFailedException extends Exception
{
    public AuthenticationFailedException (String message)
    {
        super(message);
    }
}
