//
// $Id: RedirectException.java,v 1.1 2001/03/02 01:21:06 mdb Exp $

package com.samskivert.servlet;

/**
 * A redirect exception is thrown by servlet services when they require
 * that the user be redirected to a different URL rather than continue
 * processing this request. It is expected that redirect handling can be
 * implemented in a single place such that servlets can simply allow this
 * exception to propagate up to the proper handler which will then issue
 * the appropriate redirect header.
 */
public class RedirectException extends Exception
{
    public RedirectException (String url)
    {
	super(url);
    }

    public String getRedirectURL ()
    {
	return getMessage();
    }
}
