//
// $Id: DependsTask.java,v 1.1 2002/04/11 07:23:59 mdb Exp $

package com.samskivert.depends.tasks;

import java.io.File;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.samskivert.depends.Dependency;
import com.samskivert.depends.DependencyParser;

/**
 * A base class that handles functionality common to all dependency
 * related ant tasks.
 */
public abstract class DependsTask extends Task
{
    /**
     * Called by ant to set the 'deps' attribute.
     */
    public void setDeps (File deps)
    {
        _deps = deps;
    }

    /**
     * Called by ant to set the 'repository' attribute.
     */
    public void setRepository (File repository)
    {
        _repository = repository;
    }

    /**
     * Throws a build exception using the supplied error message if the
     * supplied value is null.
     */
    protected void ensureSet (Object value, String errmsg)
        throws BuildException
    {
        if (value == null) {
            throw new BuildException(errmsg);
        }
    }

    /**
     * Loads up a set of dependencies from the dependency file specified
     * via the 'deps' task attribute.
     */
    protected List loadDependencies ()
        throws BuildException
    {
        ensureSet(_deps, "Missing 'deps' attribute which should " +
                  "reference the dependency definition file.");
        try {
            return DependencyParser.parseDependencies(_deps);
        } catch (Exception e) {
            throw new BuildException("Error parsing dependency " +
                                     "definition file.", e);
        }
    }

    /**
     * Configures the supplied set of dependencies with the local library
     * repository specified via the 'repository' task attribute.
     */
    protected void resolveDependencies (List depends)
        throws BuildException
    {
        // make sure the repository directory was specified...
        ensureSet(_repository, "Missing 'repository' attribute which should " +
                  "reference the local library repository directory.");

        // ...exists...
        if (!_repository.exists()) {
            String errmsg = "Local library repository directory '" +
                _repository.getPath() + "' does not exist.";
            throw new BuildException(errmsg);
        }

        // ...and is actually a directory
        if (!_repository.isDirectory()) {
            String errmsg = "Specified local library repository '" +
                _repository.getPath() + "' is not a directory.";
            throw new BuildException(errmsg);
        }

        int dcount = depends.size();
        for (int i = 0; i < dcount; i++) {
            Dependency dep = (Dependency)depends.get(i);
            dep.setLocalRepository(_repository);
        }
    }

    /** The dependency definition file. */
    protected File _deps;

    /** The local library repository directory. */
    protected File _repository;
}
