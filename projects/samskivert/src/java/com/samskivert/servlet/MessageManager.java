//
// $Id: MessageManager.java,v 1.11 2004/05/11 03:14:03 mdb Exp $
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

package com.samskivert.servlet;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

import com.samskivert.Log;
import com.samskivert.text.MessageUtil;
import com.samskivert.util.StringUtil;

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
    public MessageManager (String bundlePath, Locale deflocale)
    {
        // keep these for later
        _bundlePath = bundlePath;
        _deflocale = deflocale;
    }

    /**
     * If the message manager is to be used in a multi-site environment,
     * it can be configured to load site-specific message resources in
     * addition to the default application message resources. It must be
     * configured with the facilities to load site-specific resources by a
     * call to this function.
     *
     * @param siteBundlePath the path to the site-specific message
     * resources.
     * @param siteLoader a site-specific resource loader, properly
     * configured with a site identifier.
     * @param siteIdent a site identifier that can be used to identify the
     * site via which an http request was made.
     */
    public void activateSiteSpecificMessages (String siteBundlePath,
                                              SiteResourceLoader siteLoader,
                                              SiteIdentifier siteIdent)
    {
        _siteBundlePath = siteBundlePath;
        _siteLoader = siteLoader;
        _siteIdent = siteIdent;
    }

    /**
     * Return true if the specifed path exists in the resource bundle.
     */
    public boolean exists (HttpServletRequest req, String path)
    {
        return (getMessage(req, path, false) != null);
    }

    /**
     * Looks up the message with the specified path in the resource bundle
     * most appropriate for the locales described as preferred by the
     * request.  Always reports missing paths.
     */
    public String getMessage (HttpServletRequest req, String path)
    {
        return getMessage(req, path, true);
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
        String msg = getMessage(req, path, true);
        // we may cache message formatters later, but for now just
        // use the static convenience function
        return MessageFormat.format(msg, args);
    }

    /**
     * Looks up the message with the specified path in the resource bundle
     * most appropriate for the locales described as preferred by the
     * request.  If requested it will log a missing path and return the
     * path as the translation (which should make it obvious in the
     * servlet that the translation is missing) otherwise it returns null.
     */
    protected String getMessage (HttpServletRequest req, String path,
                                 boolean reportMissing)
    {
        if (path == null) {
            return "[null message key]";
        }

        // if the key is tainted, just strip the taint character
        if (path.startsWith(MessageUtil.TAINT_CHAR)) {
            return path.substring(1);
        }

        // attempt to determine whether or not this is a compound key
        int tidx = path.indexOf("|");
        if (tidx != -1) {
            String key = path.substring(0, tidx);
            String argstr = path.substring(tidx+1);
            String[] args = StringUtil.split(argstr, "|");
            // unescape and translate the arguments
            for (int i = 0; i < args.length; i++) {
                // if the argument is tainted, do no further translation
                // (it might contain |s or other fun stuff)
                if (args[i].startsWith(MessageUtil.TAINT_CHAR)) {
                    args[i] = MessageUtil.unescape(args[i].substring(1));
                } else {
                    args[i] = getMessage(req, MessageUtil.unescape(args[i]));
                }
            }
            return getMessage(req, key, args);
        }

        // load up the matching resource bundles (the array will contain
        // the site-specific resources first and the application resources
        // second); use the locale preferred by the client if possible
        ResourceBundle[] bundles = resolveBundles(req);
        if (bundles != null) {
            int blength = bundles.length;
            for (int i = 0; i < blength; i++) {
                try {
                    if (bundles[i] != null) {
                        return bundles[i].getString(path);
                    }
                } catch (MissingResourceException mre) {
                    // no complaints, just try the bundle in the enclosing
                    // scope
                }
            }
        }

        if (reportMissing) {
            // if there's no translation for this path, complain about it
            Log.warning("Missing translation message [path=" + path +
                        ", url=" + getURL(req) + "].");
            return path;
        }

        return null;
    }

    /**
     * Finds the closest matching resource bundle for the locales
     * specified as preferred by the client in the supplied http request.
     */
    protected ResourceBundle[] resolveBundles (HttpServletRequest req)
    {
        // first look to see if we've cached the bundles for this request
        // in the request object
        ResourceBundle[] bundles = null;
        if (req != null) {
            bundles = (ResourceBundle[])req.getAttribute(BUNDLE_CACHE_NAME);
        }
        if (bundles != null) {
            return bundles;
        }

        // grab our site-specific class loader if we have one
        ClassLoader siteLoader = null;
        if (_siteLoader != null && _siteIdent != null) {
            int siteId = _siteIdent.identifySite(req);
            try {
                siteLoader = _siteLoader.getSiteClassLoader(siteId);

            } catch (IOException ioe) {
                Log.warning("Unable to fetch site-specific classloader " +
                            "[siteId=" + siteId + ", error=" + ioe + "].");
            }
        }

        // try looking up the appropriate bundles
        bundles = new ResourceBundle[2];

        // first from the site-specific classloader
        if (siteLoader != null) {
            bundles[0] = resolveBundle(req, _siteBundlePath, siteLoader);
        }

        // then from the default classloader
        bundles[1] = resolveBundle(req, _bundlePath,
                                   getClass().getClassLoader());

        // if we found either or both bundles, cache 'em
        if (bundles[0] != null || bundles[1] != null && req != null) {
            req.setAttribute(BUNDLE_CACHE_NAME, bundles);
        }

        return bundles;
    }

    /**
     * Resolves the default resource bundle based on the locale
     * information provided in the supplied http request object.
     */
    protected ResourceBundle resolveBundle (
        HttpServletRequest req, String bundlePath, ClassLoader loader)
    {
        ResourceBundle bundle = null;

        if (req != null) {
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
                    bundle = ResourceBundle.getBundle(
                        bundlePath, locale, loader);

                    // if it's an exact match, off we go
                    if (bundle.getLocale().equals(locale)) {
                        break;
                    }

                } catch (MissingResourceException mre) {
                    // no need to freak out quite yet, see if we have
                    // something for one of the other preferred locales
                }
            }
        }

        // if we were unable to find an exact match for any of the user's
        // preferred locales, take their most preferred and let java
        // perform it's fallback logic on that one
        if (bundle == null) {
            Locale locale = (req == null) ? _deflocale : req.getLocale();
            try {
                bundle = ResourceBundle.getBundle(bundlePath, locale, loader);
            } catch (MissingResourceException mre) {
                // if we were unable even to find a default bundle, we've
                // got real problems. time to freak out
                Log.warning("Unable to resolve any message bundle " +
                            "[req=" + getURL(req) + ", locale=" + locale +
                            ", bundlePath=" + bundlePath +
                            ", classLoader=" + loader +
                            ", siteBundlePath=" + _siteBundlePath +
                            ", siteLoader=" + _siteLoader + "].");
            }
        }

        return bundle;
    }

    /** Helper function. */
    protected String getURL (HttpServletRequest req)
    {
        return (req == null) ? "<none>" : req.getRequestURL().toString();
    }

    /** The path, relative to the classpath, to our resource bundles. */
    protected String _bundlePath;

    /** The path to the site-specific message bundles, fetched via the
     * site-specific resource loader. */
    protected String _siteBundlePath;

    /** The resource loader with which to fetch our site-specific message
     * bundles. */
    protected SiteResourceLoader _siteLoader;

    /** The site identifier we use to determine through which site a
     * request was made. */
    protected SiteIdentifier _siteIdent;

    /** The locale to use if we are accessed without an HTTP request. */
    protected Locale _deflocale;

    /** The attribute name that we use for caching resource bundles in
     * request objects. */
    protected static final String BUNDLE_CACHE_NAME =
        "com.samskivert.servlet.MessageManager:CachedResourceBundle";
}
