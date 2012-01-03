//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.velocity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
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
     * Returns true if the key exists.
     */
    public boolean exists (String key)
    {
        return _msgmgr.exists(_req, key);
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
        Date when = massageDate(arg);
        if (when == null) {
            return format;
        }
        SimpleDateFormat fmt = new SimpleDateFormat(format, getLocale());
        return fmt.format(when);
    }

    /**
     * Formats the supplied argument (a {@link Long} or {@link Date})
     * using a {@link DateFormat} obtained in the {@link DateFormat#SHORT}
     * style.
     */
    public String shortDate (Object arg)
    {
        return date(DateFormat.SHORT, arg);
    }

    /**
     * Like {@link #shortDate} but in the {@link DateFormat#MEDIUM} style.
     */
    public String mediumDate (Object arg)
    {
        return date(DateFormat.MEDIUM, arg);
    }

    /**
     * Like {@link #shortDate} but in the {@link DateFormat#LONG} style.
     */
    public String longDate (Object arg)
    {
        return date(DateFormat.LONG, arg);
    }

    /** Helper function for formatting dates. */
    protected String date (int style, Object arg)
    {
        Date when = massageDate(arg);
        if (when == null) {
            return "<!" + arg + ">";
        }
        return DateFormat.getDateInstance(style, getLocale()).format(when);
    }

    /** Returns the locale associated with this request. */
    protected Locale getLocale ()
    {
        return _req.getLocale();
    }

    /** Converts an argument to a {@link Date} if possible. */
    protected Date massageDate (Object arg)
    {
        if (arg instanceof Long) {
            return new Date(((Long)arg).longValue());
        } else if (arg instanceof Date) {
            return (Date)arg;
        } else if (arg instanceof Calendar) {
            return ((Calendar)arg).getTime();
        } else {
            System.err.println("Date provided with invalid argument " +
                               "[arg=" + arg + ", aclass=" +
                               StringUtil.shortClassName(arg) + "].");
            return null;
        }
    }

    /** The servlet request was are providing i18n messages for. */
    protected HttpServletRequest _req;

    /** The message manager we're using to provide i18n messages. */
    protected MessageManager _msgmgr;
}
