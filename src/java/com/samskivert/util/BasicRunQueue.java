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

import static com.samskivert.Log.log;

/**
 * A very basic implementation of RunQueue for general purpose use.
 */
public class BasicRunQueue extends LoopingThread
    implements RunQueue
{
    /**
     * Construct a BasicRunQueue with a default Queue implementation.
     */
    public BasicRunQueue ()
    {
        super("RunQueue");
        _queue = new Queue<Runnable>();
    }
    
    // documentation inherited from interface
    public void postRunnable (Runnable r)
    {
        _queue.append(r);
    }

    // documentation inherited from interface
    public boolean isDispatchThread ()
    {
        return Thread.currentThread() == this;
    }

    // documentation inherited
    protected void iterate ()
    {
        Runnable r = _queue.get();
        try {
            r.run();

        } catch (Throwable t) {
            log.warning("Runnable posted to RunQueue barfed.", t);
        }
    }

    // documentation inherited
    protected void kick ()
    {
        postRunnable(new Runnable() {
            public void run () {
                // nothing
            }
        });
    }

    /** The queue of things to run. */
    protected Queue<Runnable> _queue;
}
