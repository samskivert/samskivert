//
// $Id: Logic.java,v 1.2 2004/02/25 13:16:32 mdb Exp $
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

package com.samskivert.velocity;

import com.samskivert.servlet.util.ExceptionMap;

/**
 * The logic class is called upon to populate the WebMacro web context,
 * prior to invoking a particular WebMacro template upon it to generate a
 * response page. The logic takes the place of the servlet in the standard
 * WebMacro architecture and should perform all of the logic involved in
 * handling a particular request.
 *
 * @see DispatcherServlet
 */
public interface Logic
{
    /**
     * Perform any necessary computation and populate the context with
     * data for this request. Any exceptions that are thrown will be
     * converted into friendly error messages using the exception mapping
     * services.
     *
     * @param app The application that generated this logic instance (used
     * to access application-wide resources).
     * @param context The invocation context in scope for this request.
     *
     * @see ExceptionMap
     */
    public void invoke (Application app, InvocationContext context)
        throws Exception;
}
