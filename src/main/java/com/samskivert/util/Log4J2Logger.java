//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

/**
 * A factory for creating log4j2 logger implementations.
 */
public class Log4J2Logger implements Logger.Factory
{
    // from interface Logger.Factory
    public void init ()
    {
    }

    // from interface Logger.Factory
    public Logger getLogger (String name)
    {
        return new Impl(LogManager.getLogger(name));
    }

    // from interface Logger.Factory
    public Logger getLogger (Class<?> clazz)
    {
        return new Impl(LogManager.getLogger(clazz));
    }

    protected static class Impl extends Logger
    {
        public Impl (org.apache.logging.log4j.Logger impl)
        {
            _impl = impl;
        }

        @Override // from Logger
        protected boolean shouldLog (int levIdx)
        {
            return _impl.isEnabled(LEVELS[levIdx]);
        }

        @Override // from Logger
        protected void doLog (int levIdx, String formatted, Throwable throwable)
        {
            _impl.log(LEVELS[levIdx], formatted, throwable);
        }

        protected final org.apache.logging.log4j.Logger _impl;
        protected static final Level[] LEVELS = {
            Level.DEBUG, Level.INFO, Level.WARN, Level.ERROR };
    }
}
