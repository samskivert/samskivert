//
// $Id: ButtonUtil.java,v 1.1 2002/12/04 23:58:22 ray Exp $

package com.samskivert.swing.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;

/**
 * Utilities for buttons.
 */
public class ButtonUtil
{
    /**
     * Set the specified button such that it alternates between being
     * selected and not whenever it is pushed.
     */
    public static synchronized void setToggling (AbstractButton b)
    {
        if (_toggler == null) {
            _toggler = new ActionListener () {
                public void actionPerformed (ActionEvent event)
                {
                    AbstractButton but = (AbstractButton) event.getSource();
                    but.setSelected(!but.isSelected());
                }
            };
        }

        b.addActionListener(_toggler);
    }

    /** Our lazily-initialized toggling action listener. */
    protected static ActionListener _toggler;
}
