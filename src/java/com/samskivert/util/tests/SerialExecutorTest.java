//
// $Id$

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
