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

import com.samskivert.jdbc.depot.Query;

/**
 * An expression identifying a column of a class, e.g. GameRecord.itemId. If no class is given, no
 * disambiguation occurs in the generated SQL.
 */
public class ColumnExpression
    implements SQLExpression
{
    public ColumnExpression (String column)
    {
        this(null, column);
    }

    public ColumnExpression (Class c, String column)
    {
        super();
        _class = c;
        _column = column;
    }

    // from SQLExpression
    public void appendExpression (Query query, StringBuilder builder)
    {
        if (_class == null || query == null) {
            builder.append(_column);
        } else {
            String tRef = query.getTableAbbreviation(_class);
            builder.append(tRef).append(".").append(_column);
        }
    }

    // from SQLExpression
    public int bindArguments (PreparedStatement pstmt, int argIdx)
        throws SQLException
    {
        return argIdx;
    }

    /** The table that hosts the column we reference, or null. */
    protected Class _class;

    /** The name of the column we reference. */
    protected String _column;
}
