//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;

import com.samskivert.swing.event.CommandEvent;

/**
 * A button that fires CommandEvents when it is actioned.
 */
public class CommandButton extends JButton
{
    /**
     * Set the argument of the CommandEvents which we generate.
     */
    public void setActionArgument (Object arg)
    {
        _argument = arg;
    }

    /**
     * Get the argument that we'll use when we generate CommandEvents.
     */
    public Object getActionArgument ()
    {
        return _argument;
    }

    @Override
    protected void fireActionPerformed (ActionEvent event)
    {
        Object[] listeners = listenerList.getListenerList();
        ActionEvent e = null;
        // Process the listeners last to first, notfiying
        // those that are interested in this event
        for (int ii = listeners.length - 2; ii >= 0; ii -= 2) {
            if (listeners[ii] == ActionListener.class) {
                // Lazily create the event:
                if (e == null) {
                    e = createEventToFire(event);
                }
                ((ActionListener) listeners[ii + 1]).actionPerformed(e);
            }
        }
    }

    /**
     * Create the event to fire from the prototype.
     * If this method were broken out in AbstractButton, we wouldn't even
     * have had to override fireActionPerformed().
     */
    protected ActionEvent createEventToFire (ActionEvent proto)
    {
        String actionCommand = proto.getActionCommand();
        if (actionCommand == null) {
            actionCommand = getActionCommand();
        }

        Object arg = (proto instanceof CommandEvent)
            ? ((CommandEvent) proto).getArgument() : null;

        // if we were passed an action event, or if it was a command event
        // with a null arg...
        if (arg == null) {
            // ...use ours
            arg = getActionArgument();
        }

        if (arg == null) {
            // just create a plain ActionEvent
            return new ActionEvent(CommandButton.this,
                                   ActionEvent.ACTION_PERFORMED, actionCommand,
                                   proto.getWhen(), proto.getModifiers());
        } else {
            // if we found an arg somewhere, create a CommandEvent
            return new CommandEvent(CommandButton.this, actionCommand, arg,
                                    proto.getWhen(), proto.getModifiers());
        }
    }

    /** The CommandEvent argument for our actions. */
    protected Object _argument;
}
