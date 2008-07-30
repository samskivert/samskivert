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

/**
 * Provides logging services to this library and others which depend on this library in such a way
 * that they can be used in a larger project and easily be made to log to the logging framework in
 * use by that project.
 *
 * <p> The default implementation uses log4j if it is available and falls back to the Java logging
 * services if not. A specific (or custom) logging implementation can be configured like so:
 * <pre>-Dcom.samskivert.util.Logger=com.samskivert.util.Log4Logger</pre>
 *
 * <p> One additional enhancement is the use of varargs log methods to allow for some handy
 * automatic formatting. The methods take a log message and list of zero or more additional
 * parameters. Those parameters should be key/value pairs which will be logged like so:
 * <pre>message [key=value, key=value, key=value]</pre>
 *
 * <p> The final parameter can optionally be a Throwable, which will be supplied to the underlying
 * log system as an exception to accompany the log message.
 */
public abstract class Logger
{
    /**
     * Used to create logger instances. This is only public so that the log factory can be
     * configured programmatically via {@link Logger#setFactory}.
     */
    public static interface Factory
    {
        public void init ();
        public Logger getLogger (String name);
        public Logger getLogger (Class<?> clazz);
    }

    /**
     * Obtains a logger with the specified name.
     */
    public static Logger getLogger (String name)
    {
        return _factory.getLogger(name);
    }

    /**
     * Obtains a logger with a name equal to the supplied class's name.
     */
    public static Logger getLogger (Class<?> clazz)
    {
        return _factory.getLogger(clazz);
    }

    /**
     * Configures the logging factory be used by the logging system. Normally you would not do this
     * and would instead use the <code>com.samskivert.util.Logger</code> system property instead,
     * but in some situations, like in an applet, you cannot pass system properties and we can't
     * read them anyway because we're running in a security sandbox.
     */
    public static void setFactory (Factory factory)
    {
        _factory = factory;
        _factory.init();
    }

    /**
     * Logs a debug message.
     *
     * @param message the message to be logged.
     * @param args a list of key/value pairs and an optional final Throwable.
     */
    public abstract void debug (Object message, Object... args);

    /**
     * Logs an info message.
     *
     * @param message the message to be logged.
     * @param args a list of key/value pairs and an optional final Throwable.
     */
    public abstract void info (Object message, Object... args);

    /**
     * Logs a warning message.
     *
     * @param message the message to be logged.
     * @param args a list of key/value pairs and an optional final Throwable.
     */
    public abstract void warning (Object message, Object... args);

    /**
     * Logs an error message.
     *
     * @param message the message to be logged.
     * @param args a list of key/value pairs and an optional final Throwable.
     */
    public abstract void error (Object message, Object... args);

    /**
     * Format messages and arguments. For use by logger implementations. 
     */
    protected String format (Object message, Object[] args)
    {
        StringBuilder buf = new StringBuilder();
        buf.append(message);
        if (args != null && args.length > 1) {
            buf.append(" [");
            for (int ii = 0, nn = args.length/2; ii < nn; ii++) {
                if (ii > 0) {
                    buf.append(", ");
                }
                buf.append(args[2*ii]).append("=");
                try {
                    StringUtil.toString(buf, args[2*ii+1]);
                } catch (Throwable t) {
                    buf.append("<toString() failure: " + t + ">");
                }
            }
            buf.append("]");
        }
        return buf.toString();
    }

    /**
     * Extracts the exception from the message and arguments (if there is one). For use by logger
     * implementations.
     */
    protected Throwable getException (Object message, Object[] args)
    {
        if (message instanceof Throwable) {
            return (Throwable)message;
        } else if (args.length % 2 == 1 && args[args.length-1] instanceof Throwable) {
            return (Throwable)args[args.length-1];
        } else {
            return null;
        }
    }

    /**
     * Called at static initialization time. Selects and initializes our logging backend.
     */
    protected static void initLogger ()
    {
        // if a custom class was specified as a system property, use that
        Factory factory = createConfiguredFactory();

        // create and a log4j logger if the log4j configuration system property is set
        try {
            if (factory == null && System.getProperty("log4j.configuration") != null) {
                factory = (Factory)Class.forName("com.samskivert.util.Log4JLogger").newInstance();
            }
        } catch (SecurityException se) {
            // in a sandbox, no biggie
        } catch (Throwable t) {
            System.err.println("Unable to instantiate Log4JLogger: " + t);
        }

        // lastly, fall back to the Java logging system
        if (factory == null) {
            factory = new JDK14Logger();
        }

        // and finally configure our factory
        setFactory(factory);
    }

    /**
     * A helper function for {@link #initLogger}.
     */
    protected static Factory createConfiguredFactory ()
    {
        String implClass;
        try {
            implClass = System.getProperty("com.samskivert.util.Log");
        } catch (SecurityException se) {
            // in a sandbox
            return null;
        }
        if (!StringUtil.isBlank(implClass)) {
            try {
                return (Factory)Class.forName(implClass).newInstance();
            } catch (Throwable t) {
                System.err.println("Unable to instantiate logging implementation: " + implClass);
                t.printStackTrace(System.err);
            }
        }
        return null;
    }

    protected static Factory _factory;

    static {
        initLogger();
    }
}
