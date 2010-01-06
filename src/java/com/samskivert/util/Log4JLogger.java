//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2010 Michael Bayne, et al.
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

import org.apache.log4j.Level;

/**
 * A factory for creating log4j logger implementations.
 */
public class Log4JLogger implements Logger.Factory
{
    // from interface Logger.Factory
    public void init ()
    {
    }

    // from interface Logger.Factory
    public Logger getLogger (String name)
    {
        return new Impl(org.apache.log4j.Logger.getLogger(name));
    }

    // from interface Logger.Factory
    public Logger getLogger (Class<?> clazz)
    {
        return getLogger(clazz.getName());
    }

    protected static class Impl extends Logger
    {
        public Impl (org.apache.log4j.Logger impl)
        {
            _impl = impl;
        }

        @Override // from Logger
        public void debug (Object message, Object... args)
        {
            if (_impl.isEnabledFor(Level.DEBUG)) {
                _impl.log(_self, Level.DEBUG, format(message, args), getException(message, args));
            }
        }

        @Override // from Logger
        public void info (Object message, Object... args)
        {
            if (_impl.isEnabledFor(Level.INFO)) {
                _impl.log(_self, Level.INFO, format(message, args), getException(message, args));
            }
        }

        @Override // from Logger
        public void warning (Object message, Object... args)
        {
            if (_impl.isEnabledFor(Level.WARN)) {
                _impl.log(_self, Level.WARN, format(message, args), getException(message, args));
            }
        }

        @Override // from Logger
        public void error (Object message, Object... args)
        {
            if (_impl.isEnabledFor(Level.ERROR)) {
                _impl.log(_self, Level.ERROR, format(message, args), getException(message, args));
            }
        }

        protected final org.apache.log4j.Logger _impl;
        protected final String _self = getClass().getName();
    }
}
