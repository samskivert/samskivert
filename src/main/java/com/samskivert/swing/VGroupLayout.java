//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.swing;

import java.awt.*;

public class VGroupLayout extends GroupLayout
{
    public VGroupLayout (Policy policy, Policy offpolicy, int gap, Justification justification)
    {
        _policy = policy;
        _offpolicy = offpolicy;
        _gap = gap;
        _justification = justification;
    }

    public VGroupLayout (Policy policy, int gap, Justification justification)
    {
        _policy = policy;
        _gap = gap;
        _justification = justification;
    }

    public VGroupLayout (Policy policy, Justification justification)
    {
        _policy = policy;
        _justification = justification;
    }

    public VGroupLayout (Policy policy)
    {
        _policy = policy;
    }

    public VGroupLayout ()
    {
    }

    @Override
    protected Dimension getLayoutSize (Container parent, int type)
    {
        DimenInfo info = computeDimens(parent, type);
        Dimension dims = new Dimension();

        if (_policy == STRETCH) {
            dims.height = info.maxfreehei * (info.count - info.numfix) + info.fixhei;
        } else if (_policy == EQUALIZE) {
            dims.height = info.maxhei * info.count;
        } else { // NONE or CONSTRAIN
            dims.height = info.tothei;
        }

        dims.height += (info.count - 1) * _gap;
        dims.width = info.maxwid;

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
        int tothei, totgap = _gap * (info.count-1);
        int freecount = info.count - info.numfix;

        // when stretching, there is the possibility that a pixel or more
        // will be lost to rounding error. we account for that here and
        // assign the extra space to the first free component
        int freefrac = 0;

        // do the on-axis policy calculations
        int defhei = 0;
        if (_policy == STRETCH) {
            if (freecount > 0) {
                int freehei = b.height - info.fixhei - totgap;
                defhei = freehei / info.totweight;
                freefrac = freehei % info.totweight;
                tothei = b.height;
            } else {
                tothei = info.fixhei + totgap;
            }

        } else if (_policy == EQUALIZE) {
            defhei = info.maxhei;
            tothei = info.fixhei + defhei * freecount + totgap;

        } else {
            tothei = info.tothei + totgap;
        }

        // do the off-axis policy calculations
        int defwid = 0;
        if (_offpolicy == STRETCH) {
            defwid = b.width;
        } else if (_offpolicy == EQUALIZE) {
            defwid = info.maxwid;
        }

        // do the justification-related calculations
        if (_justification == LEFT || _justification == TOP) {
            sy = insets.top;
        } else if (_justification == CENTER) {
            sy = insets.top + (b.height - tothei)/2;
        } else { // RIGHT or BOTTOM
            sy = insets.top + b.height - tothei;
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
                newhei = info.dimens[i].height;
            } else {
                newhei = freefrac + ((_policy == STRETCH) ? defhei * c.getWeight() : defhei);
                // clear out the extra pixels the first time they're used
                freefrac = 0;
            }

            if (_offpolicy == NONE) {
                newwid = info.dimens[i].width;
            } else if (_offpolicy == CONSTRAIN) {
                newwid = Math.min(info.dimens[i].width, b.width);
            } else {
                newwid = defwid;
            }

            // determine our off-axis position
            if (_offjust == LEFT || _offjust == TOP) {
                sx = insets.left;
            } else if (_offjust == RIGHT || _offjust == BOTTOM) {
                sx = insets.left + b.width - newwid;
            } else { // CENTER
                sx = insets.left + (b.width - newwid)/2;
            }

            child.setBounds(sx, sy, newwid, newhei);
            sy += child.getSize().height + _gap;
        }
    }
}
