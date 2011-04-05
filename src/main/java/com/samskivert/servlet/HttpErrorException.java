//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2011 Michael Bayne, et al.
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

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
