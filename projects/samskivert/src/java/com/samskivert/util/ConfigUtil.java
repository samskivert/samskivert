//
// $Id: ConfigUtil.java,v 1.3 2001/08/08 23:46:00 mdb Exp $

package com.samskivert.util;

import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

import com.samskivert.Log;

/**
 * The config util class provides routines for loading configuration
 * information out of a file that lives somewhere in the classpath.
 */
public class ConfigUtil
{
    /**
     * Loads a properties file from the named file that exists somewhere
     * in the classpath. A full path should be supplied, but variations
     * including and not including a leading slash will be used because
     * JVMs differ on their opinion of whether this is necessary.
     *
     * <p> The classloader that loaded the <code>ConfigUtil</code> class
     * is searched first, followed by the system classpath. If you wish to
     * provide an additional classloader, use the version of this function
     * that takes a classloader as an argument.
     *
     * @param path The path to the properties file, relative to the root
     * of the classpath entry from which it will be loaded
     * (e.g. <code>/conf/foo.properties</code>).
     *
     * @return A properties object loaded with the contents of the
     * specified file if the file could be found, null otherwise.
     */
    public static Properties loadProperties (String path)
	throws IOException
    {
	return loadProperties(path, ConfigUtil.class.getClassLoader());
    }

    /**
     * Like the other version of {@link #loadProperties(String)}
     * but this one uses the supplied class loader rather than the class
     * loader used to load the <code>ConfigUtil</code> class.
     *
     * @see #loadProperties(String)
     */
    public static Properties loadProperties (String path, ClassLoader loader)
	throws IOException
    {
	InputStream in = getStream(path, loader);
	Properties props = null;

	if (in != null) {
	    props = new Properties();
	    props.load(in);
	}

	return props;
    }

    /**
     * Creates a properties instance by combining properties files loaded
     * using the specified classpath-relative property file path. A
     * leading slash should be supplied, but variations including and not
     * including a leading slash will be used because JVMs differ on their
     * opinion of whether this is necessary.
     *
     * <p> The inheritance works like so: the file will be searched for in
     * the classpath from farthest to nearest. Near and far in the
     * classpath are defined by the class loading search order. Normal
     * class loading searches from nearest to farthest. Beginning with the
     * farthest copy of the properties file, sucessively nearer copies
     * will be overlaid onto those properties to achieve a sort of
     * inheritance.  Properties specified in nearer versions of the file
     * will override those in farther versions of the file, but properties
     * not specified in nearer versions will be "inherited" from the
     * farther versions.  Using this mechanism, a standard set of defaults
     * can be provided and users need only place a properties file with
     * their preferred overrides somewhere nearer in the classpath to have
     * those overrides properly combined with the original
     * defaults. Because the entire classpath is searched, this process
     * can cascade up through a set of properties files and provide a
     * powerful mechanism for inheriting configuration information.
     *
     * <p> The classloader that loaded the <code>ConfigUtil</code> class
     * is searched first, followed by the system classpath. If you wish to
     * provide an additional classloader, use the version of this function
     * that takes a classloader as an argument.
     *
     * @param path The path to the properties file, relative to the root
     * of the classpath entries from which it will be loaded
     * (e.g. <code>/conf/foo.properties</code>).
     *
     * @return A properties object loaded with the contents of the
     * specified file if the file could be found, null otherwise.
     */
    public static Properties loadInheritedProperties (String path)
	throws IOException
    {
	return loadInheritedProperties(
            path, ConfigUtil.class.getClassLoader());
    }

