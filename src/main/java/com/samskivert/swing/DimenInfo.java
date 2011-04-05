//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2011 Michael Bayne, et al.
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

import java.awt.Dimension;

/**
 * This record is used by the group layout managers to return a set of
 * statistics computed for their target widgets.
 */
public class DimenInfo
{
    public int count;

    public int totwid;
    public int tothei;

    public int maxwid;
    public int maxhei;

    public int numfix;
    public int fixwid;
    public int fixhei;

    public int maxfreewid;
    public int maxfreehei;

    public int totweight;

    public Dimension[] dimens;

    @Override
    public String toString ()
    {
        StringBuilder buf = new StringBuilder();
        buf.append("[count=").append(count);
        buf.append(", totwid=").append(totwid);
        buf.append(", tothei=").append(tothei);
        buf.append(", maxwid=").append(maxwid);
        buf.append(", maxhei=").append(maxhei);
        buf.append(", numfix=").append(numfix);
        buf.append(", fixwid=").append(fixwid);
        buf.append(", fixhei=").append(fixhei);
        buf.append(", maxfreewid=").append(maxfreewid);
        buf.append(", maxfreehei=").append(maxfreehei);
        buf.append(", totweight=").append(totweight);
        return buf.append("]").toString();
    }
}
