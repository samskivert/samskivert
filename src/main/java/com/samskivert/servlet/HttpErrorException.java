//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.servlet;

import javax.servlet.http.HttpServletResponse;

/**
 * An http error exception is thrown by servlet services when they wish to fail
 * the request in some particular way (ie. {@link
 * HttpServletResponse#SC_FORBIDDEN}, etc.). It is expected that error handling
 * can be implemented in a single place such that servlets can simply allow
 * this exception to propagate up to the proper handler which will then issue
 * the appropriate failure header.
 */
public class HttpErrorException extends Exception
{
    public HttpErrorException (int errorCode)
    {
        super(String.valueOf(errorCode));
        _errorCode = errorCode;
    }

    public HttpErrorException (int errorCode, String errorMessage)
    {
        super(errorMessage);
        _errorCode = errorCode;
    }

    /**
     * Returns the HTTP error code supplied with this exception.
     */
    public int getErrorCode ()
    {
        return _errorCode;
    }

    /**
     * Returns the textual error message supplied with this exception or null
     * if only an error code was supplied.
     */
    public String getErrorMessage ()
    {
        String msg = getMessage();
        return String.valueOf(_errorCode).equals(msg) ? null : msg;
    }

    protected int _errorCode;
}
