//
// $Id: FriendlyException.java,v 1.3 2001/08/11 22:43:29 mdb Exp $
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001 Michael Bayne
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
