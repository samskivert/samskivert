//
// $Id: ContextPopulator.java,v 1.1 2001/02/15 01:44:34 mdb Exp $

package com.samskivert.webmacro;

import org.webmacro.servlet.WebContext;

/**
 * The context populator is called upon to populate the WebMacro web
 * context, prior to invoking a particular WebMacro template upon it to
 * generate a response page. The populator takes the place of the servlet
 * in the standard WebMacro architecture and should perform all of the
 * logic involved in handling a particular request.
 *
 * @see DispatcherServlet
 */
public interface ContextPopulator
{
    /**
     * Perform any necessary computation and populate the context with
     * data for this request. Any exceptions that are thrown will be
     * converted into friendly error messages using the exception mapping
     * services.
     *
     * @param context The WebMacro context in scope for this request.
     *
     * @see ExceptionMap
     */
    public void populate (WebContext context) throws Exception;
}
