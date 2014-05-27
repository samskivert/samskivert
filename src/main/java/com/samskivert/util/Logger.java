//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

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
     * Formats the given message and array of alternating key value pairs like so:
     *
     * <pre>message [key=value, key=value, key=value]</pre>
     */
    public static String format (Object message, Object... args)
    {
        return new LogBuilder(message, args).toString();
    }

    /**
     * Logs a debug message.
     *
     * @param message the message to be logged.
     * @param args a list of key/value pairs and an optional final Throwable.
     */
    public void debug (Object message, Object... args)
    {
        doLog(0, message, args);
    }

    /**
     * Logs an info message.
     *
     * @param message the message to be logged.
     * @param args a list of key/value pairs and an optional final Throwable.
     */
    public void info (Object message, Object... args)
    {
        doLog(1, message, args);
    }

    /**
     * Logs a warning message.
     *
     * @param message the message to be logged.
     * @param args a list of key/value pairs and an optional final Throwable.
     */
    public void warning (Object message, Object... args)
    {
        doLog(2, message, args);
    }

    /**
     * Logs an error message.
     *
     * @param message the message to be logged.
     * @param args a list of key/value pairs and an optional final Throwable.
     */
    public void error (Object message, Object... args)
    {
        doLog(3, message, args);
    }

    protected void doLog (int levIdx, Object message, Object[] args)
    {
        if (!shouldLog(levIdx)) {
            return;
        }
        Throwable err = null;
        int nn = args.length;
        if (message instanceof Throwable) {
            err = (Throwable)message;
        } else if (nn % 2 == 1 && (args[nn - 1] instanceof Throwable)) {
            err = (Throwable)args[--nn];
        }
        String msg = String.valueOf(message);
        if (nn > 0) {
            StringBuilder buf = new StringBuilder(msg);
            if (msg.length() > 0) {
                buf.append(' ');
            }
            buf.append('[');
            for (int ii = 0; ii < nn; ii += 2) {
                if (ii > 0) {
                    buf.append(',').append(' ');
                }
                buf.append(args[ii]).append('=');
                try {
                    buf.append(StringUtil.toString(args[ii + 1]));
                } catch (Throwable t) {
                    buf.append("<toString() failure: ").append(t).append(">");
                }
            }
            msg = buf.append(']').toString();
        }
        doLog(levIdx, msg, err);
    }

    /** Returns true if a log message at the specified level should be logged. */
    protected abstract boolean shouldLog (int levIdx);

    /** Performs the actual logging of a message at the specified level.
     * @param throwable an exception that accompanies this message or null. */
    protected abstract void doLog (int levIdx, String formatted, Throwable throwable);

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

        // create and a log4j2 logger if the log4j2 configuration system property is set
        try {
            if (factory == null && System.getProperty("log4j.configurationFile") != null) {
                factory = (Factory)Class.forName("com.samskivert.util.Log4J2Logger").newInstance();
            }
        } catch (SecurityException se) {
            // in a sandbox, no biggie
        } catch (Throwable t) {
            System.err.println("Unable to instantiate Log4J2Logger: " + t);
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
