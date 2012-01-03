//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import java.io.UnsupportedEncodingException;
import java.util.logging.Handler;
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
            boolean reportedUTF8Missing = false;
            for (Handler handler : java.util.logging.Logger.getLogger("").getHandlers()) {
                try {
                    handler.setEncoding("UTF-8");
                } catch (UnsupportedEncodingException e) {
                    // JVMs are required to support UTF-8 so this shouldn't happen, but if it does,
                    // tell somebody that things are really fucked
                    if (!reportedUTF8Missing) {
                        reportedUTF8Missing = true;
                        System.err.println("Unable to find UTF-8 encoding. " +
                                           "This JVM ain't right: " + e.getMessage());
                    }
                }
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
        protected boolean shouldLog (int levIdx)
        {
            return _impl.isLoggable(LEVELS[levIdx]);
        }

        @Override // from Logger
        protected void doLog (int levIdx, String formatted, Throwable throwable)
        {
            _impl.log(LEVELS[levIdx], formatted, throwable);
        }

        protected java.util.logging.Logger _impl;
        protected static final Level[] LEVELS = {
            Level.FINE, Level.INFO, Level.WARNING, Level.SEVERE };
    }
}
