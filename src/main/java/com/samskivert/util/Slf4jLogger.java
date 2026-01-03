//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

/**
 * A factory for creating Logback logger implementations.
 */
public class Slf4jLogger implements Logger.Factory
{
    // from interface Logger.Factory
    public void init ()
    {
    }

    // from interface Logger.Factory
    public Logger getLogger (String name)
    {
        return new Impl(LoggerFactory.getLogger(name));
    }

    // from interface Logger.Factory
    public Logger getLogger (Class<?> clazz)
    {
        return getLogger(clazz.getName());
    }

    protected static class Impl extends com.samskivert.util.Logger
    {
        public Impl (org.slf4j.Logger impl)
        {
            _impl = impl;
        }

        @Override // from Logger
        protected boolean shouldLog (int levIdx)
        {
            if (levIdx < 0 || levIdx >= LEVELS.length) {
                _impl.error("Unknown log level: " + levIdx + " for logger: " + _self);
                return false;
            }
            return _impl.isEnabledForLevel(LEVELS[levIdx]);
        }

        @Override // from Logger
        protected void doLog (int levIdx, String formatted, Throwable throwable)
        {
            switch (levIdx) {
                case LEVEL_DEBUG:
                    _impl.debug(formatted, throwable);
                    break;
                case LEVEL_INFO:
                    _impl.info(formatted, throwable);
                    break;
                case LEVEL_WARN:
                    _impl.warn(formatted, throwable);
                    break;
                case LEVEL_ERROR:
                    _impl.error(formatted, throwable);
                    break;
                default:
                    _impl.error("Unknown log level: " + levIdx + " for message: " + formatted, throwable);
                    break;
            }
        }

        protected final org.slf4j.Logger _impl;
        protected final String _self = getClass().getName();
        protected static final int LEVEL_DEBUG = 0;
        protected static final int LEVEL_INFO = 1;
        protected static final int LEVEL_WARN = 2;
        protected static final int LEVEL_ERROR = 3;
        protected static final Level[] LEVELS = {
            Level.DEBUG, Level.INFO, Level.WARN, Level.ERROR };
    }
}
