//
// $Id: ServletContextLogger.java,v 1.1 2001/11/20 21:08:35 mdb Exp $
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
    // documentation inherited
    public void init (RuntimeServices rsvc)
    {
        // the web framework was kind enough to slip this into the runtime
        // instance when it started up
        _sctx = (ServletContext)rsvc.getApplicationContext();
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
