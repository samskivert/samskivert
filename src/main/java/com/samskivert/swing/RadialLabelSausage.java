//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.swing;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;

import javax.swing.Icon;
import javax.swing.UIManager;

/**
 * Specializes the label sausage to suit the needs of items placed within
 * a radial menu. The colors used to render the radial label sausage must
 * be configured via the {@link UIManager}. These colors are:
 *
 * <pre>
 * RadialLabelSausage.activeBorder
 * RadialLabelSausage.inactiveBorder
 * RadialLabelSausage.background
 * </pre>
 *
 * This would be configured in a custom look and feel like so:
 *
 * <pre>
 * protected void initComponentDefaults (UIDefaults table)
 * {
 *     super.initComponentDefaults(table);
 *     Object[] defaults = {
 *         "RadialLabelSausage.inactiveBorder", new ColorUIResource(0x478ABA),
 *         "RadialLabelSausage.activeBorder", new ColorUIResource(0xE0A000),
 *         "RadialLabelSausage.background", new ColorUIResource(0xD5E7E7),
 *     };
 *     table.putDefaults(defaults);
 * }
 * </pre>
 *
 * An application that is not using a custom look and feel can simply set
 * these values with {@link UIManager#put}.
 */
public class RadialLabelSausage extends LabelSausage
{
    /** The dimensions of this item when we are in our closed state as
     * well as our most recently laid out coordinates. This is only valid
     * after a call to {@link #layout}. */
    public Rectangle closedBounds = new Rectangle();

    /** The dimensions of this item when we are in open state. This is
     * only valid after a call to {@link #layout}. */
    public Rectangle openBounds = new Rectangle();

    /**
     * Constructs a radial label sausage.
     */
    public RadialLabelSausage (String label, Icon icon)
    {
        super(new Label(label), icon);
    }

    /**
     * Returns true if this menu item should be enabled when it is
     * displayed.  The default implementation always returns true.
     */
    public boolean isEnabled (RadialMenu menu)
    {
        return true;
    }

    /**
     * Sets whether the label is to be rendered as an active label.
     */
    public void setActive (boolean active)
    {
        _active = active;
    }

    /**
     * Computes the dimensions of this label based on the specified font
     * and in the specified graphics context.
     */
    public void layout (Graphics2D gfx, Font font)
    {
        _label.setFont(font);
        layout(gfx, BORDER_THICKNESS);

        openBounds.width = _size.width;
        openBounds.height = _size.height;

        // and closed up, we're just a circle
        closedBounds.height = closedBounds.width = _size.height;
    }

    @Override
    protected void drawLabel (Graphics2D gfx, int x, int y)
    {
        if (_active) {
            super.drawLabel(gfx, x, y);
        }
    }

    @Override
    protected void drawBorder (Graphics2D gfx, int x, int y)
    {
        // then around all that draw the borders
        Stroke ostroke = gfx.getStroke();
        gfx.setStroke(BORDER_STROKE);
        if (_active) {
            // draw the active border
            gfx.setColor(UIManager.getColor("RadialLabelSausage.activeBorder"));
            gfx.drawRoundRect(
                x + (BORDER_THICKNESS / 2), y + (BORDER_THICKNESS / 2),
                openBounds.width - 1 - BORDER_THICKNESS,
                openBounds.height - 1 - BORDER_THICKNESS,
                _dia - BORDER_THICKNESS, _dia - BORDER_THICKNESS);

            // draw the black outer border
            gfx.setStroke(ostroke);
            super.drawBorder(gfx, x, y);

        } else {
            // draw the inactive border
            gfx.setColor(UIManager.getColor(
                             "RadialLabelSausage.inactiveBorder"));
            gfx.drawOval(
                x + (BORDER_THICKNESS / 2), y + (BORDER_THICKNESS / 2),
                closedBounds.width - 1 - BORDER_THICKNESS,
                closedBounds.height - 1 - BORDER_THICKNESS);

            // draw the black outer border
            gfx.setStroke(ostroke);
            gfx.setColor(Color.black);
            gfx.drawOval(x, y, closedBounds.width - 1, closedBounds.height - 1);
        }
    }

    @Override
    protected void drawExtras (Graphics2D gfx, int x, int y, Object cliData)
    {
        RadialMenu menu = (RadialMenu) cliData;
        // finally, if we're dimmed out: dim us out
        if (!isEnabled(menu)) {
            Composite ocomp = gfx.getComposite();
            gfx.setComposite(DISABLED_ALPHA);
            gfx.setColor(Color.black);
            drawBase(gfx, x, y);
            gfx.setComposite(ocomp);
        }
    }

    /**
     * Draw the base circle or sausage within which all the other
     * decorations are added.
     */
    @Override
    protected void drawBase (Graphics2D gfx, int x, int y)
    {
        if (_active) {
            // render the sausage
            super.drawBase(gfx, x, y);

        } else {
            // render the circle
            gfx.fillOval(x, y, closedBounds.width - 1, closedBounds.height - 1);
        }
    }

    /**
     * Paints the radial label sausage.
     */
    protected void paint (Graphics2D gfx, int x, int y, RadialMenu menu)
    {
        paint(gfx, x, y, UIManager.getColor("RadialLabelSausage.background"), menu);
    }

    /** Indicates whether or not this label is active. */
    protected boolean _active;

    /** The thickness of the colored active/inactive border. */
    protected static final int BORDER_THICKNESS = 4;

    /** The stroke to use when drawing the selected/unselected color. */
    protected static final Stroke BORDER_STROKE = new BasicStroke(BORDER_THICKNESS);

    /** The alpha level for how much we dim an inactive menu item by. */
    protected static final Composite DISABLED_ALPHA =
        AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
}
