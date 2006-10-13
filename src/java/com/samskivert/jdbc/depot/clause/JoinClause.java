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

/**
 *  Represents a JOIN -- currently just an INNER one.
 */
public class JoinClause
    implements QueryClause
{
    public JoinClause (String pCol, Class joinClass, String jCol)
        throws PersistenceException
    {
        super();
        _primaryColumn = pCol;
        _joinClass = joinClass;
        _joinColumn = jCol;
    }

    // from QueryClause
    public Collection<Class> getClassSet () {
        return Arrays.asList(new Class[] { _joinClass });
    }

    // from QueryClause
    public void appendClause (Query query, StringBuilder builder)
    {
        String jAbbrev = query.getTableAbbreviation(_joinClass);
        builder.append(query.getTableName(_joinClass) + " as " + jAbbrev + " on T." +
                       _primaryColumn + " = " + jAbbrev + "." + _joinColumn);
    }

    // from QueryClause
    public int bindArguments (PreparedStatement pstmt, int argIdx)
        throws SQLException
    {
        return argIdx;
    }

    /** The column on which to join. */
    protected String _primaryColumn;

    /** The class of the table we're to join against. */
    protected Class _joinClass;

    /** The column we're joining against. */
    protected String _joinColumn;
}
