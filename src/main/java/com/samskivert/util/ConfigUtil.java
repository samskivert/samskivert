//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import java.io.*;
import java.net.URL;
import java.security.AccessControlException;
import java.util.*;
import java.util.Map;

import com.samskivert.io.StreamUtil;

import static com.samskivert.util.UtilLog.log;

/**
 * The config util class provides routines for loading configuration information.
 */
public class ConfigUtil
{
    /**
     * Obtains the specified system property via {@link System#getProperty}, parses it into an
     * integer and returns the value. If the property is not set, the default value will be
     * returned. If the property is not a properly formatted integer, an error will be logged and
     * the default value will be returned.
     */
    public static int getSystemProperty (String key, int defval)
    {
        String valstr = System.getProperty(key);
        int value = defval;
        if (valstr != null) {
            try {
                value = Integer.parseInt(valstr);
            } catch (NumberFormatException nfe) {
                log.warning("'" + key + "' must be a numeric value", "value", valstr, "error", nfe);
            }
        }
        return value;
    }

    /**
     * Loads a properties file from the named file that exists somewhere in the classpath.
     *
     * <p> The classloader that loaded the <code>ConfigUtil</code> class is searched first,
     * followed by the system classpath. If you wish to provide an additional classloader, use the
     * version of this function that takes a classloader as an argument.
     *
     * @param path The path to the properties file, relative to the root of the classpath entry
     * from which it will be loaded (e.g. <code>com/foo/config.properties</code>).
     *
     * @return A properties object loaded with the contents of the specified file if the file could
     * be found, null otherwise.
     */
    public static Properties loadProperties (String path)
        throws IOException
    {
        return loadProperties(path, ConfigUtil.class.getClassLoader());
    }

    /**
     * Like {@link #loadProperties(String)} but this method uses the supplied class loader rather
     * than the class loader used to load the <code>ConfigUtil</code> class.
     */
    public static Properties loadProperties (String path, ClassLoader loader)
        throws IOException
    {
        Properties props = new Properties();
        loadProperties(path, loader, props);
        return props;
    }

    /**
     * Like {@link #loadProperties(String,ClassLoader)} but the properties are loaded into the
     * supplied {@link Properties} object.
     */
    public static void loadProperties (String path, ClassLoader loader, Properties target)
        throws IOException
    {
        InputStream in = getStream(path, loader);
        if (in == null) {
            throw new FileNotFoundException(path);
        }
        target.load(in);
        in.close();
    }

