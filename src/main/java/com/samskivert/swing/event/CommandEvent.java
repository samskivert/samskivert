//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.swing.event;

import java.awt.event.ActionEvent;

/**
 * An action event with an associated argument. Often times with
 * controllers, one wants to post a command with an associated object
 * (which we call the argument), but action event provides no mechanism to
 * do so. So this class is provided for such situations.
 */
public class CommandEvent extends ActionEvent
{
    public CommandEvent (Object source, String command, Object argument)
    {
        super(source, ActionEvent.ACTION_PERFORMED, command);
        _argument = argument;
    }

    public CommandEvent (
        Object source, String command, Object argument,
        long when, int modifiers)
    {
        super(source, ActionEvent.ACTION_PERFORMED, command, when, modifiers);
        _argument = argument;
    }

    /**
     * Returns the argument provided to the command event at construct
     * time.
     */
    public Object getArgument ()
    {
        return _argument;
    }

    @Override
    public String toString ()
    {
        return "[cmd=" + getActionCommand() + ", src=" + getSource() +
            ", arg=" + _argument + "]";
    }

    /** The argument to this command event. */
    protected Object _argument;
}
