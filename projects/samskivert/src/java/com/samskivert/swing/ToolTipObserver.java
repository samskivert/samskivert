//
// $Id: ToolTipObserver.java,v 1.3 2001/08/28 23:51:48 shaper Exp $

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
     * Called when the tool tip associated with the given provider
     * should be displayed.
     *
     * @param tipper the tool tip provider.
     * @param x the last mouse x-position.
     * @param y the last mouse y-position.
     */
    public void showToolTip (ToolTipProvider tipper, int x, int y);

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
