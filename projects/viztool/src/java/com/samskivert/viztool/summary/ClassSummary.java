//
// $Id: ClassSummary.java,v 1.2 2001/12/03 06:14:03 mdb Exp $
// 
// viztool - a tool for visualizing collections of java classes
// Copyright (C) 2001 Michael Bayne
// 
// This program is free software; you can redistribute it and/or modify it
// under the terms of the GNU General Public License as published by the
// Free Software Foundation; either version 2.1 of the License, or (at your
// option) any later version.
// 
// This program is distributed in the hope that it will be useful, but
// WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// General Public License for more details.
// 
// You should have received a copy of the GNU General Public License along
// with this program; if not, write to the Free Software Foundation, Inc.,
// 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.samskivert.viztool.summary;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import com.samskivert.viztool.Log;
import com.samskivert.viztool.layout.Element;
import com.samskivert.viztool.util.FontPicker;
import com.samskivert.viztool.util.LayoutUtil;
import com.samskivert.viztool.util.RenderUtil;

/**
 * A class summary displays information about a particular class
 * (specifically, the interfaces it implements, the class it extends, its
 * public instances, member functions, and inner class definitions).
 */
public class ClassSummary
    implements Element
{
    /**
     * Constructs a class summary for the specified class.
     */
    public ClassSummary (Class subject, SummaryVisualizer viz)
    {
        _viz = viz;
        _subject = subject;
        _name = _viz.name(_subject);

        // obtain information on our subject class
        Class parent = _subject.getSuperclass();
        if (parent != null && !parent.equals(Object.class)) {
            _parentName = _viz.name(parent);
        }

        // get the implemented interfaces
        Class[] interfaces = _subject.getInterfaces();
        int icount = interfaces.length;
        _interfaces = new String[icount];
        for (int i = 0; i < icount; i++) {
            _interfaces[i] = _viz.name(interfaces[i]);
        }

        // create a comparator that we can use to sort the fields and
        // methods alphabetically and by staticness
        Comparator comp = new Comparator() {
            public int compare (Object o1, Object o2) {
                Member m1 = (Member)o1;
                Member m2 = (Member)o2;

                int s1 = m1.getModifiers() & Modifier.STATIC;
                int s2 = m2.getModifiers() & Modifier.STATIC;

                // if one's static and one isn't...
                if (s1 + s2 == Modifier.STATIC) {
                    // put the statics after the non-statics
                    return s1 - s2;

                } else {
                    // otherwise compare names
                    return m1.getName().compareTo(m2.getName());
                }
            }

            public boolean equals (Object other) {
                return (other == this);
            }
        };

        // get the public fields
        Field[] fields = _subject.getDeclaredFields();
        Arrays.sort(fields, comp);
        ArrayList fsigtypes = new ArrayList();
        ArrayList fsigs = new ArrayList();
        for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            if ((f.getModifiers() & Modifier.PUBLIC) != 0) {
                fsigtypes.add(genFieldTypeSig(f));
                fsigs.add(f.getName());
            }
        }
        _fieldTypes = new String[fsigtypes.size()];
        fsigtypes.toArray(_fieldTypes);
        _fields = new String[fsigs.size()];
        fsigs.toArray(_fields);

        // get the public constructors and methods
        ArrayList sigrets = new ArrayList();
        ArrayList sigs = new ArrayList();
        Constructor[] ctors = _subject.getConstructors();
        for (int i = 0; i < ctors.length; i++) {
            Constructor c = ctors[i];
            // make sure it's public
            if ((c.getModifiers() & Modifier.PUBLIC) != 0 &&
                // skip the zero argument constructor because it's
                // uninteresting
                c.getParameterTypes().length > 0) {
                sigrets.add(" ");
                sigs.add(genConstructorSig(c));
            }
        }
        Method[] methods = _subject.getDeclaredMethods();
        Arrays.sort(methods, comp);
        for (int i = 0; i < methods.length; i++) {
            Method m = methods[i];
            if ((m.getModifiers() & Modifier.PUBLIC) != 0) {
                sigrets.add(genMethodRetSig(m));
                sigs.add(genMethodSig(m));
            }
        }
        _methodReturns = new String[sigrets.size()];
        sigrets.toArray(_methodReturns);
        _methods = new String[sigs.size()];
        sigs.toArray(_methods);
    }

    /**
     * Introspects on our subject class and determines how much space
     * we'll need to visualize it.
     *
     * @param gfx the graphics context in which this summary will
     * eventually be rendered.
     */
    public void layout (Graphics2D gfx)
    {
        FontRenderContext frc = gfx.getFontRenderContext();

        // the header will be the name of this class surrounded by N
        // points of space and a box
        Rectangle2D bounds = LayoutUtil.getTextBox(
            _subject.isInterface() ? FontPicker.getInterfaceFont() :
            FontPicker.getClassFont(), frc, _name);
        double spacing = 0;

        // add our parent class if we've got one
        if (_parentName != null) {
            String subtext = "extends " + _parentName;
            bounds = LayoutUtil.accomodate(
                bounds, FontPicker.getDeclaresFont(), frc,
                LayoutUtil.SUBORDINATE_INSET, subtext);
        }

        // add our interfaces
        bounds = LayoutUtil.accomodate(
            bounds, FontPicker.getImplementsFont(), frc,
            LayoutUtil.SUBORDINATE_INSET, _interfaces);

        // add our fields
        bounds = LayoutUtil.accomodate(
            bounds, FontPicker.getClassFont(), frc, 0, _fieldTypes, _fields);
        spacing += (_fields.length > 0) ? 2*LayoutUtil.HEADER_BORDER : 0;

        // add our constructors and methods
        bounds = LayoutUtil.accomodate(
            bounds, FontPicker.getClassFont(), frc, 0,
            _methodReturns, _methods);
        spacing += (_methods.length > 0) ? 2*LayoutUtil.HEADER_BORDER : 0;

        // incorporate space for the gaps
        bounds.setRect(bounds.getX(), bounds.getY(), bounds.getWidth(),
                       bounds.getHeight() + spacing);

        // grab the new bounds
        _bounds = bounds;
    }

    /**
     * Renders this class summary to the specified graphics context.
     */
    public void render (Graphics2D gfx)
    {
        // figure out where we'll be rendering
        Rectangle2D bounds = getBounds();
        double x = bounds.getX() + LayoutUtil.HEADER_BORDER;
        double y = bounds.getY() + LayoutUtil.HEADER_BORDER;
        double maxwid = 0, sy1 = 0, sy2 = 0;

        // draw the name
        FontRenderContext frc = gfx.getFontRenderContext();
        Font font = _subject.isInterface() ?
            FontPicker.getInterfaceFont() : FontPicker.getClassFont();
        Rectangle2D bnds = 
            RenderUtil.renderString(gfx, frc, font, x, y, _name);
        maxwid = Math.max(maxwid, bnds.getWidth() +
                          2*LayoutUtil.HEADER_BORDER);
        y += bnds.getHeight();

        // render the parent classname
        if (_parentName != null) {
            bnds = RenderUtil.renderString(
                gfx, frc, FontPicker.getDeclaresFont(),
                x + LayoutUtil.SUBORDINATE_INSET, y, _parentName);
            maxwid = Math.max(maxwid, bnds.getWidth() +
                              2*LayoutUtil.HEADER_BORDER +
                              LayoutUtil.SUBORDINATE_INSET);
            y += bnds.getHeight();
        }

        // render our implemented interfaces
        bnds = RenderUtil.renderStrings(
            gfx, frc, FontPicker.getImplementsFont(),
            x + LayoutUtil.SUBORDINATE_INSET, y, _interfaces);
        maxwid = Math.max(maxwid, bnds.getWidth() +
                          2*LayoutUtil.HEADER_BORDER +
                          LayoutUtil.SUBORDINATE_INSET);
        y += bnds.getHeight();

        // stroke a box that contains the header
        Rectangle2D outline = new Rectangle2D.Double(
            bounds.getX(), bounds.getY(),
            maxwid, y + LayoutUtil.HEADER_BORDER - bounds.getY());
        gfx.draw(outline);

        // leave space for a separator
        if (_fields.length > 0) {
            y += LayoutUtil.HEADER_BORDER;
            sy1 = y;
            y += LayoutUtil.HEADER_BORDER;
        }

        // render our fields
        bnds = RenderUtil.renderStrings(
            gfx, frc, FontPicker.getClassFont(), x, y, _fieldTypes, _fields);
        maxwid = Math.max(maxwid, bnds.getWidth() +
                          2*LayoutUtil.HEADER_BORDER);
        y += bnds.getHeight();

        // leave space for a separator
        if (_methods.length > 0) {
            y += LayoutUtil.HEADER_BORDER;
            sy2 = y;
            y += LayoutUtil.HEADER_BORDER;
        }

        // render our constructors and methods
        bnds = RenderUtil.renderStrings(
            gfx, frc, FontPicker.getClassFont(), x, y,
            _methodReturns, _methods);
        maxwid = Math.max(maxwid, bnds.getWidth() +
                          2*LayoutUtil.HEADER_BORDER);
        y += bnds.getHeight();

        // draw our separators now that we know how wide things are
        double x1 = bounds.getX(), x2 = x1 + maxwid;
        if (sy1 > 0) {
            gfx.draw(new Line2D.Double(x1, sy1, x2, sy1));
        }
        if (sy2 > 0) {
            gfx.draw(new Line2D.Double(x1, sy2, x2, sy2));
        }
    }

    // documentation inherited
    public String getName ()
    {
        return _subject.getName();
    }

    // documentation inherited
    public Rectangle2D getBounds ()
    {
        return _bounds;
    }

    // documentation inherited
    public void setBounds (double x, double y, double width, double height)
    {
        _bounds.setRect(x, y, width, height);
    }

    /**
     * Generates a signature for the type of the supplied field.
     */
    public String genFieldTypeSig (Field field)
    {
        StringBuffer buf = new StringBuffer();
        if ((field.getModifiers() & Modifier.STATIC) != 0) {
            buf.append("static ");
        }
        buf.append(_viz.name(field.getType())).append(" ");
        return buf.toString();
    }

    /**
     * Generates a signature for the supplied constructor.
     */
    public String genConstructorSig (Constructor ctor)
    {
        StringBuffer buf = new StringBuffer();
        buf.append(_viz.name(ctor.getDeclaringClass())).append(" (");
        Class[] ptypes = ctor.getParameterTypes();
        for (int i = 0; i < ptypes.length; i++) {
            if (i > 0) {
                buf.append(", ");
            }
            buf.append(_viz.name(ptypes[i]));
        }
        buf.append(")");
        Class[] etypes = ctor.getExceptionTypes();
        if (etypes.length > 0) {
            buf.append(" throws ");
            for (int i = 0; i < etypes.length; i++) {
                if (i > 0) {
                    buf.append(", ");
                }
                buf.append(_viz.name(etypes[i]));
            }
        }
        return buf.toString();
    }

    /**
     * Generates a signature for the return value of the supplied method.
     */
    public String genMethodRetSig (Method method)
    {
        StringBuffer buf = new StringBuffer();
        if ((method.getModifiers() & Modifier.STATIC) != 0) {
            buf.append("static ");
        }
        buf.append(_viz.name(method.getReturnType()));
        return buf.toString();
    }

    /**
     * Generates a signature for the supplied method (minus return type).
     */
    public String genMethodSig (Method method)
    {
        StringBuffer buf = new StringBuffer();
        buf.append(method.getName()).append(" (");
        Class[] ptypes = method.getParameterTypes();
        for (int i = 0; i < ptypes.length; i++) {
            if (i > 0) {
                buf.append(", ");
            }
            buf.append(_viz.name(ptypes[i]));
        }
        buf.append(")");
        Class[] etypes = method.getExceptionTypes();
        if (etypes.length > 0) {
            buf.append(" throws ");
            for (int i = 0; i < etypes.length; i++) {
                if (i > 0) {
                    buf.append(", ");
                }
                buf.append(_viz.name(etypes[i]));
            }
        }
        return buf.toString();
    }

    public String toString ()
    {
        return "[subject=" + _subject.getName() + "]";
    }

    /** The package we're visualizing, which we'll strip from the front of
     * class names. */
    protected SummaryVisualizer _viz;

    /** The class for which we're generating a summary visualization. */
    protected Class _subject;

    /** The cleaned up name of the class we're summarizing. */
    protected String _name;

    /** The name of our parent class or null if we don't have an
     * interesting parent class. */
    protected String _parentName;

    /** The names of interfaces that we implement. */
    protected String[] _interfaces;

    /** The types of our public fields. */
    protected String[] _fieldTypes;

    /** The names of our public fields. */
    protected String[] _fields;

    /** The return types of our public constructors and methods. */
    protected String[] _methodReturns;

    /** The signatures of our public constructors and methods (minus
     * return type). */
    protected String[] _methods;

    /** Our bounds. */
    protected Rectangle2D _bounds = new Rectangle2D.Double();
}
