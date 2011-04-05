//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2011 Michael Bayne, et al.
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

import javax.swing.JPanel;

/**
 * A controlled panel takes care of setting up its controller and
 * providing it. When operating in conjunction with a controlled panel,
 * the controller can automatically invoke {@link Controller#wasAdded} and
 * {@link Controller#wasRemoved}.
 */
public abstract class ControlledPanel extends JPanel
    implements ControllerProvider
{
    /**
     * Creates a controlled panel and its associated controller.
     */
    public ControlledPanel ()
    {
        _controller = createController();

        // let the controller know about this panel
        if (_controller != null) {
            _controller.setControlledPanel(this);
        }
    }

    // documentation inherited from interface
    public Controller getController ()
    {
        return _controller;
    }

    /**
     * Called to create the controller associated with this controlled
     * panel. Derived classes should override this and instantiate the
     * appropriate controller derived class. Note that this will be called
     * very early in the construction process, before derived classes have
     * access to their member data and as such, the derived panel will not
     * be able to pass anything interesting to its associated controller
     * constructor. The expectation is that it will obtain a reference to
     * its controller later in its own constructor and supply any extra
     * initialization data that is needed at that time.
     */
    protected abstract Controller createController ();

    /** The controller with which we are working. */
    protected Controller _controller;
}
