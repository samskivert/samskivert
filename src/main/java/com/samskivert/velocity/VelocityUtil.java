//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.velocity;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogChute;

import static com.samskivert.servlet.Log.log;

/**
 * Handy Velocity-related routines.
 */
public class VelocityUtil
{
    /**
     * Creates a {@link VelocityEngine} that is configured to load templates
     * from the classpath and log using the samskivert logging classes and not
     * complain about a bunch of pointless stuff that it generally complains
     * about.
     *
     * @throws Exception if a problem occurs initializing Velocity. We'd throw
     * something less generic, but that's what {@link VelocityEngine#init}
     * throws.
     */
    public static VelocityEngine createEngine ()
        throws Exception
    {
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(VelocityEngine.VM_LIBRARY, "");
        ve.setProperty(VelocityEngine.RESOURCE_LOADER, "classpath");
        ve.setProperty("classpath." + VelocityEngine.RESOURCE_LOADER + ".class",
                       ClasspathResourceLoader.class.getName());
        ve.setProperty(VelocityEngine.RUNTIME_LOG_LOGSYSTEM, _logger);
        ve.init();
        return ve;
    }

    /**
     * Creates a {@link VelocityEngine} that is configured to load templates
     * from the specified path and log using the samskivert logging classes and
     * not complain about a bunch of pointless stuff that it generally
     * complains about.
     *
     * @throws Exception if a problem occurs initializing Velocity. We'd throw
     * something less generic, but that's what {@link VelocityEngine#init}
     * throws.
     */
    public static VelocityEngine createEngine (String templatePath)
        throws Exception
    {
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(VelocityEngine.VM_LIBRARY, "");
        ve.setProperty("file.resource.loader.path", templatePath);
        ve.setProperty(VelocityEngine.RUNTIME_LOG_LOGSYSTEM, _logger);
        ve.init();
        return ve;
    }

    /** Handles logging for Velocity. */
    protected static LogChute _logger = new LogChute() {
        public void init (RuntimeServices rs) throws Exception {
            // nothing doing
        }
        public void log (int level, String message) {
            if (level == ERROR_ID || level == WARN_ID) {
                log.warning(message);
            }
        }
        public void log (int level, String message, Throwable t) {
            if (level == ERROR_ID || level == WARN_ID) {
                log.warning(message, t);
            }
        }
        public boolean isLevelEnabled (int level) {
            return (level == WARN_ID || level == ERROR_ID);
        }
    };
}
