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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import com.samskivert.jdbc.depot.clause.QueryClause;
import com.samskivert.jdbc.depot.expression.ColumnExpression;


/**
 * Encapsulates a key used to match persistent objects in a query.
 *
 * TODO: Is it useful for this to implement QueryClause? Can it be meaningfully put on a peer level
 * with the other clauses?
 */
public class Key
    implements QueryClause
{
    public ColumnExpression[] columns;
    public Comparable[] values;

    public Key (String index, Comparable value)
    {
        this(new ColumnExpression(null, index), value);
    }

    public Key (ColumnExpression column, Comparable value)
    {
        this(new ColumnExpression[] { column }, new Comparable[] { value });
    }

    public Key (String index1, Comparable value1, String index2, Comparable value2)
    {
        this(new ColumnExpression[] { new ColumnExpression(null, index1),
                                      new ColumnExpression(null, index2) },
             new Comparable[] { value1, value2 });
    }

    public Key (ColumnExpression[] columns, Comparable[] values)
    {
        this.columns = columns;
        this.values = values;
    }
    
    public List<Class> getClassSet() {
        return Arrays.asList(new Class[] { });
    }

    public void appendClause (Query query, StringBuilder builder)
    {
        for (int ii = 0; ii < columns.length; ii++) {
            if (ii > 0) {
                builder.append(" and ");
            }
            columns[ii].appendExpression(query, builder);
            builder.append(" = ?");
        }
    }

    public int bindArguments (PreparedStatement stmt, int startIdx)
        throws SQLException
    {
        for (int ii = 0; ii < values.length; ii++) {
            stmt.setObject(startIdx++, values[ii]);
        }
        return startIdx;
    }
}
