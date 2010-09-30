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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

    protected static class Impl extends Logger<Level>
    {
        public Impl (org.apache.log4j.Logger impl)
        {
            _impl = impl;
        }

        @Override // from Logger
        protected List<Level> getLevels ()
        {
            return LEVELS;
        }

        @Override // from Logger
        protected boolean shouldLog (Level level)
        {
            return _impl.isEnabledFor(level);
        }

        @Override // from Logger
        protected void doLog (Level level, String formatted, Throwable throwable)
        {
            _impl.log(_self, level, formatted, throwable);
        }

        protected final org.apache.log4j.Logger _impl;
        protected final String _self = getClass().getName();
        protected static final List<Level> LEVELS = Collections.unmodifiableList(Arrays.asList(
            Level.DEBUG, Level.INFO, Level.WARN, Level.ERROR));
    }
}
