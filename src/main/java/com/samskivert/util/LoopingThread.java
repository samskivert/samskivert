//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import static com.samskivert.util.UtilLog.log;

/**
 * The looping thread provides the basic functionality for a thread that does its processing in a
 * simple loop. This is characteristic of most event processing threads that pull an event off of a
 * queue and process it each time through the loop.
 */
public class LoopingThread extends Thread
{
    /**
     * Ye olde zero argument constructor.
     */
    public LoopingThread ()
    {
    }

    /**
     * Creates a looping thread with the specified name.
     */
    public LoopingThread (String name)
    {
        super(name);
    }

    /**
     * Requests that this thread shut itself down. The running flag will be cleared and if this
     * function is being called by an external thread, the derived-class-specific kick function
     * will be called to awake the thread from any slumber it might be in.
     */
    public void shutdown ()
    {
        _running = false;

        // only kick the thread if it's not requesting its own shutdown
        if (this != Thread.currentThread()) {
            kick();
        }
    }

    /**
     * Processes the loop. Derived classes should override {@link #iterate} to perform whatever
     * action needs to be performed inside the loop.
     */
    @Override
    public void run ()
    {
        try {
            _running = true;
            willStart();

            while (isRunning()) {
                try {
                    iterate();
                } catch (Exception e) {
                    handleIterateFailure(e);
                }
            }

        } catch (Throwable t) {
            log.warning("Looping thread terminated with exception.", "thread", this, t);

        } finally {
            // running needs to be false even if this exited due to a Throwable from iterate
            _running = false;
            didShutdown();
        }
    }

    /**
     * Indicates whether or not the thread should still be running. If a thread is calling this
     * within {@link #iterate}, it should exit quickly and cleanly if the function returns
     * false. It is automatically called as part of the {@link #iterate} loop, so normally a
     * derived-class won't have to call it.
     */
    public boolean isRunning ()
    {
        return _running;
    }

    /**
     * Called to wake the thread up from any blocking wait that it might be in. This function
     * should result in the thread exiting the {@link #iterate} function as soon as possible so
     * that the running flag can be checked and the thread can cleanly exit.
     */
    protected void kick ()
    {
        // nothing doing by default
    }

    /**
     * Called before the thread enters the processing loop. Any initialization that needs to be
     * performed prior to the {@link #iterate} loop can be done here.
     */
    protected void willStart ()
    {
    }

    /**
     * This is the main body of the loop. It will be called over and over again until the thread is
     * requested to exit via a call to {@link #shutdown}. At minimum, a derived class must override
     * this function and do something useful.
     */
    protected void iterate ()
    {
        throw new RuntimeException("Derived class must implement iterate().");
    }

    /**
     * If an exception propagates past {@link #iterate}, it will be caught and supplied to this
     * function, which may want to do something useful with it. Presently, the exception is logged
     * and the thread is allowed to terminate.
     */
    protected void handleIterateFailure (Exception e)
    {
        // log the exception
        log.warning("LoopingThread.iterate() uncaught exception.", e);

        // and shut the thread down
        shutdown();
    }

    /**
     * Called after the thread has been requested to exit. Any cleanup that should take place after
     * the {@link #iterate} loop has exited can be done here.
     */
    protected void didShutdown ()
    {
    }

    protected volatile boolean _running;
}
