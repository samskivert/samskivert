//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.samskivert.io.StreamUtil;

/**
 * Reads the output from a process and copies any output to a supplied Logger.
 */
public class ProcessLogger
{
    /**
     * Starts threads that copy the output of the supplied process's stdout and stderr streams to
     * the supplied target logger. When the streams reach EOF, the threads will exit. The threads
     * will be set to daemon so that they do not prevent the VM from exiting.
     *
     * @param target the logger to which to copy the output.
     * @param name prepended to all output lines as either <code>name stderr:</code> or <code>name
     * stdout:</code>.
     * @param process the process whose output should be copied.
     */
    public static void copyOutput (Logger target, String name, Process process)
    {
        new StreamReader(target, name + " stdout", process.getInputStream()).start();
        new StreamReader(target, name + " stderr", process.getErrorStream()).start();
    }

    /**
     * Starts a thread to copy the output of the supplied process's stdout stream to the supplied
     * target logger (it assumes the process was created with a ProcessBuilder and the stdout and
     * stderr streams have been merged).
     *
     * @see #copyOutput
     */
    public static void copyMergedOutput (Logger target, String name, Process process)
    {
        new StreamReader(target, name + " output", process.getInputStream()).start();
    }

    protected static class StreamReader extends Thread
    {
        public StreamReader (Logger target, String name, InputStream input)
        {
            super("ProcessLogger_StreamReader");
            setDaemon(true);
            _target = target;
            _name = name;
            _reader = new BufferedReader(new InputStreamReader(input));
        }

        @Override public void run ()
        {
            String line;
            try {
                while ((line = _reader.readLine()) != null) {
                    _target.warning(_name + ": " + line);
                }
            } catch (Exception e) {
                _target.warning(_name + " failure: " + e);
            } finally {
                StreamUtil.close(_reader);
            }
        }

        protected Logger _target;
        protected String _name;
        protected BufferedReader _reader;
    }
}
