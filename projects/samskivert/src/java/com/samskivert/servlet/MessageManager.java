//
// $Id: MessageManager.java,v 1.1 2001/03/04 06:15:39 mdb Exp $

package com.samskivert.servlet;

import java.text.MessageFormat;
import java.util.*;
import javax.servlet.http.HttpServletRequest;

import com.samskivert.Log;

/**
 * The message manager handles the translation messages for a web
 * application. The webapp should construct the message manager with the
 * name of its message properties file and it can then make use of the
 * message manager to generate locale specific messages for a request.
 */
public class MessageManager
{
    /**
     * Constructs a message manager with the specified bundle path. The
     * message manager will be instantiating a <code>ResourceBundle</code>
     * with the supplied path, so it should conform to the naming
     * conventions defined by that class.
     *
     * @see java.util.ResourceBundle
     */
    public MessageManager (String bundlePath)
    {
        // keep this for later
        _bundlePath = bundlePath;
    }

    /**
     * Looks up the message with the specified path in the resource bundle
     * most appropriate for the locales described as preferred by the
     * request.
     */
    public String getMessage (HttpServletRequest req, String path)
    {
        try {
            // use the locale preferred by the client if possible
            ResourceBundle bundle = resolveBundle(req);
            if (bundle != null) {
                return bundle.getString(path);
            }

            // if we couldn't find a bundle, things are way wacked out,
            // but we've already complained about it so we just fall
            // through and return the path back to the caller

        } catch (MissingResourceException mre) {
            // if there's no translation for this path, complain about it
            Log.warning("Missing translation message [path=" + path + "].");
        }

        return path;
    }

    /**
     * Looks up the message with the specified path in the resource bundle
     * most appropriate for the locales described as preferred by the
     * request, then substitutes the supplied arguments into that message
     * using a <code>MessageFormat</code> object.
     *
     * @see java.text.MessageFormat
     */
    public String getMessage (HttpServletRequest req, String path,
                              Object[] args)
    {
        try {
            // use the locale preferred by the client if possible
            ResourceBundle bundle = resolveBundle(req);
            if (bundle != null) {
                String msg = bundle.getString(path);
		// we may cache message formatters later, but for now just
		// use the static convenience function
		return MessageFormat.format(msg, args);
            }

            // if we couldn't find a bundle, things are way wacked out,
            // but we've already complained about it so we just fall
            // through and return the path back to the caller

        } catch (MissingResourceException mre) {
            // if there's no translation for this path, complain about it
            Log.warning("Missing translation message [path=" + path + "].");
        }

        return path;
    }

    /**
     * Finds the closest matching resource bundle for the locales
     * specified as preferred by the client in the supplied http request.
     */
    protected ResourceBundle resolveBundle (HttpServletRequest req)
        throws MissingResourceException
    {
        Enumeration locales = req.getLocales();

        while (locales.hasMoreElements()) {
            Locale locale = (Locale)locales.nextElement();

            try {
                // java caches resource bundles, so we don't need to
                // reinvent the wheel here. however, java also falls back
                // from a specific bundle to a more general one if it
                // can't find a specific bundle. that's real nice of it,
                // but we want first to see whether or not we have exact
                // matches on any of the preferred locales specified by
                // the client. if we don't, then we can rely on java's
                // fallback mechanisms
                ResourceBundle bundle =
                    ResourceBundle.getBundle(_bundlePath, locale);

                // if it's an exact match, off we go
                if (bundle.getLocale().equals(locale)) {
                    return bundle;
                }

            } catch (MissingResourceException mre) {
                // no need to freak out quite yet, see if we have
                // something for one of the other preferred locales
            }
        }

        try {
            // if we were unable to find an exact match for any of the
            // user's preferred locales, take their most preferred and let
            // java perform it's fallback logic on that one
            return ResourceBundle.getBundle(_bundlePath, req.getLocale());

        } catch (MissingResourceException mre) {
            // if we were unable even to find a default bundle, we've got
            // real problems. time to freak out
            Log.warning("Unable to resolve any message bundle " +
                        "[bundlePath=" + _bundlePath +
                        ", locale=" + req.getLocale() + "].");
            return null;
        }
    }

    protected String _bundlePath;
}
