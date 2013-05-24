//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import java.awt.font.TextLayout;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;

import java.awt.geom.Rectangle2D;

import java.text.AttributedString;
import java.text.AttributedCharacterIterator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SwingConstants;

import com.samskivert.util.RunAnywhere;
import com.samskivert.util.StringUtil;
import com.samskivert.util.Tuple;

import com.samskivert.swing.util.SwingUtil;

import static com.samskivert.swing.Log.log;

/**
 * The label is a multipurpose text display mechanism that can display small amounts of text
 * wrapped to fit into a variety of constrained spaces. It can be requested to conform to a
 * particular width or height and will expand into the other dimension in order to accomodate the
 * text at hand. It is not a component, but is intended for use by components and other more
 * heavyweight entities.
 */
public class Label implements SwingConstants, LabelStyleConstants
{
    /** The pattern used to mark the start/end of color blocks. */
    public static final Pattern COLOR_PATTERN = Pattern.compile("#([Xx]|[0-9A-Fa-f]{6}+)");

    /**
     * Filter out any color tags from the specified text.
     */
    public static String filterColors (String txt)
    {
        if (txt == null) return null;
        return COLOR_PATTERN.matcher(txt).replaceAll("");
    }

    /**
     * Escape any special tags so that they won't be interpreted by the label.
     */
    public static String escapeColors (String txt)
    {
        if (txt == null) return null;
        return COLOR_PATTERN.matcher(txt).replaceAll("#''$1");
    }

    /**
     * Un-escape special tags so that they again look correct. Called by rendering components that
     * do not understand the color tags to filter colors and unescape any escaped colors.
     */
    public static String unescapeColors (String txt)
    {
        return unescapeColors(filterColors(txt), true);
    }

    /**
     * Un-escape escaped tags so that they look as the users intended.
     */
    private static String unescapeColors (String txt, boolean restore)
    {
        if (txt == null) return null;
        String prefix = restore ? "#" : "%";
        return ESCAPED_PATTERN.matcher(txt).replaceAll(prefix + "$1");
    }

    /**
     * Constructs a blank label.
     */
    public Label ()
    {
        this("");
    }

    /**
     * Constructs a label with the supplied text.
     */
    public Label (String text)
    {
        this(text, null, null);
    }

    /**
     * Constructs a label with the supplied text and configuration parameters.
     */
    public Label (String text, Color textColor, Font font)
    {
        this(text, NORMAL, textColor, null, font);
    }

    /**
     * Constructs a label with the supplied text and configuration parameters.
     */
    public Label (String text, int style, Color textColor, Color altColor, Font font)
    {
        setText(text);
        setStyle(style);
        setTextColor(textColor);
        setAlternateColor(altColor);
        setFont(font);
    }

    /**
     * Returns the text displayed by this label.
     */
    public String getText ()
    {
        return _text;
    }

    /**
     * Sets the text to be displayed by this label.
     *
     * <p> This should be followed by a call to {@link #layout} before a call is made to {@link
     * #render} as this method invalidates the layout information.
     *
     * @return true if the text changed as a result of being set, false if the label was already
     * displaying the requested text.
     */
    public boolean setText (String text)
    {
        // the Java text stuff freaks out in a variety of ways if it is asked to deal with the
        // empty string, so we fake blank labels by just using a space
        if (StringUtil.isBlank(text)) {
            text = " ";
        }

        // if there is no change then avoid doing anything
        if (text.equals((_rawText == null) ? _text : _rawText)) {
            return false;
        }

        // _text should contain the text without any tags
        _text = filterColors(text);
        // _rawText will be null if there are no tags
        _rawText = text.equals(_text) ? null : text;

        // if what we were passed contains escaped color tags, unescape them
        _text = unescapeColors(_text, true);
        if (_rawText != null) {
            _rawText = unescapeColors(_rawText, false);
        }

        invalidate("setText");
        return true;
    }

