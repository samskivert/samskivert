//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.swing;

import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;

import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.samskivert.swing.event.CommandEvent;
import com.samskivert.swing.util.MouseHijacker;
import com.samskivert.swing.util.SwingUtil;
import com.samskivert.util.ObserverList;
import com.samskivert.util.RunAnywhere;

/**
 * Provides a radial menu with iconic menu items that expand to include textual descriptions when
 * moused over.
 */
public class RadialMenu
    implements MouseMotionListener, MouseListener
{
    /**
     * The interface used to communicate back to the component that hosts this menu.
     */
    public static interface Host
    {
        /**
         * Returns the component on which we will be rendered.
         */
        public Component getComponent ();

        /**
         * Requests the bounds of the visible region in which we'll be rendering the menu. The menu
         * will be constrained to fit within these bounds starting with a natural position around
         * the supplied target bounds and adjusting minimally to fit.
         */
        public Rectangle getViewBounds ();

        /**
         * Requests that the appropriate region of the host be repainted.
         */
        public void repaintRect (int x, int y, int width, int height);

        /**
         * Instructs the host component to stop rendering this menu because it has been popped
         * down.
         */
        public void menuDeactivated (RadialMenu menu);
    }

    /**
     * Used to determine at the time that a menu is shown, whether its items should be included
     * and/or enabled. Predicate methods are called immediately before showing a radial menu and
     * thus should not perform complicated computations. They generally will check some simple
     * model to determine a menu item's status.
     */
    public static interface Predicate
    {
        /**
         * If true, the menu will be included in the character menu when it is displayed.
         *
         * @param menu the menu that will include or not include the item in question.
         * @param item the menu item that will or will not be included.
         */
        public boolean isIncluded (RadialMenu menu, RadialMenuItem item);

        /**
         * If true, the menu will be enabled when displayed in a character's menu.
         *
         * @param menu the menu that contains the item in question.
         * @param item the menu item that will or will not be enabled.
         */
        public boolean isEnabled (RadialMenu menu, RadialMenuItem item);
    }

    /**
     * An additional interface that can be implemented by the predicate to display extra status.
     */
    public static interface IconPredicate extends Predicate
    {
        /**
         * Return an additional predicate that should be drawn.
         */
        public Icon getIcon (RadialMenu menu, RadialMenuItem item);
    }

    /**
     * Constructs a radial menu.
     */
    public RadialMenu ()
    {
    }

    /**
     * Adds a menu item to the menu. The menu should not currently be active.
     *
     * @param command the command to be issued when the item is selected.
     * @param label the textual label to be displayed with the menu item.
     * @param icon the icon to display next to the menu text or null if no
     * icon is desired.
     * @param predicate a predicate that will be used to determine whether or not this menu item
     * should be included in the menu and enabled when it is shown.
     *
     * @return the item that was added to the menu. It can be modified while the menu is not being
     * displayed.
     */
    public RadialMenuItem addMenuItem (String command, String label, Image icon, Predicate predicate)
    {
        RadialMenuItem item = new RadialMenuItem(command, label, new ImageIcon(icon), predicate);
        addMenuItem(item);
        return item;
    }

    /**
     * Adds an already constructed menu item to the menu. The menu should not currently be active.
     *
     * @param item the menu item instance to be added.
     */
    public void addMenuItem (RadialMenuItem item, boolean makeDefault)
    {
        addMenuItem(item);
        if (makeDefault) {
            _defaultItem = item;
        }
    }

    /**
     * Adds an already constructed menu item to the menu. The menu should not currently be active.
     *
     * @param item the menu item instance to be added.
     */
    public void addMenuItem (RadialMenuItem item)
    {
        _items.add(item);
    }

    /**
     * Removes the specified menu item from this menu.
     */
    public void removeMenuItem (RadialMenuItem item)
    {
        _items.remove(item);
    }

    /**
     * Removes the first menu item that matches the specified command from this menu.
     *
     * @return true if a matching menu item was removed, false if not.
     */
    public boolean removeMenuItem (String command)
    {
        int icount = _items.size();
        for (int i = 0; i < icount; i++) {
            RadialMenuItem item = _items.get(i);
            if (item.command.equals(command)) {
                _items.remove(i);
                return true;
            }
        }

        return false;
    }

    /**
     * May be called by an item when it changes in a material way.
     */
    public void itemUpdated ()
    {
        if (_host != null) {
            layout();
            repaint();
        }
    }

    /**
     * Adds a listener that will be notified when a menu item is selected. When the the menu is
     * popped down, all listeners will be cleared.
     */
    public void addActionListener (ActionListener listener)
    {
        _actlist.add(listener);
    }

    /**
     * Sets the optional centerpiece icon to be displayed in the center of the menu.
     */
    public void setCenterIcon (Icon icon)
    {
        _centerIcon = icon;
    }

    /**
     * Activates the radial menu, rendering a menu around the prescribed bounds. It is expected
     * that the host component will subsequently call {@link #render} if the menu invalidates the
     * region of the component occupied by the menu and requests it to repaint.
     *
     * @param host the host component within which the radial menu is displayed.
     * @param bounds the bounds of the object that was clicked to activate the menu.
     * @param argument a reference to an object that will be provided along with the command that
     * is issued if the user selects a menu item (unless that item has an overriding argument, in
     * which case the overriding argument will be used).
     */
    public void activate (Host host, Rectangle bounds, Object argument)
    {
        setActivationArgument(argument);
        activate(host, bounds);
    }

    /**
     * Activates the radial menu, rendering a menu around the prescribed bounds. It is expected
     * that the host component will subsequently call {@link #render} if the menu invalidates the
     * region of the component occupied by the menu and requests it to repaint.
     *
     * @param host the host component within which the radial menu is displayed.
     * @param bounds the bounds of the object that was clicked to activate the menu.
     */
    public void activate (Host host, Rectangle bounds)
    {
        _host = host;
        _tbounds = bounds;
        _poptime = System.currentTimeMillis();
        _msMode = false;

        Component comp = _host.getComponent();
        _hijacker = new MouseHijacker(comp);

        // start listening to the host component
        comp.addMouseListener(this);
        comp.addMouseMotionListener(this);

        // lay ourselves out and repaint
        layout();
        repaint();
    }

    /**
     * Sets the argument that will be defaultly posted along with commands.
     */
    public void setActivationArgument (Object arg)
    {
        _argument = arg;
    }

    /**
     * Returns the argument provided to this menu when {@link #activate} was called. This will only
     * be non-null while the menu is active and only then if an argument was provided in the call
     * to {@link #activate}.
     */
    public Object getActivationArgument ()
    {
        return _argument;
    }

    /**
     * Deactivates the menu.
     */
    public void deactivate ()
    {
        if (_host != null) {
            // unwire ourselves from the host component
            Component comp = _host.getComponent();
            comp.removeMouseListener(this);
            comp.removeMouseMotionListener(this);

            // reinstate the previous mouse listeners
            if (_hijacker != null) {
                _hijacker.release();
                _hijacker = null;
            }

            // tell our host that we are no longer
            _host.menuDeactivated(this);

            // fire off a last repaint to clean up after ourselves
            repaint();
        }

        // clear out our references
        _host = null;
        _tbounds = null;
        _argument = null;

        // clear out our action listeners
        _actlist.clear();
    }

    /**
     * Renders the current configuration of this menu.
     */
    public void render (Graphics2D gfx)
    {
        Component host = _host.getComponent();

        int x = _bounds.x, y = _bounds.y;

        if (_centerLabel != null) {
            // render the centerpiece label
            _centerLabel.paint(gfx, x + _centerLabel.closedBounds.x,
                               y + _centerLabel.closedBounds.y, this);
        }

        // render each of our items in turn
        int icount = _items.size();
        for (int i = 0; i < icount; i++) {
            RadialMenuItem item = _items.get(i);
            if (!item.isIncluded(this)) {
                continue;
            }
            // we have to wait and render the active item last
            if (item != _activeItem) {
                item.render(host, this, gfx, x + item.closedBounds.x, y + item.closedBounds.y);
            }
        }

        if (_activeItem != null) {
            _activeItem.render(host, this, gfx, x + _activeItem.closedBounds.x,
                               y + _activeItem.closedBounds.y);
        }

        // render our bounds
//         gfx.setColor(Color.green);
//         gfx.draw(_bounds);
//         gfx.setColor(Color.blue);
//         gfx.draw(_tbounds);
    }

    // documentation inherited
    public void mouseDragged (MouseEvent event)
    {
        // same as moved
        mouseMoved(event);
    }

    // documentation inherited
    public void mouseMoved (MouseEvent event)
    {
        int x = event.getX(), y = event.getY();
        // translate the coords into our space
        x -= _bounds.x;
        y -= _bounds.y;
        boolean repaint = false;

        // see if we dragged out of the active item
        if (_activeItem != null) {
            // oldway: if (!_activeItem.openBounds.contains(x, y)) {
            if (!_activeItem.closedBounds.contains(x, y)) {
                _activeItem.setActive(false);
                _activeItem = null;
                repaint = true;
            }
        }

        // see if we dragged into a new menu item
        if ((_activeItem == null) &&
            (RunAnywhere.getWhen(event) > _poptime + DEBOUNCE_DELAY)) {

            int icount = _items.size();
            for (int i = 0; i < icount; i++) {
                RadialMenuItem item = _items.get(i);
                if (item.isIncluded(this) &&
                    item.closedBounds.contains(x, y)) {
                    _activeItem = item;
                    _activeItem.setActive(true);
                    repaint = true;

                    // if we got here with a mouse moved, we've definately made the decision to be
                    // in non drag mode
                    if (event.getID() == MouseEvent.MOUSE_MOVED) {
                        _msMode = true;
                    }
                    break;
                }
            }
        }

        // repaint ourselves if something changed
        if (repaint) {
            repaint();
        }
    }

    // documentation inherited
    public void mouseClicked (MouseEvent event)
    {
    }

    // documentation inherited
    public void mouseEntered (MouseEvent event)
    {
    }

    // documentation inherited
    public void mouseExited (MouseEvent event)
    {
    }

    // documentation inherited
    public void mousePressed (MouseEvent event)
    {
    }

    // documentation inherited
    public void mouseReleased (MouseEvent event)
    {
        if (_activeItem == null) {
            long when = RunAnywhere.getWhen(event);
            if (!_msMode && (when < _poptime + DRAGMODE_DELAY)) {
                // if the dragmode delay time hasn't passed, then we're going to leave the menu up
                // because the user wants it to behave like a MS Windows menu.
                _msMode = true;
                return;

            // if they've double clicked, then activate the default item
            } else if (_msMode && (when < _poptime + DOUBLECLICK_DELAY)) {
                _activeItem = _defaultItem;
            }
        }

        // deactivate the active item and post a command for it
        if (_activeItem != null) {
            // only process the selection if the item is enabled
            if (!_activeItem.isEnabled(this)) {
                return;
            }

            // if the item has an overriding argument, use that
            if (_activeItem.command != null) {
                Object itemArg = _activeItem.argument;
                Object arg = (itemArg == null) ? _argument : itemArg;

                // notify our listeners that the action is posted
                final CommandEvent evt = new CommandEvent(
                    _host.getComponent(), _activeItem.command, arg);
                _actlist.apply(new ObserverList.ObserverOp<ActionListener>() {
                    public boolean apply (ActionListener obs) {
                        obs.actionPerformed(evt);
                        return true;
                    }
                });
            }

            _activeItem.setActive(false);
            _activeItem = null;
        }

        // deactive ourselves
        deactivate();
    }

    /**
     * Lays the radial menu items out based on the currently configured bounds information.
     */
    protected void layout ()
    {
        Graphics2D gfx = (Graphics2D)_host.getComponent().getGraphics();
        Font font = gfx.getFont();
        _bounds = new Rectangle();

        // figure out which items are included
        int icount = _items.size();
        ArrayList<RadialMenuItem> items = new ArrayList<RadialMenuItem>();
        for (int i = 0; i < icount; i++) {
            RadialMenuItem item = _items.get(i);
            if (item.isIncluded(this)) {
                items.add(item);
            }
        }

        // lay out all of our menu items
        int maxwid = 0, maxhei = 0;
        icount = items.size();
        for (int i = 0; i < icount; i++) {
            RadialMenuItem item = items.get(i);
            item.layout(gfx, font);

            // track maximum menu item size
            if (item.closedBounds.width > maxwid) {
                maxwid = item.closedBounds.width;
            }
            if (item.closedBounds.height > maxhei) {
                maxhei = item.closedBounds.height;
            }
        }
        gfx.dispose();

        // use the maximum of either width or height and make a circle around that
        double radius = Math.max(_tbounds.height, _tbounds.width) / 2;

        // be sure to add a gap and space for the menu item itself
        radius += (5 + maxwid/2);

        // compute the angle between menu items (we use the diameter of the menu items as an
        // approximate measure of the distance along the circumference)
        double theta = (maxwid + 10) / radius ;

        // now position each item accordingly
        double angle = -Math.PI/2;
        for (int i = 0; i < icount; i++) {
            RadialMenuItem item = items.get(i);
            int ix = (int)(radius * Math.cos(angle));
            int iy = (int)(radius * Math.sin(angle));
            item.openBounds.x = item.closedBounds.x = ix - maxwid/2;
            item.openBounds.y = item.closedBounds.y = iy - maxhei/2;

            // move along the circle
            angle += theta;
        }

        // create and position the centerpiece label
        if (_centerIcon != null) {
            _centerLabel = new RadialLabelSausage("", _centerIcon);
            _centerLabel.layout(gfx, font);
            _centerLabel.openBounds.x = _centerLabel.closedBounds.x =
                -(_centerLabel.closedBounds.width / 2);
            _centerLabel.openBounds.y = _centerLabel.closedBounds.y =
                -(_centerLabel.closedBounds.height / 2);
        }

        // now compute the rectangle that encloses the entire menu

        // include the bounds for the centerpiece label
        if (_centerLabel != null) {
            _bounds.add(_centerLabel.openBounds);
        }

        // include the bounds for all menu items
        for (int i = 0; i < icount; i++) {
            RadialMenuItem item = items.get(i);
            // we need the open bounds rather than the closed ones
            _bounds.add(item.openBounds);
        }

        // now translate everything from the center of the target bounds to the upper left of the
        // menu bounds
        if (_centerLabel != null) {
            _centerLabel.openBounds.translate(-_bounds.x, -_bounds.y);
            _centerLabel.closedBounds.translate(-_bounds.x, -_bounds.y);
        }
        for (int i = 0; i < icount; i++) {
            RadialMenuItem item = items.get(i);
            item.openBounds.translate(-_bounds.x, -_bounds.y);
            item.closedBounds.translate(-_bounds.x, -_bounds.y);
        }

        // the origin was at the center of the encircled rectangle; we need to translate it such
        // that the origin of our bounds are in screen coordinates and at the upper left rather
        // than the center
        _bounds.x += (_tbounds.x + _tbounds.width/2);
        _bounds.y += (_tbounds.y + _tbounds.height/2);

        // now make sure the whole shebang is fully visible within the host component
        Rectangle hbounds = _host.getViewBounds();
        Point pos = SwingUtil.fitRectInRect(_bounds, hbounds);
        _bounds.setLocation(pos);
    }

    /**
     * Requests that our host component repaint the part of itself that we occupy.
     */
    protected void repaint ()
    {
        // only repaint the area that we overlap
        _host.repaintRect(_bounds.x, _bounds.y, _bounds.width+1, _bounds.height+1);
    }

    /** Our host component. */
    protected Host _host;

    /** The bounds around which we're rendering a menu. */
    protected Rectangle _tbounds;

    /** The bounds that all of our menu items occupy. */
    protected Rectangle _bounds;

    /** The argument object, which will be delivered along with a posted command when a menu item
     * is selected. */
    protected Object _argument;

    /** The centerpiece icon or null if there is none. */
    protected Icon _centerIcon;

    /** The label used to render the centerpiece icon if there is one. */
    protected RadialLabelSausage _centerLabel;

    /** Our menu items. */
    protected ArrayList<RadialMenuItem> _items = new ArrayList<RadialMenuItem>();

    /** The item that has the mouse over it presently, and the default item if we're double
     * clicked. */
    protected RadialMenuItem _activeItem, _defaultItem;

    /** The amount of time the user must hold down the mouse in order to put it in drag mode, after
     * which the first mouse-up will pop down the menu. */
    protected static final long DRAGMODE_DELAY = 1000L;

    /** The amount of time after the menu has been popped up before we accept selections. */
    protected static final long DEBOUNCE_DELAY = 200L;

    /** How quickly a second click must arrive for us to count it as a double click and not two
     * single clicks. */
    protected static final long DOUBLECLICK_DELAY = 400L;

    /** The time at which the menu was popped up. */
    protected long _poptime;

    /** Whether we're popping the menu up in microsoft mode, where the user does not need to drag
     * to the item they want to select. */
    protected boolean _msMode = false;

    /** This hijacks and holds onto the previous mouse listeners. */
    protected MouseHijacker _hijacker;

    /** Maintains a list of action listeners. */
    protected ObserverList<ActionListener> _actlist = ObserverList.newSafeInOrder();
}
