//
// $Id: FriendlyServlet.java,v 1.1 2001/02/13 18:49:41 mdb Exp $

package com.samskivert.webmacro;

import org.webmacro.*;
import org.webmacro.servlet.*;

/**
 * The friendly servlet adds a common error handling paradigm to the basic
 * webmacro servlet. This paradigm is to catch any exceptions thrown in
 * the <code>populateContext</code> method (used instead of the
 * <code>handle</code> method) and to convert them into friendly error
 * messages that are inserted into the context with the key
 * <code>"error"</code> for easy display to the user in the resulting web
 * page.
 *
 * <p> To make this work, the process of template selection is separated
 * from the process of data generation. First the template is selected; if
 * that process fails, a standard error response is given to the user as
 * we have no template into which to substitute the template selection
 * failure error message. Subsequently, the data is generated. If that
 * fails, we can substitute a friendly error message into the selected
 * template. Finally, the template is executed, which is a standard
 * webmacro procedure.
 *
 * <p> The process of mapping exceptions to friendly error messages is
 * done through a properties file loaded via the classpath. The properties
 * file should be named 'exceptionmap.properties' and placed in a
 * directory contained in the classpath of the JVM in which the servlet is
 * executed. The file should contain mappings from exception classes to
 * friendly error messages. For example:
 *
 * <pre>
 * com.samskivert.webmacro.FriendlyException = An error occurred while \
 * processing your request: {m}
 * java.sql.SQLException = The database is currently unavailable. Please \
 * try your request again later.
 * java.lang.Exception = An unexpected error occurred while processing \
 * your request. Please try again later.
 * </pre>
 *
 * The message associated with the exception will be substituted into the
 * error string in place of <code>{m}</code>.
 */
public abstract class FriendlyServlet extends WMServlet
{
    public Template handle (WebContext ctx) throws HandlerException
    {
	// first we select the template
	Template tmpl;
	try {
	    tmpl = selectTemplate(ctx);
	} catch (Exception e) {
	    throw new HandlerException("Unable to load template: " + e);
	}

	// then we populate the context with data
	try {
	    populateContext(ctx, tmpl);
	} catch (Exception e) {
	}

	return tmpl;
    }

    /**
     * Override this member function with code that selects and loads the
     * proper template based on whatever criterion appropriate for this
     * servlet.
     *
     * @param ctx The context of this request.
     *
     * @return The template to be used in generating the response.
     */
    protected abstract Template selectTemplate (WebContext ctx)
	throws Exception;

    /**
     * Override this member function with code that populates the context
     * with whatever data is appropriate for this servlet. Presumably this
     * is where the real work of the servlet takes place.
     *
     * @param ctx The context of this request.
     * @param tmpl The webmacro template previously selected by the
     * invocation of the <code>selectTemplate</code> method.
     */
    protected abstract void populateContext (WebContext ctx, Template tmpl)
	throws Exception;
}
