//
// $Id: PropertiesUtil.java,v 1.2 2000/10/31 00:51:00 mdb Exp $

package com.samskivert.util;

import java.util.Enumeration;
import java.util.Properties;

/**
 * Utility functions related to properties objects.
 */
public class PropertiesUtil
{
    /**
     * Extracts all properties from the supplied properties object with
     * the specified prefix, removes the prefix from the key for those
     * properties and inserts them into a new properties object which is
     * then returned. This is useful for extracting properties from a
     * global configuration object that must be passed to a service that
     * expects it's own private properties (JDBC for example).
     *
     * The property file might look like so:
     *
     * <pre>
     * my_happy_param=my_happy_value
     * ...
     * jdbc.driver=foo.bar.Driver
     * jdbc.url=jdbc://blahblah
     * jdbc.username=bob
     * jdbc.password=is your uncle
     * ...
     * my_happy_other_param=my_happy_other_value
     * </pre>
     *
     * This can be supplied to <code>getSubProperties()</code> with a
     * prefix of <code>"jdbc"</code> and the following properties would be
     * returned:
     *
     * <pre>
     * driver=foo.bar.Driver
     * url=jdbc://blahblah
     * username=bob
     * password=is your uncle
     * </pre>
     */
    public static
	Properties getSubProperties (Properties source, String prefix)
    {
	Properties dest = new Properties();

	// extend the prefix to contain a dot
	prefix = prefix + ".";
	int preflen = prefix.length();

	// scan the source properties
	Enumeration names = source.propertyNames();
	while (names.hasMoreElements()) {
	    String name = (String)names.nextElement();

	    // skip unrelated properties
	    if (!name.startsWith(prefix)) {
		continue;
	    }

	    // insert the value into the new properties minus the prefix
	    dest.put(name.substring(preflen), source.getProperty(name));
	}

	return dest;
    }
}
