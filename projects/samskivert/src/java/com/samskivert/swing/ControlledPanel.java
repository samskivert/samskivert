//
// $Id: ControlledPanel.java,v 1.1 2002/03/16 20:52:07 mdb Exp $

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
        _controller.setControlledPanel(this);
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
