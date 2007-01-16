//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2006 Michael Bayne, Pär Winzell
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

package com.samskivert.jdbc.depot.clause;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;

import com.samskivert.jdbc.depot.ConstructedQuery;
import com.samskivert.jdbc.depot.PersistentRecord;

/**
 * Represents a piece or modifier of an SQL query.
 */
public abstract class QueryClause
{
    /**
     * Return a set of all persistent classes referenced by this clause. The default implementation
     * returns null to indicate that no classes are rererenced.
     */
    public Collection<Class<? extends PersistentRecord>> getClassSet ()
    {
        return null;
    }
    
    /**
     * Construct the SQL form of this query clause. The implementor is expected to call methods on
     * the Query object to e.g. resolve current table abbreviations associated with classes.
     */
    public abstract void appendClause (ConstructedQuery<?> query, StringBuilder builder);

    /**
     * Bind any objects that were referenced in the generated SQL.  For each ? that appears in the
     * SQL, precisely one parameter must be claimed and bound in this method, and argIdx
     * incremented and returned. The default implementation binds nothing.
     */
    public int bindArguments (PreparedStatement pstmt, int argIdx)
        throws SQLException
    {
        return argIdx;
    }
}