    /**
     * Sets the font to be used by this label. If the font is not set, the current font of the
     * graphics context will be used.
     *
     * <p> This should be followed by a call to {@link #layout} before a call is made to {@link
     * #render} as this method invalidates the layout information.
     */
    public void setFont (Font font)
    {
        _font = font;
        invalidate("setFont");
    }

    /**
     * Returns the color used to render the text.
     */
    public Color getTextColor ()
    {
        return _textColor;
    }

    /**
     * Sets the color used to render the text.  Setting the text color to <code>null</code> will
     * render the label in the graphics context color (which is the default).
     */
    public void setTextColor (Color color)
    {
        _textColor = color;
    }

    /**
     * Returns the alternate color used to render the text's outline or shadow, if any.
     */
    public Color getAlternateColor ()
    {
        return _alternateColor;
    }

    /**
     * Instructs the label to render the text with the specified alternate color when
     * rendering. The text itself will be rendered in whatever color is currently set in the
     * graphics context, but the outline or shadow (if any) will always be in the specified color.
     */
    public void setAlternateColor (Color color)
    {
        _alternateColor = color;
    }

    /**
     * Returns the alignment of the text within the label.
     */
    public int getAlignment ()
    {
        return _align;
    }

    /**
     * Sets the alignment of the text within the label to either {@link SwingConstants#LEFT},
     * {@link SwingConstants#RIGHT}, or {@link SwingConstants#CENTER}. The default alignment is
     * selected to be appropriate for the locale of the text being rendered.
     *
     * <p> This should be followed by a call to {@link #layout} before a call is made to {@link
     * #render} as this method invalidates the layout information.
     */
    public void setAlignment (int align)
    {
        _align = align;
    }

    /**
     * Sets the style of the text within the label to one of the styles defined in {@link
     * LabelStyleConstants}. Some styles can be combined together into a mask,
     * ie. <code>BOLD|UNDERLINE</code>.
     *
     * <p> This should be followed by a call to {@link #layout} before a call is made to {@link
     * #render} as this method invalidates the layout information.
     */
    public void setStyle (int style)
    {
        _style = style;
        invalidate("setStyle");
    }

    /**
     * Instructs the label to attempt to achieve a balance between width and height that
     * approximates the golden ratio (width ~1.618 times height).
     *
     * <p> This should be followed by a call to {@link #layout} before a call is made to {@link
     * #render} as this method invalidates the layout information.
     */
    public void setGoldenLayout ()
    {
        // use -1 as an indicator that we should be golden
        _constraints.width = -1;
        _constraints.height = -1;
        invalidate("setGoldenLayout");
    }

    /**
     * Sets the target width for this label. Text will be wrapped to fit into this width, forcibly
     * breaking words on character boundaries if a single word is too long to fit into the target
     * width. Calling this method will annul any previously established target height as we must
     * have one degree of freedom in which to maneuver.
     *
     * <p> This should be followed by a call to {@link #layout} before a call is made to {@link
     * #render} as this method invalidates the layout information.
     */
    public void setTargetWidth (int targetWidth)
    {
        if (targetWidth <= 0) {
            throw new IllegalArgumentException(
                "Invalid target width '" + targetWidth + "'");
        }
        _constraints.width = targetWidth;
        _constraints.height = 0;
        invalidate("setTargetWidth");
    }

    /**
     * Sets the target height for this label. A simple algorithm will be used to balance the width
     * of the text in order that there are only as many lines of text as fit into the target
     * height. If rendering the label as a single line of text causes it to be taller than the
     * target height, we simply render ourselves anyway. Calling this method will annul any
     * previously established target width as we must have one degree of freedom in which to
     * maneuver.
     *
     * <p> This should be followed by a call to {@link #layout} before a call is made to {@link
     * #render} as this method invalidates the layout information.
     */
    public void setTargetHeight (int targetHeight)
    {
        if (targetHeight <= 0) {
            throw new IllegalArgumentException(
                "Invalid target height '" + targetHeight + "'");
        }
        _constraints.width = 0;
        _constraints.height = targetHeight;
        invalidate("setTargetHeight");
    }

