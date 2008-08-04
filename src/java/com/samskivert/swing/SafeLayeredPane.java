//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2007 Michael Bayne
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.samskivert.swing;

import java.awt.Component;
import javax.swing.JLayeredPane;

/**
 * A JLayeredPane that removes all popups when a non-popup component is removed.
 * This gets around an apparent bug in awt/swing that fucks up hard when
 * a component is removed and a popup is up.
 */
public class SafeLayeredPane extends JLayeredPane
{
    public void remove (int index)
    {
        Component c = getComponent(index);

        // if it's a popup we're removing, leave any other popups in place
        if (getLayer(c) == POPUP_LAYER.intValue()) {
            super.remove(index);
            return;
        }

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
