//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.servlet.util;

/**
 * A data validation exception is thrown when a value supplied in a form
 * element is not valid.
 *
 * @see ParameterUtil
 */
public class DataValidationException extends FriendlyException
{
    public DataValidationException (String message)
    {
        super(message);
    }
}
