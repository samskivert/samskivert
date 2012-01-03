//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.servlet.util;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

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
        return StringUtil.encode(getLocation(req));
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
        StringBuffer rurl = req.getRequestURL();
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
        StringBuffer buf = req.getRequestURL();
        String csname = req.getServerName();
        int csidx = buf.indexOf(csname);
        if (csidx != -1) {
            buf.delete(csidx, csidx + csname.length());
            buf.insert(csidx, servername);
        }
        String query = req.getQueryString();
        if (!StringUtil.isBlank(query)) {
            buf.append("?").append(query);
        }
        return buf.toString();
    }

    /**
     * Prepends the server, port and servlet context path to the supplied
     * path, resulting in a fully-formed URL for requesting a servlet.
     */
    public static String getServletURL (HttpServletRequest req, String path)
    {
        StringBuffer buf = req.getRequestURL();
        String sname = req.getServletPath();
        buf.delete(buf.length() - sname.length(), buf.length());
        if (!path.startsWith("/")) {
            buf.append("/");
        }
        buf.append(path);
        return buf.toString();
    }

    /**
     * Reconstructs the request URL including query parameters. <em>Note:</em>
     * the output of this method is purely for logging purposes only, thus POST
     * parameters are shown as if they were GET parameters and parameters are
     * <em>not</em> URL encoded.
     */
    public static String reconstructURL (HttpServletRequest req)
    {
        StringBuffer buf = req.getRequestURL();
        @SuppressWarnings("unchecked") Map<String, String[]> map = req.getParameterMap();
        if (map.size() > 0) {
            buf.append("?");
            for (Map.Entry<String, String[]> entry : map.entrySet()) {
                if (buf.charAt(buf.length()-1) != '?') {
                    buf.append("&");
                }
                buf.append(entry.getKey()).append("=");
                String[] values = entry.getValue();
                if (values.length == 1) {
                    buf.append(values[0]);
                } else {
                    buf.append("(");
                    for (int ii = 0; ii < values.length; ii++) {
                        if (ii > 0) {
                            buf.append(", ");
                        }
                        buf.append(values[ii]);
                    }
                    buf.append(")");
                }
            }
        }
        return buf.toString();
    }
}
