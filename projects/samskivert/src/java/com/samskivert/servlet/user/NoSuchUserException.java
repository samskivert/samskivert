//
// $Id: NoSuchUserException.java,v 1.1 2001/03/02 02:08:50 mdb Exp $

package com.samskivert.servlet.user;

public class NoSuchUserException extends Exception
{
    public NoSuchUserException (String message)
    {
	super(message);
    }
}