    /**
     * Like the other version of {@link #loadInheritedProperties(String)}
     * but this one uses the supplied class loader rather than the class
     * loader used to load the <code>ConfigUtil</code> class.
     *
     * @see #loadInheritedProperties(String)
     */
    public static Properties loadInheritedProperties (
        String path, ClassLoader loader)
	throws IOException
    {
        // first look for the files in the supplied class loader
        Enumeration enum = getResources(path, loader);
        if (!enum.hasMoreElements()) {
            // if we couldn't find anything there, try the system class
            // loader (but only if that's not where we were already
            // looking)
            ClassLoader sysloader = ClassLoader.getSystemClassLoader();
            if (sysloader != loader) {
                enum = getResources(path, sysloader);
            }
        }

        // we need to process the resources in reverse order, so we put
        // them all into an array first
        ArrayList rsrcs = new ArrayList();
        while (enum.hasMoreElements()) {
            rsrcs.add(enum.nextElement());
        }

        // now load each file in turn into our properties object
        Properties props = new Properties();
        for (int i = rsrcs.size()-1; i >= 0; i--) {
            URL rurl = (URL)rsrcs.get(i);
            InputStream in = rurl.openStream();
            props.load(in);
        }

	return props;
    }

    /**
     * Returns an input stream referencing a file that exists somewhere in
     * the classpath. A full path (relative to the classpath directories)
     * should be supplied, but variations including and not including a
     * leading slash will be used because JVMs differ on their opinion of
     * whether this is necessary.
     *
     * <p> The classloader that loaded the <code>ConfigUtil</code> class
     * is searched first, followed by the system classpath. If you wish to
     * provide an additional classloader, use the version of this function
     * that takes a classloader as an argument.
     *
     * @param path The path to the file, relative to the root of the
     * classpath directory from which it will be loaded
     * (e.g. <code>/conf/foo.gif</code> or perhaps just
     * <code>/bar.gif</code> if the file is at the top level).
     */
    public static InputStream getStream (String path)
    {
	return getStream(path, ConfigUtil.class.getClassLoader());
    }

    /**
     * Returns an input stream referencing a file that exists somewhere in
     * the classpath. A full path (relative to the classpath directories)
     * should be supplied, but variations including and not including a
     * leading slash will be used because JVMs differ on their opinion of
     * whether this is necessary.
     *
     * <p> The supplied classloader is searched first, followed by the
     * system classloader.
     *
     * @param path The path to the file, relative to the root of the
     * classpath directory from which it will be loaded
     * (e.g. <code>/conf/foo.gif</code> or perhaps just
     * <code>/bar.gif</code> if the file is at the top level).
     */
    public static InputStream getStream (String path, ClassLoader loader)
    {
	// first try the supplied class loader
	InputStream in = getResourceAsStream(path, loader);
	if (in != null) {
	    return in;
	}

	// if that didn't work, try the system class loader (but only if
	// it's different from the class loader we just tried)
        ClassLoader sysloader = ClassLoader.getSystemClassLoader();
        if (sysloader != loader) {
            return getResourceAsStream(path, loader);
        } else {
            return null;
        }
    }

    protected static InputStream getResourceAsStream (
        String path, ClassLoader loader)
    {
        // make sure the class loader isn't null
        if (loader == null) {
//              Log.debug("No loader for get resource request " +
//                        "[path=" + path + "].");
            return null;
        }
        // try the path as is
	InputStream in = loader.getResourceAsStream(path);
	if (in != null) {
	    return in;
	}
	// try toggling the leading slash
	return loader.getResourceAsStream(togglePath(path));
    }

    protected static Enumeration getResources (
        String path, ClassLoader loader)
        throws IOException
    {
        // make sure the class loader isn't null
        if (loader == null) {
//              Log.debug("No loader for get resource request " +
//                        "[path=" + path + "].");
            return null;
        }
        // try the path as is
	Enumeration enum = loader.getResources(path);
	if (enum.hasMoreElements()) {
	    return enum;
	}
	// try toggling the leading slash
	return loader.getResources(togglePath(path));
    }

    protected static String togglePath (String path)
    {
	if (path.startsWith("/")) {
	    return path.substring(1);
	} else {
	    return "/" + path;
	}
    }

    /**
     * Unit test driver.
     */
    public static void main (String[] args)
    {
        try {
            String path = "/com/samskivert/util/test.properties";
            Properties props = loadInheritedProperties(path);
            System.out.println(props);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}
