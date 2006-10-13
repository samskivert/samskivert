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
import com.samskivert.jdbc.depot.Query;
import com.samskivert.jdbc.depot.expression.SQLExpression;

/**
 * Redirects one field of the persistent object we're creating from its default associated column
 * to a general {@link SQLExpression}.
 * 
 * Thus the select portion of a query can include a reference to a different column in a different
 * table through a {@link ColumnExpression}, or a literal expression such as COUNT(*) through a
 * {@link LiteralExpression}.
 */ 
public class FieldOverrideClause
    implements QueryClause
{
    public FieldOverrideClause (String field, SQLExpression override)
        throws PersistenceException
    {
        super();
        _field = field;
        _override = override;
    }

    /** The field we're overriding. The Query object uses this for indexing. */
    public String getField ()
    {
        return _field;
    }

    // from QueryClause
    public Collection<Class> getClassSet ()
    {
        return null;
    }

    // from QueryClause
    public void appendClause (Query query, StringBuilder builder)
    {
        _override.appendExpression(query, builder);
    }

    // from QueryClause
    public int bindArguments (PreparedStatement pstmt, int argIdx)
        throws SQLException
    {
        return argIdx;
    }
    
    /** The name of the field on the persistent object to override. */
    protected String _field;

    /** The overriding expression. */
    protected SQLExpression _override;
}
