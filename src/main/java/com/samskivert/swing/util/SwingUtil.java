//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.swing.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.Window;

import java.awt.geom.Area;
import java.awt.geom.Point2D;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.Spring;
import javax.swing.SpringLayout;

import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.JTextComponent;

import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import com.samskivert.util.SortableArrayList;

import static com.samskivert.swing.Log.log;

/**
 * Miscellaneous useful Swing-related utility functions.
 */
public class SwingUtil
{
    /**
     * An operation that may be applied to a component.
     */
    public static interface ComponentOp
    {
        /** Apply an operation to the given component. */
        public void apply (Component comp);
    }

    /**
     * An interface for validating the text contained within a document.
     */
    public static interface DocumentValidator
    {
        /** Return false if the text is not valid for any reason. */
        public boolean isValid (String text);
    }

    /**
     * An interface for transforming the text contained within a document.
     */
    public static interface DocumentTransformer
    {
        /** Transform the specified text in some way, or simply return the text untransformed. */
        public String transform (String text);
    }

    /**
     * Center the given window within the screen boundaries.
     *
     * @param window the window to be centered.
     */
    public static void centerWindow (Window window)
    {
        Rectangle bounds;
        try {
            bounds = GraphicsEnvironment.getLocalGraphicsEnvironment().
                getDefaultScreenDevice().getDefaultConfiguration().getBounds();
        } catch (Throwable t) {
            Toolkit tk = window.getToolkit();
            Dimension ss = tk.getScreenSize();
            bounds = new Rectangle(ss);
        }

        int width = window.getWidth(), height = window.getHeight();
        window.setBounds(bounds.x + (bounds.width-width)/2, bounds.y + (bounds.height-height)/2,
                         width, height);
    }

    /**
     * Centers component <code>b</code> within component <code>a</code>.
     */
    public static void centerComponent (Component a, Component b)
    {
        Dimension asize = a.getSize(), bsize = b.getSize();
        b.setLocation((asize.width - bsize.width) / 2, (asize.height - bsize.height) / 2);
    }

    /**
     * Draw a string centered within a rectangle.  The string is drawn using the graphics context's
     * current font and color.
     *
     * @param g the graphics context.
     * @param str the string.
     * @param x the bounding x position.
     * @param y the bounding y position.
     * @param width the bounding width.
     * @param height the bounding height.
     */
    public static void drawStringCentered (
        Graphics g, String str, int x, int y, int width, int height)
    {
        FontMetrics fm = g.getFontMetrics(g.getFont());
        int xpos = x + ((width - fm.stringWidth(str)) / 2);
        int ypos = y + ((height + fm.getAscent()) / 2);
        g.drawString(str, xpos, ypos);
    }

    /**
     * Returns the most reasonable position for the specified rectangle to be placed at so as to
     * maximize its containment by the specified bounding rectangle while still placing it as near
     * its original coordinates as possible.
     *
     * @param rect the rectangle to be positioned.
     * @param bounds the containing rectangle.
     */
    public static Point fitRectInRect (Rectangle rect, Rectangle bounds)
    {
        // Guarantee that the right and bottom edges will be contained and do our best for the top
        // and left edges.
        return new Point(Math.min(bounds.x + bounds.width - rect.width,
                                  Math.max(rect.x, bounds.x)),
                         Math.min(bounds.y + bounds.height - rect.height,
                                  Math.max(rect.y, bounds.y)));
    }

