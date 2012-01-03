//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.velocity;

import javax.servlet.ServletContext;

import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogChute;

/**
 * Routes Velocity log messages to the servlet context.
 */
public class ServletContextLogger implements LogChute
{
    /**
     * Constructs a servlet context logger that will obtain its servlet
     * context reference via {@link
     * RuntimeServices#getApplicationAttribute} when initialized.
     */
    public ServletContextLogger ()
    {
    }

    /**
     * Constructs a servlet context logger with the supplied servlet
     * context.
     */
    public ServletContextLogger (ServletContext sctx)
    {
        _sctx = sctx;
    }

    // from interface LogChute
    public void init (RuntimeServices rsvc)
    {
        // if we weren't constructed with a servlet context, try to obtain
        // one via the application context
        if (_sctx == null) {
            // first look for the servlet context directly
            _sctx = (ServletContext)rsvc.getApplicationAttribute("ServletContext");
        }

        // if that didn't work, look for an application
        if (_sctx == null) {
            // first look for an application
            Application app = (Application)
                rsvc.getApplicationAttribute(Application.VELOCITY_ATTR_KEY);
            if (app != null) {
                _sctx = app.getServletContext();
            }
        }

        // if we still don't have one, complain
        if (_sctx == null) {
            rsvc.getLog().warn("ServletContextLogger: servlet context was not supplied. A user " +
                               "of the servlet context logger must call " +
                               "Velocity.setApplicationAttribute(\"ServletContext\", " +
                               "getServletContext()).");
        }
    }

    // from interface LogChute
    public void log (int level, String message)
    {
        if (isLevelEnabled(level)) {
            _sctx.log(message);
        }
    }

    // from interface LogChute
    public void log (int level, String message, Throwable t)
    {
        if (isLevelEnabled(level)) {
            _sctx.log(message, t);
        }
    }

    // from interface LogChute
    public boolean isLevelEnabled (int level)
    {
        return (level >= WARN_ID);
    }

    /** A reference to our servlet context. */
    protected ServletContext _sctx;
}
