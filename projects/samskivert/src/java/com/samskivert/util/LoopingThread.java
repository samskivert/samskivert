//
// $Id: LoopingThread.java,v 1.1 2001/05/29 03:29:44 mdb Exp $

package com.samskivert.util;

/**
 * The looping thread provides the basic functionality for a thread that
 * does its processing in a simple loop. This is characteristic of most
 * event processing threads that pull an event off of a queue and process
 * it each time through the loop.
 */
public class LoopingThread extends Thread
{
    /**
     * Requests that this thread shut itself down. The running flag will
     * be cleared and if this function is being called by an external
     * thread, the derived-class-specific kick function will be called to
     * awake the thread from any slumber it might be in.
     */
    public synchronized void shutdown ()
    {
        _running = false;

        // only kick the thread if it's not requesting it's own shutdown
        if (this != Thread.currentThread()) {
            kick();
        }
    }

    /**
     * Processes the loop. Derived classes should override
     * <code>iterate()</code> to perform whatever action needs to be
     * performed inside the loop.
     */
    public void run ()
    {
        willStart();
        while (isRunning()) {
            iterate();
        }
        didShutdown();
    }

    /**
     * Called to wake the thread up from any blocking wait that it might
     * be in. This function should result in the thread exiting the
     * iterate() function as soon as possible so that the running flag can
     * be checked and the thread can cleanly exit.
     */
    protected void kick ()
    {
        // nothing doing by default
    }

    /**
     * Called before the thread enters the processing loop. Any
     * initialization that needs to be performed prior to the
     * <code>iterate()</code> loop can be done here.
     */
    protected void willStart ()
    {
    }

    /**
     * This is the main body of the loop. It will be called over and over
     * again until the thread is requested to exit via a call to
     * <code>shutdown()</code>. At minimum, a derived class must override
     * this function and do something useful.
     */
    protected void iterate ()
    {
        throw new RuntimeException("Derived class must implement iterate().");
    }

    /**
     * Called after the thread has been requested to exit. Any cleanup
     * that should take place after the <code>iterate()</code> loop has
     * exited can be done here.
     */
    protected void didShutdown ()
    {
    }

    /**
     * Indicates whether or not the thread should still be running. If a
     * thread is calling this within <code>iterate()</code>, it should
     * exit quickly and cleanly if the function returns false. It is
     * automatically called as part of the iterate() loop, so normally a
     * derived-class won't have to call it.
     */
    protected synchronized boolean isRunning ()
    {
        return _running;
    }

    protected boolean _running = true;
}
