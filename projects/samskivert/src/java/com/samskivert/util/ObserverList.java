//
// $Id: ObserverList.java,v 1.4 2002/12/12 23:55:56 shaper Exp $

package com.samskivert.util;

import java.util.ArrayList;
import java.util.Collection;

import com.samskivert.Log;
import com.samskivert.util.StringUtil;

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
 *         public boolean apply (Object observer) {
 *             ((MyHappyObserver)observer).foozle(foo, bar);
 *             return true;
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
 *         public boolean apply (Object observer)
 *         {
 *             ((MyHappyObserver)observer).foozle(_foo, _bar);
 *             return true;
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
         *
         * @return true if the observer should remain in the list, false
         * if it should be removed in response to this application.
         */
        public boolean apply (Object observer);
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
     * policy and that only allows observers to observe the list once.
     *
     * @param notifyPolicy Either {@link #SAFE_IN_ORDER_NOTIFY} or {@link
     * #FAST_UNSAFE_NOTIFY}.
     */
    public ObserverList (int notifyPolicy)
    {
        this(notifyPolicy, false);
    }

    /**
     * Creates an empty observer list with the supplied notification and
     * duplicate observer policy.
     *
     * @param notifyPolicy Either {@link #SAFE_IN_ORDER_NOTIFY} or {@link
     * #FAST_UNSAFE_NOTIFY}.
     * @param allowDups whether to allow observers to be added to the list
     * more than once.
     */
    public ObserverList (int notifyPolicy, boolean allowDups)
    {
        // make sure the policy is valid
        if (notifyPolicy != SAFE_IN_ORDER_NOTIFY &&
            notifyPolicy != FAST_UNSAFE_NOTIFY) {
            throw new RuntimeException("Invalid notification policy " +
                                       "[policy=" + notifyPolicy + "]");
        }

        _policy = notifyPolicy ;
        _allowDups = allowDups;
    }

    // documentation inherited
    public void add (int index, Object element)
    {
        throw new UnsupportedOperationException(UNSUPPORTED_ADD_MESSAGE);
    }

    // documentation inherited
    public boolean add (Object o)
    {
        // make sure we're not violating the list constraints
        if (!_allowDups && contains(o)) {
            throw new RuntimeException(
                "Observer attempted to observe list it's already observing! " +
                "[obs=" + o + "].");
        }

        // go ahead and add the observer
        return super.add(o);
    }

    // documentation inherited
    public boolean addAll (Collection c)
    {
        throw new UnsupportedOperationException(UNSUPPORTED_ADD_MESSAGE);
    }

    // documentation inherited
    public boolean addAll (int index, Collection c)
    {
        throw new UnsupportedOperationException(UNSUPPORTED_ADD_MESSAGE);
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
                if (!checkedApply(obop, obs[ii])) {
                    remove(obs[ii]);
                }
            }

        } else if (_policy == FAST_UNSAFE_NOTIFY) {
            int ocount = size();
            for (int ii = ocount-1; ii >= 0; ii--) {
                if (!checkedApply(obop, get(ii))) {
                    remove(ii);
                }
            }
        }
    }

    /**
     * Applies the operation to the observer, catching and logging any
     * exceptions thrown in the process.
     */
    protected static boolean checkedApply (ObserverOp obop, Object obs)
    {
        try {
            return obop.apply(obs);
        } catch (Throwable thrown) {
            Log.warning("ObserverOp choked during notification " +
                        "[op=" + obop +
                        ", obs=" + StringUtil.safeToString(obs) + "].");
            Log.logStackTrace(thrown);

            // if they booched it, definitely don't remove them
            return true;
        }
    }

    /** The notification policy. */
    protected int _policy;

    /** Whether to allow observers to observe more than once simultaneously. */
    protected boolean _allowDups;

    /** Message reported for unsupported <code>add()</code> variants. */
    protected static final String UNSUPPORTED_ADD_MESSAGE =
        "Observers may only be added via ObserverList.add(Object).";
}
