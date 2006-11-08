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
import com.samskivert.jdbc.depot.Query;
import com.samskivert.jdbc.depot.expression.ColumnExp;
import com.samskivert.jdbc.depot.operator.SQLOperator;
import com.samskivert.jdbc.depot.operator.Conditionals.*;

/**
 *  Represents a JOIN -- currently just an INNER one.
 */
public class Join
    implements QueryClause
{
    public Join (Class pClass, String pCol, Class joinClass, String jCol)
        throws PersistenceException
    {
        _joinClass = joinClass;
        _joinCondition = new Equals(new ColumnExp(joinClass, jCol), new ColumnExp(pClass, pCol));
    }

    public Join (ColumnExp primary, ColumnExp join)
        throws PersistenceException
    {
        _joinClass = join.pClass;
        _joinCondition = new Equals(primary, join);
    }

    public Join (Class joinClass, SQLOperator joinCondition)
    {
        _joinClass = joinClass;
        _joinCondition = joinCondition;
    }

    // from QueryClause
    public Collection<Class> getClassSet ()
    {
        return Arrays.asList(new Class[] { _joinClass });
    }

    // from QueryClause
    public void appendClause (Query query, StringBuilder builder)
    {
        builder.append(" inner join " );
        builder.append(query.getTableName(_joinClass)).append(" as ");
        builder.append(query.getTableAbbreviation(_joinClass)).append(" on ");
        _joinCondition.appendExpression(query, builder);
    }

    // from QueryClause
    public int bindArguments (PreparedStatement pstmt, int argIdx)
        throws SQLException
    {
        return _joinCondition.bindArguments(pstmt, argIdx);
    }

    /** The class of the table we're to join against. */
    protected Class _joinClass;

    /** The condition used to join in the new table. */
    protected SQLOperator _joinCondition;
}
