//
// $Id: DnDManager.java,v 1.16 2003/05/08 21:46:28 ray Exp $

package com.samskivert.swing.dnd;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.IllegalComponentStateException;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Transparency;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;

import javax.swing.JComponent;
import javax.swing.Timer;

import javax.swing.event.AncestorEvent;

import com.samskivert.swing.event.AncestorAdapter;
import com.samskivert.swing.util.SwingUtil;
import com.samskivert.Log;

/**
 * A custom Drag and Drop manager for use within a single JVM. Does what we
 * need it to do and no more.
 */
public class DnDManager
    implements MouseMotionListener, AWTEventListener
{
    /**
     * Add the specified component as a source of drags, with the DragSource
     * controller, and remove the source when it is removed from the component
     * hierarchy.
     */
    public static void addDragSource (DragSource source, JComponent comp)
    {
        addDragSource(source, comp, true);
    }

    /**
     * Add the specified component as a source of drags, with the DragSource
     * controller.
     *
     * @param autoremove if true, the source will automatically be removed
     * from the DnD system when it is removed from the component hierarchy.
     */
    public static void addDragSource (
        DragSource source, JComponent comp, boolean autoremove)
    {
        singleton.addSource(source, comp, autoremove);
    }

    /**
     * Add the specified component as a drop target, and remove the target
     * when it is removed from the component hierarchy.
     */
    public static void addDropTarget (DropTarget target, JComponent comp)
    {
        addDropTarget(target, comp, true);
    }

    /**
     * Add the specified component as a drop target.
     *
     * @param autoremove if true, the source will automatically be removed
     * from the DnD system when it is removed from the component hierarchy.
     */
    public static void addDropTarget (
        DropTarget target, JComponent comp, boolean autoremove)
    {
        singleton.addTarget(target, comp, autoremove);
    }

    /**
     * Remove the specified component as a drag source.
     */
    public static void removeDragSource (JComponent comp)
    {
        singleton.removeSource(comp);
    }

    /**
     * Remove the specified component as a drop target.
     */
    public static void removeDropTarget (JComponent comp)
    {
        singleton.removeTarget(comp);
    }

    /**
     * Create a custom cursor out of the specified image, putting the hotspot
     * in the exact center of the created cursor.
     */
    public static Cursor createImageCursor (Image img)
    {
        return createImageCursor(img, null);
    }

    /**
     * Create a custom cursor out of the specified image, with the specified
     * hotspot.
     */
    public static Cursor createImageCursor (Image img, Point hotspot)
    {
        Toolkit tk = Toolkit.getDefaultToolkit();

        // for now, just report the cursor restrictions, then blindly create
        int w = img.getWidth(null);
        int h = img.getHeight(null);
        Dimension d = tk.getBestCursorSize(w, h);
        int colors = tk.getMaximumCursorColors();
        Log.debug("Creating custom cursor [desiredSize=" + w + "x" + h +
                  ", bestSize=" + d.width + "x" + d.height +
                  ", maxcolors=" + colors + "].");

        // if the passed-in image is smaller, pad it with transparent pixels
        // and use it anyway.
        if (((w < d.width) && (h <= d.height)) ||
            ((w <= d.width) && (h < d.height))) {
            Image padder = GraphicsEnvironment.getLocalGraphicsEnvironment().
                getDefaultScreenDevice().getDefaultConfiguration().
                createCompatibleImage(d.width, d.height, Transparency.BITMASK);
            Graphics g = padder.getGraphics();
            g.drawImage(img, 0, 0, null);
            g.dispose();

            // and reassign the image to the padded image
            img = padder;

            // and adjust the 'best' to cheat the hotspot checking code
            d.width = w;
            d.height = h;
        }

        // make sure the hotspot is valid
        if (hotspot == null) {
            hotspot = new Point(d.width / 2, d.height / 2);
        } else {
            hotspot.x = Math.min(d.width - 1, Math.max(0, hotspot.x));
            hotspot.y = Math.min(d.height - 1, Math.max(0, hotspot.y));
        }

        // and create the cursor
        return tk.createCustomCursor(img, hotspot, "samskivertDnDCursor");
    }

    /**
     * Restrict construction.
     */
    private DnDManager ()
    {
    }

    /**
     * Add a dragsource.
     */
    protected void addSource (
        DragSource source, JComponent comp, boolean autoremove)
    {
        _draggers.put(comp, source);
        comp.addMouseMotionListener(this);
        if (autoremove) {
            comp.addAncestorListener(_remover);
        }
    }

    /**
     * Remove a dragsource.
     */
    protected void removeSource (JComponent comp)
    {
        _draggers.remove(comp);
        comp.removeMouseMotionListener(this);
    }

    /**
     * Add a droptarget.
     */
    protected void addTarget (
        DropTarget target, JComponent comp, boolean autoremove)
    {
        _droppers.put(comp, target);
        if (autoremove) {
            comp.addAncestorListener(_remover);
        }
    }

    /**
     * Remove a droptarget.
     */
    protected void removeTarget (JComponent comp)
    {
        _droppers.remove(comp);
    }

    // documentation inherited from interface MouseMotionListener
    public void mouseMoved (MouseEvent me)
    {
        // who cares.
    }

    // documentation inherited from interface MouseMotionListener
    public void mouseDragged (MouseEvent me)
    {
        // make sure a drag hasn't already started.
        if (_sourceComp != null) {
            return;
        }

        _sourceComp = me.getComponent();
        _source = (DragSource) _draggers.get(_sourceComp);

        // make sure the source wants to start a drag.
        if ((_source == null) || (!_sourceComp.isEnabled()) ||
            (!_source.startDrag(_cursors, _data))) {
            // if not, reset our start conditions and bail
            reset();
            return;
        }

        // use standard cursors if custom ones not specified
        if (_cursors[0] == null) {
            _cursors[0] = java.awt.dnd.DragSource.DefaultMoveDrop;
        }
        if (_cursors[1] == null) {
            _cursors[1] = java.awt.dnd.DragSource.DefaultMoveNoDrop;
        }

        // start out with the no-drop cursor.
        _curCursor = _cursors[1];

        // install a listener so we know everywhere that the mouse enters
        Toolkit.getDefaultToolkit().addAWTEventListener(this, 
                AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);

        // find the top-level window and set the cursor there.
        for (_topComp = _sourceComp; true; ) {
            Component c = _topComp.getParent();
            if (c == null) {
                break;
            }
            _topComp = c;
        }
        _topCursor = _topComp.getCursor();
        _topComp.setCursor(_curCursor);

        setComponentCursor(_sourceComp);
    }

    /**
     * Check to see if we need to do component-level cursor setting and take
     * care of it if needed.
     */
    protected void setComponentCursor (Component comp)
    {
        Cursor c = comp.getCursor();
        if (c != _curCursor) {
            _lastComp = comp;
            _oldCursor = comp.isCursorSet() ? c : null;
            comp.setCursor(_curCursor);
        }
    }

    /**
     * Clear out the component-level cursor.
     */
    protected void clearComponentCursor ()
    {
        if (_lastComp != null) {
            _lastComp.setCursor(_oldCursor);
            _lastComp = null;
        }
    }

    // documentation inherited from interface AWTEventListener
    public void eventDispatched (AWTEvent event)
    {
        switch (event.getID()) {
        case MouseEvent.MOUSE_ENTERED:
            globalMouseEntered((MouseEvent) event);
            break;

        case MouseEvent.MOUSE_EXITED:
            globalMouseExited((MouseEvent) event);
            break;

        case MouseEvent.MOUSE_RELEASED:
            globalMouseReleased((MouseEvent) event);
            break;

        case MouseEvent.MOUSE_DRAGGED:
            globalMouseDragged((MouseEvent) event);
            break;
        }
    }

    /**
     * Handle the mouse entering a new component.
     */
    protected void globalMouseEntered (MouseEvent event)
    {
        Component newcomp = ((MouseEvent) event).getComponent();
        _lastTarget = findAppropriateTarget(newcomp);
        Cursor newcursor = _cursors[(_lastTarget == null) ? 1 : 0];

        // see if the current cursor changed.
        if (newcursor != _curCursor) {
            _topComp.setCursor(_curCursor = newcursor);
        }

        // and check the cursor at the component level
        setComponentCursor(newcomp);
    }

    /**
     * Handle the mouse leaving a component.
     */
    protected void globalMouseExited (MouseEvent event)
    {
        clearComponentCursor();

        // and if we were over a target, let the target know that we left
        if (_lastTarget != null) {
            _lastTarget.noDrop();
            _lastTarget = null;
        }

        checkAutoscroll(event);
    }

    /**
     * Handle the mouse button being released.
     */
    protected void globalMouseReleased (MouseEvent event)
    {
        // stop listening to every little event
        Toolkit.getDefaultToolkit().removeAWTEventListener(this);

        // reset cursors
        clearComponentCursor();
        _topComp.setCursor(_topCursor);

        // the event.getComponent() will be the source component here (huh..)
        // so we instead use the last component seen in mouseEnter
        if (_lastTarget != null) {
            _lastTarget.dropCompleted(_source, _data[0]);
            _source.dragCompleted(_lastTarget);
        }
        reset();
    }

    /**
     * Track global drags for autoscrolling support.
     */
    protected void globalMouseDragged (MouseEvent event)
    {
        if (_scrollComp == null) {
            return;
        }

        int x = event.getX();
        int y = event.getY();

        try {
            Point p = event.getComponent().getLocationOnScreen();
            p.translate(x, y);

            Rectangle r = getRectOnScreen(_scrollComp);
            if (!r.contains(p)) {
                r.grow(_scrollDim.width, _scrollDim.height);
                if (r.contains(p)) {
                    _scrollPoint = p;
                    return;  // still autoscrolling
                }
            }
        } catch (IllegalComponentStateException icse) {
            // the component could no longer be on screen
            // don't complain, just stop autoscroll
        }

        // stop autoscrolling
        _scrollComp = null;
        _scrollTimer.stop();
    }

    /**
     * Find the lowest accepting parental target to this component.
     */
    protected DropTarget findAppropriateTarget (Component comp)
    {
        DropTarget target;
        while (comp != null) {
            // here we sneakily prevent dropping on the source
            target = (comp == _sourceComp) ? null
                                           : (DropTarget) _droppers.get(comp);
            if ((target != null) && comp.isEnabled() &&
                target.checkDrop(_source, _data[0])) {
                return target;
            }
            comp = comp.getParent();
        }
        return null;
    }

    /**
     * Check to see if we want to enter autoscrolling mode. 
     */
    protected void checkAutoscroll (MouseEvent exitEvent)
    {
        Component comp = exitEvent.getComponent();
        Point p = exitEvent.getPoint();
        try {
            Point scr = comp.getLocationOnScreen();
            p.translate(scr.x, scr.y);
        } catch (IllegalComponentStateException icse) {
            // the component is no longer on screen. Deal.
            return;
        }

        Component parent;
        Object target;
        while (true) {
            target = _droppers.get(comp);
            if (target instanceof AutoscrollingDropTarget) {
                AutoscrollingDropTarget adt = (AutoscrollingDropTarget) target;
                JComponent jc = (JComponent) comp;

                Rectangle r = getRectOnScreen(jc);
                // make sure we're actually out of the autoscrolling component
                if (!r.contains(p)) {
                    // start autoscrolling.
                    _scrollComp = jc;
                    _scrollDim = adt.getAutoscrollBorders();
                    _scrollPoint = p;
                    _scrollTimer.start();
                    return;
                }
            }

            parent = comp.getParent();
            if (parent == null) {
                return;
            }
            comp = parent;
        }
    }

    /**
     * Find the rectangular area that is visible in screen coordinates
     * for the given component.
     */
    protected Rectangle getRectOnScreen (JComponent comp)
    {
        Rectangle r = comp.getVisibleRect();
        Point p = comp.getLocationOnScreen();
        r.translate(p.x, p.y);
        return r;
    }

    /**
     * Reset dnd to a starting state.
     */
    protected void reset ()
    {
        _scrollTimer.stop();

        _source = null;
        _sourceComp = null;
        _lastComp = null;
        _lastTarget = null;
        _data[0] = null;
        _cursors[0] = null;
        _cursors[1] = null;
        _topComp = null;
        _topCursor = null;
        _curCursor = null;

        _scrollComp = null;
        _scrollDim = null;
        _scrollPoint = null;
    }

    /** A handy helper that removes components when they're no longer in
     * the hierarchy. */
    protected AncestorAdapter _remover = new AncestorAdapter() {
        public void ancestorRemoved (AncestorEvent ae)
        {
            JComponent comp = ae.getComponent();
            // try both..
            singleton.removeTarget(comp);
            singleton.removeSource(comp);
        }
    };

    /** A timer used for autoscrolling. */
    protected Timer _scrollTimer = new Timer(100, new ActionListener() {
        public void actionPerformed (ActionEvent x)
        {
            // bail if we're behind the times
            if (_scrollComp == null) {
                return;
            }

            // translate the scrollpoint into a point in the scroll component's
            // coordinates
            Point p = _scrollComp.getLocationOnScreen();

            // and tell the scrolling component to scroll that bit on screen
            _scrollComp.scrollRectToVisible(new Rectangle(
                _scrollPoint.x - p.x, _scrollPoint.y - p.y, 1, 1));
        }
    });

    /** Our DropTargets, indexed by associated Component. */
    protected HashMap _droppers = new HashMap();

    /** Our DragSources, indexed by associated component. */
    protected HashMap _draggers = new HashMap();

    /** The original, last, and top-level components during a drag. */
    protected Component _sourceComp, _lastComp, _topComp;

    /** The source of a drag. */
    protected DragSource _source;

    /** The last target, or null if no last target. */
    protected DropTarget _lastTarget;

    /** The current cursor we're showing the user. */
    protected Cursor _curCursor;

    /** The cursor that used to be set for _lastComp. */
    protected Cursor _oldCursor;

    /** The original top-level cursor. */
    protected Cursor _topCursor;

    /** The accept/reject cursors. */
    protected Cursor[] _cursors = new Cursor[2];

    /** The data to be passed in the drop. */
    protected Object[] _data = new Object[1];

    /** The component associated with an AutoscrollingDropTarget when we're
     * in autoscrolling mode. */
    protected JComponent _scrollComp;

    /** The area around the _scrollComp that is active for autoscrolling. */
    protected Dimension _scrollDim;

    /** The last screen-coordinate point of a drag while autoscrolling. */
    protected Point _scrollPoint;

    /** A single manager for the entire JVM. */
    protected static final DnDManager singleton = new DnDManager();
}