    /**
     * Clears out previously configured target dimensions for this label.
     */
    public void clearTargetDimens ()
    {
        _constraints.width = 0;
        _constraints.height = 0;
    }

    /**
     * Returns the number of lines used by this label.
     */
    public int getLineCount ()
    {
        return _layouts.length;
    }

    /**
     * Returns our computed dimensions. Only valid after a call to {@link #layout}.
     */
    public Dimension getSize ()
    {
        return _size;
    }

    /**
     * Returns true if this label has been laid out, false if not.
     */
    public boolean isLaidOut ()
    {
        return (_layouts != null);
    }

    /**
     * Calls {@link #layout(Graphics2D)} with the graphics context for the given component.
     */
    public void layout (Component comp)
    {
        Graphics2D gfx = (Graphics2D)comp.getGraphics();
        if (gfx != null) {
            layout(gfx);
            gfx.dispose();
        }
    }

    /**
     * Requests that this label lay out its text, obtaining information from the supplied graphics
     * context to do so. It is expected that the label will be subsequently rendered in the same
     * graphics context or at least one that is configured very similarly. If not, wackiness may
     * ensue.
     */
    public void layout (Graphics2D gfx)
    {
        // if text antialiasing is enabled by default, honor that setting
        Object oalias = SwingUtil.getDefaultTextAntialiasing() ?
            SwingUtil.activateAntiAliasing(gfx) : null;

        // now we can get our font render context (antialias settings are part of our context)
        FontRenderContext frc = gfx.getFontRenderContext();
        List<Tuple<TextLayout,Rectangle2D>> layouts = null;

        // if we have a target height, do some processing and convert that into a target width
        if (_constraints.height > 0 || _constraints.width == -1) {
            int targetHeight = _constraints.height;

            // if we're approximating the golden ratio, target a height that gets us near that
            // ratio, then we can err on the side of being a bit wider which is generally nicer
            // than being taller (for those of us that don't speak verticall written languages,
            // anyway)
            if (_constraints.width == -1) {
                TextLayout layout = new TextLayout(textIterator(gfx), frc);
                Rectangle2D bounds = getBounds(layout);

                int lines = 1;
                double width = getWidth(bounds)/lines;
                double height = getHeight(layout)*lines;
                double delta = Math.abs(width/height - GOLDEN_RATIO);

                do {
                    width = getWidth(bounds) / (lines+1);
                    double nheight = getHeight(layout) * (lines+1);
                    double ndelta = Math.abs(width/nheight - GOLDEN_RATIO);
                    if (delta <= ndelta) {
                        break;
                    }
                    delta = ndelta;
                    height = nheight;
                } while (++lines < 200); // cap ourselves at 200 lines

                targetHeight = (int)Math.ceil(height);
            }

            TextLayout layout = new TextLayout(textIterator(gfx), frc);
            Rectangle2D bounds = getBounds(layout);
            int lines = Math.round(targetHeight / getHeight(layout));
            if (lines > 1) {
                int targetWidth = (int)Math.round(getWidth(bounds) / lines);

                // attempt to lay the text out in the specified width, incrementing by 10% each
                // time; limit our attempts to 10 expansions to avoid infinite loops if something
                // is fucked
                for (int i = 0; i < 10; i++) {
                    LineBreakMeasurer measurer = new LineBreakMeasurer(textIterator(gfx), frc);
                    layouts = computeLines(measurer, targetWidth, _size, true);
                    if ((layouts != null) && (layouts.size() <= lines)) {
                        break;
                    }
                    targetWidth = (int)Math.round(targetWidth * 1.1);
                }
            }

        } else if (_constraints.width > 0) {
            LineBreakMeasurer measurer =
                new LineBreakMeasurer(textIterator(gfx), frc);
            layouts = computeLines(measurer, _constraints.width, _size, false);
        }

        // if no constraint, or our constraining height puts us on one line then layout on one line
        // and call it good
        if (layouts == null) {
            TextLayout layout = new TextLayout(textIterator(gfx), frc);
            Rectangle2D bounds = getBounds(layout);
            // for some reason JDK1.3 on Linux chokes on setSize(double,double)
            _size.setSize(Math.ceil(getWidth(bounds)), Math.ceil(getHeight(layout)));
            layouts = new ArrayList<Tuple<TextLayout,Rectangle2D>>();
            layouts.add(new Tuple<TextLayout,Rectangle2D>(layout, bounds));
        }

        // create our layouts array
        int lcount = layouts.size();
        _layouts = new TextLayout[lcount];
        _lbounds = new Rectangle2D[lcount];
        _leaders = new float[lcount];
        for (int ii = 0; ii < lcount; ii++) {
            Tuple<TextLayout,Rectangle2D> tup = layouts.get(ii);
            _layouts[ii] = tup.left;
            _lbounds[ii] = tup.right;
            // account for potential leaders
            if (_lbounds[ii].getX() < 0) {
                _leaders[ii] = (float)-_lbounds[ii].getX();
            }
        }

        // finally restore our antialiasing state
        SwingUtil.restoreAntiAliasing(gfx, oalias);
    }

