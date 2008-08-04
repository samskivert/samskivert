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
 * The CDDB protocol class provides constants related to the CDDB protocol
 * as well as utility routines useful in dealing with the protocol.
 */
public class CDDBProtocol
{
    /**
     * The terminating marker which occurs alone on it's own line to
     * indicate the end of an extended command.
     */
    public static final String TERMINATOR = ".";

    /* Class constant offsets */
    public static final int INFORMATIONAL = 100;
    public static final int OK = 200;
    public static final int OK_WITH_CONTINUATION = 300;
    public static final int UNABLE_TO_PERFORM = 400;
    public static final int SERVER_ERROR = 500;

    /**
     * @return The family to which the supplied specific response code
     * belongs.
     */
    public static int codeFamily (int code)
    {
	return (code / 100) * 100;
    }

    /**
     * @return The genus to which the supplied specific response code
     * belongs.
     */
    public static int codeGenus (int code)
    {
	return ((code % 100) / 10) * 10;
    }

    /**
     * @return The species to which the supplied specific response code
     * belongs.
     */
    public static int codeSpecies (int code)
    {
	return code % 10;
    }
}
