//
// $Id: FriendlyException.java,v 1.2 2001/02/15 01:44:34 mdb Exp $

package com.samskivert.webmacro;

/**
 * The friendly exception provides a mechanism by which a servlet can
 * abort its processing and report a human readable error to the servlet
 * framework that will be inserted into the context in the appropriate
 * place so that the error message will be displayed to the user. Simply
 * construct a friendly exception with the desired error message and throw
 * it during the call to <code>populateContext</code>.
 *
 * @see DispatcherServlet
 */
public class FriendlyException extends Exception
{
    public FriendlyException (String message)
    {
	super(message);
    }
}
