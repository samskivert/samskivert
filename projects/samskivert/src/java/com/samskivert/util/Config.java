//
// $Id: Config.java,v 1.1 2001/07/12 02:44:34 mdb Exp $

package com.samskivert.util;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Properties;

import com.samskivert.Log;

/**
 * The config class provides a unified interaface to application
 * configuration information. It takes care of loading properties files
 * from locations in the classpath and binding the properties in those
 * files into the global config namespace. It also provides access to more
 * datatypes than simply strings, handling the parsing of ints as well as
 * int arrays and string arrays.
 *
 * <p> An application should construct a single instance of
 * <code>Config</code> and use it to access all of its configuration
 * information.
 */
public class Config
{
    /**
     * Constructs a new config object which can be used immediately by
     * binding properties files into the namespace and subsequently
     * requesting values.
     */
    public Config ()
    {
    }

    /**
     * Binds the specified properties file into the namespace with the
     * specified name. If the properties file in question contains a
     * property of the name <code>foo.bar</code> and the file is bound
     * into the namespace under <code>baz</code>, then that property would
     * be accessed as <code>baz.foo.bar</code>.
     *
     * @param name the root name for all properties in this file.
     * @param path the path to the properties file which must live
     * somewhere in the classpath. For example: <code>foo/bar/baz</code>
     * would indicate a file named "foo/bar/baz.properties" living in the
     * classpath.
     *
     * @exception IOException thrown if an error occurrs loading the
     * properties file (like it doesn't exist or cannot be accessed).
     */
    public void bindProperties (String name, String path)
        throws IOException
    {
        // append the file suffix onto the path
        path += PROPS_SUFFIX;
        // load the properties file
        Properties props = ConfigUtil.loadProperties(path);
        if (props == null) {
            throw new IOException("Unable to load properties file: " + path);
        }
        // put it into the hashtable with the specified name
        _props.put(name, props);
    }

    /**
     * Fetches and returns the value for the specified configuration
     * property. If the value is not specified in the associated
     * properties file, the supplied default value is returned instead. If
     * the property specified in the file is poorly formatted (not and
     * integer, not in proper array specification), a warning message will
     * be logged and the default value will be returned.
     *
     * @param the fully qualified name of the property (fully qualified
     * meaning that it contains the namespace identifier as well), for
     * example: <code>foo.bar.baz</code>.
     * @param defval the value to return if the property is not specified
     * in the config file.
     *
     * @return the value of the requested property.
     */
    public int getValue (String name, int defval)
    {
        String val = resolveProperty(name);

        // if it's not specified, we return the default
        if (val == null) {
            return defval;
        }

        // otherwise parse  it into an integer
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException nfe) {
            Log.warning("Malformed integer property [fqn=" + name +
                        ", value=" + val + "].");
            return defval;
        }
    }

    /**
     * Fetches and returns the value for the specified configuration
     * property. If the value is not specified in the associated
     * properties file, the supplied default value is returned instead.
     *
     * @param the fully qualified name of the property (fully qualified
     * meaning that it contains the namespace identifier as well), for
     * example: <code>foo.bar.baz</code>.
     * @param defval the value to return if the property is not specified
     * in the config file.
     *
     * @return the value of the requested property.
     */
    public String getValue (String name, String defval)
    {
        String val = resolveProperty(name);
        // if it's not specified, we return the default
        return (val == null) ? defval : val;
    }

    /**
     * Fetches and returns the value for the specified configuration
     * property. If the value is not specified in the associated
     * properties file, the supplied default value is returned instead. If
     * the property specified in the file is poorly formatted (not and
     * integer, not in proper array specification), a warning message will
     * be logged and the default value will be returned.
     *
     * @param the fully qualified name of the property (fully qualified
     * meaning that it contains the namespace identifier as well), for
     * example: <code>foo.bar.baz</code>.
     * @param defval the value to return if the property is not specified
     * in the config file.
     *
     * @return the value of the requested property.
     */
    public int[] getValue (String name, int[] defval)
    {
        String val = resolveProperty(name);

        // if it's not specified, we return the default
        if (val == null) {
            return defval;
        }

        // otherwise parse it into an array of ints
        int[] result = StringUtil.parseIntArray(val);
        if (result == null) {
            Log.warning("Malformed int array property [fqn=" + name +
                        ", value=" + val + "].");
            return defval;
        }

        return result;
    }

    /**
     * Fetches and returns the value for the specified configuration
     * property. If the value is not specified in the associated
     * properties file, the supplied default value is returned instead. If
     * the property specified in the file is poorly formatted (not and
     * integer, not in proper array specification), a warning message will
     * be logged and the default value will be returned.
     *
     * @param the fully qualified name of the property (fully qualified
     * meaning that it contains the namespace identifier as well), for
     * example: <code>foo.bar.baz</code>.
     * @param defval the value to return if the property is not specified
     * in the config file.
     *
     * @return the value of the requested property.
     */
    public String[] getValue (String name, String[] defval)
    {
        String val = resolveProperty(name);

        // if it's not specified, we return the default
        if (val == null) {
            return defval;
        }

        // otherwise parse it into an array of strings
        String[] result = StringUtil.parseStringArray(val);
        if (result == null) {
            Log.warning("Malformed string array property [fqn=" + name +
                        ", value=" + val + "].");
            return defval;
        }

        return result;
    }

    protected String resolveProperty (String name)
    {
        int didx = name.indexOf(".");
        if (didx == -1) {
            Log.warning("Invalid fully qualified property name " +
                        "[name=" + name + "].");
            return null;
        }

        String id = name.substring(0, didx);
        String key = name.substring(didx+1);

        Properties props = (Properties)_props.get(id);
        if (props == null) {
            Log.warning("No property file bound to top-level name " +
                        "[name=" + id + ", key=" + key + "].");
            return null;
        }

        return props.getProperty(key);
    }

    public static void main (String[] args)
    {
        Config config = new Config();
        try {
            config.bindProperties("test", "com/samskivert/util/test");

            System.out.println("test.prop1: " +
                               config.getValue("test.prop1", 1));
            System.out.println("test.prop2: " +
                               config.getValue("test.prop2", "two"));

            int[] ival = new int[] { 1, 2, 3 };
            ival = config.getValue("test.prop3", ival);
            System.out.println("test.prop3: " + StringUtil.toString(ival));

            String[] sval = new String[] { "one", "two", "three" };
            sval = config.getValue("test.prop4", sval);
            System.out.println("test.prop4: " + StringUtil.toString(sval));

            System.out.println("test.prop5: " +
                               config.getValue("test.prop5", "undefined"));

        } catch (IOException ioe) {
            ioe.printStackTrace(System.err);
        }
    }

    protected Hashtable _props = new Hashtable();

    protected static final String PROPS_SUFFIX = ".properties";
}
