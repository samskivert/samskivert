//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.swing.util;

import java.lang.reflect.Method;
import java.util.Hashtable;
import javax.swing.SwingUtilities;

import static com.samskivert.swing.Log.log;

/**
 * The task master provides the ability for swing applications to invoke
 * tasks on another thread and to conveniently receive the results of
 * those tasks back on the swing event dispatch thread where the swing
 * application can safely manipulate its user interface in response to the
 * results of the task.
 *
 * <p> Each task is run in it's own thread. Tasks are assumed to be
 * infrequently run and expensive, so the overhead of creating a new
 * thread to run each task is considered acceptable. If the need arises,
 * the task master can be extended to support more sophisticated thread
 * pooling but we'll cross that bridge when we come to it.
 */
public class TaskMaster
{
    /**
     * Instructs the task master to run the supplied task. The task is
     * given the supplied name and can be referenced by that name in
     * subsequent dealings with the task master. The supplied observer (if
     * non-null) will be notified when the task has completed.
     */
    public static void invokeTask (String name, Task task, TaskObserver observer)
    {
        // create a task runner and stick it in our task table
        TaskRunner runner = new TaskRunner(name, task, observer);
        _tasks.put(name, runner);
        // then start the runner up
        runner.start();
    }

    /**
     * Invokes the method with the specified name on the supplied source
     * object as if it were a task. The observer is notified when the
     * method has completed and returned its result or if it fails. The
     * named method must have a signature the same as the
     * <code>invoke</code> method of the <code>Task</code> interface.
     * Aborting tasks run in this way is not supported.
     */
    public static void invokeMethodTask (String name, Object source, TaskObserver observer)
    {
        // create a method task instance to invoke the named method and
        // then run that through the normal task invocation mechanism
        invokeTask(name, new MethodTask(name, source), observer);
    }

    /**
     * Called by the task runner to remove itself from the task table when
     * the task has completed or been aborted.
     */
    protected static void removeTask (String name)
    {
        _tasks.remove(name);
    }

    protected static class TaskRunner extends Thread
    {
        public TaskRunner (String name, Task task, TaskObserver observer) {
            _name = name;
            _task = task;
            _observer = observer;
            _mode = INVOKE;
        }

        /**
         * Invokes the task and then reports completion or failure later on the swing event
         * dispatcher thread. We need to ensure that _mode and _result are visible to the various
         * threads that invoke this runnable so run() is synchronized. Oh how I love Chapter 17.
         */
        @Override
        public synchronized void run () {
            switch (_mode) {
            default:
            case INVOKE:
                // invoke the task and catch any errors
                try {
                    _result = _task.invoke();
                    _mode = COMPLETED;
                } catch (Throwable t) {
                    _result = t;
                    _mode = FAILED;
                } finally {
                    TaskMaster.removeTask(_name);
                }

                // queue ourselves up to run again on the swing event
                // dispatch thread if we have an observer
                if (_observer != null) {
                    SwingUtilities.invokeLater(this);
                }
                break;

            case COMPLETED:
                try {
                    _observer.taskCompleted(_name, _result);
                } catch (Throwable t) {
                    log.warning("Observer choked in taskCompleted()", t);
                }
                break;

            case FAILED:
                try {
                    _observer.taskFailed(_name, (Throwable)_result);
                } catch (Throwable ot) {
                    log.warning("Observer choked in taskFailed()", ot);
                }
                break;
            }
        }

        public void abort () {
            log.warning("abort() not currently supported.");
        }

        protected String _name;
        protected Task _task;
        protected TaskObserver _observer;

        protected int _mode;
        protected Object _result;

        protected static final int INVOKE = 0;
        protected static final int COMPLETED = 1;
        protected static final int FAILED = 2;
    }

    protected static class MethodTask implements Task
    {
        public MethodTask (String name, Object source) {
            _name = name;
            _source = source;
        }

        public Object invoke () throws Exception {
            // look up the named method on the source object and invoke it
            Method meth = _source.getClass().getMethod(_name, (Class<?>[]) null);
            meth.setAccessible(true);
            return meth.invoke(_source, (Object[]) null);
        }

        public boolean abort () {
            // aborting not supported
            return false;
        }

        protected String _name;
        protected Object _source;
    }

    protected static Hashtable<String, TaskRunner> _tasks = new Hashtable<String, TaskRunner>();
}
