//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.swing.dnd;

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.IllegalComponentStateException;
import java.awt.Point;
import java.awt.Rectangle;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.MouseEvent;

import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import javax.swing.event.AncestorEvent;
import javax.swing.event.MouseInputAdapter;

import com.samskivert.swing.event.AncestorAdapter;

import static com.samskivert.swing.Log.log;

/**
 * A custom Drag and Drop manager for use within a single JVM. Does what we
 * need it to do and no more.
 */
public class DnDManager
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
        comp.addMouseListener(_sourceListener);
        comp.addMouseMotionListener(_sourceListener);
        if (autoremove) {
            comp.addAncestorListener(_remover);
        }
    }

    /**
     * Remove a dragsource.
     */
    protected void removeSource (JComponent comp)
    {
        if (_sourceComp == comp) {
            // reset cursors
            clearComponentCursor();
            _topComp.setCursor(_topCursor);
            reset();
        }
        _draggers.remove(comp);
        comp.removeMouseListener(_sourceListener);
        comp.removeMouseMotionListener(_sourceListener);
    }

    /**
     * Add a droptarget.
     */
    protected void addTarget (
        DropTarget target, JComponent comp, boolean autoremove)
    {
        _droppers.put(comp, target);
        addTargetListeners(comp);
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
        removeTargetListeners(comp);
    }

    /**
     * Add the appropriate target listeners to this component
     * and all its children.
     */
    protected void addTargetListeners (Component comp)
    {
        comp.addMouseListener(_targetListener);
        comp.addMouseMotionListener(_targetListener);
        if (comp instanceof Container) { // hm, always true for JComp..
            Container cont = (Container) comp;
            cont.addContainerListener(_childListener);
            for (int ii=0, nn=cont.getComponentCount(); ii < nn; ii++) {
                addTargetListeners(cont.getComponent(ii));
            }
        }
    }

    /**
     * Remove the appropriate target listeners to this component
     * and all its children.
     */
    protected void removeTargetListeners (Component comp)
    {
        comp.removeMouseListener(_targetListener);
        comp.removeMouseMotionListener(_targetListener);
        if (comp instanceof Container) { // again, always true for JComp...
            Container cont = (Container) comp;
            cont.removeContainerListener(_childListener);
            for (int ii=0, nn=cont.getComponentCount(); ii < nn; ii++) {
                removeTargetListeners(cont.getComponent(ii));
            }
        }
    }

    /**
     * Check to see if we need to do component-level cursor setting and take
     * care of it if needed.
     */
    protected void setComponentCursor (Component comp)
    {
        Cursor c = comp.getCursor();
        if (c != _curCursor) {
            assertComponentCursorCleared();
            _lastComp = comp;
            _oldCursor = comp.isCursorSet() ? c : null;
            comp.setCursor(_curCursor);
        }
    }

    /**
     * Makes sure the component cursor is cleared.  If it isn't, generates a
     * warning message and clears it.
     */
    protected void assertComponentCursorCleared ()
    {
        if (_lastComp != null) {
            log.warning("In DnDManager, last component cursor not cleared.");
            clearComponentCursor();
        }
    }

    protected void assertTopCursorCleared ()
    {
        if (_topComp != null) {
            log.warning("In DnDManager, top component cursor not cleared.");
            _topComp.setCursor(_topCursor);
            _topComp = null;
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

    /**
     * Are we currently involved in a drag?
     */
    protected boolean isDragging ()
    {
        boolean dragging = (_source != null);

        if (!dragging) {
            // make sure there's no component/top cursor
            assertComponentCursorCleared();
            assertTopCursorCleared();
        }

        return dragging;
    }

    /**
     * Find the lowest accepting parental target to this component.
     */
    protected DropTarget findAppropriateTarget (Component comp)
    {
        DropTarget target;
        while (comp != null) {
            // here we sneakily prevent dropping on the source
            target = (comp == _sourceComp) ? null : _droppers.get(comp);
            if ((target != null) && comp.isEnabled() &&
                _source.checkDrop(target) &&
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
        DropTarget target;
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
        @Override public void ancestorRemoved (AncestorEvent ae)
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

    /** Listens to registered drag source components. */
    protected MouseInputAdapter _sourceListener = new MouseInputAdapter() {
        @Override public void mouseDragged (MouseEvent me)
        {
            // make sure a drag hasn't already started.
            if (isDragging()) {
                return;
            }

            _sourceComp = me.getComponent();
            _source = _draggers.get(_sourceComp);

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

        @Override public void mouseReleased (MouseEvent event)
        {
            if (!isDragging()) {
                return;
            }

            // reset cursors
            clearComponentCursor(); // _lastComp cleared here
            _topComp.setCursor(_topCursor);

            // get the last target seen...
            if (_lastTarget != null) {
                // determine drop location
                Point pos = event.getPoint();
                SwingUtilities.convertPointToScreen(pos, event.getComponent());
                _lastTarget.dropCompleted(_source, _data[0], pos);
                _source.dragCompleted(_lastTarget);
            }
            reset();
        }

        @Override public void mouseExited (MouseEvent event)
        {
            if (isDragging()) {
                clearComponentCursor();
            }
        }

        @Override public void mouseEntered (MouseEvent event)
        {
            if (isDragging()) {
                setComponentCursor(event.getComponent());
            }
        }
    };

    /** Listens to registered drop targets and their children. */
    protected MouseInputAdapter _targetListener = new MouseInputAdapter() {
        @Override public void mouseEntered (MouseEvent event)
        {
            if (!isDragging()) {
                return;
            }

            Component newcomp = event.getComponent();
            _lastTarget = findAppropriateTarget(newcomp);
            Cursor newcursor = _cursors[(_lastTarget == null) ? 1 : 0];

            // see if the current cursor changed.
            if (newcursor != _curCursor) {
                _topComp.setCursor(_curCursor = newcursor);
            }

            // and check the cursor at the component level
            clearComponentCursor();
            setComponentCursor(newcomp);
        }

        @Override public void mouseExited (MouseEvent event)
        {
            if (!isDragging()) {
                return;
            }

            clearComponentCursor();

            // and if we were over a target, let the target know that we left
            if (_lastTarget != null) {
                _lastTarget.noDrop();
                _lastTarget = null;
            }

            checkAutoscroll(event);
        }

        @Override public void mouseDragged (MouseEvent event)
        {
            if (!isDragging()) {
                return;
            }
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
    };

    /** Listens to the drop target components and all their children. */
    protected ContainerListener _childListener = new ContainerListener() {
        public void componentAdded (ContainerEvent e)
        {
            addTargetListeners(e.getChild());
        }

        public void componentRemoved (ContainerEvent e)
        {
            removeTargetListeners(e.getChild());
        }
    };

    /** Our DropTargets, indexed by associated Component. */
    protected HashMap<Component,DropTarget> _droppers =
        new HashMap<Component,DropTarget>();

    /** Our DragSources, indexed by associated component. */
    protected HashMap<Component,DragSource> _draggers =
        new HashMap<Component,DragSource>();

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
