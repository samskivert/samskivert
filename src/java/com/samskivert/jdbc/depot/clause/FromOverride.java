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
import java.util.Arrays;
import java.util.Collection;

import com.samskivert.io.PersistenceException;
import com.samskivert.jdbc.depot.ConstructedQuery;

/**
 *  Completely overrides the FROM clause, if it exists.
 */
public class FromOverride
    implements QueryClause
{
    public FromOverride (Class... fromClasses)
        throws PersistenceException
    {
        _fromClasses = fromClasses;
    }

    // from QueryClause
    public Collection<Class> getClassSet ()
    {
        return Arrays.asList(_fromClasses);
    }

    // from QueryClause
    public void appendClause (ConstructedQuery query, StringBuilder builder)
    {
        builder.append(" from " );
        for (int ii = 0; ii < _fromClasses.length; ii++) {
            if (ii > 0) {
                builder.append(", ");
            }
            builder.append(query.getTableName(_fromClasses[ii])).
                append(" as ").
                append(query.getTableAbbreviation(_fromClasses[ii]));
        }
    }

    // from QueryClause
    public int bindArguments (PreparedStatement pstmt, int argIdx)
        throws SQLException
    {
        return argIdx;
    }

    /** The classes of the tables we're selecting from. */
    protected Class[] _fromClasses;
}
