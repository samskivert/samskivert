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

import com.samskivert.jdbc.depot.Query;

/**
 *  Represents a LIMIT/OFFSET clause, for pagination.
 */
public class LimitClause
    implements QueryClause
{
    public LimitClause (int offset, int count)
    {
        super();
        _offset = offset;
        _count = count;
    }

    // from QueryClause
    public Collection<Class> getClassSet ()
    {
        return null;
    }

    // from QueryClause
    public void appendClause (Query query, StringBuilder builder)
    {
        builder.append("? offset ?");
    }

    // from QueryClause
    public int bindArguments (PreparedStatement pstmt, int argIdx)
        throws SQLException
    {
        pstmt.setObject(argIdx++, _count);
        pstmt.setObject(argIdx++, _offset);
        return argIdx;
    }
    
    /** The first row of the result set to return. */
    protected int _offset;

    /** The number of rows, at most, to return. */
    protected int _count;
}
