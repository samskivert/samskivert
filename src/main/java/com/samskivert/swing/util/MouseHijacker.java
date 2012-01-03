//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.swing.util;

import java.awt.Component;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;

/**
 * Hijacks normal mouse input from reaching the specified component's
 * normal listeners. There is one special consideration it makes for your
 * component: it tracks mouse movement events and will relay the last
 * one to each MouseMotionListener before reinstating it on the component.
 *
 * Typically this will be used in conjunction with
 * installation of your own listeners, like so:
 *
 * <pre>
 *     public void startStuff (Component comp)
 *     {
 *         _hijacker = new MouseHijacker(comp);
 *         comp.addMouseListener(this);
 *     }
 *
 *     public void mouseClicked (MouseEvent evt)
 *     {
 *         doStuff();
 *         endStuff();
 *     }
 *
 *     protected void endStuff ()
 *     {
 *         Component comp = _hijacker.release();
 *         comp.removeMouseListener(this);
 *     }
 *
 *     protected MouseHijacker _hijacker;
 * </pre>
 */
public class MouseHijacker
{
    /**
     * Construct a mouse hijacker and hijack the specified component.
     */
    public MouseHijacker (Component comp)
    {
        _comp = comp;
        hijack();
    }

    /**
     * Hijack the component's mouse listeners.
     */
    public void hijack ()
    {
        _mls = _comp.getMouseListeners();
        for (int ii=0; ii < _mls.length; ii++) {
            _comp.removeMouseListener(_mls[ii]);
        }

        _mmls = _comp.getMouseMotionListeners();
        for (int ii=0; ii < _mmls.length; ii++) {
            _comp.removeMouseMotionListener(_mmls[ii]);
        }

        _comp.addMouseMotionListener(_motionCatcher);
    }

    /**
     * Release the component from the hijacking: reenable all of its
     * previous mouse listeners, and return the component for convenience.
     */
    public Component release ()
    {
        _comp.removeMouseMotionListener(_motionCatcher);

        for (int ii=0; ii < _mls.length; ii++) {
            _comp.addMouseListener(_mls[ii]);
        }
        _mls = null;

        for (int ii=0; ii < _mmls.length; ii++) {
            // fake the last movement
            if (_lastMotion != null) {
                _mmls[ii].mouseMoved(_lastMotion);
            }
            _comp.addMouseMotionListener(_mmls[ii]);
        }
        _mmls = null;

        return _comp;
    }

    /** The component we've hijacked. */
    protected Component _comp;

    /** The last mouse moved event, captured. We're so naughty. */
    protected MouseEvent _lastMotion;

    /** Used to capture each motion event while everything's hijacked. */
    protected MouseMotionListener _motionCatcher = new MouseMotionAdapter() {
        @Override public void mouseMoved (MouseEvent evt)
        {
            _lastMotion = evt;
        }
    };

    /** The previous mouse listeners. */
    protected MouseListener[] _mls;

    /** The previous mouse motion listeners. */
    protected MouseMotionListener[] _mmls;
}
