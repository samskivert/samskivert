//
// $Id: TaskMaster.java,v 1.1 2000/12/06 03:25:19 mdb Exp $

package com.samskivert.swing.util;

import java.util.Hashtable;
import com.samskivert.util.Log;

/**
 * The task master provides the ability for swing applications to invoke
 * tasks on another thread and to conveniently receive the results of
 * those tasks back on the swing event dispatch thread where the swing
 * application can safely manipulate its user interface in response to the
 * results of the task.
 *
 * <p/> Each task is run in it's own thread. Tasks are assumed to be
 * infrequently run and expensive, so the overhead of creating a new
 * thread to run each task is considered acceptable. If the need arises,
 * the task master can be extended to support more sophisticated thread
 * pooling but we'll cross that bridge when we come to it.
 */
public class TaskMaster
{
    /** The log object to be used by the task master and services. */
    public static Log log = new Log("com.samskivert.swing.util.task");

    /**
     * Instructs the task master to run the supplied task. The task is
     * given the supplied name and can be referenced by that name in
     * subsequent dealings with the task master. The supplied observer (if
     * non-null) will be notified when the task has completed.
     */
    public static void invokeTask (String name, Task task,
				   TaskObserver observer)
    {
	// create a task runner and stick it in our task table
	TaskRunner runner = new TaskRunner(name, task, observer);
	_tasks.put(name, runner);
	// then start the runner up
	runner.start();
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
	public TaskRunner (String name, Task task, TaskObserver observer)
	{
	    _name = name;
	    _task = task;
	    _observer = observer;
	}

	public void run ()
	{
	    try {
		Object result = _task.invoke();
		if (_observer != null) {
		    try {
			_observer.taskCompleted(_name, result);
		    } catch (Throwable t) {
			log.warning("Observer choked in " +
				    "taskCompleted(): " + t);
		    }
		}

	    } catch (Throwable t) {
		if (_observer != null) {
		    try {
			_observer.taskFailed(_name, t);
		    } catch (Throwable ot) {
			log.warning("Observer choked in taskFailed(): " + ot);
		    }
		}
	    }

	    TaskMaster.removeTask(_name);
	}

	public void abort ()
	{
	    log.warning("abort() not currently supported.");
	}

	protected String _name;
	protected Task _task;
	protected TaskObserver _observer;
    }

    protected static Hashtable _tasks = new Hashtable();
}
