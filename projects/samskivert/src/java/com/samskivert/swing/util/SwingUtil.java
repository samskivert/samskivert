//
// $Id: SwingUtil.java,v 1.22 2003/04/04 18:55:24 mdb Exp $
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001 Michael Bayne
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

package com.samskivert.swing.util;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.Window;

import java.awt.geom.Area;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

import javax.swing.JComponent;

import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.JTextComponent;

import com.samskivert.util.SortableArrayList;
import javax.swing.JTable;
import javax.swing.table.TableModel;
import javax.swing.table.TableColumn;

/**
 * Miscellaneous useful Swing-related utility functions.
 */
public class SwingUtil
{
    /**
     * Center the given window within the screen boundaries.
     *
     * @param window the window to be centered.
     */
    public static void centerWindow (Window window)
    {
        Toolkit tk = window.getToolkit();
        Dimension ss = tk.getScreenSize();
        int width = window.getWidth(), height = window.getHeight();
        window.setBounds((ss.width-width)/2, (ss.height-height)/2,
                         width, height);
    }

    /**
     * Centers component <code>b</code> within component <code>a</code>.
     */
    public static void centerComponent (Component a, Component b)
    {
        Dimension asize = a.getSize(), bsize = b.getSize();
        b.setLocation((asize.width - bsize.width) / 2,
                      (asize.height - bsize.height) / 2);
    }

    /**
     * Draw a string centered within a rectangle.  The string is drawn
     * using the graphics context's current font and color.
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
     * Returns the most reasonable position for the specified rectangle to
     * be placed at so as to maximize its containment by the specified
     * bounding rectangle while still placing it as near its original
     * coordinates as possible.
     *
     * @param rect the rectangle to be positioned.
     * @param bounds the containing rectangle.
     */
    public static Point fitRectInRect (
        Rectangle rect, Rectangle bounds)
    {
	// make sure left edge is within bounds
	Rectangle erect = new Rectangle(rect);
	if (erect.x < bounds.x) {
	    erect.x = bounds.x;
	}

	// make sure top edge is within bounds
	if (erect.y < bounds.y) {
	    erect.y = bounds.y;
	}

	// do our best to fit entire rectangle into bounds horizontally
	if ((erect.x + erect.width) > (bounds.x + bounds.width)) {
	    erect.x = (bounds.x + bounds.width) - erect.width;
	}

	// do our best to fit entire rect into bounds vertically
	if ((erect.y + erect.height) > (bounds.y + bounds.height)) {
	    erect.y = (bounds.y + bounds.height) - erect.height;
	}

	return new Point(erect.x, erect.y);
    }

