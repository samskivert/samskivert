//
// $Id: CookieUtil.java,v 1.1 2003/10/06 22:50:28 ray Exp $

package com.samskivert.servlet.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

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
}
