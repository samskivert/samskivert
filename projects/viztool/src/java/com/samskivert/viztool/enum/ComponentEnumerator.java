//
// $Id: ComponentEnumerator.java,v 1.1 2001/06/14 20:57:15 mdb Exp $

package com.samskivert.viztool.enum;

import com.samskivert.util.StringUtil;

/**
 * A component enumerator knows how to enumerate all of the classes in a
 * particular classpath component. Examples include a zip file enumerator,
 * a directory tree enumerator and a jar file enumerator.
 */
public abstract class ComponentEnumerator
{
    /**
     * To determine which component enumerator should be used for a given
     * classpath component, one instance of each is maintained and used
     * for the matching process.
     *
     * @return true if this enumerator should be used to enumerate the
     * specified classpath component; false otherwise.
     */
    public abstract boolean matchesComponent (String component);

    /**
     * Instantiates an instance of the underlying enumerator and
     * configures it to enumerate the specified classpath component.
     *
     * @exception EnumerationException thrown if some problem (like file
     * or directory not existing or being inaccessible) prevents the
     * enumerator from enumerating the component.
     */
    public abstract ComponentEnumerator enumerate (String component)
        throws EnumerationException;

    /**
     * Returns true if there are more classes yet to be enumerated for
     * this component.
     */
    public abstract boolean hasMoreClasses ();

    /**
     * Returns the next class in this component's enumeration.
     */
    public abstract String nextClass ();

    /**
     * Converts a classfile path to a class name (eg. foo/bar/Baz.class
     * converts to foo.bar.Baz).
     */
    protected String pathToClassName (String path)
    {
        // strip off the .class suffix
        path = path.substring(0, path.length() - CLASS_SUFFIX.length());
        // convert slashes to dots
        return StringUtil.replace(path, "/", ".");
    }

    /**
     * This is used to identify class files.
     */
    protected static final String CLASS_SUFFIX = ".class";
}
