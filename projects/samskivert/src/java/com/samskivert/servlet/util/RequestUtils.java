//
// $Id: RequestUtils.java,v 1.4 2002/05/09 05:01:09 mdb Exp $
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

package com.samskivert.servlet.util;

import java.net.URLEncoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpUtils;

import com.samskivert.util.StringUtil;

/**
 * A repository of utility functions related to HTTP servlet stuff.
 */
public class RequestUtils
{
    /**
     * Reconstructs the current location (URL) from the request and
     * servlet configuration (which is the only way to know which server
     * we're on because that's not provided in the request) and returns it
     * URL encoded so that it can be substituted into another URL.
     *
     * @return The URL encoded URL that represents our current location.
     */
    public static String getLocationEncoded (HttpServletRequest req)
    {
        return URLEncoder.encode(getLocation(req));
    }

    /**
     * Reconstructs the current location (URL) from the request and
     * servlet configuration (which is the only way to know which server
     * we're on because that's not provided in the request) and returns
     * it.
     *
     * @return The URL that represents our current location.
     */
    public static String getLocation (HttpServletRequest req)
    {
        StringBuffer rurl = HttpUtils.getRequestURL(req);
        String qs = req.getQueryString();
        if (qs != null) {
            rurl.append("?").append(qs);
        }
        return rurl.toString();
    }

    /**
     * Recreates the URL used to make the supplied request, replacing the
     * server part of the URL with the supplied server name.
     */
    public static String rehostLocation (
        HttpServletRequest req, String servername)
    {
        StringBuffer buf = HttpUtils.getRequestURL(req);
        String csname = req.getServerName();
        int csidx = buf.indexOf(csname);
        if (csidx != -1) {
            buf.delete(csidx, csidx + csname.length());
            buf.insert(csidx, servername);
        }
        String query = req.getQueryString();
        if (!StringUtil.blank(query)) {
            buf.append("?").append(query);
        }
        return buf.toString();
    }
}
