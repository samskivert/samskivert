//
// $Id: RunQueue.java,v 1.1 2003/08/13 01:55:30 ray Exp $

package com.samskivert.util;

import com.samskivert.Log;

/**
 * Used to serialize access to some resource.
 */
public class RunQueue extends LoopingThread
{
    public RunQueue ()
    {
        super("RunQueue");
    }
    
    /**
     * Post a runnable unit for running on the run queue.
     */
    public void post (Runnable r)
    {
        _queue.append(r);
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
        post(new Runnable() {
            public void run () {
                // nothing
            }
        });
    }

    /** The queue of things to run. */
    protected Queue _queue = new Queue();
}
