//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.servlet.util;

/**
 * The friendly exception provides a mechanism by which a servlet or
 * underlying code can abort its processing and report a human readable
 * error to the servlet framework.
 */
public class FriendlyException extends Exception
{
    public FriendlyException (String message)
    {
        super(message);
    }
}
