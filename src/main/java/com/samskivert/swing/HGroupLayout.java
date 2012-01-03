//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.swing;

import java.awt.*;

public class HGroupLayout extends GroupLayout
{
    public HGroupLayout (Policy policy, Policy offpolicy, int gap, Justification justification)
    {
        _policy = policy;
        _offpolicy = offpolicy;
        _gap = gap;
        _justification = justification;
    }

    public HGroupLayout (Policy policy, int gap, Justification justification)
    {
        _policy = policy;
        _gap = gap;
        _justification = justification;
    }

    public HGroupLayout (Policy policy, Justification justification)
    {
        _policy = policy;
        _justification = justification;
    }

    public HGroupLayout (Policy policy)
    {
        _policy = policy;
    }

    public HGroupLayout ()
    {
    }

    @Override
    protected Dimension getLayoutSize (Container parent, int type)
    {
        DimenInfo info = computeDimens(parent, type);
        Dimension dims = new Dimension();

        if (_policy == STRETCH) {
            dims.width = info.maxfreewid * (info.count - info.numfix) + info.fixwid;
        } else if (_policy == EQUALIZE) {
            dims.width = info.maxwid * info.count;
        } else { // NONE or CONSTRAIN
            dims.width = info.totwid;
        }

        dims.width += (info.count - 1) * _gap;
        dims.height = info.maxhei;

        // account for the insets
        Insets insets = parent.getInsets();
        dims.width += insets.left + insets.right;
        dims.height += insets.top + insets.bottom;

        return dims;
    }

    @Override
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
        int totwid, totgap = _gap * (info.count-1);
        int freecount = info.count - info.numfix;

        // when stretching, there is the possibility that a pixel or more
        // will be lost to rounding error. we account for that here and
        // assign the extra space to the first free component
        int freefrac = 0;

        // do the on-axis policy calculations
        int defwid = 0;
        if (_policy == STRETCH) {
            if (freecount > 0) {
                int freewid = b.width - info.fixwid - totgap;
                defwid = freewid / info.totweight;
                freefrac = freewid % info.totweight;
                totwid = b.width;
            } else {
                totwid = info.fixwid + totgap;
            }

        } else if (_policy == EQUALIZE) {
            defwid = info.maxwid;
            totwid = info.fixwid + defwid * freecount + totgap;

        } else { // NONE or CONSTRAIN
            totwid = info.totwid + totgap;
        }

        // do the off-axis policy calculations
        int defhei = 0;
        if (_offpolicy == STRETCH) {
            defhei = b.height;
        } else if (_offpolicy == EQUALIZE) {
            defhei = info.maxhei;
        }

        // do the justification-related calculations
        if (_justification == LEFT || _justification == TOP) {
            sx = insets.left;
        } else if (_justification == CENTER) {
            sx = insets.left + (b.width - totwid)/2;
        } else { // RIGHT or BOTTOM
            sx = insets.left + b.width - totwid;
        }

        // do the layout
        for (int i = 0; i < nk; i++) {
            // skip non-visible kids
            if (info.dimens[i] == null) {
                continue;
            }

            Component child = parent.getComponent(i);
            Constraints c = getConstraints(child);
            int newwid, newhei;

            if (_policy == NONE || c.isFixed()) {
                newwid = info.dimens[i].width;
            } else {
                newwid = freefrac + ((_policy == STRETCH) ? defwid * c.getWeight() : defwid);
                // clear out the extra pixels the first time they're used
                freefrac = 0;
            }

            if (_offpolicy == NONE) {
                newhei = info.dimens[i].height;
            } else if (_offpolicy == CONSTRAIN) {
                newhei = Math.min(info.dimens[i].height, b.height);
            } else {
                newhei = defhei;
            }

            // determine our off-axis position
            if (_offjust == LEFT || _offjust == TOP) {
                sy = insets.top;
            } else if (_offjust == RIGHT || _offjust == BOTTOM) {
                sy = insets.top + b.height - newhei;
            } else { // CENTER
                sy = insets.top + (b.height - newhei)/2;
            }

            child.setBounds(sx, sy, newwid, newhei);
            sx += child.getSize().width + _gap;
        }
    }
}
