//
// $Id$

package com.samskivert.util;

import java.awt.EventQueue;

/**
 * An interface for a service that queues up execution of Runnables.
 */
public interface RunQueue
{
    /**
     * A useful RunQueue that uses the AWT dispatch thread.
     */
    public static final RunQueue AWT = new RunQueue()
    {
        public void postRunnable (Runnable r)
        {
            EventQueue.invokeLater(r);
        }

        public boolean isDispatchThread ()
        {
            return EventQueue.isDispatchThread();
        }
    };

    /**
     * Post the specified Runnable to be run on the RunQueue.
     */
    public void postRunnable (Runnable r);

    /**
     * @return true if the calling thread is the RunQueue dispatch thread.
     */
    public boolean isDispatchThread ();
}
