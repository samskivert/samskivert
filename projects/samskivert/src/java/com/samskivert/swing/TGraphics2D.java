//
// $Id: TGraphics2D.java,v 1.3 2004/02/25 13:17:41 mdb Exp $

package com.samskivert.swing;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;

import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;

import java.awt.geom.AffineTransform;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;

import java.text.AttributedCharacterIterator;

import java.util.Map;

/**
 * A Graphics2D that pipes all processing to two subgraphics.
 */
public class TGraphics2D extends Graphics2D
{
    /**
     * Construct a TGraphics2D.
     * @param primary the primary underlying graphics. Get methods will
     * result in calls only to the primary Graphics2D.
     * @param copy the underlying graphics of lesser importance.
     */
    public TGraphics2D (Graphics2D primary, Graphics2D copy)
    {
        _primary = primary;
        _copy = copy;
    }

    /**
     * Get the primary Graphics2D underneath us, for bypass purposes.
     */
    public Graphics2D getPrimary ()
    {
        return _primary;
    }

    public void draw3DRect (int x, int y, int w, int h, boolean r)
    {
        _copy.draw3DRect(x, y, w, h, r);
        _primary.draw3DRect(x, y, w, h, r);
    }

    public void fill3DRect (int x, int y, int w, int h, boolean r)
    {
        _copy.fill3DRect(x, y, w, h, r);
        _primary.fill3DRect(x, y, w, h, r);
    }

    public void draw (Shape s)
    {
        _copy.draw(s);
        _primary.draw(s);
    }

    public boolean drawImage (Image i, AffineTransform a, ImageObserver o)
    {
        _copy.drawImage(i, a, null);
        return _primary.drawImage(i, a, o);
    }

    public void drawImage (BufferedImage i, BufferedImageOp o, int x, int y)
    {
        _copy.drawImage(i, o, x, y);
        _primary.drawImage(i, o, x, y);
    }

    public void drawRenderedImage (RenderedImage i, AffineTransform a)
    {
        _copy.drawRenderedImage(i, a);
        _primary.drawRenderedImage(i, a);
    }

    public void drawRenderableImage (RenderableImage i, AffineTransform a)
    {
        _copy.drawRenderableImage(i, a);
        _primary.drawRenderableImage(i, a);
    }

    public void drawString (String s, int x, int y)
    {
        _copy.drawString(s, x, y);
        _primary.drawString(s, x, y);
    }

    public void drawString (String s, float x, float y)
    {
        _copy.drawString(s, x, y);
        _primary.drawString(s, x, y);
    }

    public void drawString (AttributedCharacterIterator i, int x, int y)
    {
        _copy.drawString(i, x, y);
        _primary.drawString(i, x, y);
    }

    public void drawString (AttributedCharacterIterator i, float x, float y)
    {
        _copy.drawString(i, x, y);
        _primary.drawString(i, x, y);
    }

    public void drawGlyphVector (GlyphVector g, float x, float y)
    {
        _copy.drawGlyphVector(g, x, y);
        _primary.drawGlyphVector(g, x, y);
    }

    public void fill (Shape s)
    {
        _copy.fill(s);
        _primary.fill(s);
    }

    public boolean hit (Rectangle r, Shape s, boolean x)
    {
        _copy.hit(r, s, x);
        return _primary.hit(r, s, x);
    }

    public GraphicsConfiguration getDeviceConfiguration ()
    {
        return _primary.getDeviceConfiguration();
    }

    public void setComposite (Composite c)
    {
        _copy.setComposite(c);
        _primary.setComposite(c);
    }

    public void setPaint (Paint p)
    {
        _copy.setPaint(p);
        _primary.setPaint(p);
    }

    public void setStroke (Stroke s)
    {
        _copy.setStroke(s);
        _primary.setStroke(s);
    }

    public void setRenderingHint (RenderingHints.Key k, Object v)
    {
        _copy.setRenderingHint(k, v);
        _primary.setRenderingHint(k, v);
    }

    public Object getRenderingHint (RenderingHints.Key k)
    {
        return _primary.getRenderingHint(k);
    }

    public void setRenderingHints (Map m)
    {
        _copy.setRenderingHints(m);
        _primary.setRenderingHints(m);
    }

    public void addRenderingHints (Map m)
    {
        _copy.addRenderingHints(m);
        _primary.addRenderingHints(m);
    }

