//
// $Id: I18nTool.java,v 1.2 2003/07/02 21:32:27 mdb Exp $
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

import java.text.SimpleDateFormat;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;

import com.samskivert.servlet.MessageManager;
import com.samskivert.util.StringUtil;

/**
 * Provides access to a set of application messages (translation strings)
 * to templates that wish to display localized text.
 *
 * <p> If the tool is mapped into the context as <code>i18n</code>, then
 * it might be accessed as follows:
 *
 * <pre>
 * $i18n.xlate("main.intro", $user.username)
 * </pre>
 *
 * where <code>main.intro</code> might be defined in the message resource
 * file as:
 *
 * <pre>
 * main.intro=Hello there {0}, welcome to our site!
 * </pre>
 *
 * @see MessageManager
 */
public class I18nTool
{
    /**
     * Constructs a new i18n tool which will use the supplied message
     * manager to obtain translated strings and the supplied request
     * instance to determine the locale in which to do so.
     */
    public I18nTool (HttpServletRequest req, MessageManager msgmgr)
    {
        _req = req;
        _msgmgr = msgmgr;
    }

    /**
     * Looks up the specified message and returns the translation string.
     */
    public String xlate (String key)
    {
        return _msgmgr.getMessage(_req, key);
    }

    /**
     * Looks up the specified message and creates the translation string
     * using the supplied argument.
     */
    public String xlate (String key, Object arg)
    {
        return _msgmgr.getMessage(_req, key, new Object[] { arg });
    }

    /**
     * Looks up the specified message and creates the translation string
     * using the supplied arguments.
     */
    public String xlate (String key, Object arg1, Object arg2)
    {
        return _msgmgr.getMessage(_req, key, new Object[] { arg1, arg2 });
    }

    /**
     * Looks up the specified message and creates the translation string
     * using the supplied arguments.
     */
    public String xlate (String key, Object arg1, Object arg2, Object arg3)
    {
        return _msgmgr.getMessage(_req, key,
                                  new Object[] { arg1, arg2, arg3 });
    }

    /**
     * Uses {@link SimpleDateFormat} to translate the supplied {@link
     * Long} or {@link Date} argument into a formatted date string using
     * the locale appropriate to the current request.
     */
    public String date (String format, Object arg)
    {
        Date when = null;
        if (arg instanceof Long) {
            when = new Date(((Long)arg).longValue());
        } else if (arg instanceof Date) {
            when = (Date)arg;
        } else {
            System.err.println("Date provided with invalid argument " +
                               "[fmt=" + format + ", arg=" + arg + ", aclass=" +
                               StringUtil.shortClassName(arg) + "].");
            return format;
        }
        SimpleDateFormat fmt = new SimpleDateFormat(format, _req.getLocale());
        return fmt.format(when);
    }

    /** The servlet request was are providing i18n messages for. */
    protected HttpServletRequest _req;

    /** The message manager we're using to provide i18n messages. */
    protected MessageManager _msgmgr;
}
