//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2006-2007 Michael Bayne, Pär Winzell
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
 * Implementors of this interface performs perform cache invalidation for calls to
 * {@link DepotRepository#updateLiteral}, {@link DepotRepository#updatePartial} and 
 * {@link DepotRepository#deleteAll).
 */
public interface CacheInvalidator
{
    /**
     * Must invalidate all cache entries that depend on the records being modified or deleted.
     * This method is called just before the database statement is executed.
     */
    public void invalidate (PersistenceContext ctx);
}