    public RenderingHints getRenderingHints ()
    {
        return _primary.getRenderingHints();
    }

    public void translate (int x, int y)
    {
        _copy.translate(x, y);
        _primary.translate(x, y);
    }

    public void translate (double x, double y)
    {
        _copy.translate(x, y);
        _primary.translate(x, y);
    }

    public void rotate (double t)
    {
        _copy.rotate(t);
        _primary.rotate(t);
    }

    public void rotate (double t, double u, double v)
    {
        _copy.rotate(t, u, v);
        _primary.rotate(t, u, v);
    }

    public void scale (double x, double y)
    {
        _copy.scale(x, y);
        _primary.scale(x, y);
    }

    public void shear (double x, double y)
    {
        _copy.shear(x, y);
        _primary.shear(x, y);
    }

    public void transform (AffineTransform a)
    {
        _copy.transform(a);
        _primary.transform(a);
    }

    public void setTransform (AffineTransform a)
    {
        _copy.setTransform(a);
        _primary.setTransform(a);
    }

    public AffineTransform getTransform ()
    {
        return _primary.getTransform();
    }

    public Paint getPaint ()
    {
        return _primary.getPaint();
    }

    public Composite getComposite ()
    {
        return _primary.getComposite();
    }

    public void setBackground (Color c)
    {
        _copy.setBackground(c);
        _primary.setBackground(c);
    }

    public Color getBackground ()
    {
        return _primary.getBackground();
    }

    public Stroke getStroke ()
    {
        return _primary.getStroke();
    }

    public void clip (Shape s)
    {
        _copy.clip(s);
        _primary.clip(s);
    }

    public FontRenderContext getFontRenderContext ()
    {
        return _primary.getFontRenderContext();
    }

    public Graphics create ()
    {
        return _primary.create();
    }

    public Graphics create (int x, int y, int w, int h)
    {
        return _primary.create(x, y, w, h);
    }

    public Color getColor ()
    {
        return _primary.getColor();
    }

    public void setColor (Color c)
    {
        _copy.setColor(c);
        _primary.setColor(c);
    }

    public void setPaintMode ()
    {
        _copy.setPaintMode();
        _primary.setPaintMode();
    }

    public void setXORMode (Color c)
    {
        _copy.setXORMode(c);
        _primary.setXORMode(c);
    }

    public Font getFont ()
    {
        return _primary.getFont();
    }

    public void setFont (Font f)
    {
        _copy.setFont(f);
        _primary.setFont(f);
    }

    public FontMetrics getFontMetrics ()
    {
        return _primary.getFontMetrics();
    }

    public FontMetrics getFontMetrics (Font f)
    {
        return _primary.getFontMetrics(f);
    }

    public Rectangle getClipBounds ()
    {
        return _primary.getClipBounds();
    }

    public void clipRect (int x, int y, int w, int h)
    {
        _copy.clipRect(x, y, w, h);
        _primary.clipRect(x, y, w, h);
    }

    public void setClip (int x, int y, int w, int h)
    {
        _copy.setClip(x, y, w, h);
        _primary.setClip(x, y, w, h);
    }

    public Shape getClip ()
    {
        return _primary.getClip();
    }

    public void setClip (Shape s)
    {
        _copy.setClip(s);
        _primary.setClip(s);
    }

    public void copyArea (int x, int y, int w, int h, int a, int b)
    {
        // was seeing errors here, Don't worry about failure on copy
        try {
            _copy.copyArea(x, y, w, h, a, b);
        } catch (Error e) {
        }
        _primary.copyArea(x, y, w, h, a, b);
    }

    public void drawLine (int x, int y, int a, int b)
    {
        _copy.drawLine(x, y, a, b);
        _primary.drawLine(x, y, a, b);
    }

    public void fillRect (int x, int y, int w, int h)
    {
        _copy.fillRect(x, y, w, h);
        _primary.fillRect(x, y, w, h);
    }

    public void drawRect (int x, int y, int w, int h)
    {
        _copy.drawRect(x, y, w, h);
        _primary.drawRect(x, y, w, h);
    }

    public void clearRect (int x, int y, int w, int h)
    {
        _copy.clearRect(x, y, w, h);
        _primary.clearRect(x, y, w, h);
    }

    public void drawRoundRect (int x, int y, int w, int h, int a, int b)
    {
        _copy.drawRoundRect(x, y, w, h, a, b);
        _primary.drawRoundRect(x, y, w, h, a, b);
    }

