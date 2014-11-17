//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.servlet.util;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.samskivert.util.StringUtil;

/**
 * HTML related utility functions.
 */
public class HTMLUtil
{
    /**
     * Converts instances of {@code <, >, & and "} into their
     * entified equivalents: {@code &lt;, &gt;, &amp; and &quot;}.
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
        text = text.replace("&quot;", "\"");
        text = text.replace("&gt;", ">");
        text = text.replace("&lt;", "<");
        text = text.replace("&amp;", "&");
        text = text.replace("&", "&amp;");
        text = text.replace("<", "&lt;");
        text = text.replace(">", "&gt;");
        text = text.replace("\"", "&quot;");
        return text;
    }

    /**
     * Inserts a {@code <p>} tag between every two consecutive newlines.
     */
    public static String makeParagraphs (String text)
    {
        if (text == null) {
            return text;
        }
        // handle both line ending formats
        text = text.replace("\n\n", "\n<p>\n");
        text = text.replace("\r\n\r\n", "\r\n<p>\r\n");
        return text;
    }

    /**
     * Inserts a {@code <br>} tag before every newline.
     */
    public static String makeLinear (String text)
    {
        if (text == null) {
            return text;
        }
        // handle both line ending formats
        text = text.replace("\n", "<br>\n");
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
        Matcher m = URL_PAT.matcher(text);
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

        // then do our paragraph and list processing
        String[] lines = StringUtil.split(tbuf.toString(), "\n");
        StringBuilder lbuf = new StringBuilder();

        boolean inpara = false, inlist = false;
        for (String line2 : lines) {
            String line = line2;
            if (StringUtil.isBlank(line2)) {
                if (inlist) {
                    lbuf.append("</ul>");
                    inlist = false;
                }
                if (inpara) {
                    lbuf.append("</p>\n");
                    inpara = false;
                }
                continue;
            }

            if (!inpara) {
                inpara = true;
                lbuf.append("<p> ");
            }

            if (line.startsWith("*")) {
                if (inlist) {
                    lbuf.append("<li>");
                } else {
                    lbuf.append("<ul><li>");
                    inlist = true;
                }
                line = line.substring(1);
            }

            lbuf.append(line).append("\n");
        }

        if (inlist) {
            lbuf.append("</ul>");
        }
        if (inpara) {
            lbuf.append("</p>\n");
        }

        return lbuf.toString().trim();
    }

    /**
     * Restrict all HTML from the specified String.
     */
    public static String restrictHTML (String src)
    {
        return restrictHTML(src, new String[0]);
    }

    /**
     * Restrict HTML except for the specified tags.
     *
     * @param allowFormatting enables &lt;i&gt;, &lt;b&gt;, &lt;u&gt;,
     *                        &lt;font&gt;, &lt;br&gt;, &lt;p&gt;, and
     *                        &lt;hr&gt;.
     * @param allowImages enabled &lt;img ...&gt;.
     * @param allowLinks enabled &lt;a href ...&gt;.
     */
    public static String restrictHTML (String src, boolean allowFormatting,
        boolean allowImages, boolean allowLinks)
    {
        // TODO: these regexes should probably be checked to make
        // sure that javascript can't live inside a link
        ArrayList<String> allow = new ArrayList<String>();
        if (allowFormatting) {
            allow.add("<b>"); allow.add("</b>");
            allow.add("<i>"); allow.add("</i>");
            allow.add("<u>"); allow.add("</u>");
            allow.add("<font [^\"<>!-]*(\"[^\"<>!-]*\"[^\"<>!-]*)*>");
                allow.add("</font>");
            allow.add("<br>"); allow.add("</br>"); allow.add("<br/>");
            allow.add("<p>"); allow.add("</p>");
            allow.add("<hr>"); allow.add("</hr>"); allow.add("<hr/>");
        }
        if (allowImages) {
            // Until I find a way to disallow "---", no - can be in a url
            allow.add("<img [^\"<>!-]*(\"[^\"<>!-]*\"[^\"<>!-]*)*>");
                allow.add("</img>");
        }
        if (allowLinks) {
            allow.add("<a href=[^\"<>!-]*(\"[^\"<>!-]*\"[^\"<>!-]*)*>");
                allow.add("</a>");
        }
        return restrictHTML(src, allow.toArray(new String[allow.size()]));
    }

    /**
     * Restrict HTML from the specified string except for the specified
     * regular expressions.
     */
    public static String restrictHTML (String src, String[] regexes)
    {
        if (StringUtil.isBlank(src)) {
            return src;
        }

        ArrayList<String> list = new ArrayList<String>();
        list.add(src);
        for (String regexe : regexes) {
            Pattern p = Pattern.compile(regexe, Pattern.CASE_INSENSITIVE);
            for (int jj=0; jj < list.size(); jj += 2) {
                String piece = list.get(jj);
                Matcher m = p.matcher(piece);
                if (m.find()) {
                    list.set(jj, piece.substring(0, m.start()));
                    list.add(jj + 1, piece.substring(m.start(), m.end()));
                    list.add(jj + 2, piece.substring(m.end()));
                }
            }
        }

        // now, the even elements of list contain untrusted text, the
        // odd elements contain stuff that matched a regex
        StringBuilder buf = new StringBuilder();
        for (int jj=0, nn = list.size(); jj < nn; jj++) {
            String s = list.get(jj);
            if (jj % 2 == 0) {
                s = s.replace("<", "&lt;");
                s = s.replace(">", "&gt;");
            }
            buf.append(s);
        }
        return buf.toString();
    }

    protected static final Pattern URL_PAT = Pattern.compile("^http://\\S+", Pattern.MULTILINE);
}
