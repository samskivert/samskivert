//
// $Id: InvalidPasswordException.java,v 1.1 2001/03/02 02:08:50 mdb Exp $

package com.samskivert.servlet.user;

public class InvalidPasswordException extends Exception
{
    public InvalidPasswordException (String message)
    {
	super(message);
    }
}
