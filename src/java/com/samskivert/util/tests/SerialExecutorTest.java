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

package com.samskivert.util.tests;

import junit.framework.Test;
import junit.framework.TestCase;

import com.samskivert.util.Queue;
import com.samskivert.util.RunQueue;
import com.samskivert.util.SerialExecutor;

/**
 * Tests the {@link SerialExecutor} class.
 */
public class SerialExecutorTest extends TestCase
    implements RunQueue
{
    public SerialExecutorTest ()
    {
        super(SerialExecutorTest.class.getName());
        _main = Thread.currentThread();
    }

    @Override
    public void runTest ()
    {
        SerialExecutor executor = new SerialExecutor(this);
        int added = 0;

        // _sleeps++, _exits++, _results++
        executor.addTask(new Sleeper(500L, false));
        added++;

        // _interrupts++, _exits++, _timeouts++
        executor.addTask(new Sleeper(1500L, false));
        added++;

        // _interrupts++, _timeouts++
        executor.addTask(new Sleeper(1500L, true));
        added++;

        // _sleeps++, _exits++, _results++
        executor.addTask(new Sleeper(500L, false));
        added++;

        // process the results posted on our run queue
        for (int ii = 0; ii < added; ii++) {
            Runnable r = _queue.get();
            r.run();
        }

        // give the executor threads a second to run to completion
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException ie) {
        }

        // make sure we got the expected number of sleeps, interrupts, etc.
        assertCount("_sleeps", _sleeps, 2);
        assertCount("_interrupts", _interrupts, 2);
        assertCount("_exits", _exits, 3);
        assertCount("_results", _results, 2);
        assertCount("_timeouts", _timeouts, 2);
        assertCount("_doubleints", _doubleints, 0);
    }

    protected void assertCount (String field, int value, int expected)
    {
        assertTrue(field + " != " + expected + " (" + value + ")",
                   value == expected);
    }

    public void postRunnable (Runnable r)
    {
        _queue.append(r);
    }

    public boolean isDispatchThread ()
    {
        return Thread.currentThread() == _main;
    }

    public boolean isRunning ()
    {
        return true;
    }

    public static Test suite ()
    {
        return new SerialExecutorTest();
    }

    public static void main (String[] args)
    {
        SerialExecutorTest test = new SerialExecutorTest();
        test.runTest();
    }

    protected class Sleeper implements SerialExecutor.ExecutorTask
    {
        public Sleeper (long sleepFor, boolean hang) {
            _sleepFor = sleepFor;
            _hang = hang;
        }

        public boolean merge (SerialExecutor.ExecutorTask task) {
            return false;
        }

        public long getTimeout () {
            return 1000L;
        }

        public void executeTask () {
            try {
                Thread.sleep(_sleepFor);
                _sleeps++;
            } catch (InterruptedException ie) {
                _interrupts++;
            }

            if (_hang) {
                try {
                    Thread.sleep(100000L);
                } catch (InterruptedException ie) {
                    _doubleints++;
                }
            }

            _exits++;
        }

        public void resultReceived () {
            _results++;
        }

        public void timedOut () {
            _timeouts++;
        }

        protected long _sleepFor;
        protected boolean _hang;
    }

    protected Thread _main;
    protected Queue<Runnable> _queue = new Queue<Runnable>();

    protected int _sleeps, _interrupts, _doubleints, _exits;
    protected int _results, _timeouts;
}
