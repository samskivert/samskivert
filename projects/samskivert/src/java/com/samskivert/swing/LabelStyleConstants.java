//
// $Id: LabelStyleConstants.java,v 1.1 2002/09/23 21:19:06 shaper Exp $
//
// samskivert library - useful routines for java programs
// Copyright (C) 2002 Walter Korman
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

/**
 * Defines text style constants for use with the {@link Label} and {@link
 * MultiLineLabel}.
 */
public interface LabelStyleConstants
{
    /** Constant denoting normal text style. */
    public static final int NORMAL = 0;

    /** Constant denoting bold text style. */
    public static final int BOLD = 1;

    /** Constant denoting outline text style. */
    public static final int OUTLINE = 2;

    /** Constant denoting shadow text style. */
    public static final int SHADOW = 3;
}
