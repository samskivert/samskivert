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

import java.awt.EventQueue;

/**
 * An interface for a service that queues up execution of Runnables.
 */
public interface RunQueue
{
    /** A useful RunQueue that uses the AWT dispatch thread. */
    public static final RunQueue AWT = new RunQueue() {
        public void postRunnable (Runnable r) {
            EventQueue.invokeLater(r);
        }
        public boolean isDispatchThread () {
            return EventQueue.isDispatchThread();
        }
        public boolean isRunning () {
            return true;
        }
    };

    /**
     * Post the specified Runnable to be run on the RunQueue.
     */
    void postRunnable (Runnable r);

    /**
     * @return true if the calling thread is the RunQueue dispatch thread.
     */
    boolean isDispatchThread ();

    /**
     * @return true if this run queue is still processing runnables, false if it has been shutdown.
     */
    boolean isRunning ();
}
