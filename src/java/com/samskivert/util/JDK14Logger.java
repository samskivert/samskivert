//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2008 Michael Bayne
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

package com.samskivert.util;

import java.util.logging.Level;

/**
 * A factory for creating JDK14 logger implementations. Automatically configures a formatter to
 * display log messages on a single line unless the following system property is set:
 * <pre>com.samskivert.util.JDK14Logger.noFormatter=true</pre>
 */
public class JDK14Logger implements Logger.Factory
{
    // from interface Logger.Factory
    public void init ()
    {
        try {
            if (!Boolean.getBoolean("com.samskivert.util.JDK14Logger.noFormatter")) {
                OneLineLogFormatter.configureDefaultHandler();
            }
        } catch (SecurityException se) {
            // running in sandbox; no custom logging; no problem!
        }
    }

    // from interface Logger.Factory
    public Logger getLogger (String name)
    {
        return new Impl(java.util.logging.Logger.getLogger(name));
    }

    // from interface Logger.Factory
    public Logger getLogger (Class<?> clazz)
    {
        return getLogger(clazz.getName());
    }

    /**
     * Infers the caller of a Logger method from the current stack trace. This can be used by
     * wrappers to provide the correct calling class and method information to their underlying log
     * implementation.
     *
     * @return a two element array containing { class name, method name } or { null, null } if the
     * caller could not be inferred.
     */
    protected String[] inferCaller ()
    {
        String self = getClass().getName();

        // locate ourselves in the call stack
	StackTraceElement[] stack = (new Throwable()).getStackTrace();
        int ii = 0;
        for (; ii < stack.length; ii++) {
	    if (self.equals(stack[ii].getClassName())) {
		break;
	    }
	}
        System.err.println("Found self at " + ii);
        // now locate the first thing that's not us, that's the caller
	for (; ii < stack.length; ii++) {
	    String cname = stack[ii].getClassName();
	    if (!cname.equals(self)) {
                System.err.println("Found non-self at " + ii + " " + cname);
                return new String[] { cname, stack[ii].getMethodName() };
	    }
	}
        System.err.println("Failed to find non-self.");
        return new String[] { null, null };
    }

    protected static class Impl extends Logger
    {
        public Impl (java.util.logging.Logger impl)
        {
            _impl = impl;
        }

        @Override // from Logger
        public void debug (Object message, Object... args)
        {
            if (_impl.isLoggable(Level.FINE)) {
                _impl.log(Level.FINE, format(message, args), getException(message, args));
            }
        }

        @Override // from Logger
        public void info (Object message, Object... args)
        {
            if (_impl.isLoggable(Level.INFO)) {
                _impl.log(Level.INFO, format(message, args), getException(message, args));
            }
        }

        @Override // from Logger
        public void warning (Object message, Object... args)
        {
            if (_impl.isLoggable(Level.WARNING)) {
                _impl.log(Level.WARNING, format(message, args), getException(message, args));
            }
        }

        @Override // from Logger
        public void error (Object message, Object... args)
        {
            if (_impl.isLoggable(Level.SEVERE)) {
                _impl.log(Level.SEVERE, format(message, args), getException(message, args));
            }
        }

        protected java.util.logging.Logger _impl;
    }
}
