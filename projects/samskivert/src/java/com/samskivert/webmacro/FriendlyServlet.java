//
// $Id: FriendlyServlet.java,v 1.2 2001/02/13 20:00:28 mdb Exp $

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
 * done through a configuration file loaded via the classpath. The file
 * should be named <code>exceptionmap.properties</code> and placed in the
 * classpath of the JVM in which the servlet is executed. The file should
 * contain colon-separated mappings from exception classes to friendly
 * error messages. For example:
 *
 * <pre>
 * # Exception mappings (lines beginning with # are ignored)
 * com.samskivert.webmacro.FriendlyException: An error occurred while \
 * processing your request: {m}
 *
 * # lines ending with \ are continued on the next line
 * java.sql.SQLException: The database is currently unavailable. Please \
 * try your request again later.
 *
 * java.lang.Exception: An unexpected error occurred while processing \
 * your request. Please try again later.
 * </pre>
 *
 * The message associated with the exception will be substituted into the
 * error string in place of <code>{m}</code>. The exceptions should be
 * listed in order of most specific to least specific, for the first
 * mapping for which the exception to report is an instance of the
 * exception in the left hand side will be used.
 *
 * <em>Note:</em> These exception mappings are used for all servlets
 * (perhaps some day only for servlets associated with a particular
 * application identifier). Regardless, this error handling mechanism
 * should not be used for servlet specific errors. For example, an SQL
 * exception reporting a duplicate key should probably be caught and
 * reported specifically by the servlet (it can still leverage the pattern
 * of inserting the error message into the context as
 * <code>"error"</code>) rather than relying on the default SQL exception
 * error message which is not likely to be meaningful for such a
 * situation.
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
	    ctx.put(ERROR_KEY, ExceptionMap.getMessage(e));
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

    /** This is the key used in the context for error messages. */
    protected static final String ERROR_KEY = "error";
}
