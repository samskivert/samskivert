//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import java.awt.EventQueue;

import java.util.concurrent.Executor;

/**
 * An interface for a service that queues up execution of Runnables.
 */
public interface RunQueue
{
    /** A useful RunQueue that uses the AWT dispatch thread. */
    public static final RunQueue AWT = new RunQueue() {
        public void postRunnable (Runnable r) {
            EventQueue.invokeLater(r);
        }
        public boolean isDispatchThread () {
            return EventQueue.isDispatchThread();
        }
        public boolean isRunning () {
            return true;
        }
    };

    /**
     * Wee helper class to adapt a RunQueue into an Executor.
     * While we transition from RunQueue to Executor.
     */
    public static class AsExecutor
        implements Executor
    {
        public AsExecutor (RunQueue toAdapt)
        {
            _runQueue = toAdapt;
        }

        // from Executor
        public void execute (Runnable command)
        {
            _runQueue.postRunnable(command);
        }

        protected RunQueue _runQueue;
    }

    /**
     * Post the specified Runnable to be run on the RunQueue.
     */
    void postRunnable (Runnable r);

    /**
     * @return true if the calling thread is the RunQueue dispatch thread.
     */
    boolean isDispatchThread ();

    /**
     * @return true if this run queue is still processing runnables, false if it has been shutdown.
     */
    boolean isRunning ();
}
