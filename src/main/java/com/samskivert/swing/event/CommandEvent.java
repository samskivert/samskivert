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
