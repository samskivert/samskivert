//
// $Id: InvocationContext.java,v 1.4 2003/10/13 17:40:10 eric Exp $
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

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.runtime.RuntimeSingleton;

import com.samskivert.util.StringUtil;

/**
 * The invocation context provides access to request related information
 * as well as being the place where objects are placed to make them
 * available to the template.
 */
public class InvocationContext extends VelocityContext
{
    /**
     * Constructs a new invocation context instance with the supplied http
     * request and response objects.
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
     * Fetches a Velocity template that can be used for later formatting.
     *
     * @exception Exception thrown if an error occurs loading or parsing
     * the template.
     */
    public Template getTemplate (String path)
        throws Exception
    {
        return RuntimeSingleton.getTemplate(path);
    }

    /**
     * A convenience method for putting an int value into the context.
     */
    public void put (String key, int value)
    {
        put(key, new Integer(value));
    }

    /**
     * A convenience method for putting a boolean value into the context.
     */
    public void put (String key, boolean value)
    {
        put(key, new Boolean(value));
    }

    /**
     * Puts all the parameters in the request into the context.
     */
    public void putAllParameters ()
    {
        Enumeration e = _req.getParameterNames();

        while (e.hasMoreElements()) {
            String param = (String)e.nextElement();
            put(param, _req.getParameter(param));
        }
    }

    /**
     * Encodes all the request params so they can be slapped
     * onto a different URL which is useful when doing redirects.
     */
    public String encodeAllParameters ()
    {
        Enumeration e = _req.getParameterNames();

        String url = "";
        while (e.hasMoreElements()) {
            String param = (String)e.nextElement();
            url += StringUtil.blank(url) ? param : "&" + param;
            url += "=" + StringUtil.encode(_req.getParameter(param));
        }

        return url;
    }
    
    protected HttpServletRequest _req;
    protected HttpServletResponse _rsp;
}
