//
// $Id: ServletContextLogger.java,v 1.2 2001/11/20 21:13:47 mdb Exp $
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001 Michael Bayne
// 
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.samskivert.velocity;

import javax.servlet.ServletContext;

import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogSystem;

/**
 * Routes Velocity log messages to the servlet context.
 */
public class ServletContextLogger implements LogSystem
{
    /**
     * Constructs a servlet context logger that will obtain its servlet
     * context reference via {@link RuntimeServices#getApplicationContext}
     * when initialized.
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

    // documentation inherited
    public void init (RuntimeServices rsvc)
    {
        // if we weren't constructed with a servlet context, try to obtain
        // one via the application context
        if (_sctx == null) {
            _sctx = (ServletContext)rsvc.getApplicationContext();
        }

        // if we still don't have one, complain
        if (_sctx == null) {
            rsvc.warn("ServletContextLogger: servlet context was not " +
                      "supplied as application context. A user of the " +
                      "servlet context logger must call " +
                      "Velocity.setApplicationContext(getServletContext()).");
        }
    }

    // documentation inherited
    public void logVelocityMessage (int level, String message)
    {
        // log everything for now
        _sctx.log(message);
    }

    /** A reference to our servlet context. */
    protected ServletContext _sctx;
}