    /**
     * Creates a properties instance by combining properties files loaded using the specified
     * classpath-relative property file path.
     *
     * <p> The inheritance works in two ways:
     *
     * <ul><li><b>Overrides</b>
     *
     * <p> All properties files with the specified name are located in the classpath and merged
     * into a single set of properties according to an explicit inheritance hierarchy defined by a
     * couple of custom properties. This is best explained with an example:
     *
     * <p><code>com/samskivert/foolib/config.properties</code> contains:
     *
     * <pre>
     * _package = foolib
     *
     * config1 = value1
     * config2 = value2
     * ...
     * </pre>
     *
     * and this is packaged into <code>foolib.jar</code>. Happy Corp writes an application that
     * uses foolib and wants to override some properties in foolib's configuration. They create a
     * properties file in <code>happyapp.jar</code> with the path
     * <code>com/samskivert/foolib/config.properties</code>. It contains:
     *
     * <pre>
     * _package = happyapp
     * _overrides = foolib
     *
     * config2 = happyvalue
     * </pre>
     *
     * When foolib loads its <code>config.properties</code> the overrides from
     * <code>happyapp.jar</code> will be applied to the properties defined in
     * <code>foolib.jar</code> and foolib will see Happy Corp's overridden properties.
     *
     * <p> Note that conflicting overrides must be resolved "by hand" so to speak. For example, if
     * Happy Corp used some other library that also overrode foolib's configuration, say barlib,
     * whose <code>barlib.jar</code> contained:
     *
     * <pre>
     * _package = barlib
     * _overrides = foolib
     *
     * config1 = barvalue
     * </pre>
     *
     * Happy Corp's <code>config.properties</code> would not be able to override foolib's
     * configuration directly because the config system would not know which overrides to use. It
     * instead must override barlib's configuration, like so:
     *
     * <pre>
     * _package = happyapp
     * _overrides = barlib
     * ...
     * </pre>
     *
     * Moreover, if there were yet a third library that also overrode foolib, Happy Corp would have
     * to resolve the conflict between barlib and bazlib explicitly:
     *
     * <pre>
     * _package = happyapp
     * _overrides = barlib, bazlib
     * ...
     * </pre>
     *
     * This would force bazlib's overrides to take precedence over barlib's overrides, resolving
     * the inheritance ambiguity created when both barlib and bazlib claimed to override foolib.
     *
     * <p> This all certainly seems a bit complicated, but in most cases there is only one user of
     * a library and overriding is very straightforward. The additional functionality is provided
     * to resolve cases where conflicts do arise.
     *
     * <li><b>Extends</b>
     *
     * <p> The second type of inheritance, extension, is more straightforward. In this case, a
     * properties file explicitly extends another properties file. For example,
     * <code>com/foocorp/puzzle/config.properties</code> contains:
     *
     * <pre>
     * # Standard configuration options
     * score = 25
     * multiplier = 2
     * </pre>
     *
     * <code>com/foocorp/puzzle/footris/config.properties</code> contains:
     *
     * <pre>
     * _extends = com/foocorp/puzzle/config.properties
     *
     * # Footris configuration options
     * score = 15 # override the default score
     * bonus = 55
     * </pre>
     *
     * The Footris configuration will inherit default values from the general puzzle configuration,
     * overriding any that are specified explicitly.
     *
     * <p> When resolving a properties file that extends another properties file, first the
     * extended properties are loaded and all <code>_overrides</code> are applied to that
     * properties file. Then all <code>_overrides</code> are applied to the extending properties
     * file and then the extending properties are merged with the extended properties.  </ul>
     *
     * <em>A final note:</em> All of the inheritance directives must be grouped together in an
     * uninterrupted sequence of lines. One the parsing code finds the first directive, it stops
     * parsing when it sees a line that does not contain a directive.
     *
     * @param path The path to the properties file, relative to the root of the classpath entries
     * from which it will be loaded (e.g. <code>com/foo/config.properties</code>).
     *
     * @return A properties object loaded with the contents of the specified file if the file could
     * be found, null otherwise.
     */
    public static Properties loadInheritedProperties (String path)
        throws IOException
    {
        return loadInheritedProperties(path, ConfigUtil.class.getClassLoader());
    }

    /**
     * Like {@link #loadInheritedProperties(String)} but loads the properties into the supplied
     * target object.
     */
    public static void loadInheritedProperties (String path, Properties target)
        throws IOException
    {
        loadInheritedProperties(path, ConfigUtil.class.getClassLoader(), target);
    }

    /**
     * Like {@link #loadInheritedProperties(String)} but this method uses the supplied class loader
     * rather than the class loader used to load the <code>ConfigUtil</code> class.
     */
    public static Properties loadInheritedProperties (String path, ClassLoader loader)
        throws IOException
    {
        Properties props = new Properties();
        loadInheritedProperties(path, loader, props);
        return props;
    }