    /**
     * Computes the lines of text for this label given the specified target width. The overall size
     * of the computed lines is stored into the <code>size</code> parameter.
     *
     * @return an {@link List} or null if <code>keepWordsWhole</code> was true and the lines could
     * not be layed out in the target width.
     */
    protected List<Tuple<TextLayout,Rectangle2D>> computeLines (
        LineBreakMeasurer measurer, int targetWidth, Dimension size, boolean keepWordsWhole)
    {
        // start with a size of zero
        double width = 0, height = 0;
        List<Tuple<TextLayout,Rectangle2D>> layouts = new ArrayList<Tuple<TextLayout,Rectangle2D>>();

        try {
            // obtain our new dimensions by using a line break iterator to lay out our text one
            // line at a time
            TextLayout layout;
            int lastposition = _text.length();
            while (true) {
                int nextret = _text.indexOf('\n', measurer.getPosition() + 1);
                if (nextret == -1) {
                    nextret = lastposition;
                }
                layout = measurer.nextLayout(targetWidth, nextret, keepWordsWhole);
                if (layout == null) {
                    break;
                }
                Rectangle2D bounds = getBounds(layout);
                width = Math.max(width, getWidth(bounds));
                height += getHeight(layout);
                layouts.add(new Tuple<TextLayout,Rectangle2D>(layout, bounds));
            }

            // fill in the computed size; for some reason JDK1.3 on Linux chokes on
            // setSize(double,double)
            size.setSize(Math.ceil(width), Math.ceil(height));

            // this can only happen if keepWordsWhole is true
            if (measurer.getPosition() < lastposition) {
                return null;
            }

        } catch (Throwable t) {
            log.warning("Label layout failed", "text", _text, t);
        }

        return layouts;
    }

