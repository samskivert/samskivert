//
// $Id$

package com.samskivert.util;

/**
 * An interface for a service that queues up execution of Runnables.
 */
public interface RunQueue
{
    /**
     * Post the specified Runnable to be run on the RunQueue.
     */
    public void postRunnable (Runnable r);

    /**
     * @return true if the calling thread is the RunQueue dispatch thread.
     */
    public boolean isDispatchThread ();
}
