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

import com.samskivert.jdbc.depot.ConstructedQuery;

/**
 * An expression for a function, e.g. FLOOR(blah).
 */
public class FunctionExp
    implements SQLExpression
{
    /**
     * Create a new FunctionExp with the given function and arguments.
     */
    public FunctionExp (String function, SQLExpression... arguments)
    {
        _function = function;
        _arguments = arguments;
    }

    // from SQLExpression
    public void appendExpression (ConstructedQuery query, StringBuilder builder)
    {
        builder.append(_function);
        builder.append("(");
        for (int ii = 0; ii < _arguments.length; ii ++) {
            if (ii > 0) {
                builder.append(", ");
            }
            _arguments[ii].appendExpression(query, builder);
        }
        builder.append(")");
    }

    // from SQLExpression
    public int bindArguments (PreparedStatement pstmt, int argIdx)
        throws SQLException
    {
        for (int ii = 0; ii < _arguments.length; ii ++) {
            argIdx = _arguments[ii].bindArguments(pstmt, argIdx);
        }
        return argIdx;
    }

    /** The literal name of this function, e.g. FLOOR */
    protected String _function;

    /** The arguments to this function */
    protected SQLExpression[] _arguments;
}
