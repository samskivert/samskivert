//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import static com.samskivert.util.UtilLog.log;

/**
 * A very basic implementation of RunQueue for general purpose use.
 */
public class BasicRunQueue extends LoopingThread
    implements RunQueue
{
    /**
     * Construct a BasicRunQueue with a default Queue implementation and name.
     */
    public BasicRunQueue ()
    {
        this("RunQueue");
    }

    /**
     * Construct a BasicRunQueue with a default Queue implementation and the given name.
     */
    public BasicRunQueue (String name)
    {
        super(name);
        _queue = new Queue<Runnable>();
    }

    // from interface RunQueue
    public void postRunnable (Runnable r)
    {
        _queue.append(r);
    }

    // from interface RunQueue
    public boolean isDispatchThread ()
    {
        return Thread.currentThread() == _dispatcher;
    }

    @Override // from LoopingThread
    protected void willStart ()
    {
        super.willStart();
        _dispatcher = Thread.currentThread();
    }

    @Override // from LoopingThread
    protected void iterate ()
    {
        Runnable r = _queue.get();
        try {
            r.run();

        } catch (Throwable t) {
            log.warning("Runnable posted to RunQueue barfed.", t);
        }
    }

    @Override // from LoopingThread
    protected void kick ()
    {
        postRunnable(new Runnable() {
            public void run () {
                // nothing
            }
        });
    }

    /** The queue of things to run. */
    protected Queue<Runnable> _queue;

    /** Our dispatcher thread (may == this or may be something else if we're being used directly
     * rather than in separate thread mode). */
    protected Thread _dispatcher;
}
