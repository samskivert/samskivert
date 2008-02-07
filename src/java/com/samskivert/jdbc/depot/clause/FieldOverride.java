//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2006-2007 Michael Bayne, Pär Winzell
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

import com.samskivert.jdbc.depot.PersistentRecord;
import com.samskivert.jdbc.depot.expression.ColumnExp;
import com.samskivert.jdbc.depot.expression.LiteralExp;
import com.samskivert.jdbc.depot.expression.SQLExpression;

/**
 * Redirects one field of the persistent object we're creating from its default associated column
 * to a general {@link SQLExpression}.
 *
 * Thus the select portion of a query can include a reference to a different column in a different
 * table through a {@link ColumnExp}, or a literal expression such as COUNT(*) through a
 * {@link LiteralExp}.
 */
public class FieldOverride extends FieldDefinition
{
    public FieldOverride (String field, String str)
    {
        super(field, str);
    }

    public FieldOverride (String field, Class<? extends PersistentRecord> pClass, String pCol)
    {
        super(field, pClass, pCol);
    }

    public FieldOverride (String field, SQLExpression override)
    {
        super(field, override);
    }
}
