//
// $Id: CDDBProtocol.java,v 1.1 2000/10/23 07:32:12 mdb Exp $

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
