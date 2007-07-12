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

import com.samskivert.io.PersistenceException;
import com.samskivert.jdbc.depot.QueryBuilderContext;
import com.samskivert.jdbc.depot.PersistentRecord;
import com.samskivert.jdbc.depot.expression.ColumnExp;
import com.samskivert.jdbc.depot.expression.SQLExpression;
import com.samskivert.jdbc.depot.operator.Conditionals.*;

/**
 *  Represents a JOIN.
 */
public class Join extends QueryClause
{
    /** Indicates the join type to be used. The default is INNER. */
    public static enum Type { INNER, LEFT_OUTER, RIGHT_OUTER };

    public Join (Class<? extends PersistentRecord> pClass, String pCol,
                 Class<? extends PersistentRecord> joinClass, String jCol)
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

    public Join (Class<? extends PersistentRecord> joinClass, SQLExpression joinCondition)
    {
        _joinClass = joinClass;
        _joinCondition = joinCondition;
    }

    // from QueryClause
    public void addClasses (Collection<Class<? extends PersistentRecord>> classSet)
    {
        classSet.add(_joinClass);
    }

    /**
     * Configures the type of join to be performed.
     */
    public Join setType (Type type)
    {
        _type = type;
        return this;
    }

    // from QueryClause
    public void appendClause (QueryBuilderContext<?> query, StringBuilder builder)
    {
        switch (_type) {
        case INNER:
            builder.append(" inner join " );
            break;
        case LEFT_OUTER:
            builder.append(" left outer join " );
            break;
        case RIGHT_OUTER:
            builder.append(" right outer join " );
            break;
        }
        builder.append(query.getTableName(_joinClass)).append(" as ");
        builder.append(query.getTableAbbreviation(_joinClass)).append(" on ");
        _joinCondition.appendExpression(query, builder);
    }

    // from QueryClause
    public int bindClauseArguments (PreparedStatement pstmt, int argIdx)
        throws SQLException
    {
        return _joinCondition.bindExpressionArguments(pstmt, argIdx);
    }

    /** Indicates the type of join to be performed. */
    protected Type _type = Type.INNER;

    /** The class of the table we're to join against. */
    protected Class<? extends PersistentRecord> _joinClass;

    /** The condition used to join in the new table. */
    protected SQLExpression _joinCondition;
}
