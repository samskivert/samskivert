//
// $Id$

package com.samskivert.velocity;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogSystem;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

/**
 * Handy Velocity-related routines.
 */
public class VelocityUtil
{
    /**
     * Creates a {@link VelocityEngine} that is configured to load
     * templates from the classpath and log using the samskivert logging
     * classes and not complain about a bunch of pointless stuff that it
     * generally complains about.
     *
     * @throws Exception if a problem occurs initializing Velocity. We'd
     * throw something less generic, but that's what
     * {@link VelocityEngine#init} throws.
     */
    public static VelocityEngine createEngine ()
        throws Exception
    {
        // initialize velocity which we'll use for templating
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(VelocityEngine.VM_LIBRARY, "");
        ve.setProperty(VelocityEngine.RESOURCE_LOADER, "classpath");
        ve.setProperty("classpath." + VelocityEngine.RESOURCE_LOADER + ".class",
                       ClasspathResourceLoader.class.getName());
        ve.setProperty(VelocityEngine.RUNTIME_LOG_LOGSYSTEM, _logger);
        ve.init();
        return ve;
    }

    /** Handles logging for Velocity. */
    protected static LogSystem _logger = new LogSystem() {
        public void init (RuntimeServices rs) {
            // nothing doing
        }
        public void logVelocityMessage (int level, String message) {
            // skip anything other than warnings or errors
            if (level == WARN_ID || level == ERROR_ID) {
                System.err.println(message);
            }
        }
    };
}
