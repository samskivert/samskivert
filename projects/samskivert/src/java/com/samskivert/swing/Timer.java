//
// $Id: Timer.java,v 1.1 2002/11/05 01:47:32 ray Exp $

package com.samskivert.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;

/**
 * A timer that posts commands to a controller.
 */
public class Timer extends javax.swing.Timer
{
    /**
     * Construct a samskivert timer.
     *
     * @param delay the delay between posts.
     * @param source the source component to use for finding the 
     * appropriate controller.
     * @param command the command to post.
     */
    public Timer (int delay, Component source, String command)
    {
        super(delay, Controller.DISPATCHER);

        _event = new RepeatingActionEvent(source, command);
    }

    // documentation inherited
    protected void fireActionPerformed (ActionEvent e)
    {
        _event.setWhen(e.getWhen());
        super.fireActionPerformed(_event);
    }

    /**
     * We reuse this event each time so that we don't have to create a new one.
     */
    protected static class RepeatingActionEvent extends ActionEvent
    {
        /**
         * Construct a repeating action event.
         */
        public RepeatingActionEvent (Object src, String cmd)
        {
            super(src, 0, cmd);
        }

        /**
         * Get the time at which this event happened. Note that this
         * is provided for convenience but should not be depended on
         * due to the repeating nature: the event could get re-fired
         * before your controller has dealt with it.
         */
        public long getWhen ()
        {
            return _when;
        }

        /**
         * Set the time at which this event happened.
         */
        public void setWhen (long when)
        {
            _when = when;
        }

        /** The latest time that we were fired. */
        protected long _when;
    }

    /** The event we re-use to post the command. */
    protected RepeatingActionEvent _event;
}
