//
// $Id: Dependency.java,v 1.1 2002/04/11 07:23:59 mdb Exp $

package com.samskivert.depends;

import java.io.File;
import java.io.IOException;

import java.net.URL;
import java.net.URLConnection;

/**
 * Describes a particular library dependency.
 */
public class Dependency
{
    /**
     * Constructs a dependency from its specification criterion.
     */
    public Dependency (String libraryName,
                       Version requiredVersion,
                       URL descriptorURL)
    {
        _libraryName = libraryName;
        _requiredVersion = requiredVersion;
        _descriptorURL = descriptorURL;
    }

    /**
     * Informs this dependency of the location of our local library
     * repository.
     */
    public void setLocalRepository (File repository)
    {
        _repository = repository;
    }

    /**
     * Returns the human-readable library identifier.
     */
    public String getLibraryName ()
    {
        return _libraryName;
    }

    /**
     * Returns the library version required by this dependency
     * specification.
     */
    public Version getRequiredVersion ()
    {
        return _requiredVersion;
    }

    /**
     * Returns the descriptor URL for the library referenced by this
     * dependency.
     */
    public URL getDescriptorURL ()
    {
        return _descriptorURL;
    }

    /**
     * Returns the version of our local copy of the library referenced by
     * this dependency, or null if we have no local copy of the library.
     *
     * @exception IllegalStateException thrown if the dependency has not
     * yet been configured with the path to the local library repository.
     */
    public Version getLocalVersion ()
        throws IOException, FormatException
    {
        if (_localVersion == null) {
            resolveLocalVersion();
        }
        return _localVersion;
    }
 
    /**
     * Returns a file object referencing our local copy of the library
     * referenced by this dependency, or null if the dependency hasn't
     * been resolved.
     *
     * @exception IllegalStateException thrown if the dependency has not
     * yet been configured with the path to the local library repository.
     */
    public File getLocalJar ()
    {
        if (_repository == null) {
            String errmsg = "Dependency has not yet been configured " +
                "with path to local library repository.";
            throw new IllegalStateException(errmsg);
        }
        return new File(_repository, constructJarName());
    }

    /**
     * Returns true if we have a local copy of the library referenced by
     * this dependency that satisfies the dependency's version constraints
     * <em>and</em> that we have local copies with satisfactory versions
     * of all libraries upon which this library depends, ad infinitum. If
     * we have no local copy of the library or the version of the local
     * copy is older than the version specified by the dependency or same
     * for any derived dependencies, this method returns false.
     *
     * <p> Note that this method caches its results once they are computed
     * in the expectation that it will be used in a "one shot" manner
     * (meaning the JVM that instantiates and uses this class is not long
     * lived). Once a dependency is resolved, it does not go back and
     * reresolve it, thus it could become out of date if the library (or
     * its dependencies) is updated between the time that this method is
     * called and the time that the calling JVM exits.
     */
    public boolean isSatisified ()
    {
        // TBD
        return false;
    }

    /**
     * Returns true if we have a local copy of the library and it is the
     * most recent release of that library. This method results in an HTTP
     * request being issued to download the latest library descriptor for
     * the library
     *
     * <p> Note that this method caches its results once they are computed
     * in the expectation that it will be used in a "one shot" manner
     * (meaning the JVM that instantiates and uses this class is not long
     * lived). Once a dependency is resolved, it does not go back and
     * reresolve it, thus it could become out of date if the library (or
     * its dependencies) is updated between the time that this method is
     * called and the time that the calling JVM exits.
     */
    public boolean isUpToDate ()
        throws IOException
    {
        // TBD
        return false;
    }

    /**
     * Downloads the library descriptor for the library referenced by this
     * dependency and returns it. Note that once the descriptor has been
     * downloaded once, it is cached for the remainder of the lifetime of
     * the JVM, hiding updates that take place between the time that this
     * method is called and the time that the calling JVM exists. This is
     * considered to be more desirable than making an HTTP request every
     * time this method is called.
     */
    public LibraryDescriptor getLibraryDescriptor ()
        throws IOException, FormatException
    {
        if (_remoteDescriptor != null) {
            return _remoteDescriptor;
        }

        // download the descriptor data from the publishing site
        URLConnection uconn = _descriptorURL.openConnection();
        _remoteDescriptor = LibraryDescriptorParser.parseLibraryDescriptor(
            uconn.getInputStream());
        return _remoteDescriptor;
    }

    /**
     * Downloads the library descriptor for the library referenced by this
     * dependency and reports the most recent version available of said
     * library.
     *
     * @see #getLibraryDescriptor
     */
    public Version getRemoteVersion ()
        throws IOException, FormatException
    {
        LibraryDescriptor descriptor = getLibraryDescriptor();
        return descriptor.getLatestRelease().getVersion();
    }

    /**
     * Returns a string representation of this instance.
     */
    public String toString ()
    {
        return "[name=" + _libraryName + ", version=" + _requiredVersion +
            ", url=" + _descriptorURL + "]";
    }

    /**
     * Constructs the file name of our local copy of the jar file.
     */
    protected String constructJarName ()
    {
        return _libraryName + LIBRARY_SUFFIX;
    }

    /**
     * Loads up the library stamp from our local copy of the library and
     * extracts the version information from it.
     */
    protected Version resolveLocalVersion ()
        throws IOException, FormatException
    {
        File jar = getLocalJar();
        // TBD
        return null;
    }

    /** The name of the library referenced by this dependency. */
    protected String _libraryName;

    /** The version required by this dependency. */
    protected Version _requiredVersion;

    /** The URL for the descriptor of the library referenced by this
     * dependency. */
    protected URL _descriptorURL;

    /** The path to our local library repository. */
    protected File _repository;

    /** The version of our local copy of the library or null if we have no
     * local copy. */
    protected Version _localVersion;

    /** A parsed library descriptor, obtained from our local copy of the
     * library. */
    protected LibraryDescriptor _localDescriptor;

    /** A parsed library descriptor, downloaded from the site that
     * publishes this library.  */
    protected LibraryDescriptor _remoteDescriptor;

    /** All libraries end with this suffix: <code>.jar</code> */
    protected static final String LIBRARY_SUFFIX = ".jar";
}
