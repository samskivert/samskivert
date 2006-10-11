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

/**
 * Encapsulates a key used to match persistent objects in a query.
 */
public class Key
{
    public String[] indices;
    public Comparable[] values;

    public Key (String index, Comparable value)
    {
        this(new String[] { index }, new Comparable[] { value });
    }

    public Key (String index1, Comparable value1,
                String index2, Comparable value2)
    {
        this(new String[] { index1, index2 },
             new Comparable[] { value1, value2 });
    }

    public Key (String[] indices, Comparable[] values)
    {
        this.indices = indices;
        this.values = values;
    }

    public String toWhereClause ()
    {
        StringBuilder where = new StringBuilder();
        for (int ii = 0; ii < indices.length; ii++) {
            if (ii > 0) {
                where.append(" and ");
            }
            where.append(indices[ii]).append(" = ?");
        }
        return where.toString();
    }

    public void bindArguments (PreparedStatement stmt, int startIdx)
        throws SQLException
    {
        for (int ii = 0; ii < indices.length; ii++) {
            stmt.setObject(startIdx++, values[ii]);
        }
    }
}
