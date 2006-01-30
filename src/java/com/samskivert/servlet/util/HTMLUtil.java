//
// $Id: HTMLUtil.java,v 1.2 2002/12/30 04:52:36 mdb Exp $
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

package com.samskivert.servlet.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.samskivert.util.StringUtil;

/**
 * HTML related utility functions.
 */
public class HTMLUtil
{
    /**
     * Converts instances of <code><, >, & and "</code> into their
     * entified equivalents: <code>&lt;, &gt;, &amp; and &quot;</code>.
     * These characters are mentioned in the HTML spec as being common
     * candidates for entification.
     *
     * @return the entified string.
     */
    public static String entify (String text)
    {
        // this could perhaps be done more efficiently, but this function
        // is not likely to be called on large quantities of text
        // (first we turn the entified versions normal so that if text is
        // repeatedly run through this it doesn't keep changing successive
        // &'s into "&amp;".
        text = StringUtil.replace(text, "&quot;", "\"");
        text = StringUtil.replace(text, "&gt;", ">");
        text = StringUtil.replace(text, "&lt;", "<");
        text = StringUtil.replace(text, "&amp;", "&");
        text = StringUtil.replace(text, "&", "&amp;");
        text = StringUtil.replace(text, "<", "&lt;");
        text = StringUtil.replace(text, ">", "&gt;");
        text = StringUtil.replace(text, "\"", "&quot;");
        return text;
    }

    /**
     * Inserts a &lt;p&gt; tag between every two consecutive newlines.
     */
    public static String makeParagraphs (String text)
    {
        if (text == null) {
            return text;
        }
        // handle both line ending formats
        text = StringUtil.replace(text, "\n\n", "\n<p>\n");
        text = StringUtil.replace(text, "\r\n\r\n", "\r\n<p>\r\n");
        return text;
    }

    /**
     * Inserts a &lt;br&gt; tag before every newline.
     */
    public static String makeLinear (String text)
    {
        if (text == null) {
            return text;
        }
        // handle both line ending formats
        text = StringUtil.replace(text, "\n", "<br>\n");
        return text;
    }

    /**
     * Does some simple HTML markup, matching bare image URLs and wrapping
     * them in image tags, matching other URLs and wrapping them in href
     * tags, and wrapping * prefixed lists into ul-style HTML lists.
     */
    public static String simpleFormat (String text)
    {
        // first replace the image and other URLs
        Matcher m = _url.matcher(text);
        StringBuffer tbuf = new StringBuffer();
        while (m.find()) {
            String match = m.group();
            String lmatch = match.toLowerCase();
            if (lmatch.endsWith(".png") ||
                lmatch.endsWith(".jpg") ||
                lmatch.endsWith(".gif")) {
                match = "<img src=\"" + match + "\">";
            } else {
                match = "<a href=\"" + match + "\">" + match + "</a>";
            }
            m.appendReplacement(tbuf, match);
        }
        m.appendTail(tbuf);

        // then tackle the *s
        text = tbuf.toString();
        m = _star.matcher(text);
        tbuf.setLength(0);
        while (m.find()) {
            String match = m.group();
            int start = m.start();
            if (start == 0 || (start >= 2 && text.charAt(start-1) == '\n' &&
                               text.charAt(start-2) == '\n')) {
                m.appendReplacement(tbuf, "<ul><li>");
            } else {
                m.appendReplacement(tbuf, "<li>");
            }
        }
        m.appendTail(tbuf);
        text = tbuf.toString();

        // finally close the </ul>s and paragraphy
        String[] paras = _blank.split(text);
        tbuf.setLength(0);
        for (int ii = 0; ii < paras.length; ii++) {
            String para = paras[ii].trim();
            if (para.startsWith("<ul>")) {
                tbuf.append(para).append(" </ul>\n");
            } else {
                tbuf.append("<p> ").append(para).append(" </p>\n");
            }            
        }

        return tbuf.toString().trim();
    }

    protected static Pattern _url =
        Pattern.compile("http://\\S+", Pattern.MULTILINE);
    protected static Pattern _star = Pattern.compile("^\\*", Pattern.MULTILINE);
    protected static Pattern _blank = Pattern.compile("^$", Pattern.MULTILINE);
}
