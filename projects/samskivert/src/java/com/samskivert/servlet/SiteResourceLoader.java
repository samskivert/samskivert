//
// $Id: SiteResourceLoader.java,v 1.3 2001/11/06 04:48:54 mdb Exp $
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.samskivert.Log;
import com.samskivert.util.HashIntMap;

/**
 * Web applications may wish to load resources in such a way that the site
 * on which they are running is allowed to override a resource with a
 * site-specific version (a header, footer or navigation template for
 * example). The site resource loader provides this capability by loading
 * resources, first from a site-specific jar file and if that doesn't
 * contain an overriding resource, it loads the resource via the servlet
 * context.
 *
 * <p> The site resource loader must be configured with the path to the
 * site-specific jar files, the names of which are dictated by the string
 * identifiers returned by the {@link SiteIdentifier} provided to the
 * resource loader at construct time. For example, if the configuration
 * dictates that site-specific jar files are located in
 * <code>/usr/share/java/webapps/site-data</code> and the site identifier
 * returns <code>samskivert</code> as the site identifier for a particular
 * request, site-specific resources will be loaded from
 * <code>/usr/share/java/webapps/site-data/samskivert.jar</code>.
 *
 * <p> Also note that if the site identifier returns {@link
 * SiteIdentifier#DEFAULT_SITE_ID} as the site for a particular request,
 * no site-specific jar file will be searched and the default version of
 * the resource will assumed to have been provided by the webapp itself
 * and be available via the servlet context.
 */
public class SiteResourceLoader
{
    /**
     * Constructs a new resource loader.
     *
     * @param siteIdent the site identifier to be used to identify which
     * site through which a request was made when loading resources.
     * @param context the servlet context from which site-agnostic
     * resources will be loaded and from which configuration information
     * will be obtained.
     */
    public SiteResourceLoader (
        SiteIdentifier siteIdent, ServletContext context)
    {
        // keep this stuff around
        _siteIdent = siteIdent;
        _context = context;

        // obtain the path to the site-specific jar files
        _jarPath = context.getInitParameter(SITE_JAR_PATH);
        if (_jarPath == null) {
            // use the default
            _jarPath = DEFAULT_SITE_JAR_PATH;
            // and complain about it
            Log.warning("Site resource loader not configured with a " +
                        "site-specific jar path. Will use default " +
                        "[path=" + _jarPath + "], but you should provide " +
                        "a proper value via a servlet context init " +
                        "parameter named '" + SITE_JAR_PATH + "' to " +
                        "quiet this warning message.");
        }
    }

    /**
     * Loads the specific resource, from the site-specific jar file if one
     * exists and contains the specified resource, or via the servlet
     * context. If no resource exists with that path in either location,
     * null will be returned.
     *
     * @param req the http request for which we are loading a resource
     * (this will be used to determine for which site the resource will be
     * loaded).
     * @param path the path to the desired resource.
     *
     * @return an input stream via which the resource can be read or null
     * if no resource could be located with the specified path.
     *
     * @exception IOException thrown if an I/O error occurs while loading
     * a resource.
     */
    public InputStream getResourceAsStream (
        HttpServletRequest req, String path)
        throws IOException
    {
        return getResourceAsStream(_siteIdent.identifySite(req), path);
    }

    /**
     * Loads the specific resource, from the site-specific jar file if one
     * exists and contains the specified resource, or via the servlet
     * context. If no resource exists with that path in either location,
     * null will be returned.
     *
     * @param siteId the unique identifer for the site for which we are
     * loading the resource.
     * @param path the path to the desired resource.
     *
     * @return an input stream via which the resource can be read or null
     * if no resource could be located with the specified path.
     *
     * @exception IOException thrown if an I/O error occurs while loading
     * a resource.
     */
    public InputStream getResourceAsStream (int siteId, String path)
        throws IOException
    {
        return getResourceAsStream(siteId, path, true);
    }

    /**
     * Loads the specific resource, from the site-specific jar file if one
     * exists and contains the specified resource, or via the servlet
     * context (if <code>fallbackToServletContext</code> is true). If no
     * resource exists with that path in either location, null will be
     * returned.
     *
     * @param siteId the unique identifer for the site for which we are
     * loading the resource.
     * @param path the path to the desired resource.
     * @param fallbackToServletContext if true, the servlet context will
     * be searched for the resource if there is no site-specific
     * resource. If false, it will not.
     *
     * @return an input stream via which the resource can be read or null
     * if no resource could be located with the specified path.
     *
     * @exception IOException thrown if an I/O error occurs while loading
     * a resource.
     */
    public InputStream getResourceAsStream (int siteId, String path,
                                            boolean fallbackToServletContext)
        throws IOException
    {
        InputStream stream = null;

        // if we identified a non-default site, we first look for a
        // site-specific resource
        if (siteId != SiteIdentifier.DEFAULT_SITE_ID) {
            // and fetch the site specific resource
            stream = getSiteResourceAsStream(siteId, path);
        }

        // if we didn't find a site-specific resource (or didn't look for
        // one because we're loading for the default site), we need to
        // attempt to load the resource from the servlet context
        if (stream == null && fallbackToServletContext) {
            stream = _context.getResourceAsStream(path);
        }

        // that's all she wrote
        return stream;
    }

