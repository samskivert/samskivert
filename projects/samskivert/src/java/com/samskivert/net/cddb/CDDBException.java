//
// $Id: CDDBException.java,v 1.1 2000/10/23 07:32:12 mdb Exp $

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