    /**
     * Like {@link #loadInheritedProperties(String,ClassLoader)} but the properties are loaded into
     * the supplied properties object.  Properties that already exist in the supplied object will
     * be overwritten by the loaded properties where they have the same key.
     *
     * @exception FileNotFoundException thrown if no properties files are found for the specified
     * path.
     */
    public static void loadInheritedProperties (String path, ClassLoader loader, Properties target)
        throws IOException
    {
        // first look for the files in the supplied class loader
        Enumeration<URL> enm = getResources(path, loader);
        if (!enm.hasMoreElements()) {
            // if we couldn't find anything there, try the system class loader (but only if that's
            // not where we were already looking)
            try {
                ClassLoader sysloader = ClassLoader.getSystemClassLoader();
                if (sysloader != loader) {
                    enm = getResources(path, sysloader);
                }
            } catch (AccessControlException ace) {
                // can't get the system loader, no problem!
            }
        }

        // stick the matches into an array list so that we can count them
        ArrayList<URL> rsrcs = new ArrayList<URL>();
        while (enm.hasMoreElements()) {
            rsrcs.add(enm.nextElement());
        }

        // load up the metadata for the properties files
        PropRecord root = null, crown = null;
        HashMap<String,PropRecord> map = null;

        if (rsrcs.size() == 0) {
            // if we found no resources in our enumerations, complain
            throw new FileNotFoundException(path);

        } else if (rsrcs.size() == 1) {
            // parse the metadata for our only properties file
            root = parseMetaData(path, rsrcs.get(0));

        } else {
            map = new HashMap<String,PropRecord>();
            for (int ii = 0; ii < rsrcs.size(); ii++) {
                // parse the metadata for this properties file
                PropRecord record = parseMetaData(path, rsrcs.get(ii));

                // make sure the record we parseded is valid because we're going to be doing
                // overrides
                record.validate();

                // then map it according to its package defintion
                map.put(record._package, record);

                // if this guy overrides nothing, he's the root property
                if (record._overrides == null) {
                    // make sure there aren't two or more roots
                    if (root != null) {
                        String errmsg = record + " cannot have the same path as " + root +
                            " without one overriding the other.";
                        throw new IOException(errmsg);
                    }
                    root = record;
                }
            }

            // now wire up all the records according to the hierarchy
            for (PropRecord prec : map.values()) {
                if (prec._overrides == null) {
                    // sanity check
                    if (prec != root) {
                        String errmsg = "Internal error: found multiple roots where we shouldn't " +
                            "have [root=" + root + ", prec=" + prec + "]";
                        throw new IOException(errmsg);
                    }
                    continue;
                }

                // wire this guy up to whomever he overrides
                for (String ppkg : prec._overrides) {
                    PropRecord parent = map.get(ppkg);
                    if (parent == null) {
                        throw new IOException("Cannot find parent '" + ppkg + "' for " + prec);
                    }
                    parent.add(prec);
                }
            }

            // verify that there is only one crown
            ArrayList<PropRecord> crowns = new ArrayList<PropRecord>();
            for (PropRecord prec : map.values()) {
                if (prec.size() == 0) {
                    crowns.add(prec);
                }
            }

            if (crowns.size() == 0) {
                String errmsg = "No top-level property override could be found, perhaps there " +
                    "are circular references " + StringUtil.toString(map.values());
                throw new IOException(errmsg);

            } else if (crowns.size() > 1) {
                StringBuilder errmsg = new StringBuilder();
                errmsg.append("Multiple top-level properties were found, ");
                errmsg.append("one definitive top-level file must provide ");
                errmsg.append("an order for all others:\n");
                for (int ii = 0; ii < crowns.size(); ii++) {
                    if (ii > 0) {
                        errmsg.append("\n");
                    }
                    errmsg.append(crowns.get(ii));
                }
                throw new IOException(errmsg.toString());
            }

            crown = crowns.get(0);
        }

        // if the root extends another file, resolve that first
        if (!StringUtil.isBlank(root._extends)) {
            try {
                loadInheritedProperties(root._extends, loader, target);
            } catch (FileNotFoundException fnfe) {
                throw new IOException(
                    "Unable to locate parent '" + root._extends + "' for '" + root.path + "'");
            }
        }

        // now apply all of the overrides, in the appropriate order
        if (crown != null) {
            HashMap<String,PropRecord> applied = new HashMap<String,PropRecord>();
            loadPropertiesOverrides(crown, map, applied, loader, target);

        } else {
            // we have no overrides, so load our props up straight
            InputStream in = root.sourceURL.openStream();
            target.load(in);
            in.close();
        }

        // finally remove the metadata properties
        target.remove(PACKAGE_KEY);
        target.remove(EXTENDS_KEY);
        target.remove(OVERRIDES_KEY);
    }

    /** {@link #loadInheritedProperties(String,ClassLoader,Properties)} helper function. */
    protected static void loadPropertiesOverrides (
        PropRecord prec, Map<String,PropRecord> records, Map<String,PropRecord> applied,
        ClassLoader loader, Properties target)
        throws IOException
    {
        if (applied.containsKey(prec._package)) {
            return;
        }

        // first load up properties from all of our parent nodes
        if (prec._overrides != null) {
            for (String override : prec._overrides) {
                PropRecord parent = records.get(override);
                loadPropertiesOverrides(parent, records, applied, loader, target);
            }
        }

        // now apply our properties values
        InputStream in = prec.sourceURL.openStream();
        target.load(in);
        in.close();
        applied.put(prec._package, prec);
    }

