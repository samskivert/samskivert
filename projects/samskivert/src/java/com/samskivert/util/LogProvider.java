//
// $Id: LogProvider.java,v 1.4 2002/11/21 22:41:53 mdb Exp $
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

package com.samskivert.util;

/**
 * The log provider interface allows the simple logging services provided
 * to the samskivert codebase to be mapped onto an actual logging
 * framework.
 */
public interface LogProvider
{
    /**
     * Log a message at the specified level for the specified module, if
     * messages are enabled for that particular combination.
     */
    public void log (int level, String moduleName, String message);

    /**
     * Log the stack trace of the supplied throwable at the specified
     * level for the specified module, if messages are enabled for that
     * particular combination.
     */
    public void logStackTrace (int level, String moduleName, Throwable t);

    /**
     * Set the log level for the specified module to the specified
     * level. The log services assume that all messages at or higher than
     * the specified level will be logged.
     */
    public void setLevel (String moduleName, int level);

    /**
     * Set the log level for all modules to the specified level. The log
     * services assume that all messages at or higher than the specified
     * level will be logged.
     */
    public void setLevel (int level);

    /**
     * Returns the log level for the specified module.
     */
    public int getLevel (String moduleName);

    /**
     * Returns the default log level for all modules.
     */
    public int getLevel ();
}
