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
 * Represents an SQL expression, e.g. column name, function, or constant.
 */
public interface SQLExpression
{
    /**
     * Construct the SQL form of this expression. The implementor is invited to call methods on the
     * Query object to e.g. resolve the current table abbreviations associated with classes.
     */
    public void appendExpression (ConstructedQuery query, StringBuilder builder);

    /**
     * Bind any objects that were referenced in the generated SQL.  For each ? that appears in the
     * SQL, precisely one parameter must be claimed and bound in this method, and argIdx
     * incremented and returned.
     */
    public int bindArguments (PreparedStatement pstmt, int argIdx)
        throws SQLException;
}
