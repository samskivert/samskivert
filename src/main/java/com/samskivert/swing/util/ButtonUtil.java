//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.swing.util;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.AbstractButton;
import javax.swing.JToggleButton;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import com.samskivert.util.PrefsConfig;
import com.samskivert.util.IntListUtil;

/**
 * Utilities for buttons.
 */
public class ButtonUtil
{
    /**
     * Set the specified button such that it alternates between being selected and not whenever it
     * is pushed.
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

    /**
     * Binds the supplied button to the named boolean property in the supplied config repository.
     * When the button is pressed, it will update the config property and when the config property
     * is changed (by the button or by other means) it will update the selected state of the
     * button. When the button is made non-visible, it will be unbound to the config's property and
     * rebound again if it is once again visible.
     */
    public static void bindToProperty (
        String property, PrefsConfig config, AbstractButton button, boolean defval)
    {
        // create a config binding which will take care of everything
        new ButtonConfigBinding(property, config, button, defval);
    }

    /**
     * Configure the specified button to cause the specified property to cycle through the
     * specified values whenever the button is pressed.
     */
    public static ActionListener cycleToProperty (
        final String property, final PrefsConfig config, AbstractButton button,
        final int[] values)
    {
        ActionListener al = new ActionListener() {
            public void actionPerformed (ActionEvent event)
            {
                // get the current value and find out where it is in the list
                int oldval = config.getValue(property, values[0]);
                // if it's not even in the list, newidx will be 0
                int newidx = (1 + IntListUtil.indexOf(values, oldval))
                    % values.length;
                config.setValue(property, values[newidx]);
            }
        };
        button.addActionListener(al);
        return al;
    }

    /** Used for {@link #bindToProperty}. */
    protected static class ButtonConfigBinding
        implements AncestorListener, PropertyChangeListener, ItemListener
    {
        public ButtonConfigBinding (String property, PrefsConfig config,
                                    AbstractButton button, boolean defval)
        {
            _property = property;
            _config = config;
            _button = button;
            _defval = defval;

            // wire ourselves up to the button
            button.addAncestorListener(this);
            button.addItemListener(this);

            // if this is not already a toggle button, we'll need to make
            // it toggle
            if (!(button instanceof JToggleButton)) {
                setToggling(button);
            }
        }

        public void ancestorAdded (AncestorEvent event) {
            // listen for config changes
            _config.addPropertyChangeListener(_property, this);
            // set the button up appropriately
            _button.setSelected(_config.getValue(_property, _defval));
        }

        public void ancestorRemoved (AncestorEvent event) {
            // stop listening for config changes
            _config.removePropertyChangeListener(_property, this);
        }

        public void ancestorMoved (AncestorEvent event) {
            // nothing doing
        }

        public void propertyChange (PropertyChangeEvent event) {
            // update the button
            if (event.getPropertyName().equals(_property)) {
                Boolean value = (Boolean)event.getNewValue();
                _button.setSelected(value.booleanValue());
            }
        }

        public void itemStateChanged (ItemEvent event) {
            boolean selected = (event.getStateChange() == ItemEvent.SELECTED);
            _config.setValue(_property, selected);
        }

        protected String _property;
        protected PrefsConfig _config;
        protected AbstractButton _button;
        protected boolean _defval;
    }

    /** Our lazily-initialized toggling action listener. */
    protected static ActionListener _toggler;
}
