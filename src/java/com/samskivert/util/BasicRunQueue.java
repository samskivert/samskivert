//
// $Id: RunQueue.java,v 1.1 2003/08/13 01:55:30 ray Exp $

package com.samskivert.util;

import com.samskivert.Log;

/**
 * A very basic implementation of RunQueue for general purpose use.
 */
public class BasicRunQueue extends LoopingThread
    implements RunQueue
{
    /**
     * Construct a BasicRunQueue with a default Queue implementation.
     */
    public BasicRunQueue ()
    {
        super("RunQueue");
        _queue = new Queue();
    }
    
    // documentation inherited from interface
    public void postRunnable (Runnable r)
    {
        _queue.append(r);
    }

    // documentation inherited from interface
    public boolean isDispatchThread ()
    {
        return Thread.currentThread() == this;
    }

    // documentation inherited
    protected void iterate ()
    {
        Runnable r = (Runnable) _queue.get();
        try {
            r.run();

        } catch (Throwable t) {
            Log.warning("Runnable posted to RunQueue barfed.");
            Log.logStackTrace(t);
        }
    }

    // documentation inherited
    protected void kick ()
    {
        postRunnable(new Runnable() {
            public void run () {
                // nothing
            }
        });
    }

    /** The queue of things to run. */
    protected Queue _queue;
}
