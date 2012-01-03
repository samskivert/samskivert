//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

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
