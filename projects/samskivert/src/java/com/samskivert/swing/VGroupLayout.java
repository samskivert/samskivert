//
// $Id: VGroupLayout.java,v 1.9 2001/10/17 17:01:02 mdb Exp $
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

public class VGroupLayout extends GroupLayout
{
    public VGroupLayout (int policy, int offpolicy, int gap,
			 int justification)
    {
	_policy = policy;
	_offpolicy = offpolicy;
	_gap = gap;
	_justification = justification;
    }

    public VGroupLayout (int policy, int gap, int justification)
    {
	_policy = policy;
	_gap = gap;
	_justification = justification;
    }

    public VGroupLayout (int policy, int justification)
    {
	_policy = policy;
	_justification = justification;
    }

    public VGroupLayout (int policy)
    {
	_policy = policy;
    }

    public VGroupLayout ()
    {
    }

    protected Dimension getLayoutSize (Container parent, int type)
    {
	DimenInfo info = computeDimens(parent, type);
	Dimension dims = new Dimension();

	switch (_policy) {
	case STRETCH:
	case EQUALIZE:
	    dims.height = info.maxhei * (info.count - info.numfix) +
		info.fixhei + _gap * info.count;
	    break;

	case NONE:
	default:
	    dims.height = info.tothei + _gap * info.count;
	    break;
	}

	dims.height -= _gap;
	dims.width = info.maxwid;

	// account for the insets
	Insets insets = parent.getInsets();
	dims.width += insets.left + insets.right;
	dims.height += insets.top + insets.bottom;

	return dims;
    }

    public void layoutContainer (Container parent)
    {
	Rectangle b = parent.getBounds();
	DimenInfo info = computeDimens(parent, PREFERRED);

	// adjust the bounds width and height to account for the insets
	Insets insets = parent.getInsets();
	b.width -= (insets.left + insets.right);
	b.height -= (insets.top + insets.bottom);

	int nk = parent.getComponentCount();
	int sx, sy;
	int tothei, totgap = _gap * (info.count-1);
	int freecount = info.count - info.numfix;

        // when stretching, there is the possibility that a pixel or more
        // will be lost to rounding error. we account for that here and
        // assign the extra space to the first free component
        int freefrac = 0;

        // do the on-axis policy calculations
	int defhei = 0;
	switch (_policy) {
	case STRETCH:
	    if (freecount > 0) {
                int freehei = b.height - info.fixhei - totgap;
                defhei = freehei / freecount;
                freefrac = freehei % freecount;
		tothei = b.height;
	    } else {
		tothei = info.fixhei + totgap;
	    }
	    break;

	case EQUALIZE:
	    defhei = info.maxhei;
	    tothei = info.fixhei + defhei * freecount + totgap;
	    break;

	default:
	case NONE:
	    tothei = info.tothei + totgap;
	    break;
	}

	// do the off-axis policy calculations
	int defwid = 0;
	switch (_offpolicy) {
	case STRETCH:
	    defwid = b.width;
	    break;
	case EQUALIZE:
	    defwid = info.maxwid;
	    break;
	default:
	case NONE:
	    break;
	}

	// do the justification-related calculations
	switch (_justification) {
        default:
        case LEFT:
        case TOP:
            sy = insets.top;
            break;
	case CENTER:
	    sy = insets.top + (b.height - tothei)/2;
	    break;
	case RIGHT:
	case BOTTOM:
	    sy = insets.top + b.height - tothei;
	    break;
	}

	// do the layout
	for (int i = 0; i < nk; i++) {
	    // skip non-visible kids
	    if (info.dimens[i] == null) {
		continue;
	    }

	    Component child = parent.getComponent(i);
	    int newwid, newhei;

	    if (_policy == NONE || isFixed(child)) {
		newhei = info.dimens[i].height;
	    } else {
                newhei = defhei + freefrac;
                // clear out the extra pixels the first time they're used
                freefrac = 0;
            }

	    if (_offpolicy == NONE) {
		newwid = info.dimens[i].width;
	    } else {
                newwid = defwid;
            }

            // determine our off-axis position
            switch (_offjust) {
            case LEFT:
            case TOP:
		sx = insets.left;
                break;
            case RIGHT:
            case BOTTOM:
		sx = insets.left + b.width - newwid;
                break;
            default:
            case CENTER:
		sx = insets.left + (b.width - newwid)/2;
                break;
            }

	    child.setBounds(sx, sy, newwid, newhei);
	    sy += child.getSize().height + _gap;
	}
    }
}
