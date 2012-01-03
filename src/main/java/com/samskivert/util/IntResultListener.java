//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

/**
 * Provides access to an integer result, or the exception associated with
 * failure. In the trying course of implementing a method, one is often
 * left with no choice but to foist control off onto another thread or
 * generally postpone things beyond the lifetime of the current call. In
 * such circumstances, it is handy to have a general purpose mechanism for
 * leting the caller know when things are done and whether or not the
 * succeeded; this class serves that purpose.
 *
 * <p> The following contrived example will hopefully communicate its use
 * more clearly than the previous paragraph of flowery prose:
 *
 * <pre>
 * public void doSomeStuff (IntResultListener listener)
 * {
 *     Runnable run = new Runnable () {
 *         public void run () {
 *             try {
 *                 // do our thing
 *                 listener.requestCompleted(42);
 *             } catch (Exception e) {
 *                 listener.requestFailed(e);
 *             }
 *         }
 *     };
 *     new Thread(run).start();
 * }
 * </pre>
 *
 * This interface is a convenience-variation on the plain old {@link
 * ResultListener}, which communicates the result back in the form of an
 * {@link Object} rather than an integer.
 */
public interface IntResultListener
{
    /**
     * Called to communicate that the request succeeded and that the
     * result is available.
     */
    public void requestCompleted (int result);

    /**
     * Called to communicate that the request failed and to provide the
     * reason for failure.
     */
    public void requestFailed (Exception cause);
}