    /**
     * Position the specified rectangle as closely as possible to
     * its current position, but make sure it is within the specified
     * bounds and that it does not overlap any of the Shapes contained
     * in the avoid list.
     *
     * @param r the rectangle to attempt to position.
     * @param bounds the bounding box within which the rectangle must be
     *        positioned.
     * @param avoidShapes a collection of Shapes that must not be overlapped.
     * The collection will be destructively modified.
     *
     * @return true if the rectangle was successfully placed, given the
     * constraints, or false if the positioning failed (the rectangle will
     * be left at it's original location.
     */
    public static boolean positionRect (
        Rectangle r, Rectangle bounds, Collection avoidShapes)
    {
        Point origPos = r.getLocation();
        Comparator comp = createPointComparator(origPos);
        SortableArrayList possibles = new SortableArrayList();
        // we start things off with the passed-in point (adjusted to
        // be inside the bounds, if needed)
        possibles.add(fitRectInRect(r, bounds));

        // keep track of area that doesn't generate new possibles
        Area dead = new Area();

      CHECKPOSSIBLES:
        while (!possibles.isEmpty()) {
            r.setLocation((Point) possibles.remove(0));

            // make sure the rectangle is in the view and not over a dead area
            if ((!bounds.contains(r)) || dead.intersects(r)) {
                continue;
            }

            // see if it hits any shapes we're trying to avoid
            for (Iterator iter=avoidShapes.iterator(); iter.hasNext(); ) {
                Shape shape = (Shape) iter.next();

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
     * Create a comparator that compares against the distance from 
     * the specified point.
     *
     * Used by positionRect().
     */
    public static Comparator createPointComparator (Point origin)
    {
        final int xo = origin.x;
        final int yo = origin.y;

        return new Comparator() {
            public int compare (Object o1, Object o2)
            {
                Point p1 = (Point) o1;
                Point p2 = (Point) o2;

                int x1 = xo - p1.x;
                int y1 = yo - p1.y;
                int x2 = xo - p2.x;
                int y2 = yo - p2.y;

                // since we are dealing with positive integers, we can
                // omit the Math.sqrt() step for optimization
                int dist1 = (x1 * x1) + (y1 * y1);
                int dist2 = (x2 * x2) + (y2 * y2);

                return dist1 - dist2;
            }
        };
    }

    /**
     * Return a polygon representing the rectangle defined by the
     * specified upper left coordinate and the supplied dimensions.
     *
     * @param x the left edge of the rectangle.
     * @param y the top of the rectangle.
     * @param d the rectangle's dimensions.
     *
     * @return the bounding polygon.
     */
    public static Polygon getPolygon (int x, int y, Dimension d)
    {
	Polygon poly = new Polygon();
	poly.addPoint(x, y);
	poly.addPoint(x + d.width, y);
	poly.addPoint(x + d.width, y + d.height);
	poly.addPoint(x, y + d.height);
	return poly; 
    }

    /**
     * Enables (or disables) the specified component, <em>and all of its
     * children.</cite> A simple call to {@link Container#setEnabled}
     * does not propagate the enabled state to the children of a
     * component, which is senseless in our opinion, but was surely done
     * for some arguably good reason.
     */
    public static void setEnabled (Container comp, final boolean enabled)
    {
        applyToHierarchy(comp, new ComponentOp() {
            public void apply (Component comp)
            {
                comp.setEnabled(enabled);
            }
        });
    }

    /**
     * Apply the specified ComponentOp to the supplied component
     * and then all its descendants.
     */
    public static void applyToHierarchy (Component comp, ComponentOp op)
    {
        if (comp == null) {
            return;
        }

        op.apply(comp);
        if (comp instanceof Container) {
            Container c = (Container) comp;
            int ccount = c.getComponentCount();
            for (int ii = 0; ii < ccount; ii++) {
                applyToHierarchy(c.getComponent(ii), op);
            }
        }
    }

    /**
     * An operation that may be applied to a component.
     */
    public static interface ComponentOp
    {
        /**
         * Apply an operation to the given component.
         */
        public void apply (Component comp);
    }

    /**
     * An interface for validating the text contained within a document.
     */
    public static interface DocumentValidator
    {
        /**
         * Should return false if the text is not valid for any reason.
         */
        public boolean isValid (String text);
    }

    /**
     * An interface for transforming the text contained within a document.
     */
    public static interface DocumentTransformer
    {
        /**
         * Should transform the specified text in some way, or simply
         * return the text untransformed.
         */
        public String transform (String text);
    }

    /**
     * Set active Document helpers on the specified text component.
     * Changes will not and cannot be made (either via user inputs or
     * direct method manipulation) unless the validator says that the
     * changes are ok.
     *
     * @param validator if non-null, all changes are sent to this for approval.
     * @param transformer if non-null, is queried to change the text
     * after all changes are made.
     */
    public static void setDocumentHelpers (
        JTextComponent comp, DocumentValidator validator,
        DocumentTransformer transformer)
    {
        setDocumentHelpers(comp.getDocument(), validator, transformer);
    }

    /**
     * Set active Document helpers on the specified Document.
     * Changes will not and cannot be made (either via user inputs or
     * direct method manipulation) unless the validator says that the
     * changes are ok.
     *
     * @param validator if non-null, all changes are sent to this for approval.
     * @param transformer if non-null, is queried to change the text
     * after all changes are made.
     */
    public static void setDocumentHelpers (
        final Document doc, final DocumentValidator validator,
        final DocumentTransformer transformer)
    {
        if (!(doc instanceof AbstractDocument)) {
            throw new IllegalArgumentException(
                "Specified document cannot be filtered!");
        }

        // set up the filter.
        ((AbstractDocument) doc).setDocumentFilter(new DocumentFilter()
        {
            // documentation inherited
            public void remove (FilterBypass fb, int offset, int length)
                throws BadLocationException
            {
                if (replaceOk(offset, length, "")) {
                    fb.remove(offset, length);
                    transform(fb);
                }
            }

            // documentation inherited
            public void insertString (
                FilterBypass fb, int offset, String string, AttributeSet attr)
                throws BadLocationException
            {
                if (replaceOk(offset, 0, string)) {
                    fb.insertString(offset, string, attr);
                    transform(fb);
                }
            }

            // documentation inherited
            public void replace (
                FilterBypass fb, int offset, int length, String text,
                AttributeSet attrs)
                throws BadLocationException
            {
                if (replaceOk(offset, length, text)) {
                    fb.replace(offset, length, text, attrs);
                    transform(fb);
                }
            }

            /**
             * Convenience for remove/insert/replace to see if the
             * proposed change is valid.
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
                    throw new BadLocationException(
                        "Bad Location", offset + length);
                }
            }

            /**
             * After a remove/insert/replace has taken place, we may
             * want to transform the text in some way.
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
     * Activates anti-aliasing in the supplied graphics context.
     *
     * @return an object that should be passed to {@link
     * #restoreAntiAliasing} to restore the graphics context to its
     * original settings.
     */
    public static Object activateAntiAliasing (Graphics2D gfx)
    {
        Object oalias = gfx.getRenderingHint(
            RenderingHints.KEY_ANTIALIASING);
        gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON);
        return oalias;
    }

    /**
     * Restores anti-aliasing in the supplied graphics context to its
     * original setting.
     *
     * @param rock the results of a previous call to {@link
     * #activateAntiAliasing}.
     */
    public static void restoreAntiAliasing (Graphics2D gfx, Object rock)
    {
        gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, rock);
    }

    /**
     * Adjusts the widths and heights of the cells of the supplied table
     * to fit their contents.
     */
    public static void sizeToContents (JTable table)
    {
        TableModel model = table.getModel();
        TableColumn column = null;
        Component comp = null;
        int headerWidth = 0, cellWidth = 0, cellHeight = 0;
        int ccount = model.getColumnCount(), rcount = model.getRowCount();

        for (int cc = 0; cc < ccount; cc++) {
            column = table.getColumnModel().getColumn(cc);
            try {
                comp = column.getHeaderRenderer().
                    getTableCellRendererComponent(null, column.getHeaderValue(),
                                                  false, false, 0, 0);
                headerWidth = comp.getPreferredSize().width;
            } catch (NullPointerException e) {
                // getHeaderRenderer() this doesn't work in 1.3
            }

            for (int rr = 0; rr < rcount; rr++) {
                Object cellValue = model.getValueAt(rr, cc);
                comp = table.getDefaultRenderer(model.getColumnClass(cc)).
                    getTableCellRendererComponent(table, cellValue,
                                                  false, false, 0, cc);
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
     * Refreshes the supplied {@link JComponent} to effect a call to
     * {@link JComponent#revalidate} and {@link JComponent#repaint}, which
     * is frequently necessary in cases such as adding components to or
     * removing components from a {@link JPanel} since Swing doesn't
     * automatically invalidate things for proper re-rendering.
     */
    public static void refresh (JComponent c)
    {
        c.revalidate();
        c.repaint();
    }
}
