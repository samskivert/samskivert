//
// $Id: ToolTipManager.java,v 1.4 2001/08/30 21:17:33 shaper Exp $

package com.samskivert.swing;

import javax.swing.*;
import javax.swing.event.*;

import com.samskivert.Log;
import com.samskivert.util.*;

/**
 * The tool tip manager provides generic facilities for container
 * objects to display "tool tips" associated with the objects they
 * contain.
 *
 * <p> The container object should construct a tool tip manager for
 * itself, implement the {@link ToolTipObserver} interface and then
 * add calls to {@link #handleMouseEntered}, {@link
 * #handleMouseExited}, {@link #handleMouseClicked} and {@link
 * #handleMouseMoved} as appropriate within its code to notify the
 * manager of relevant user events.
 *
 * <p> The manager will then turn around and call {@link
 * ToolTipObserver#showToolTip} or {@link ToolTipObserver#hideToolTip}
 * as necessary, taking into account standard tool tip timing and
 * semantics.
 */
public class ToolTipManager implements Interval, AncestorListener
{
    /**
     * Construct a tool tip manager for the given observer.
     *
     * @param obs the tool tip observer.
     */
    public ToolTipManager (ToolTipObserver obs)
    {
	// save off a reference to the observer
	_obs = obs;

	// set up our starting state
	_lastmove = System.currentTimeMillis();
	_lastx = _lasty = -1;
	_fastshow = false;
	_tipdelay = DEF_TIP_DELAY;

	// register the tip action interval immediately if the
	// component is already showing on-screen
	JComponent target = _obs.getComponent();
  	if (target.isShowing()) {
	    registerInterval();
	}

	// listen to the view's ancestor events
	target.addAncestorListener(this);
    }

    /**
     * Called by the <code>IntervalManager</code> whenever our
     * interval expires.
     */
    public void intervalExpired (int id, Object arg)
    {
	// disable fast tool tip display if we've been outside of an
	// object for sufficiently long
	if (_fastshow && _curobj == null) {
	    long now = System.currentTimeMillis();
	    _fastshow = (now - _lastmove < _tipdelay);
	}

	// show or hide the tool tip as appropriate
	handleTipAction();
    }

    /**
     * Register the tip action interval with the interval manager.
     */
    protected void registerInterval ()
    {
	// register ourselves with the interval manager
	IntervalManager.register(this, TIP_INTERVAL, null, true);
    }	

    /**
     * Handle showing or hiding a tip as necessary.
     */
    protected void handleTipAction ()
    {
	// bail if there's nothing doing
	if (_action == A_NONE) return;

	// do nothing if it's not yet time for us to act
	if (!isShowTime()) return;

	// throw a task on the AWT thread to handle tip actions
	SwingUtilities.invokeLater(new TipTask(_curobj, _action));

	// clear out our action state
	_action = A_NONE;

	// speed future tool tips on their way
	_fastshow = true;
    }	

    /**
     * Return whether sufficient time has passed since the last mouse
     * movement to allow us to show a tool tip.
     */
    protected boolean isShowTime ()
    {
	long now = System.currentTimeMillis();
	return (_fastshow || (now - _lastmove >= _tipdelay));
    }

    protected void updateObject (ToolTipProvider target)
    {
	_curobj = target;
	_lastmove = System.currentTimeMillis();

	// update tip display immediately if the time is right
	handleTipAction();
    }

    /**
     * Handle mouse entered events for a given object.  The {@link
     * ToolTipObserver} should call this method whenever an object it
     * manages is entered.
     *
     * @param target the object entered.
     * @param x the mouse x-position.
     * @param y the mouse y-position.
     */
    public void handleMouseEntered (ToolTipProvider target, int x, int y)
    {
	_action = A_SHOW;
	updateObject(target);
	_lastx = x;
	_lasty = y;
    }

    /**
     * Handle mouse exited events for a given object.  The {@link
     * ToolTipObserver} should call this method whenever an object it
     * manages is exited.
     */
    public void handleMouseExited ()
    {
	_action = A_HIDE;
	updateObject(null);
    }

    /**
     * Handle mouse clicked events for a given object.  The {@link
     * ToolTipObserver} should call this method whenever an object it
     * manages is clicked on.
     *
     * @param target the object clicked on.
     */
    public void handleMouseClicked (ToolTipProvider target)
    {
	_action = A_HIDE;
	updateObject(target);

	// disable fast tip display on mouse-click
	_fastshow = false;
    }

    /**
     * Handle mouse moved events.  The {@link ToolTipObserver} should
     * call this method whenever the mouse is moved within its
     * container bounds.
     *
     * @param x the mouse x-position.
     * @param y the mouse y-position.
     */
    public void handleMouseMoved (int x, int y)
    {
	// just update the last moved time since we don't perform any
	// tip antics purely as a result of mouse movement
	_lastmove = System.currentTimeMillis();

	// and save the mouse position
	_lastx = x;
	_lasty = y;
    }

    /**
     * Set the milliseconds to wait before showing a tool tip.
     *
     * @param delay the milliseconds to wait.
     */
    public void setShowDelay (int delay)
    {
	if (delay < TIP_INTERVAL) {
	    Log.warning("Tip show delay must be >= " + TIP_INTERVAL + " ms.");
	    return;
	}

	_tipdelay = delay;
    }

    /**
     * A class to encapsulate tool tip observer notification to be
     * performed on the AWT thread.
     */
    class TipTask implements Runnable
    {
	public TipTask (ToolTipProvider obj, int action)
	{
	    _obj = obj;
	    _action = action;
	}

	public void run ()
	{
	    switch (_action) {
	    case A_SHOW: _obs.showToolTip(_obj, _lastx, _lasty); break;
	    case A_HIDE: _obs.hideToolTip(); break;
	    }
	}

	protected ToolTipProvider _obj;
	protected int _action;
    }

    /** AncestorListener interface methods. */

    public void ancestorAdded (AncestorEvent event)
    {
	if (_iid == -1) {
	    // register the tip action interval since we're now visible
	    registerInterval();
	}
    }

    public void ancestorRemoved (AncestorEvent event)
    {
	// un-register the tip action interval since we're now hidden
	IntervalManager.remove(_iid);
	_iid = -1;
    }

    public void ancestorMoved (AncestorEvent event) { }

    /** Delay in milliseconds between intervals. */
    protected static final int TIP_INTERVAL = 200;

    /** Default delay between tip-related actions. */
    protected static final int DEF_TIP_DELAY = 1000;

    /** Action constants. */
    protected static final int A_NONE = 0;
    protected static final int A_SHOW = 1;
    protected static final int A_HIDE = 2;

    /** The minimum delay in milliseconds before showing a tip. */
    protected long _tipdelay;

    /** The last time a mouse move we care about was made. */
    protected long _lastmove;

    /** The current object the mouse is within. */
    protected ToolTipProvider _curobj;

    /** The last known mouse position. */
    protected int _lastx, _lasty;

    /** The next tool tip action to perform. */
    protected int _action;

    /** Whether tips should be shown quickly due to other recent tips. */
    protected boolean _fastshow;

    /** The tool tip observer. */
    protected ToolTipObserver _obs;

    /** The tip action interval id. */
    protected int _iid = -1;
}
