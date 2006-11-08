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

package com.samskivert.jdbc.depot.clause;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import com.samskivert.jdbc.depot.Query;
import com.samskivert.jdbc.depot.clause.QueryClause;
import com.samskivert.jdbc.depot.operator.SQLOperator;

/**
 * Represents a where clause: the condition can be any comparison operator or logical combination
 * thereof.
 */
public class Where
    implements QueryClause
{
    public Where (SQLOperator condition)
    {
        _condition = condition;
    }

    // from QueryClause
    public List<Class> getClassSet()
    {
        return Arrays.asList(new Class[] { });
    }

    // from QueryClause
    public void appendClause (Query query, StringBuilder builder)
    {
        builder.append(" where ");
        _condition.appendExpression(query, builder);
    }

    // from QueryClause
    public int bindArguments (PreparedStatement pstmt, int argIdx)
        throws SQLException
    {
        return _condition.bindArguments(pstmt, argIdx);
    }

    protected SQLOperator _condition;
}
