//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

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
    public static final int BOLD = 1 << 0;

    /** Constant denoting outline text style. */
    public static final int OUTLINE = 1 << 1;

    /** Constant denoting shadow text style. */
    public static final int SHADOW = 1 << 2;

    /** Constant denoting underline text style. */
    public static final int UNDERLINE = 1 << 3;
}
