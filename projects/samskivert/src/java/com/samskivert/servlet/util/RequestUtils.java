//
// $Id: RequestUtils.java,v 1.1 2001/05/26 23:18:11 mdb Exp $

package com.samskivert.servlet.util;

import java.net.URLEncoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpUtils;

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
    public static String getEncodedLocation (HttpServletRequest req)
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
}
