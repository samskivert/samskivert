//
// $Id: UserExistsException.java,v 1.1 2001/03/02 01:21:06 mdb Exp $

package com.samskivert.servlet.user;

public class UserExistsException extends Exception
{
    public UserExistsException (String message)
    {
	super(message);
    }
}
