//
// $Id: ClassEnumerator.java,v 1.3 2001/07/13 23:25:13 mdb Exp $

package com.samskivert.viztool.enum;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Iterator;

/**
 * The class enumerator is supplied with a classpath which it decomposes
 * and enumerates over all of the classes available via those classpath
 * componenets. To do this it uses component enumerators which know how to
 * enumerate all of the classes in a directory tree, in a jar file, etc.
 * The component enumerators are structured so that new enumerators can be
 * authored for new kinds of classpath component.
 */
public class ClassEnumerator implements Iterator
{
    /**
     * Constructs a class enumerator with the supplied classpath. A set of
     * component enumerators will be chosen for each element and warnings
     * will be generated for components that cannot be processed for some
     * reason or other. Those will be available following the completion
     * of the constructor via <code>getWarnings()</code>.
     *
     * @see #getWarnings
     */
    public ClassEnumerator (String classpath)
    {
        // decompose the path and select enumerators for each component
        StringTokenizer tok = new StringTokenizer(classpath, ":");
        ArrayList warnings = new ArrayList();
        ArrayList enums = new ArrayList();

        while (tok.hasMoreTokens()) {
            String component = tok.nextToken();
            // locate an enumerator for this token
            ComponentEnumerator enum = matchEnumerator(component);
            if (enum == null) {
                String wmsg = "Unable to match enumerator for " +
                    "component '" + component + "'.";
                warnings.add(new Warning(wmsg));

            } else {
                try {
//                      System.out.println("Adding [enum=" + enum +
//                                         ", component=" + component + "].");
                    // construct an enumerator to enumerate this component
                    // and put it on our list
                    enums.add(enum.enumerate(component));

                } catch (EnumerationException ee) {
                    // if there was a problem creating an enumerator for
                    // said component, create a warning to that effect
                    warnings.add(new Warning(ee.getMessage()));
                }
            }
        }

        // convert our list into an array
        _enums = new ComponentEnumerator[enums.size()];
        enums.toArray(_enums);

        // convert the warnings into an array
        _warnings = new Warning[warnings.size()];
        warnings.toArray(_warnings);

        // scan to the first class
        scanToNextClass();
    }

    /**
     * Locates an enumerator that matches the specified component and
     * returns the prototype instance of that enumerator. Returns null if
     * no enumerator could be matched.
     */
    protected ComponentEnumerator matchEnumerator (String component)
    {
        for (int i = 0; i < _enumerators.size(); i++) {
            ComponentEnumerator enum =
                (ComponentEnumerator)_enumerators.get(i);
            if (enum.matchesComponent(component)) {
                return enum;
            }
        }

        return null;
    }

    /**
     * Returns the warnings generated in parsing the classpath and
     * constructing enumerators for each component. For example, if a
     * classpath component specified a directory that was non-existent or
     * inaccessible, a warning would be generated for that component. If
     * no warnings were generated, a zero length array will be returned.
     */
    public Warning[] getWarnings ()
    {
        return _warnings;
    }

    public boolean hasNext ()
    {
        return (_nextClass != null);
    }

    public Object next ()
    {
        String clazz = _nextClass;
        _nextClass = null;
        scanToNextClass();
        return clazz;
    }

    public void remove ()
    {
        // not supported
    }

    /**
     * Queues up the next enumerator in the list or clears out our
     * enumerator reference if we have no remaining enumerators.
     */
    protected void scanToNextClass ()
    {
        if (_enumidx < _enums.length) {
            // grab the current enumerator
            ComponentEnumerator enum = _enums[_enumidx];

            // if it has more classes
            if (enum.hasMoreClasses()) {
                // get the next one
                _nextClass = enum.nextClass();
                return;

            } else {
                // otherwise try the next enum
                _enumidx++;
                scanToNextClass();
            }
        }
    }

    protected ComponentEnumerator[] _enums;
    protected int _enumidx;
    protected String _nextClass;
    protected Warning[] _warnings;

    /**
     * A warning is generated when a component of the classpath cannot be
     * processed for some reason or other.
     */
    public static class Warning
    {
        public String reason;

        public Warning (String reason)
        {
            this.reason = reason;
        }
    }

    public static void main (String[] args)
    {
        // run ourselves on the classpath
        String classpath = System.getProperty("java.class.path");
        ClassEnumerator enum = new ClassEnumerator(classpath);

        // print out the warnings
        Warning[] warnings = enum.getWarnings();
        for (int i = 0; i < warnings.length; i++) {
            System.out.println("Warning: " + warnings[i].reason);
        }

        // enumerate over whatever classes we can
        while (enum.hasNext()) {
            System.out.println("Class: " + enum.next());
        }
    }

    protected static ArrayList _enumerators = new ArrayList();

    static {
        // register our enumerators
        _enumerators.add(new ZipFileEnumerator());
        _enumerators.add(new JarFileEnumerator());
        // the directory enumerator should always be last in the list
        // because it picks up all stragglers and tries enumerating them
        // as if they were directories
        _enumerators.add(new DirectoryEnumerator());
    }
}
