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
import com.samskivert.jdbc.depot.expression.SQLExpression;

/**
 *  Represents an ORDER BY clause.
 */
public class OrderByClause
    implements QueryClause
{
    public OrderByClause (SQLExpression... values)
    {
        super();
        _values = values;
    }
    
    // from QueryClause
    public Collection<Class> getClassSet ()
    {
        return null;
    }

    // from QueryClause
    public void appendClause (Query query, StringBuilder builder)
    {
        for (int ii = 0; ii < _values.length; ii++) {
            if (ii > 0) {
                builder.append(", ");
            }
            _values[ii].appendExpression(query, builder);
        }
        builder.append(_ascending ? " ASC" : " DESC");
    }

    // from QueryClause
    public int bindArguments (PreparedStatement pstmt, int argIdx)
        throws SQLException
    {
        for (int ii = 0; ii < _values.length; ii++) {
            argIdx = _values[ii].bindArguments(pstmt, argIdx);
        }
        return argIdx;
    }
    
    /** The expressions that are generated for the clause. */
    protected SQLExpression[] _values;

    /** Whether the ordering is to be ascending rather than descending. */
    protected boolean _ascending;
}
