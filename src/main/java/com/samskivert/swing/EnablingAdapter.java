//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.swing;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;

import static com.samskivert.swing.Log.log;

/**
 * Used to enable or disable a source component based on some
 * asynchronously changing state.
 */
public class EnablingAdapter
{
    /**
     * Creates and returns an enabler that listens for changes in the
     * specified property (which must be a {@link Boolean} valued
     * property) and updates the target's enabled state accordingly.
     */
    public static PropertyChangeListener getPropChangeEnabler (
        String property, JComponent target, boolean invert)
    {
        return new PropertyChangeEnabler(property, target, invert);
    }

    /**
     * Creates an enabling adapter with the specified target component.
     *
     * @param invert if true, the target component's enabled state is set
     * to the inverse of the monitored state.
     */
    protected EnablingAdapter (JComponent target, boolean invert)
    {
        _target = target;
        _invert = invert;
    }

    /**
     * Called by the appropriate derived adapter to adjust the target's
     * enabled state.
     */
    protected void stateChanged (boolean newState)
    {
        _target.setEnabled(_invert ? !newState : newState);
    }

    /** Used by {@link #getPropChangeEnabler}. */
    protected static class PropertyChangeEnabler extends EnablingAdapter
        implements PropertyChangeListener
    {
        public PropertyChangeEnabler (
            String property, JComponent target, boolean invert)
        {
            super(target, invert);
            _property = property;
        }

        public void propertyChange (PropertyChangeEvent evt)
        {
            if (evt.getPropertyName().equals(_property)) {
                Object value = evt.getNewValue();
                if (value instanceof Boolean) {
                    stateChanged(((Boolean)value).booleanValue());
                } else {
                    log.warning("PropertyChangeEnabler connected to non-Boolean property",
                                "got", value);
                }
            }
        }

        protected String _property;
    }

    protected JComponent _target;
    protected boolean _invert;
}
