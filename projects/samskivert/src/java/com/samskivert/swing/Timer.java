//
// $Id: Timer.java,v 1.2 2003/07/27 17:31:36 ray Exp $

package com.samskivert.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
        this(delay, Controller.DISPATCHER);

        _event = new RepeatingActionEvent(source, command);
    }

    /**
     * Construct a samskivert timer with the same functionality of a swing
     * Timer except for an improved toString().
     */
    public Timer (int delay, ActionListener listener)
    {
        super(delay, listener);

        try {
            _source = new Throwable().getStackTrace()[1].toString();
        } catch (Throwable oopsie) {
            _source = "<unknown>";
        }
    }

    // documentation inherited
    public String toString ()
    {
        return "Timer [source=" + _source +
            ((_event != null) ? ", cmd=" + _event.getActionCommand() : "") +
            "]";
    }

    // documentation inherited
    protected void fireActionPerformed (ActionEvent e)
    {
        if (_event != null) {
            _event.setWhen(e.getWhen());
            e = _event;
        }
        super.fireActionPerformed(e);
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

    /** The line of code where this Timer was constructed. */
    protected String _source;
}
