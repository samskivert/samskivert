//
// $Id: ToolTipObserver.java,v 1.2 2001/08/23 00:16:21 shaper Exp $

package com.samskivert.swing;

import javax.swing.JComponent;

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

    /**
     * Return the component associated with the observer so that the
     * tool tip manager can restrict monitoring the component to when
     * it's actually visible.
     */
    public JComponent getComponent ();
}
