//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.servlet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;

import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.servlet.http.HttpServletRequest;

import com.samskivert.util.HashIntMap;

import static com.samskivert.servlet.Log.log;

/**
 * Web applications may wish to load resources in such a way that the site
 * on which they are running is allowed to override a resource with a
 * site-specific version (a header, footer or navigation template for
 * example). The site resource loader provides this capability by loading
 * resources from a site-specific jar file.
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
 */
public class SiteResourceLoader
{
    /**
     * Constructs a new resource loader.
     *
     * @param siteIdent the site identifier to be used to identify which
     * site through which a request was made when loading resources.
     * @param siteJarPath the path to the site-specific jar files.
     */
    public SiteResourceLoader (
        SiteIdentifier siteIdent, String siteJarPath)
    {
        // keep this stuff around
        _siteIdent = siteIdent;
        _jarPath = siteJarPath;
    }

    /**
     * Loads the specific resource, from the site-specific jar file if one
     * exists and contains the specified resource. If no resource exists
     * with that path, null will be returned.
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
     * exists and contains the specified resource. If no resource exists
     * with that path, null will be returned.
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
     * Returns a class loader that loads resources from the site-specific
     * jar file for the specified site. If no site-specific jar file
     * exists for the specified site, null will be returned.
     */
    public ClassLoader getSiteClassLoader (int siteId)
        throws IOException
    {
        // synchronize on the lock to ensure that only one thread per site
        // is concurrently executing
        synchronized (getLock(siteId)) {
            // see if we've already got one
            ClassLoader loader = _loaders.get(siteId);

            // create one if we've not
            if (loader == null) {
                final SiteResourceBundle bundle = getBundle(siteId);
                if (bundle == null) {
                    // no bundle... no classloader.
                    return null;
                }

                loader = AccessController.doPrivileged(new PrivilegedAction<SiteClassLoader>() {
                    public SiteClassLoader run () {
                        return new SiteClassLoader(bundle);
                    }
                });
                _loaders.put(siteId, loader);
            }

            return loader;
        }
    }

    @Override
    public String toString ()
    {
        return "[jarPath=" + _jarPath + "]";
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
        SiteResourceBundle bundle = _bundles.get(siteId);

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
            return (entry == null) ? null : jarFile.getInputStream(entry);
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

        @Override public String toString ()
        {
            return "[bundle=" + file + "]";
        }

        /**
         * Reopens our site-specific jar file if it has been modified
         * since it was last opened.
         */
        protected void refreshJarFile ()
            throws IOException
        {
            // ensure that the file exists
            if (!file.exists()) {
                String errmsg = "No site-specific jar file " +
                    "[path=" + file.getPath() + "].";
                throw new FileNotFoundException(errmsg);
            }

            // determine whether or not we need to create a new jarfile
            // instance
            if (file.lastModified() > _lastModified) {
                // make a note of the last modified time
                _lastModified = file.lastModified();

                // close our old jar file if we've got one
                if (jarFile != null) {
                    jarFile.close();
                }

                log.info("Opened site bundle", "path", file.getPath());

                // and open a new one
                jarFile = new JarFile(file);
            }
        }

        /** The last modified time of the jar file at the time that we
         * opened it for reading. */
        protected long _lastModified;
    }

    protected static class SiteClassLoader extends ClassLoader
    {
        public SiteClassLoader (SiteResourceBundle bundle)
        {
            _bundle = bundle;
        }

        @Override public InputStream getResourceAsStream (String path)
        {
            try {
                return _bundle.getResourceAsStream(path);
            } catch (IOException ioe) {
                log.warning("Error loading resource from jarfile", "bundle", _bundle, "path", path,
                            "error", ioe);
                return null;
            }
        }

        @Override public String toString ()
        {
            return _bundle.toString();
        }

        protected SiteResourceBundle _bundle;
    }

    /** The site identifier we use to identify requests. */
    protected SiteIdentifier _siteIdent;

    /** The path to our site-specific jar files. */
    protected String _jarPath;

    /** We synchronize on a per-site basis. */
    protected HashIntMap<Object> _locks = new HashIntMap<Object>();

    /** The table of site-specific jar file information. */
    protected HashIntMap<SiteResourceBundle> _bundles =
        new HashIntMap<SiteResourceBundle>();

    /** The table of site-specific class loaders. */
    protected HashIntMap<ClassLoader> _loaders = new HashIntMap<ClassLoader>();

    /** The default path to the site-specific jar files. This won't be
     * used without logging a complaint first. */
    protected static final String DEFAULT_SITE_JAR_PATH =
        "/usr/share/java/webapps/site-data";

    /** The file extension to be appended to the string site identifier to
     * obtain the file name of the site-specific jar file. */
    protected static final String JAR_EXTENSION = ".jar";
}
