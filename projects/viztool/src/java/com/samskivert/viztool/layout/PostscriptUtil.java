//
// $Id: PostscriptUtil.java,v 1.1 2001/07/13 23:25:13 mdb Exp $

package com.samskivert.viztool.viz;

/**
 * Postscript related utility functions.
 */
public class PostscriptUtil
{
    /**
     * Estimates the width of the supplied string rendered in a font of
     * the specified point size. It does this by assuming the average
     * character width is 60% of the point size and multiplies that by the
     * length of the supplied string. Not the most accurate mechanism in
     * the world but you should only use this for rough estimates and
     * should include postscript code to do the right thing (using strwid)
     * when actually rendering the page.
     */
    public static int estimateWidth (String text, int pointSize)
    {
        return (pointSize * text.length() * 6) / 10;
    }
}
