//
// $Id$

package com.samskivert.util;

import java.util.ArrayList;

import com.samskivert.Log;

/**
 * A simple serial executor, similar to what can be done in java 1.5, only
 * using 1.4 and samskivert-style tasks. This is like RunQueue only slightly
 * more sophisticated. Bloat!
 */
public class SerialExecutor
{
    /**
     * A task to run in serial on the executor.
     */
    public static interface ExecutorTask
    {
        /**
         * This will be called when one task is on the queue to be processed
         * and another task is going to be added to the queue.
         *
         * @return true if the tasks were merged and the other task does not
         * need to be added to the queue.
         */
        public boolean merge (ExecutorTask other);

        /**
         * The portion of the task that will be executed on the executor's
         * thread.
         */
        public void executeTask ();

        /**
         * After the task has been executed, this will be called
         * on the ResultReceiver thread to post-process the results of the
         * task.
         */
        public void resultReceived ();
    }

    /**
     * Construct the SerialExecutor.
     */
    public SerialExecutor (Invoker.ResultReceiver receiver)
    {
        _receiver = receiver;
    }

    /**
     * Add a task to the executor, it is expected that this method is
     * called on the ResultReceiver thread.
     */
    public void addTask (ExecutorTask task)
    {
        for (int ii=0, nn=_queue.size(); ii < nn; ii++) {
            ExecutorTask taskOnQueue = (ExecutorTask) _queue.get(ii);
            if (taskOnQueue.merge(task)) {
                return;
            }
        }

        // otherwise, add it on
        _queue.add(task);

        // and perhaps start it going now
        if (!_executingNow) {
            checkNext();
        }
    }

    /**
     * Execute the next task, if applicable.
     */
    protected void checkNext ()
    {
        _executingNow = !_queue.isEmpty();
        if (_executingNow) {
            ExecutorTask task = (ExecutorTask) _queue.remove(0);
            ExecutorThread thread = new ExecutorThread(task);
            thread.start();
        }
    }

    /**
     * The basic processing unit of the Executor.
     */
    protected class ExecutorThread extends Thread
    {
        public ExecutorThread (ExecutorTask task)
        {
            _task = task;
        }

        // documentation inherited
        public void run ()
        {
            try {
                _task.executeTask();
            } catch (Throwable t) {
                Log.warning("ExecutorTask choked: " + t);
                Log.logStackTrace(t);
            }

            _receiver.postUnit(new Runnable() {
                public void run () {
                    try {
                        _task.resultReceived();
                    } catch (Throwable t) {
                        Log.warning("ExecutorTask choked: " + t);
                        Log.logStackTrace(t);
                    }
                    checkNext();
                }
            });
        }

        /** The task to execute. */
        protected ExecutorTask _task;
    }

    /** The receiver to which we post a unit to process results. */
    protected Invoker.ResultReceiver _receiver;

    /** True if there is a thread currently executing a task. */
    protected boolean _executingNow = false;

    /** The queue of tasks to execute. */
    protected ArrayList _queue = new ArrayList();
}
