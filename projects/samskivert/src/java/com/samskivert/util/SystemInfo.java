//
// $Id: SystemInfo.java,v 1.1 2003/01/14 22:07:05 shaper Exp $
//
// samskivert library - useful routines for java programs
// Copyright (C) 2003 Walter Korman
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

package com.samskivert.util;

import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

/**
 * A record detailing that gathers and makes available various useful bits
 * of system information.  The record is populated with the current system
 * statistics at construction time, and may be refreshed as desired with a
 * call to {@link #update}.
 */
public class SystemInfo
{
    /** The operating system name. */
    public String osName;

    /** The operating system version. */
    public String osVersion;

    /** The operating system architecture. */
    public String osArch;

    /** The JVM version. */
    public String javaVersion;

    /** The JVM vendor. */
    public String javaVendor;

    /** The amount of free memory in kilobytes. */
    public long freeMemory;

    /** The amount of used memory in kilobytes. */
    public long usedMemory;

    /** The amount of total memory available in kilobytes. */
    public long totalMemory;

    /** The maximum amount of memory available in kilobytes. */
    public long maxMemory;

    /** The video display bit depth; see {@link DisplayMode}. */
    public int bitDepth;

    /** The video display refresh rate in kHz; see {@link DisplayMode}. */
    public int refreshRate;

    /** Whether the video display is currently set to full-screen mode. */
    public boolean isFullScreen;

    /** The video display width in pixels. */
    public int displayWidth;

    /** The video display height in pixels. */
    public int displayHeight;

    /**
     * Constructs a system info object.
     */
    public SystemInfo ()
    {
        // gather initial system info
        update();
    }

    /**
     * Updates the system info record's statistics to reflect the current
     * state of the JVM.
     */
    public void update ()
    {
        // grab the various system properties
        osName = System.getProperty("os.name");
        osVersion = System.getProperty("os.version");
        osArch = System.getProperty("os.arch");
        javaVersion = System.getProperty("java.version");
        javaVendor = System.getProperty("java.vendor");

        // determine memory usage
        Runtime rtime = Runtime.getRuntime();
        freeMemory = rtime.freeMemory() / 1024;
        totalMemory = rtime.totalMemory() / 1024;
        usedMemory = totalMemory - freeMemory;
        maxMemory = rtime.maxMemory() / 1024;

        // determine video display information
        GraphicsEnvironment env =
            GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = env.getDefaultScreenDevice();
        DisplayMode mode = gd.getDisplayMode();
        bitDepth = mode.getBitDepth();
        refreshRate = mode.getRefreshRate() / 1024;
        isFullScreen = (gd.getFullScreenWindow() != null);
        displayWidth = mode.getWidth();
        displayHeight = mode.getHeight();
    }

    /**
     * Returns a string representation of this instance.
     */
    public String toString ()
    {
        StringBuffer buf = new StringBuffer();

        // hardware and platform information
        buf.append("OS: ").append(osName).append(" (").
            append(osVersion).append("-").append(osArch).append(")\n");
        buf.append("JVM: ").append(javaVersion).
            append(", ").append(javaVendor).append("\n");

        // memory information
        buf.append("Memory: ").append(freeMemory).append("k free, ").
            append(usedMemory).append("k used, ").
            append(totalMemory).append("k total, ").
            append(maxMemory).append("k max\n");

        // video information
        String sbitDepth = (bitDepth == -1) ? "unknown bit depth" :
            bitDepth + "-bit";
        String srefreshRate =
            (refreshRate == DisplayMode.REFRESH_RATE_UNKNOWN) ?
            "unknown refresh rate" : (refreshRate + "kHz");
        String sfullScreen = (isFullScreen) ? "full-screen" : "windowed";
        buf.append("Display: ").append(displayWidth).append("x").
            append(displayHeight).append(", ").append(sbitDepth).
            append(", ").append(srefreshRate).append(", ").append(sfullScreen);

        return buf.toString();
    }
}