    /**
     * Performs simple processing of the supplied input stream to obtain inheritance metadata from
     * the properties file.
     */
    protected static PropRecord parseMetaData (String path, URL sourceURL)
        throws IOException
    {
        InputStream input = sourceURL.openStream();
        BufferedReader bin = new BufferedReader(new InputStreamReader(input));
        try {
            PropRecord record = new PropRecord(path, sourceURL);
            boolean started = false;
            String line;
            while ((line = bin.readLine()) != null) {
                if (line.startsWith(PACKAGE_KEY)) {
                    record._package = parseValue(line);
                    started = true;
                } else if (line.startsWith(EXTENDS_KEY)) {
                    record._extends = parseValue(line);
                    started = true;
                } else if (line.startsWith(OVERRIDES_KEY)) {
                    record._overrides = parseValues(line);
                    started = true;
                } else if (started) {
                    break;
                }
            }
            return record;
        } finally {
            StreamUtil.close(bin);
        }
    }

    /** {@link #parseMetaData} helper function. */
    protected static String parseValue (String line)
    {
        int eidx = line.indexOf("=");
        return (eidx == -1) ? "" : line.substring(eidx+1).trim();
    }

    /** {@link #parseMetaData} helper function. */
    protected static String[] parseValues (String line)
    {
        String value = parseValue(line);
        if (StringUtil.isBlank(value)) {
            return null;
        }

        String[] values = StringUtil.split(value, ",");
        for (int ii = 0; ii < values.length; ii++) {
            values[ii] = values[ii].trim();
        }
        return values;
    }

    /**
     * Returns an input stream referencing a file that exists somewhere in the classpath.
     *
     * <p> The classloader that loaded the <code>ConfigUtil</code> class is searched first,
     * followed by the system classpath. If you wish to provide an additional classloader, use the
     * version of this function that takes a classloader as an argument.
     *
     * @param path The path to the file, relative to the root of the classpath directory from which
     * it will be loaded (e.g. <code>com/foo/bar/foo.gif</code>).
     */
    public static InputStream getStream (String path)
    {
        return getStream(path, ConfigUtil.class.getClassLoader());
    }

    /**
     * Returns an input stream referencing a file that exists somewhere in the classpath.
     *
     * <p> The supplied classloader is searched first, followed by the system classloader.
     *
     * @param path The path to the file, relative to the root of the classpath directory from which
     * it will be loaded (e.g. <code>com/foo/bar/foo.gif</code>).
     */
    public static InputStream getStream (String path, ClassLoader loader)
    {
        // first try the supplied class loader
        InputStream in = getResourceAsStream(path, loader);
        if (in != null) {
            return in;
        }

        // if that didn't work, try the system class loader (but only if it's different from the
        // class loader we just tried)
        try {
            ClassLoader sysloader = ClassLoader.getSystemClassLoader();
            if (sysloader != loader) {
                return getResourceAsStream(path, loader);
            }
        } catch (AccessControlException ace) {
            // can't get the system loader, no problem!
        }
        return null;
    }

    protected static InputStream getResourceAsStream (String path, ClassLoader loader)
    {
        // make sure the class loader isn't null
        if (loader == null) {
//             log.debug("No loader for get resource request", "path", path);
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

    protected static Enumeration<URL> getResources (String path, ClassLoader loader)
        throws IOException
    {
        // make sure the class loader isn't null
        if (loader == null) {
//             log.debug("No loader for get resource request", "path", path);
            return null;
        }
        // try the path as is
        Enumeration<URL> enm = loader.getResources(path);
        if (enm.hasMoreElements()) {
            return enm;
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

    /** Used when parsing inherited properties. */
    protected static final class PropRecord extends ArrayList<PropRecord>
    {
        public String _package;
        public String[] _overrides;
        public String _extends;

        public String path;
        public URL sourceURL;

        public PropRecord (String path, URL sourceURL) {
            this.path = path;
            this.sourceURL = sourceURL;
        }

        public void validate () throws IOException {
            if (StringUtil.isBlank(_package)) {
                throw new IOException("Properties file missing '_package' identifier " + this);
            }

            if ((_overrides != null && _overrides.length > 0) &&
                (!StringUtil.isBlank(_extends))) {
                throw new IOException(
                    "Properties file cannot use both '_overrides' and '_extends' " + this);
            }
        }

        @Override public boolean equals (Object other) {
            return (other instanceof PropRecord) && _package.equals(((PropRecord)other)._package);
        }

        @Override public String toString () {
            return "[package=" + _package + ", overrides=" + StringUtil.toString(_overrides) +
                ", extends=" + _extends + ", path=" + path + ", source=" + sourceURL + "]";
        }
    }

    protected static final String PACKAGE_KEY = "_package";
    protected static final String OVERRIDES_KEY = "_overrides";
    protected static final String EXTENDS_KEY = "_extends";
}
