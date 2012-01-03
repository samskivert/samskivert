//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import java.awt.Dimension;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.samskivert.io.StreamUtil;

/**
 * Provides access to the very platform specific concept of the terminal
 * (ie. vt100, xterm, etc.) in which our code is running. Bear in mind
 * that applications will commonly not be running in a terminal and this
 * code should only be used when the operating environment is well known.
 * It is generally useful to make things behave more nicely for the
 * developer while developing.
 */
public class TermUtil
{
    /** VT100 formatting code to enabled bold text. */
    public static final String BOLD = "\033[1m";

    /** VT100 formatting code to enabled reverse video text. */
    public static final String REVERSE = "\033[7m";

    /** VT100 formatting code to enabled underlined text. */
    public static final String UNDERLINE = "\033[4m";

    /** VT100 formatting code to revert to plain text. */
    public static final String PLAIN = "\033[m";

    /**
     * Returns a string that wraps the supplied string in the VT100 esacpe
     * codes necessary to make it bold.
     */
    public static String makeBold (String text)
    {
        return BOLD + text + PLAIN;
    }

    /**
     * Returns a string that wraps the supplied string in the VT100 esacpe
     * codes necessary to make it underlined.
     */
    public static String makeUnderlined (String text)
    {
        return UNDERLINE + text + PLAIN;
    }

    /**
     * Returns a string that wraps the supplied string in the VT100 esacpe
     * codes necessary to display it in reverse video.
     */
    public static String makeReverseVideo (String text)
    {
        return REVERSE + text + PLAIN;
    }

    /**
     * Attempts to obtain the dimensions of the terminal in which the
     * application is running (the units are columns by lines, for example
     * 80 by 24).
     *
     * @return the terminal dimensions or null if we were unable to obtain
     * the dimensions.
     */
    public static Dimension getTerminalSize ()
    {
        // we may eventually have a variety of methods for obtaining
        // terminal size, but for now there's only one
        return getSizeViaResize();
    }

    /**
     * Tries to obtain the terminal dimensions by running 'resize'.
     */
    protected static Dimension getSizeViaResize ()
    {
        BufferedReader bin = null;
        try {
            Process proc = Runtime.getRuntime().exec("resize");
            InputStream in = proc.getInputStream();
            bin = new BufferedReader(new InputStreamReader(in));
            Pattern regex = Pattern.compile("([0-9]+)");
            String line;
            int columns = -1, lines = -1;
            while ((line = bin.readLine()) != null) {
                if (line.indexOf("COLUMNS") != -1) {
                    Matcher match = regex.matcher(line);
                    if (match.find()) {
                        columns = safeToInt(match.group());
                    }
                } else if (line.indexOf("LINES") != -1) {
                    Matcher match = regex.matcher(line);
                    if (match.find()) {
                        lines = safeToInt(match.group());
                    }
                }
            }

            if (columns != -1 && lines != -1) {
                return new Dimension(columns, lines);
            }
            return null;

        } catch (PatternSyntaxException pse) {
            return null; // logging a warning here may be annoying
        } catch (SecurityException se) {
            return null; // logging a warning here may be annoying
        } catch (IOException ioe) {
            return null; // logging a warning here may be annoying
        } finally {
            StreamUtil.close(bin);
        }
    }

    /** Converts the string to an integer, returning -1 on any error. */
    protected static int safeToInt (String intstr)
    {
        if (!StringUtil.isBlank(intstr)) {
            try {
                return Integer.parseInt(intstr);
            } catch (NumberFormatException nfe) {
            }
        }
        return -1;
    }
}
