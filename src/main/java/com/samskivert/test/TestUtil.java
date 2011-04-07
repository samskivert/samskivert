//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2011 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static com.samskivert.Log.log;

/**
 * Utilities used by unit tests for the samskivert library which are
 * potentially useful for projects that wish to implement tests in the
 * same manner. The samskivert unit tests are built using the
 * <a href="http://junit.org">JUnit</a> testing framework.
 */
public class TestUtil
{
    /**
     * Returns the path via which a test-related resource can be loaded.
     * This assumes that the mechanism used to invoke the test code
     * defined a system property named <code>test_dir</code> which is the
     * path to the project top-level test directory, to which the supplied
     * path will be appended to obtain the path to the resource.
     *
     * @param path the path to the resource, relative to the the top-level
     * test directory. It should contain a leading slash but one will be
     * provided if necessary.
     */
    public static String getResourcePath (String path)
    {
        String testdir = System.getProperty("test_dir");
        if (testdir == null) {
            log.warning("The 'test_dir' system property was not set " +
                        "to the top-level test directory.");
            // fake it and hope for the best
            testdir = ".";
        }

        StringBuilder rpath = new StringBuilder(testdir);
        if (!path.startsWith("/")) {
            rpath.append("/");
        }
        rpath.append(path);

        return rpath.toString();
    }

    /**
     * Returns an input stream via which a test-related resource can be
     * loaded. The path is constructed as in {@link #getResourcePath}.
     *
     * @param path the path to the resource (see {@link
     * #getResourcePath}).
     *
     * @exception FileNotFoundException thrown if the resource file with
     * the specified path does not exist.
     */
    public static InputStream getResourceAsStream (String path)
        throws FileNotFoundException
    {
        // get the path to the resource and return a file input stream
        // from which to load it
        return new FileInputStream(getResourcePath(path));
    }
}