    /**
     * Renders the layout at the specified position in the supplied graphics context.
     */
    public void render (Graphics2D gfx, float x, float y)
    {
        // nothing to do if we haven't been laid out
        if (_layouts == null) {
            log.warning(hashCode() + " Unlaid-out label asked to render", "text", _text
                        /*, "last", _invalidator */);
            return;
        }

        Color old = gfx.getColor();
        if (_textColor != null) {
            gfx.setColor(_textColor);
        }

        // We're going to do a couple things differently if we're antialiased
        RenderingHints hints = gfx.getRenderingHints();
        boolean antialiased = hints.containsKey(RenderingHints.KEY_ANTIALIASING) ||
            hints.containsKey(RenderingHints.KEY_TEXT_ANTIALIASING);

        // render our text
        for (int i = 0; i < _layouts.length; i++) {
            TextLayout layout = _layouts[i];
            Rectangle2D lbounds = _lbounds[i];
            y += layout.getAscent();

            float extra = (float)Math.floor(_size.width - getWidth(lbounds));
            float rx;
            switch (_align) {
            case -1: rx = x + (layout.isLeftToRight() ? 0 : extra); break;
            default:
            case LEFT: rx = x; break;
            case RIGHT: rx = x + extra; break;
            case CENTER: rx = x + extra/2; break;
            }

            // shift over any lines that start with a font that extends into negative x-land
            rx += _leaders[i];

//             System.out.println(i + " x: " + x + " y: " + y + " rx: " + rx + " a: " + _align +
//                                " width: " + _size.width + " lx: " + lbounds.getX() +
//                                " lwidth: " + getWidth(lbounds) + " extra: " + extra);

            Color textColor;

            if ((_style & OUTLINE) != 0) {
                // render the outline using the hacky, but much nicer than using "real" outlines
                // (via TextLayout.getOutline), method
                textColor = gfx.getColor();
                gfx.setColor(_alternateColor);
                _mainDraw = false;
                if (antialiased) {
                    // We need to fill in the actual spot we'll be drawing on top of if antialiased
                    layout.draw(gfx, rx + 1, y + 1);
                }
                layout.draw(gfx, rx, y);
                layout.draw(gfx, rx, y + 1);
                layout.draw(gfx, rx, y + 2);
                layout.draw(gfx, rx + 1, y);
                layout.draw(gfx, rx + 1, y + 2);
                layout.draw(gfx, rx + 2, y);
                layout.draw(gfx, rx + 2, y + 1);
                layout.draw(gfx, rx + 2, y + 2);
                _mainDraw = true;
                gfx.setColor(textColor);
                layout.draw(gfx, rx + 1, y + 1);
                if (antialiased) {
                    // If antialiased, draw a second time to make sure our letters are nice and
                    // solid looking on top of the outline
                    layout.draw(gfx, rx + 1, y + 1);
                }

            } else if ((_style & SHADOW) != 0) {
                textColor = gfx.getColor();
                gfx.setColor(_alternateColor);
                _mainDraw = false;
                layout.draw(gfx, rx, y + 1);
                _mainDraw = true;
                gfx.setColor(textColor);
                layout.draw(gfx, rx + 1, y);

            } else if ((_style & BOLD) != 0) {
                _mainDraw = false;
                layout.draw(gfx, rx, y);
                _mainDraw = true;
                layout.draw(gfx, rx + 1, y);

            } else {
                layout.draw(gfx, rx, y);
            }

            y += layout.getDescent() + layout.getLeading();
        }

        gfx.setColor(old);
    }

    /**
     * Constructs an attributed character iterator with our text and the appropriate font.
     */
    protected AttributedCharacterIterator textIterator (Graphics2D gfx)
    {
        // first set up any attributes that apply to the entire text
        Font font = (_font == null) ? gfx.getFont() : _font;
        HashMap<TextAttribute,Object> map = new HashMap<TextAttribute,Object>();
        map.put(TextAttribute.FONT, font);
        if ((_style & UNDERLINE) != 0) {
            map.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_LOW_ONE_PIXEL);
        }

