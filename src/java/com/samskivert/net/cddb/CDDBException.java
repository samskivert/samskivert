//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2007 Michael Bayne
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
