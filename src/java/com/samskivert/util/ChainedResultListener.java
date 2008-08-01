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

/**
 * A result listener that contains another result listener to which failure is
 * passed directly, but allows for success to be handled in whatever way is
 * desired by the chaining result listener.
 */
public abstract class ChainedResultListener<T,TT>
    implements ResultListener<T>
{
    /**
     * Creates a chained result listener that will pass failure through to the
     * specified target.
     */
    public ChainedResultListener (ResultListener<TT> target)
    {
        _target = target;
    }

    // documentation inherited from interface ResultListener
    public void requestFailed (Exception cause)
    {
        _target.requestFailed(cause);
    }

    protected ResultListener<TT> _target;
}
