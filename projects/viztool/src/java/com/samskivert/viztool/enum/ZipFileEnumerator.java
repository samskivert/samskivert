//
// $Id: ZipFileEnumerator.java,v 1.1 2001/06/14 20:57:15 mdb Exp $

package com.samskivert.viztool.enum;

import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.*;

import com.samskivert.viztool.Log;

/**
 * The zip file enumerator enumerates all of the classes in a .zip class
 * archive.
 */
public class ZipFileEnumerator extends ComponentEnumerator
{
    /**
     * Constructs a prototype enumerator that can be used for matching.
     */
    public ZipFileEnumerator ()
    {
    }

    /**
     * Constructs a zip file enumerator with the specified zip file for
     * enumeration.
     */
    public ZipFileEnumerator (String zippath)
        throws EnumerationException
    {
        try {
            _zipfile = new ZipFile(zippath);
            _entenum = _zipfile.entries();
            scanToNextClass();

        } catch (IOException ioe) {
            String msg = "Can't enumerate zip file '" + zippath + "': " +
                ioe.getMessage();
            throw new EnumerationException(msg);
        }
    }

    // documentation inherited from interface
    public boolean matchesComponent (String component)
    {
        return component.endsWith(ZIP_SUFFIX);
    }

    // documentation inherited from interface
    public ComponentEnumerator enumerate (String component)
        throws EnumerationException
    {
        return new ZipFileEnumerator(component);
    }

    // documentation inherited from interface
    public boolean hasMoreClasses ()
    {
        return (_nextClass != null);
    }

    // documentation inherited from interface
    public String nextClass ()
    {
        String clazz = _nextClass;
        _nextClass = null;
        scanToNextClass();
        return clazz;
    }

    protected void scanToNextClass ()
    {
        // if we've already scanned to the end of our zipfile, we can bail
        // immediately
        if (_zipfile == null) {
            return;
        }

        // otherwise scan through the zip contents for the next thing that
        // looks like a class
        while (_entenum.hasMoreElements()) {
            ZipEntry entry = (ZipEntry)_entenum.nextElement();
            String nextClass = entry.getName();
            if (nextClass.endsWith(CLASS_SUFFIX)) {
                _nextClass = pathToClassName(nextClass);
                break;
            }
        }

        // if we've reached the end of the zip file, we want to close
        // things up
        if (_zipfile != null && _nextClass == null) {
            try {
                _zipfile.close();
            } catch (IOException ioe) {
                Log.warning("Error closing archive: " + ioe.getMessage());
            }
            _zipfile = null;
        }
    }

    protected ZipFile _zipfile;
    protected Enumeration _entenum;
    protected String _nextClass;

    protected static final String ZIP_SUFFIX = ".zip";
}
