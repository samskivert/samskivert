//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

/**
 * An exception thrown when a required property was not found.
 *
 * @see PropertiesUtil#requireProperty
 */
public class MissingPropertyException extends RuntimeException
{
    public MissingPropertyException (String key, String message)
    {
        super(message);
        _key = key;
    }

    public String getKey ()
    {
        return _key;
    }

    protected String _key;
}
