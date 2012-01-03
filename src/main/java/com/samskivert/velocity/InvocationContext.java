//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.velocity;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.runtime.RuntimeSingleton;

import com.samskivert.util.StringUtil;

/**
 * The invocation context provides access to request related information as well as being the place
 * where objects are placed to make them available to the template.
 */
public class InvocationContext extends VelocityContext
{
    /**
     * Constructs a new invocation context instance with the supplied http request and response
     * objects.
     */
    public InvocationContext (HttpServletRequest req, HttpServletResponse rsp)
    {
        _req = req;
        _rsp = rsp;
    }

    /**
     * Returns the http request object associated with this invocation.
     */
    public HttpServletRequest getRequest ()
    {
        return _req;
    }

    /**
     * Returns the http response object associated with this invocation.
     */
    public HttpServletResponse getResponse ()
    {
        return _rsp;
    }

    /**
     * Fetches a Velocity template that can be used for later formatting. The template is read with
     * the specified encoding or the default encoding if encoding is null.
     *
     * @exception Exception thrown if an error occurs loading or parsing the template.
     */
    public Template getTemplate (String path, String encoding)
        throws Exception
    {
        Object siteId = get("__siteid__");
        if (siteId != null) {
            path = siteId + ":" + path;
        }
        if (encoding == null) {
            return RuntimeSingleton.getRuntimeServices().getTemplate(path);
        } else {
            return RuntimeSingleton.getRuntimeServices().getTemplate(path, encoding);
        }
    }

    /**
     * Fetches a Velocity template that can be used for later formatting.
     *
     * @exception Exception thrown if an error occurs loading or parsing the template.
     */
    public Template getTemplate (String path)
        throws Exception
    {
        return getTemplate(path, null);
    }

    /**
     * A convenience method for putting an int value into the context.
     */
    public void put (String key, int value)
    {
        put(key, Integer.valueOf(value));
    }

    /**
     * A convenience method for putting a boolean value into the context.
     */
    public void put (String key, boolean value)
    {
        put(key, Boolean.valueOf(value));
    }

    /**
     * Don't use this method. It is terribly unsafe and was written by a lazy engineer.
     */
    @Deprecated
    public void putAllParameters ()
    {
        Enumeration<?> e = _req.getParameterNames();
        while (e.hasMoreElements()) {
            String param = (String)e.nextElement();
            put(param, _req.getParameter(param));
        }
    }

    /**
     * Encodes all the request params so they can be slapped onto a different URL which is useful
     * when doing redirects.
     */
    public String encodeAllParameters ()
    {
        StringBuilder buf = new StringBuilder();
        Enumeration<?> e = _req.getParameterNames();
        while (e.hasMoreElements()) {
            if (buf.length() > 0) {
                buf.append('&');
            }
            String param = (String) e.nextElement();
            buf.append(StringUtil.encode(param));
            buf.append('=');
            buf.append(StringUtil.encode(_req.getParameter(param)));
        }

        return buf.toString();
    }

    protected HttpServletRequest _req;
    protected HttpServletResponse _rsp;
}
