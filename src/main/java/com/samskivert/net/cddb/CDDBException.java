//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.net.cddb;

/**
 * This exception class encapsulates errors that may occur while
 * communicating to a CDDB server. It is not used to communicate IO errors
 * (an IOException is used for that), but it is used to communicate
 * failures communicated within the scope of the CDDB protocol.
 *
 * @see CDDB
 */
public class CDDBException extends Exception
{
    public CDDBException (int code, String message)
    {
        super(message);
        _code = code;
    }

    public int getCode ()
    {
        return _code;
    }

    protected int _code;
}
