//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2007 Michael Bayne
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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A ResultListener that does nothing on success and logs a warning message on failure, that's all.
 */
public class ComplainingListener<T>
    implements ResultListener<T>
{
    public ComplainingListener (Log log, String errorText)
    {
        _log = log;
        _errorText = errorText;
    }

    public ComplainingListener (Logger logger, String errorText)
    {
        _logger = logger;
        _errorText = errorText;
    }

    // documentation inherited from interface ResultListener
    public void requestCompleted (T result) { /* nada */ }

    // documentation inherited from interface ResultListener
    public void requestFailed (Exception cause)
    {
        if (_log != null) {
            _log.warning(_errorText + " [cause=" + cause + "].");
        } else if (_logger != null) {
            _logger.log(Level.WARNING, _errorText, cause);
        } else {
            System.err.println(_errorText + " [cause=" + cause + "].");
        }
    }

    /** The log to which we'll log our error, may be null. */
    protected Log _log;

    /** The logger to which we'll log our error, may be null. */
    protected Logger _logger;

    /** The text to output if the error happens. */
    protected String _errorText;
}