    /**
     * Returns the last modification time of the site-specific jar file
     * for the specified site.
     *
     * @exception IOException thrown if an error occurs accessing the
     * site-specific jar file (like it doesn't exist).
     */
    public long getLastModified (int siteId)
        throws IOException
    {
        // synchronize on the lock to ensure that only one thread per site
        // is concurrently executing
        synchronized (getLock(siteId)) {
            return getBundle(siteId).getLastModified();
        }
    }

    /**
     * Fetches the resource with the specified path from the site-specific
     * jar file associated with the specified site.
     */
    protected InputStream getSiteResourceAsStream (int siteId, String path)
        throws IOException
    {
//          Log.info("Loading site resource [siteId=" + siteId +
//                   ", path=" + path + "].");

        // synchronize on the lock to ensure that only one thread per site
        // is concurrently executing
        synchronized (getLock(siteId)) {
            SiteResourceBundle bundle = getBundle(siteId);

            // make sure the path has no leading slash
            if (path.startsWith("/")) {
                path = path.substring(1);
            }

            // obtain our resource from the bundle
            return bundle.getResourceAsStream(path);
        }
    }

    /**
     * We synchronize on a per-site basis, but we use a separate lock
     * object for each site so that the process of loading a bundle for
     * the first time does not require blocking access to resources from
     * other sites.
     */
    protected Object getLock (int siteId)
    {
        Object lock = null;

        synchronized (_locks) {
            lock = _locks.get(siteId);

            // create a lock object if we haven't one already
            if (lock == null) {
                _locks.put(siteId, lock = new Object());
            }
        }

        return lock;
    }

    /**
     * Obtains the site-specific jar file for the specified site. This
     * should only be called when the lock for this site is held.
     */
    protected SiteResourceBundle getBundle (int siteId)
        throws IOException
    {
        // look up the site resource bundle for this site
        SiteResourceBundle bundle = (SiteResourceBundle)
            _bundles.get(siteId);

        // if we haven't got one, create one
        if (bundle == null) {
            // obtain the string identifier for this site
            String ident = _siteIdent.getSiteString(siteId);
            // compose that with the jar file directory to obtain the
            // path to the site-specific jar file
            File file = new File(_jarPath, ident + JAR_EXTENSION);
            // create a handle for this site-specific jar file
            bundle = new SiteResourceBundle(file);
            // cache our new bundle
            _bundles.put(siteId, bundle);
        }

        return bundle;
    }

    /**
     * Encapsulates the information we need to load data from a site
     * resource bundle as well as to determine whether the loaded bundle
     * is up to date.
     */
    public static class SiteResourceBundle
    {
        /** The object through which we load resources from the
         * site-specific jar file. */
        public JarFile jarFile;

        /** A handle on the site-specific jar file. */
        public File file;

        /**
         * Constructs a new site resource bundle. The associated jar file
         * will be opened the first time a resource is read.
         */
        public SiteResourceBundle (File file)
            throws IOException
        {
            this.file = file;
        }

        /**
         * Fetches the specified resource from our site-specific jar file.
         * The last modified time of the underlying jar file may be
         * checked to determine whether or not it needs to be reloaded.
         *
         * @return an input stream via which the resource can be read or
         * null if no resource exists with the specified path.
         */
        public InputStream getResourceAsStream (String path)
            throws IOException
        {
            // open or reopen our underlying jar file as necessary
            refreshJarFile();

            // now load up the resource
            JarEntry entry = jarFile.getJarEntry(path);
            return entry == null ? null : jarFile.getInputStream(entry);
        }

        /**
         * Returns the last modified time of the underlying jar file.
         */
        public long getLastModified ()
            throws IOException
        {
            // open or reopen our underlying jar file as necessary
            refreshJarFile();

            return _lastModified;
        }

        /**
         * Reopens our site-specific jar file if it has been modified
         * since it was last opened.
         */
        protected void refreshJarFile ()
            throws IOException
        {
            // determine whether or not we need to create a new jarfile
            // instance
            if (file.lastModified() > _lastModified) {
                // make a note of the last modified time
                _lastModified = file.lastModified();

                // close our old jar file if we've got one
                if (jarFile != null) {
                    jarFile.close();
                }

                Log.info("Opening site-specific jar file " +
                         "[path=" + file + "].");

                // and open a new one
                jarFile = new JarFile(file);
            }
        }

        /** The last modified time of the jar file at the time that we
         * opened it for reading. */
        protected long _lastModified;
    }

    /** The site identifier we use to identify requests. */
    protected SiteIdentifier _siteIdent;

    /** The servlet context via which we load resources. */
    protected ServletContext _context;

    /** The path to our site-specific jar files. */
    protected String _jarPath;

    /** We synchronize on a per-site basis. */
    protected HashIntMap _locks = new HashIntMap();

    /** The table of site-specific jar file information. */
    protected HashIntMap _bundles = new HashIntMap();

    /** The servlet context init parameter name that is used to load our
     * site-specific jar path configuration parameter. */
    protected static final String SITE_JAR_PATH = "siteJarPath";

    /** The default path to the site-specific jar files. This won't be
     * used without logging a complaint first. */
    protected static final String DEFAULT_SITE_JAR_PATH =
        "/usr/share/java/webapps/site-data";

    /** The file extension to be appended to the string site identifier to
     * obtain the file name of the site-specific jar file. */
    protected static final String JAR_EXTENSION = ".jar";
}