    /**
     * Position the specified rectangle as closely as possible to its current position, but make
     * sure it is within the specified bounds and that it does not overlap any of the Shapes
     * contained in the avoid list.
     *
     * @param r the rectangle to attempt to position.
     * @param bounds the bounding box within which the rectangle must be positioned.
     * @param avoidShapes a collection of Shapes that must not be overlapped.  The collection will
     * be destructively modified.
     *
     * @return true if the rectangle was successfully placed, given the constraints, or false if
     * the positioning failed (the rectangle will be left at it's original location.
     */
    public static boolean positionRect (
        Rectangle r, Rectangle bounds, Collection<? extends Shape> avoidShapes)
    {
        Point origPos = r.getLocation();
        Comparator<Point> comp = createPointComparator(origPos);
        SortableArrayList<Point> possibles = new SortableArrayList<Point>();
        // start things off with the passed-in point (adjusted to be inside the bounds, if needed)
        possibles.add(fitRectInRect(r, bounds));

        // keep track of area that doesn't generate new possibles
        Area dead = new Area();

      CHECKPOSSIBLES:
        while (!possibles.isEmpty()) {
            r.setLocation(possibles.remove(0));

            // make sure the rectangle is in the view and not over a dead area
            if ((!bounds.contains(r)) || dead.intersects(r)) {
                continue;
            }

            // see if it hits any shapes we're trying to avoid
            for (Iterator<? extends Shape> iter = avoidShapes.iterator(); iter.hasNext(); ) {
                Shape shape = iter.next();
                if (shape.intersects(r)) {
                    // remove that shape from our avoid list
                    iter.remove();
                    // but add it to our dead area
                    dead.add(new Area(shape));

                    // add 4 new possible points, each pushed in one direction
                    Rectangle pusher = shape.getBounds();
                    possibles.add(new Point(pusher.x - r.width, r.y));
                    possibles.add(new Point(r.x, pusher.y - r.height));
                    possibles.add(new Point(pusher.x + pusher.width, r.y));
                    possibles.add(new Point(r.x, pusher.y + pusher.height));

                    // re-sort the list
                    possibles.sort(comp);
                    continue CHECKPOSSIBLES;
                }
            }

            // hey! if we got here, then it worked!
            return true;
        }

        // we never found a match, move the rectangle back
        r.setLocation(origPos);
        return false;
    }

    /**
     * Create a comparator that compares against the distance from the specified point.
     *
     * Note: The comparator will continue to sort by distance from the origin point, even if the
     * origin point's coordinates are modified after the comparator is created.
     *
     * Used by positionRect().
     */
    public static <P extends Point2D> Comparator<P> createPointComparator (final P origin)
    {
        return new Comparator<P>() {
            public int compare (P p1, P p2)
            {
                double dist1 = origin.distance(p1);
                double dist2 = origin.distance(p2);
                return (dist1 > dist2) ? 1 : ((dist1 < dist2) ? -1 : 0);
            }
        };
    }

    /**
     * Enables (or disables) the specified component, <em>and all of its children.</em> A simple
     * call to {@link Container#setEnabled} does not propagate the enabled state to the children of
     * a component, which is senseless in our opinion, but was surely done for some arguably good
     * reason.
     */
    public static void setEnabled (Container comp, final boolean enabled)
    {
        applyToHierarchy(comp, new ComponentOp() {
            public void apply (Component comp) {
                comp.setEnabled(enabled);
            }
        });
    }

    /**
     * Set the opacity on the specified component, <em>and all of its children.</em>
     */
    public static void setOpaque (JComponent comp, final boolean opaque)
    {
        applyToHierarchy(comp, new ComponentOp() {
            public void apply (Component comp) {
                if (comp instanceof JComponent) {
                    ((JComponent) comp).setOpaque(opaque);
                }
            }
        });
    }

    /**
     * Apply the specified ComponentOp to the supplied component and then all its descendants.
     */
    public static void applyToHierarchy (Component comp, ComponentOp op)
    {
        applyToHierarchy(comp, Integer.MAX_VALUE, op);
    }

    /**
     * Apply the specified ComponentOp to the supplied component and then all its descendants, up
     * to the specified maximum depth.
     */
    public static void applyToHierarchy (Component comp, int depth, ComponentOp op)
    {
        if (comp == null) {
            return;
        }

        op.apply(comp);
        if (comp instanceof Container && --depth >= 0) {
            Container c = (Container) comp;
            int ccount = c.getComponentCount();
            for (int ii = 0; ii < ccount; ii++) {
                applyToHierarchy(c.getComponent(ii), depth, op);
            }
        }
    }

