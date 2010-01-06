//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2010 Michael Bayne, et al.
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;

import static com.samskivert.Log.log;

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
