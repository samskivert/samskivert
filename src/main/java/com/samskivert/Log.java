//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert;

import com.samskivert.util.Logger;

/**
 * Contains a reference to the log object used by the samskivert package.
 */
public class Log
{
    /** We dispatch our log messages through this logger. */
    public static final Logger log = Logger.getLogger("com.samskivert");
}
