//
// $Id: SafeLayeredPane.java,v 1.1 2003/08/14 22:05:07 ray Exp $

package com.samskivert.swing;

import java.awt.Component;
import javax.swing.JLayeredPane;

/**
 * A JLayeredPane that removes all popups when any component is removed.
 * This gets around an apparent bug in awt/swing that fucks up hard when
 * a component is removed and a popup is up.
 */
public class SafeLayeredPane extends JLayeredPane
{
    public void remove (int index)
    {
        Component c = getComponent(index);

        // remove all popups
        boolean removedPops = false;
        for (int ii=getComponentCount() - 1; ii >= 0; ii--) {
            if (getLayer(getComponent(ii)) == POPUP_LAYER.intValue()) {
                super.remove(ii);
                removedPops = true;
            }
        }

        // if we removed any popups, the index may have changed so we
        // need to remove by reference
        if (removedPops) {
            remove(c); // which will end up calling this method again, but
            // the second time the popups will already be gone

        } else {
            super.remove(index);
        }
    }
}
