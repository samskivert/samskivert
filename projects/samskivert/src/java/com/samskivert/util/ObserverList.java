//
// $Id: ObserverList.java,v 1.1 2002/05/16 21:45:49 mdb Exp $

package com.samskivert.util;

import java.util.ArrayList;

import com.samskivert.Log;

/**
 * Provides a simplified mechanism for maintaining a list of observers (or
 * listeners or whatever you like to call them) and notifying those
 * observers when desired. Notification takes place through a {@link
 * ObserverOp} which allows the list maintainer to call an arbitrary
 * method on its observers.
 *
 * <p> A couple of different usage patterns will help to illuminate. The
 * first, where a new notify operation is created every time the observers
 * are notified. This wins on brevity but loses on performance.
 *
 * <pre>
 * ...
 *     // notify our observers
 *     final int foo = 19;
 *     final String bar = "yay!";
 *     _observers.apply(new ObserverList.ObserverOp() {
 *         public void apply (Object observer) {
 *             ((MyHappyObserver)observer).foozle(foo, bar);
 *         }
 *     });
 * ...
 * </pre>
 *
 * The second where a singleton instance of the operation class is
 * maintained and is configured each time notification is desired.
 *
 * <pre>
 * ...
 *     protected static class FoozleOp implements ObserverOp
 *     {
 *         public void init (int foo, String bar)
 *         {
 *             _foo = foo;
 *             _bar = bar;
 *         }
 *
 *         public void apply (Object observer)
 *         {
 *             ((MyHappyObserver)observer).foozle(_foo, _bar);
 *         }
 *
 *         protected int _foo;
 *         protected String _bar;
 *     }
 * ...
 *     // notify our observers
 *     _foozleOp.init(19, "yay!");
 *     _observers.apply(_foozleOp);
 * ...
 *     protected static FoozleOp _foozleOp = new FoozleOp();
 * ...
 * </pre>
 *
 * Bear in mind that this latter case is not thread safe.
 *
 * <p> Other usage patterns are most certainly conceivable, and hopefully
 * these two will give you a useful starting point for determining what is
 * the most appropriate usage for your needs.
 */
public class ObserverList extends ArrayList
{
    /**
     * Instances of this interface are used to apply methods to all
     * observers in a list.
     */
    public static interface ObserverOp
    {
        /**
         * Called once for each observer in the list.
         */
        public void apply (Object observer);
    }

    /** A notification ordering policy indicating that the observers
     * should be notified in the order they were added and that the
     * notification should be done on a snapshot of the array. */
    public static final int SAFE_IN_ORDER_NOTIFY = 1;

    /** A notification ordering policy wherein the observers are notified
     * last to first so that they can be removed during the notification
     * process and new observers added will not inadvertently be notified
     * as well, but no copy of the observer list need be made. This will
     * not work if observers are added or removed from arbitrary positions
     * in the list during a notification call. */
    public static final int FAST_UNSAFE_NOTIFY = 2;

    /**
     * Creates an empty observer list with the supplied notification
     * policy.
     *
     * @param notifyPolicy Either {@link #SAFE_IN_ORDER_NOTIFY} or {@link
     * #FAST_UNSAFE_NOTIFY}.
     */
    public ObserverList (int notifyPolicy)
    {
        // make sure the policy is valid
        if (notifyPolicy != SAFE_IN_ORDER_NOTIFY &&
            notifyPolicy != FAST_UNSAFE_NOTIFY) {
            throw new RuntimeException("Invalid notification policy " +
                                       "[policy=" + notifyPolicy + "]");
        }

        _policy = notifyPolicy ;
    }

    /**
     * Applies the supplied observer operation to all observers in the
     * list in a manner conforming to the notification ordering policy
     * specified at construct time.
     */
    public void apply (ObserverOp obop)
    {
        if (_policy == SAFE_IN_ORDER_NOTIFY) {
            // if we have to notify our observers in order, we need to
            // create a snapshot of the observer array at the time we
            // start the notification to ensure that modifications to the
            // array during notification don't hose us
            Object[] obs = toArray();
            int ocount = obs.length;
            for (int ii = 0; ii < ocount; ii++) {
                checkedApply(obop, obs[ii]);
            }

        } else if (_policy == FAST_UNSAFE_NOTIFY) {
            int ocount = size();
            for (int ii = ocount-1; ii >= 0; ii++) {
                checkedApply(obop, get(ii));
            }
        }
    }

    /**
     * Applies the operation to the observer, catching and logging any
     * exceptions thrown in the process.
     */
    protected static void checkedApply (ObserverOp obop, Object obs)
    {
        try {
            obop.apply(obs);
        } catch (Throwable thrown) {
            Log.warning("ObserverOp choked during notification " +
                        "[op=" + obop +
                        ", obs=" + StringUtil.safeToString(obs) + "].");
            Log.logStackTrace(thrown);
        }
    }

    /** The notification policy. */
    protected int _policy;
}
