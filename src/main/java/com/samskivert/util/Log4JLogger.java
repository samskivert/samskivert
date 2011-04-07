//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2011 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

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
        protected boolean shouldLog (int levIdx)
        {
            return _impl.isEnabledFor(LEVELS[levIdx]);
        }

        @Override // from Logger
        protected void doLog (int levIdx, String formatted, Throwable throwable)
        {
            _impl.log(_self, LEVELS[levIdx], formatted, throwable);
        }

        protected final org.apache.log4j.Logger _impl;
        protected final String _self = getClass().getName();
        protected static final Level[] LEVELS = {
            Level.DEBUG, Level.INFO, Level.WARN, Level.ERROR };
    }
}