    public void fillRoundRect (int x, int y, int w, int h, int a, int b)
    {
        _copy.fillRoundRect(x, y, w, h, a, b);
        _primary.fillRoundRect(x, y, w, h, a, b);
    }

    public void drawOval (int x, int y, int w, int h)
    {
        _copy.drawOval(x, y, w, h);
        _primary.drawOval(x, y, w, h);
    }

    public void fillOval (int x, int y, int w, int h)
    {
        _copy.fillOval(x, y, w, h);
        _primary.fillOval(x, y, w, h);
    }

    public void drawArc (int x, int y, int w, int h, int a, int b)
    {
        _copy.drawArc(x, y, w, h, a, b);
        _primary.drawArc(x, y, w, h, a, b);
    }

    public void fillArc (int x, int y, int w, int h, int a, int b)
    {
        _copy.fillArc(x, y, w, h, a, b);
        _primary.fillArc(x, y, w, h, a, b);
    }

    public void drawPolyline (int[] x, int[] y, int n)
    {
        _copy.drawPolyline(x, y, n);
        _primary.drawPolyline(x, y, n);
    }

    public void drawPolygon (int[] x, int[] y, int n)
    {
        _copy.drawPolygon(x, y, n);
        _primary.drawPolygon(x, y, n);
    }

    public void drawPolygon (Polygon p)
    {
        _copy.drawPolygon(p);
        _primary.drawPolygon(p);
    }

    public void fillPolygon (int[] x, int[] y, int n)
    {
        _copy.fillPolygon(x, y, n);
        _primary.fillPolygon(x, y, n);
    }

    public void fillPolygon (Polygon p)
    {
        _copy.fillPolygon(p);
        _primary.fillPolygon(p);
    }

    public void drawChars (char[] c, int x, int y, int w, int h)
    {
        _copy.drawChars(c, x, y, w, h);
        _primary.drawChars(c, x, y, w, h);
    }

    public void drawBytes (byte[] b, int x, int y, int w, int h)
    {
        _copy.drawBytes(b, x, y, w, h);
        _primary.drawBytes(b, x, y, w, h);
    }

    public boolean drawImage (Image i, int x, int y, ImageObserver o)
    {
        _copy.drawImage(i, x, y, null);
        return _primary.drawImage(i, x, y, o);
    }

    public boolean drawImage (
        Image i, int x, int y, int w, int h, ImageObserver o)
    {
        _copy.drawImage(i, x, y, w, h, null);
        return _primary.drawImage(i, x, y, w, h, o);
    }

    public boolean drawImage (Image i, int x, int y, Color c, ImageObserver o)
    {
        _copy.drawImage(i, x, y, c, null);
        return _primary.drawImage(i, x, y, c, o);
    }

    public boolean drawImage (
        Image i, int x, int y, int w, int h, Color c, ImageObserver o)
    {
        _copy.drawImage(i, x, y, w, h, c, null);
        return _primary.drawImage(i, x, y, w, h, c, o);
    }

    public boolean drawImage (
        Image i, int x, int y, int w, int h,
        int a, int b, int c, int d, ImageObserver o)
    {
        _copy.drawImage(i, x, y, w, h, a, b, c, d, null);
        return _primary.drawImage(i, x, y, w, h, a, b, c, d, o);
    }

    public boolean drawImage (
        Image i, int x, int y, int w, int h,
        int a, int b, int c, int d, Color k, ImageObserver o)
    {
        _copy.drawImage(i, x, y, w, h, a, b, c, d, k, null);
        return _primary.drawImage(i, x, y, w, h, a, b, c, d, k, o);
    }

    public void dispose ()
    {
        _copy.dispose();
        _primary.dispose();
    }

    public void finalize ()
    {
        _copy.finalize();
        _primary.finalize();
    }

    public String toString ()
    {
        return _primary.toString();
    }

    public Rectangle getClipRect ()
    {
        // getClipRect is deprecated, but getClipBounds is the new way
        // to do the same thing. We call that to avoid deprecation warnings.
        return _primary.getClipBounds();
    }

    public boolean hitClip (int x, int y, int w, int h)
    {
        return _primary.hitClip(x, y, w, h);
    }

    public Rectangle getClipBounds (Rectangle r)
    {
        return _primary.getClipBounds(r);
    }

    protected Graphics2D _primary, _copy;
}
