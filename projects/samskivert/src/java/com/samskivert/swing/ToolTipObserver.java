//
// $Id: ToolTipObserver.java,v 1.1 2001/08/22 08:15:39 shaper Exp $

package com.samskivert.swing;

/**
 * An interface to be implemented by container objects that would like
 * to be notified by the {@link ToolTipManager} when they should
 * display tool tips associated with the objects that they manage.
 */
public interface ToolTipObserver
{
    /**
     * Called when the tool tip associated with the given target
     * should be displayed.
     *
     * @param target the object whose tool tip should be shown.
     */
    public void showToolTip (Object target);

    /**
     * Called when any visible tool tip should be hidden and so the
     * observer is likely to want to repaint itself without the tip.
     */
    public void hideToolTip ();
}
