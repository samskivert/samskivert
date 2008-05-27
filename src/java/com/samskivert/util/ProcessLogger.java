//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2007 Michael Bayne
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

import java.io.BufferedReader;
import java.io.IOException;
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
            setDaemon(true);
            _target = target;
            _name = name;
            _reader = new BufferedReader(new InputStreamReader(input));
        }

        public void run ()
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
