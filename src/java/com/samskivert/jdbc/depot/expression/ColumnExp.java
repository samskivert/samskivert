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

package com.samskivert.jdbc.depot.expression;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.samskivert.jdbc.depot.QueryBuilderContext;
import com.samskivert.jdbc.depot.PersistentRecord;

/**
 * An expression identifying a column of a class, e.g. GameRecord.itemId. If no class is given,
 * no disambiguation occurs in the generated SQL.
 */
public class ColumnExp
    implements SQLExpression
{
    /** The table that hosts the column we reference, or null. */
    final public Class<? extends PersistentRecord> pClass;

    /** The name of the column we reference. */
    final public String pColumn;

    public ColumnExp (String column)
    {
        this(null, column);
    }

    public ColumnExp (Class<? extends PersistentRecord> c, String column)
    {
        super();
        pClass = c;
        this.pColumn = column;
    }

    // from SQLExpression
    public void appendExpression (QueryBuilderContext<?> query, StringBuilder builder)
    {
        if (pClass == null || query == null) {
            builder.append(pColumn);
        } else {
            String tRef = query.getTableAbbreviation(pClass);
            builder.append(tRef).append(".").append(pColumn);
        }
    }

    // from SQLExpression
    public int bindExpressionArguments (PreparedStatement pstmt, int argIdx)
        throws SQLException
    {
        return argIdx;
    }

}
