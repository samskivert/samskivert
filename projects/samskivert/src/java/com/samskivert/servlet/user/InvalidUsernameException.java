//
// $Id: InvalidUsernameException.java,v 1.1 2001/03/02 01:21:06 mdb Exp $

package com.samskivert.servlet.user;

public class InvalidUsernameException extends Exception
{
    public InvalidUsernameException (String message)
    {
	super(message);
    }
}
