//
// $Id: CookieUtil.java,v 1.3 2003/10/14 02:00:45 mdb Exp $

package com.samskivert.servlet.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Utility methods for dealing with cookies.
 */
public class CookieUtil
{
    /**
     * Get the cookie of the specified name, or null if not found.
     */
    public static Cookie getCookie (HttpServletRequest req, String name)
    {
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (int ii=0, nn=cookies.length; ii < nn; ii++) {
                if (cookies[ii].getName().equals(name)) {
                    return cookies[ii];
                }
            }
        }

        return null; // not found
    }

    /**
     * Get the value of the cookie for the cookie of the specified name,
     * or null if not found.
     */
    public static String getCookieValue (HttpServletRequest req, String name)
    {
        Cookie c = getCookie(req, name);
        return (c == null) ? null : c.getValue();
    }

    /**
     * Clear the cookie with the specified name.
     */
    public static void clearCookie (HttpServletResponse rsp, String name)
    {
        Cookie c = new Cookie(name, "x");
        c.setPath("/");
        c.setMaxAge(0);
        rsp.addCookie(c);
    }

    /**
     * Sets the domain of the specified cookie to the server name
     * associated with the supplied request minus the hostname
     * (ie. <code>www.samskivert.com</code> becomes
     * <code>.samskivert.com</code>).
     */
    public static void widenDomain (HttpServletRequest req, Cookie cookie)
    {
        String domain = req.getServerName();
        int didx = domain.indexOf(".");
        if (didx != -1) {
            domain = domain.substring(didx);
        }
        cookie.setDomain(domain);
    }
}