    /**
     * Set active Document helpers on the specified text component.  Changes will not and cannot be
     * made (either via user inputs or direct method manipulation) unless the validator says that
     * the changes are ok.
     *
     * @param validator if non-null, all changes are sent to this for approval.
     * @param transformer if non-null, is queried to change the text after all changes are made.
     */
    public static void setDocumentHelpers (JTextComponent comp, DocumentValidator validator,
                                           DocumentTransformer transformer)
    {
        setDocumentHelpers(comp.getDocument(), validator, transformer);
    }

    /**
     * Set active Document helpers on the specified Document.  Changes will not and cannot be made
     * (either via user inputs or direct method manipulation) unless the validator says that the
     * changes are ok.
     *
     * @param validator if non-null, all changes are sent to this for approval.
     * @param transformer if non-null, is queried to change the text after all changes are made.
     */
    public static void setDocumentHelpers (final Document doc, final DocumentValidator validator,
                                           final DocumentTransformer transformer)
    {
        if (!(doc instanceof AbstractDocument)) {
            throw new IllegalArgumentException("Specified document cannot be filtered!");
        }

        // set up the filter.
        ((AbstractDocument) doc).setDocumentFilter(new DocumentFilter() {
            @Override public void remove (FilterBypass fb, int offset, int length)
                throws BadLocationException
            {
                if (replaceOk(offset, length, "")) {
                    fb.remove(offset, length);
                    transform(fb);
                }
            }

            @Override public void insertString (
                FilterBypass fb, int offset, String string, AttributeSet attr)
                throws BadLocationException
            {
                if (replaceOk(offset, 0, string)) {
                    fb.insertString(offset, string, attr);
                    transform(fb);
                }
            }

            @Override public void replace (
                FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException
            {
                if (replaceOk(offset, length, text)) {
                    fb.replace(offset, length, text, attrs);
                    transform(fb);
                }
            }

            /**
             * Convenience for remove/insert/replace to see if the proposed change is valid.
             */
            protected boolean replaceOk (int offset, int length, String text)
                throws BadLocationException
            {
                if (validator == null) {
                    return true; // everything's ok
                }
                try {
                    String current = doc.getText(0, doc.getLength());
                    String potential = current.substring(0, offset) +
                        text + current.substring(offset + length);
                    // validate the potential text.
                    return validator.isValid(potential);

                } catch (IndexOutOfBoundsException ioobe) {
                    throw new BadLocationException("Bad Location", offset + length);
                }
            }

            /**
             * After a remove/insert/replace has taken place, we may want to transform the text in
             * some way.
             */
            protected void transform (FilterBypass fb)
            {
                if (transformer == null) {
                    return;
                }

                try {
                    String text = doc.getText(0, doc.getLength());
                    String xform = transformer.transform(text);
                    if (!text.equals(xform)) {
                        fb.replace(0, text.length(), xform, null);
                    }
                } catch (BadLocationException ble) {
                    // oh well.
                }
            }
        });
    }

    /**
     * Activates anti-aliasing in the supplied graphics context on both text and 2D drawing
     * primitives.
     *
     * @return an object that should be passed to {@link #restoreAntiAliasing} to restore the
     * graphics context to its original settings.
     */
    public static Object activateAntiAliasing (Graphics2D gfx)
    {
        RenderingHints ohints = gfx.getRenderingHints(), nhints = new RenderingHints(null);
        nhints.add(ohints);
        nhints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        nhints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        gfx.setRenderingHints(nhints);
        return ohints;
    }

    /**
     * Restores anti-aliasing in the supplied graphics context to its original setting.
     *
     * @param rock the results of a previous call to {@link #activateAntiAliasing} or null, in
     * which case this method will NOOP. This alleviates every caller having to conditionally avoid
     * calling restore if they chose not to activate earlier.
     */
    public static void restoreAntiAliasing (Graphics2D gfx, Object rock)
    {
        if (rock != null) {
            gfx.setRenderingHints((RenderingHints)rock);
        }
    }

    /**
     * Returns true if anti-aliasing is desired by default. This currently checks the value of the
     * <code>swing.aatext</code> property, but will someday switch to using Java Desktop Properties
     * which in theory get their values from OS preferences.
     */
    public static boolean getDefaultTextAntialiasing ()
    {
        return _defaultTextAntialiasing;
    }

    /**
     * Adjusts the widths and heights of the cells of the supplied table to fit their contents.
     */
    public static void sizeToContents (JTable table)
    {
        TableModel model = table.getModel();
        TableColumn column = null;
        Component comp = null;
        int ccount = table.getColumnModel().getColumnCount(),
            rcount = model.getRowCount(), cellHeight = 0;

        for (int cc = 0; cc < ccount; cc++) {
            int headerWidth = 0, cellWidth = 0;
            column = table.getColumnModel().getColumn(cc);
            try {
                comp = column.getHeaderRenderer().getTableCellRendererComponent(
                    null, column.getHeaderValue(), false, false, 0, 0);
                headerWidth = comp.getPreferredSize().width;
            } catch (NullPointerException e) {
                // getHeaderRenderer() this doesn't work in 1.3
            }

            for (int rr = 0; rr < rcount; rr++) {
                Object cellValue = model.getValueAt(rr, cc);
                comp = table.getDefaultRenderer(model.getColumnClass(cc)).
                    getTableCellRendererComponent(table, cellValue, false, false, 0, cc);
                Dimension psize = comp.getPreferredSize();
                cellWidth = Math.max(psize.width, cellWidth);
                cellHeight = Math.max(psize.height, cellHeight);
            }
            column.setPreferredWidth(Math.max(headerWidth, cellWidth));
        }

        if (cellHeight > 0) {
            table.setRowHeight(cellHeight);
        }
    }

    /**
     * Refreshes the supplied {@link JComponent} to effect a call to {@link JComponent#revalidate}
     * and {@link JComponent#repaint}, which is frequently necessary in cases such as adding
     * components to or removing components from a {@link JPanel} since Swing doesn't automatically
     * invalidate things for proper re-rendering.
     */
    public static void refresh (JComponent c)
    {
        c.revalidate();
        c.repaint();
    }

    /**
     * Create a custom cursor out of the specified image, putting the hotspot in the exact center
     * of the created cursor.
     */
    public static Cursor createImageCursor (Image img)
    {
        return createImageCursor(img, null);
    }

    /**
     * Create a custom cursor out of the specified image, with the specified hotspot.
     */
    public static Cursor createImageCursor (Image img, Point hotspot)
    {
        Toolkit tk = Toolkit.getDefaultToolkit();

        // for now, just report the cursor restrictions, then blindly create
        int w = img.getWidth(null);
        int h = img.getHeight(null);
        Dimension d = tk.getBestCursorSize(w, h);
//         int colors = tk.getMaximumCursorColors();
//         Log.debug("Creating custom cursor [desiredSize=" + w + "x" + h +
//                   ", bestSize=" + d.width + "x" + d.height +
//                   ", maxcolors=" + colors + "].");

        // if the passed-in image is smaller, pad it with transparent pixels and use it anyway.
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
     * Adds a one pixel border of random color to this and all panels contained in this panel's
     * child hierarchy.
     */
    public static void addDebugBorders (JPanel panel)
    {
        Color bcolor = new Color(_rando.nextInt(256), _rando.nextInt(256), _rando.nextInt(256));
        panel.setBorder(BorderFactory.createLineBorder(bcolor));

        for (int ii = 0; ii < panel.getComponentCount(); ii++) {
            Object child = panel.getComponent(ii);
            if (child instanceof JPanel) {
                addDebugBorders((JPanel)child);
            }
        }
    }

    /**
     * Sets the frame's icons. Unfortunately, the ability to pass multiple icons so the OS can
     * choose the most size-appropriate one was added in 1.6; before that, you can only set one
     * icon.
     *
     * This method attempts to find and use setIconImages, but if it can't, sets the frame's icon
     * to the first image in the list passed in.
     */
    public static void setFrameIcons (Frame frame, List<? extends Image> icons)
    {
        try {
            Method m = frame.getClass().getMethod("setIconImages", List.class);
            m.invoke(frame, icons);
            return;
        } catch (SecurityException e) {
            // Fine, fine, no reflection for us
        } catch (NoSuchMethodException e) {
            // This is fine, we must be on a pre-1.6 JVM
        } catch (Exception e) {
            // Something else went awry? Log it
            log.warning("Error setting frame icons", "frame", frame, "icons", icons, "e", e);
        }

        // We couldn't find it, couldn't reflect, or something.
        // Just use whichever's at the top of the list
        frame.setIconImage(icons.get(0));
    }

    /**
     * Aligns the first <code>rows</code> * <code>cols</code> components of <code>parent</code> in
     * a grid. Each component in a column is as wide as the maximum preferred width of the
     * components in that column; height is similarly determined for each row. The parent is made
     * just big enough to fit them all. The components should be already added to the parent in
     * row-major order.
     *
     * @param parent the container component; must be configured with a {@link SpringLayout} prior
     * to calling this method.
     * @param rows number of rows.
     * @param cols number of columns.
     * @param initialX x location at which to start the grid.
     * @param initialY y location at which to start the grid.
     * @param xPad x padding between cells.
     * @param yPad y padding between cells.
     */
    public static void makeCompactGrid (Container parent, int rows, int cols,
                                        int initialX, int initialY, int xPad, int yPad)
    {
        SpringLayout layout = (SpringLayout)parent.getLayout();

        // align all cells in each column and make them the same width
        Spring x = Spring.constant(initialX);
        for (int c = 0; c < cols; c++) {
            Spring width = Spring.constant(0);
            for (int r = 0; r < rows; r++) {
                width = Spring.max(width, getConstraintsForCell(r, c, parent, cols).getWidth());
            }
            for (int r = 0; r < rows; r++) {
                SpringLayout.Constraints constraints = getConstraintsForCell(r, c, parent, cols);
                constraints.setX(x);
                constraints.setWidth(width);
            }
            x = Spring.sum(x, Spring.sum(width, Spring.constant(xPad)));
        }

        // align all cells in each row and make them the same height
        Spring y = Spring.constant(initialY);
        for (int r = 0; r < rows; r++) {
            Spring height = Spring.constant(0);
            for (int c = 0; c < cols; c++) {
                height = Spring.max(height, getConstraintsForCell(r, c, parent, cols).getHeight());
            }
            for (int c = 0; c < cols; c++) {
                SpringLayout.Constraints constraints = getConstraintsForCell(r, c, parent, cols);
                constraints.setY(y);
                constraints.setHeight(height);
            }
            y = Spring.sum(y, Spring.sum(height, Spring.constant(yPad)));
        }

        // set the parent's size
        SpringLayout.Constraints pcons = layout.getConstraints(parent);
        pcons.setConstraint(SpringLayout.SOUTH, y);
        pcons.setConstraint(SpringLayout.EAST, x);
    }

    /* Used by {@link #makeCompactGrid}. */
    protected static SpringLayout.Constraints getConstraintsForCell (
        int row, int col, Container parent, int cols)
    {
        SpringLayout layout = (SpringLayout)parent.getLayout();
        Component c = parent.getComponent(row * cols + col);
        return layout.getConstraints(c);
    }

    /** Used by {@link #addDebugBorders}. */
    protected static final Random _rando = new Random();

    /** Used by {@link #getDefaultTextAntialiasing}. */
    protected static boolean _defaultTextAntialiasing;
    static {
        try {
            _defaultTextAntialiasing = Boolean.getBoolean("swing.aatext");
        } catch (Exception e) {
            // security exception due to running in a sandbox, no problem
        }
    }
}
