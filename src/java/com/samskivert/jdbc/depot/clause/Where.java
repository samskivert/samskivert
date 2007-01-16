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

import com.samskivert.jdbc.depot.ConstructedQuery;
import com.samskivert.jdbc.depot.PersistentRecord;
import com.samskivert.jdbc.depot.clause.QueryClause;
import com.samskivert.jdbc.depot.expression.ColumnExp;
import com.samskivert.jdbc.depot.expression.ValueExp;
import com.samskivert.jdbc.depot.operator.Conditionals.Equals;
import com.samskivert.jdbc.depot.operator.Conditionals.IsNull;
import com.samskivert.jdbc.depot.operator.Logic.And;
import com.samskivert.jdbc.depot.operator.SQLOperator;

/**
 * Represents a where clause: the condition can be any comparison operator or logical combination
 * thereof.
 */
public class Where extends QueryClause
{
    public Where (String index, Comparable value)
    {
        this(new ColumnExp(index), value);
    }

    public Where (ColumnExp column, Comparable value)
    {
        this(new ColumnExp[] { column }, new Comparable[] { value });
    }

    public Where (ColumnExp index1, Comparable value1,
                ColumnExp index2, Comparable value2)
    {
        this(new ColumnExp[] { index1, index2 }, new Comparable[] { value1, value2 });
    }

    public Where (ColumnExp index1, Comparable value1,
                ColumnExp index2, Comparable value2,
                ColumnExp index3, Comparable value3)
    {
        this(new ColumnExp[] { index1, index2, index3 },
             new Comparable[] { value1, value2, value3 });
    }

    public Where (String index1, Comparable value1, String index2, Comparable value2)
    {
        this(new ColumnExp(index1), value1, new ColumnExp(index2), value2);
    }

    public Where (String index1, Comparable value1, String index2, Comparable value2,
                String index3, Comparable value3)
    {
        this(new ColumnExp(index1), value1, new ColumnExp(index2), value2, 
             new ColumnExp(index3), value3);
    }

    public Where (ColumnExp[] columns, Comparable[] values)
    {
        this(toCondition(columns, values));
    }

    public Where (SQLOperator condition)
    {
        _condition = condition;
    }

    // from QueryClause
    public void appendClause (ConstructedQuery<?> query, StringBuilder builder)
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

    protected static SQLOperator toCondition (ColumnExp[] columns, Comparable[] values)
    {
        SQLOperator[] comparisons = new SQLOperator[columns.length];
        for (int ii = 0; ii < columns.length; ii ++) {
            comparisons[ii] = (values[ii] == null) ? new IsNull(columns[ii]) :
                new Equals(columns[ii], new ValueExp(values[ii]));
        }
        return new And(comparisons);
    }

    protected SQLOperator _condition;
}
