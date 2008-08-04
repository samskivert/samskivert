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

import java.util.ArrayList;

import static com.samskivert.Log.log;

/**
 * Executes tasks serially, but each one on a separate thread. If a task times
 * out, the executor will attempt to interrupt the thread and abort the task,
 * but will abandon the thread in any case after the abort attempt so that
 * subsequent tasks can be processed. The threads created are daemon threads so
 * that they will not block the eventual termination of the virtual machine.
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
         * Returns the number of milliseconds after which this task should be
         * considered a lost cause. If the task times out, {@link #timedOut}
         * will be called instead of {@link #resultReceived}.
         */
        public long getTimeout ();

        /**
         * The portion of the task that will be executed on the executor's
         * thread. This method should handle {@link InterruptedException} as
         * meaning that the task should be aborted.
         */
        public void executeTask ();

        /**
         * After the task has been executed, this will be called
         * on the ResultReceiver thread to post-process the results of the
         * task.
         */
        public void resultReceived ();

        /**
         * This method is called instead of {@link #resultReceived} if the task
         * does not complete within the requisite time.
         */
        public void timedOut ();
    }

    /**
     * Construct the SerialExecutor.
     */
    public SerialExecutor (RunQueue receiver)
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
            ExecutorTask taskOnQueue = _queue.get(ii);
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
     * Returns the number of ExecutorTasks that are currently waiting on
     * the queue.
     */
    public int getQueueSize ()
    {
        return _queue.size();
    }

    /**
     * Execute the next task, if applicable.
     */
    protected void checkNext ()
    {
        _executingNow = !_queue.isEmpty();
        if (_executingNow) {
            // start up a thread to execute the task in question
            ExecutorTask task = _queue.remove(0);
            final ExecutorThread thread = new ExecutorThread(task);
            thread.start();

            // start up a timer that will abort this thread after the specified
            // timeout
            new Interval() {
                public void expired () {
                    // this will NOOP if the task has already completed
                    thread.abort();
                }
            }.schedule(task.getTimeout());
        }
    }

    /**
     * The basic processing unit of the Executor.
     */
    protected class ExecutorThread extends Thread
    {
        public ExecutorThread (ExecutorTask task)
        {
            setDaemon(true);
            _task = task;
        }

        public synchronized void abort ()
        {
            if (_task != null) {
                final ExecutorTask task = _task;
                // clear out the task reference which will let the running
                // thread know to stop if/when executeTask() returns
                _task = null;

                // let the task know that it timed out
                _receiver.postRunnable(new Runnable() {
                    public void run () {
                        try {
                            task.timedOut();
                        } catch (Throwable t) {
                            log.warning("Unit failed", t);
                        }
                        checkNext();
                    }
                });

                // finally interrupt the thread in hopes of waking it up from
                // it's hangitude
                interrupt();
            }
        }

        public void run ()
        {
            final ExecutorTask task = _task;
            try {
                _task.executeTask();
                synchronized (this) {
                    if (_task == null) {
                        // we were aborted, abandon ship
                        System.err.println("Aborted!");
                        return;
                    }
                    _task = null;
                }
            } catch (Throwable t) {
                log.warning("Unit failed", t);
            }

            _receiver.postRunnable(new Runnable() {
                public void run () {
                    try {
                        task.resultReceived();
                    } catch (Throwable t) {
                        log.warning("Unit failed", t);
                    }
                    checkNext();
                }
            });
        }

        protected ExecutorTask _task;
    }

    /** The receiver to which we post a unit to process results. */
    protected RunQueue _receiver;

    /** True if there is a thread currently executing a task. */
    protected boolean _executingNow = false;

    /** The queue of tasks to execute. */
    protected ArrayList<ExecutorTask> _queue = new ArrayList<ExecutorTask>();
}
