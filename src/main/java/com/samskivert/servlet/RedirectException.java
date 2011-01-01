//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2011 Michael Bayne, et al.
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
