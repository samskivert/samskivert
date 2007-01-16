//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2006 Michael Bayne
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

import java.sql.Connection;
import java.sql.SQLException;

/**
 * The base of all read-only queries.
 */
public interface Query<T>
{
    /**
     * Returns the {@link CacheKey} associated with this query, if relevant, or null.
     */
    public CacheKey getCacheKey ();

    /**
     * Performs the actual JDBC operations associated with this query.
     */
    public T invoke (Connection conn)
        throws SQLException;

    /**
     * Overriden by subclasses to perform special operations when the query would return a cache
     * hit. The value may be mutated, modified, or null may be return to force a database hit.
     */
    public T transformCacheHit (CacheKey key, T value);

    /**
     * Overriden by subclasses to perform case-by-case cache updates.
     */
    public void updateCache (PersistenceContext ctx, T result);
}
