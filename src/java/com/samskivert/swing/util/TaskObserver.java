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

package com.samskivert.swing.util;

/**
 * The task observer interface provides the means by which the task master
 * can communicate the success or failure of a task invocation back to the
 * originator of a task.
 */
public interface TaskObserver
{
    /**
     * If the task successfully runs to completion and returns a result,
     * this member function will be called on the supplied observer.
     *
     * @param name The name under which the task was originally invoked.
     * @param result The result returned by the task's
     * <code>invoke()</code> method.
     */
    public void taskCompleted (String name, Object result);

    /**
     * If the task fails to run to completion and instead throws an
     * exception, this member function will be called on the supplied
     * observer.
     *
     * @param name The name under which the task was originally invoked.
     * @param exception The exception thrown by the task during the call
     * to <code>invoke()</code>.
     */
    public void taskFailed (String name, Throwable exception);
}
