//
// $Id: SafeScrollPane.java,v 1.1 2002/07/09 17:48:23 ray Exp $

package com.samskivert.swing;

import java.awt.Component;

import javax.swing.JScrollPane;
import javax.swing.JViewport;

/**
 * A scroll pane that is safe to use in frame managed views.
 */
public class SafeScrollPane extends JScrollPane
{
    public SafeScrollPane (Component view)
    {
        super(view);
    }

    protected JViewport createViewport ()
    {
        JViewport vp = new JViewport();
        vp.setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        return vp;
    }
}
