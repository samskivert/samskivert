//
// $Id: Version.java,v 1.1 2002/04/11 07:23:59 mdb Exp $

package com.samskivert.depends;

/**
 * Represents a library version. Version strings are not constrained to a
 * particular format, but must meet a couple of criterion: first, they
 * must contain only valid filename characters as they will be used to
 * create the filename of our local copy of the library's jar file.
 * Second, if a dependency's version specification ends with a
 * '<code>+</code>', the dependency is considered satisfied by any library
 * release newer than the specified version. Thus, if a version string
 * ends in plus, wackiness ensues. Fortunately, we can issue a warning if
 * anyone tries to stamp a library with a descriptor that declares its
 * version to end with a plus.
 */
public class Version
{
    /**
     * Constructs a version instance from the supplied version
     * specification string.
     */
    public Version (String version)
    {
        _version = version;
    }

    /**
     * Returns true if this version specification allows replacement by
     * newer versions, false otherwise.
     */
    public boolean openEnded ()
    {
        return _version.endsWith("+");
    }

    /**
     * Returns a string representation of this instance.
     */
    public String toString ()
    {
        return _version;
    }

    /** Our version specification string. */
    protected String _version;
}