        AttributedString text = new AttributedString(_text, map);
        addAttributes(text);
        return text.getIterator();
    }

    /**
     * Add any attributes to the text.
     */
    protected void addAttributes (AttributedString text)
    {
        // add any color attributes for specific segments
        if (_rawText != null) {
            Matcher m = COLOR_PATTERN.matcher(_rawText);
            int startSeg = 0, endSeg = 0;
            Color lastColor = null;
            while (m.find()) {
                // color the segment just passed
                endSeg += m.start();
                if (lastColor != null) {
                    text.addAttribute(TextAttribute.FOREGROUND, lastColor, startSeg, endSeg);
                }

                // parse the tag: start or end a color
                String group = m.group(1);
                if ("x".equalsIgnoreCase(group)) {
                    lastColor = null;
                } else {
                    lastColor = new Color(Integer.parseInt(group, 16));
                }

                // prepare for the next segment
                startSeg = endSeg;
                // Subtract the end of the segment from endSeg so that when we add the start of the
                // next match we have actually added the length of the characters in between.
                endSeg -= m.end();
            }
            // apply any final color to the tail segment
            if (lastColor != null) {
                text.addAttribute(TextAttribute.FOREGROUND, lastColor, startSeg, _text.length());
            }
        }
    }

    /**
     * Computes the total width of a {@link TextLayout} given bounds returned from a call to {@link
     * TextLayout#getBounds}.
     */
    protected double getWidth (Rectangle2D laybounds)
    {
        double width = Math.max(laybounds.getX(), 0) + laybounds.getWidth();
        if ((_style & OUTLINE) != 0) {
            width += 2;
        } else if ((_style & SHADOW) != 0) {
            width += 1;
        } else if ((_style & BOLD) != 0) {
            width += 1;
        }
        return width;
    }

    /**
     * Gets the bounds of the supplied text layout in a way that works around the various
     * befuckeries that currently happen on the Mac.
     */
    protected Rectangle2D getBounds (TextLayout layout)
    {
        if (RunAnywhere.isMacOS()) {
            return layout.getOutline(null).getBounds();
        } else {
            return layout.getBounds();
        }
    }

    /**
     * Computes the height based on the leading, ascent and descent rather than what the layout
     * reports via <code>getBounds()</code> which rarely seems to have any bearing on reality.
     */
    protected float getHeight (TextLayout layout)
    {
        float height = layout.getLeading() + layout.getAscent() + layout.getDescent();
        if ((_style & OUTLINE) != 0) {
            height += 2;
        } else if ((_style & SHADOW) != 0) {
            height += 1;
        }
        return height;
    }

    /**
     * Called when the label is changed in such a way that it must be relaid out before again being
     * rendered.
     */
    protected void invalidate (String where)
    {
        _layouts = null;
//         _invalidator = where;
    }

    /** The text of the label. */
    protected String _text;

    /** The raw text, with color tags, or null if there are no color tags. */
    protected String _rawText;

    /** The text style. */
    protected int _style;

    /** The text alignment. */
    protected int _align = -1; // -1 means default according to locale

    /** Our size constraints in either the x or y direction. */
    protected Dimension _constraints = new Dimension();

    /** Our calculated size. */
    protected Dimension _size = new Dimension();

    /** Some fonts (God bless 'em) extend to the left of the position at which you request that
     * they be rendered. We opt to push such lines to the right sufficiently that they line up with
     * the rest of the lines (perhaps not the typographically ideal thing to do, but we're in
     * computer land and when we say our bounds are (0, 0, width, height) we damned well better not
     * render outside those bounds, which these wonderful fonts are choosing to do). */
    protected float[] _leaders;

    /** The font we use when laying out and rendering out text, or null if we're to use the default
     * font. */
    protected Font _font;

    /** Formatted text layout instances that contain each line of text. */
    protected TextLayout[] _layouts;

    /** Formatted text layout instances that contain each line of text. */
    protected Rectangle2D[] _lbounds;

    /** The color in which to render the text outline or shadow if we're rendering in outline or
     * shadow mode. */
    protected Color _alternateColor = null;

    /** The color in which to render the text or null if the text should be rendered with the
     * graphics context color. */
    protected Color _textColor = null;

    /** Will be true only when we're drawing a textlayout for the "main" portion of the label. If
     * we are in OUTLINE mode, we draw each layout 9 times: the last one is the only main one. */
    protected boolean _mainDraw = true;

//     /** Used for debugging. */
//     protected String _invalidator;

    /** An approximation of the golden ratio. */
    protected static final double GOLDEN_RATIO = 1.618034;

    /** Used by {@link #unescapeColors}. */
    protected static final Pattern ESCAPED_PATTERN = Pattern.compile("#''([Xx]|[0-9A-Fa-f]{6}+)");
}
