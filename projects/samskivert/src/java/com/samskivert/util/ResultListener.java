//
// $Id: ResultListener.java,v 1.1 2002/03/20 07:07:48 mdb Exp $

package com.samskivert.util;

/**
 * Provides access to a future result, or the exception associated with
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
 * public void doSomeStuff (ResultListener listener)
 * {
 *     Runnable run = new Runnable () {
 *         public void run () {
 *             try {
 *                 // do our thing
 *                 listener.requestCompleted("Elvis!");
 *             } catch (Exception e) {
 *                 listener.requestFailed(e);
 *             }
 *         }
 *     };
 *     new Thread(run).start();
 * }
 * </pre>
 *
 * @see IntResultListener
 */
public interface ResultListener
{
    /**
     * Called to communicate that the request succeeded and that the
     * result is available.
     */
    public void requestCompleted (Object result);

    /**
     * Called to communicate that the request failed and to provide the
     * reason for failure.
     */
    public void requestFailed (Exception cause);
}
