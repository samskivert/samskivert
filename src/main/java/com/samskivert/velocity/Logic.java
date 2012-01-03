//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

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
