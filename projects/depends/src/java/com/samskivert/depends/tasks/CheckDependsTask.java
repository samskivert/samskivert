//
// $Id: CheckDependsTask.java,v 1.1 2002/04/11 07:23:59 mdb Exp $

package com.samskivert.depends.tasks;

import java.util.List;

import org.apache.tools.ant.BuildException;

import com.samskivert.depends.Dependency;

/**
 * An ant task for checking that a set of dependencies are satisfied by
 * the jar files that currently exist in our local library repository.
 */
public class CheckDependsTask extends DependsTask
{
    /**
     * Performs the actual work of the task.
     */
    public void execute ()
        throws BuildException
    {
        // load and resolve our dependencies
        List depends = loadDependencies();
        resolveDependencies(depends);

        int dcount = depends.size();
        for (int i = 0; i < dcount; i++) {
            Dependency dep = (Dependency)depends.get(i);
            try {
                if (!dep.isUpToDate()) {
                    System.out.println("Library '" + dep.getLibraryName() +
                                       "' has newer version " +
                                       "[have=" + dep.getLocalVersion() +
                                       ", latest=" + dep.getRemoteVersion() +
                                       "].");
                }

            } catch (Exception e) {
                System.err.println("Error checking dependency " +
                                   "[dep=" + dep + ", error=" + e + "].");
            }
        }
    }
}
