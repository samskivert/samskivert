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

package com.samskivert.jdbc.depot;

/**
 * An augmented cache invalidator interface for invalidators that can ensure that they are
 * operating on the proper persistent record class.
 */
public interface ValidatingCacheInvalidator extends CacheInvalidator
{
    /**
     * Validates that this invalidator operates on the supplied persistent record class. This helps
     * to catch programmer errors where one record type is used for a query clause and another is
     * used for the cache invalidator.
     *
     * @exception IllegalArgumentException thrown if the supplied persistent record class does not
     * match the class that this invalidator will flush from the cache.
     */
    public void validateFlushType (Class<?> pClass);
}
