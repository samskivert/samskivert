//
// $Id: GroupLayout.java,v 1.3 2001/08/11 22:43:28 mdb Exp $
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

package com.samskivert.swing;

import java.awt.*;
import javax.swing.SwingConstants;
import java.util.HashMap;

/**
 * Group layout managers lay out widgets in horizontal or vertical groups.
 */
public abstract class GroupLayout
    implements LayoutManager2, SwingConstants
{
    /**
     * The group layout managers supports two constraints: fixedness
     * and weight. A fixed component will not be stretched along the major
     * axis of the group. Those components that are stretched will have
     * the extra space divided among them according to their weight
     * (specifically receiving the ratio of their weight to the total
     * weight of all of the free components in the container).
     *
     * <p/> If a constraints object is constructed with fixedness set to
     * true and with a weight, the weight will be ignored.
     */
    public static class Constraints
    {
	/** Whether or not this component is fixed. */
	public boolean fixed = false;

	/**
	 * The weight of this component relative to the other components
	 * in the container.
	 */
	public int weight = 1;

	/**
	 * Constructs a new constraints object with the specified
	 * fixedness and weight.
	 */
	public Constraints (boolean fixed)
	{
	    this.fixed = fixed;
	}

	/**
	 * Constructs a new constraints object with the specified
	 * fixedness and weight.
	 */
	public Constraints (int weight)
	{
	    this.weight = weight;
	}
    }

    /**
     * A constraints object that indicates that the component should be
     * fixed and have the default weight of one. This is so commonly used
     * that we create and make this object available here.
     */
    public final static Constraints FIXED = new Constraints(true);

    /**
     * Do not adjust the widgets on this axis.
     */
    public final static int NONE = 0;

    /**
     * Stretch all the widgets to their maximum possible size on this
     * axis.
     */
    public final static int STRETCH = 1;

    /**
     * Stretch all the widgets to be equal to the size of the largest
     * widget on this axis.
     */
    public final static int EQUALIZE = 2;

    public void setPolicy (int policy)
    {
	_policy = policy;
    }

    public int getPolicy ()
    {
	return _policy;
    }

    public void setOffAxisPolicy (int offpolicy)
    {
	_offpolicy = offpolicy;
    }

    public int getOffAxisPolicy ()
    {
	return _offpolicy;
    }

    public void setGap (int gap)
    {
	_gap = gap;
    }

    public int getGap ()
    {
	return _gap;
    }

    public void setJustification (int justification)
    {
	_justification = justification;
    }

    public int getJustification ()
    {
	return _justification;
    }

    public void setOffAxisJustification (int justification)
    {
	_offjust = justification;
    }

    public int getOffAxisJustification ()
    {
	return _offjust;
    }

    public void addLayoutComponent (String name, Component comp)
    {
	// nothing to do here
    }

    public void removeLayoutComponent (Component comp)
    {
	if (_constraints != null) {
	    _constraints.remove(comp);
	}
    }

    public void addLayoutComponent (Component comp, Object constraints)
    {
	if (constraints != null) {
	    if (constraints instanceof Constraints) {
		if (_constraints == null) {
		    _constraints = new HashMap();
		}
		_constraints.put(comp, constraints);

	    } else {
		throw new RuntimeException("GroupLayout constraints " +
					   "object must be of type " +
					   "GroupLayout.Constraints");
	    }
	}
    }

    public float getLayoutAlignmentX (Container target)
    {
	// we don't support alignment like this
	return 0f;
    }

    public float getLayoutAlignmentY (Container target)
    {
	// we don't support alignment like this
	return 0f;
    }

    public Dimension minimumLayoutSize (Container parent)
    {
	return getLayoutSize(parent, MINIMUM);
    }

    public Dimension preferredLayoutSize (Container parent)
    {
	return getLayoutSize(parent, PREFERRED);
    }

    public Dimension maximumLayoutSize (Container parent)
    {
	return getLayoutSize(parent, MAXIMUM);
    }

    protected abstract Dimension getLayoutSize (Container parent, int type);

    public abstract void layoutContainer (Container parent);

    public void invalidateLayout (Container target)
    {
	// nothing to do here
    }

    protected boolean isFixed (Component child)
    {
	if (_constraints == null) {
	    return false;
	}

	Constraints c = (Constraints)_constraints.get(child);
	if (c != null) {
	    return c.fixed;
	}

	return false;
    }

    protected int getWeight (Component child)
    {
	if (_constraints == null) {
	    return 1;
	}

	Constraints c = (Constraints)_constraints.get(child);
	if (c != null) {
	    return c.weight;
	}

	return 1;
    }

    /**
     * Computes dimensions of the children widgets that are useful for the
     * group layout managers.
     */
    protected DimenInfo computeDimens (Container parent, int type)
    {
	int count = parent.getComponentCount();
	DimenInfo info = new DimenInfo();
	info.dimens = new Dimension[count];

	for (int i = 0; i < count; i++) {
	    Component child = parent.getComponent(i);
	    if (!child.isVisible()) {
		continue;
	    }

	    Dimension csize;
	    switch  (type) {
	    case MINIMUM:
		csize = child.getMinimumSize();
		break;

	    case MAXIMUM:
		csize = child.getMaximumSize();
		break;

	    default:
		csize = child.getPreferredSize();
		break;
	    }

	    info.count++;
	    info.totwid += csize.width;
	    info.tothei += csize.height;

	    if (csize.width > info.maxwid) {
		info.maxwid = csize.width;
	    }
	    if (csize.height > info.maxhei) {
		info.maxhei = csize.height;
	    }

	    if (isFixed(child)) {
		info.fixwid += csize.width;
		info.fixhei += csize.height;
		info.numfix++;

	    } else {
		info.totweight += getWeight(child);
	    }

	    info.dimens[i] = csize;
	}

	return info;
    }

    protected int _policy = NONE;
    protected int _offpolicy = NONE;
    protected int _gap = 5;
    protected int _justification = CENTER;
    protected int _offjust = CENTER;

    protected HashMap _constraints;

    protected static final int MINIMUM = 0;
    protected static final int PREFERRED = 1;
    protected static final int MAXIMUM = 2;
}
