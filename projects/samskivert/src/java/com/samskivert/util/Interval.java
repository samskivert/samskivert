//
// $Id: Interval.java,v 1.1 2001/03/02 00:47:10 mdb Exp $

package com.samskivert.util;

import java.util.*;

/**
 * An interface for doing operations after some delay. Normally,
 * <code>intervalExpired()</code> should not do anything that will take
 * very long. If you want to use the thread to do some serious stuff, look
 * at <code>incrementHelperThreads</code> and
 * <code>setMaxHelperThreads</code> in <code>IntervalManager</code>.
 *
 * @see go2net.util.IntervalManager
 * @see go2net.util.IntervalManager#incrementHelperThreads
 * @see go2net.util.IntervalManager#setMaxHelperThreads
 */
public interface Interval
{
    public void intervalExpired (int id, Object arg);
}
