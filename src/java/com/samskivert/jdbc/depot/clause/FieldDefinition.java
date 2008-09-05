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

import java.util.Collection;

import com.samskivert.jdbc.depot.PersistentRecord;
import com.samskivert.jdbc.depot.expression.ColumnExp;
import com.samskivert.jdbc.depot.expression.ExpressionVisitor;
import com.samskivert.jdbc.depot.expression.LiteralExp;
import com.samskivert.jdbc.depot.expression.SQLExpression;

/**
 * Supplies a definition for a computed field of the persistent object we're creating.
 *
 * Thus the select portion of a query can include a reference to a different column in a different
 * table through a {@link ColumnExp}, or a literal expression such as COUNT(*) through a
 * {@link LiteralExp}.
 *
 * @see FieldOverride
 */
public class FieldDefinition extends QueryClause
{
    public FieldDefinition (String field, String str)
    {
        this(field, new LiteralExp(str));
    }

    public FieldDefinition (String field, Class<? extends PersistentRecord> pClass, String pCol)
    {
        this(field, new ColumnExp(pClass, pCol));
    }

    public FieldDefinition (String field, SQLExpression override)
    {
        _field = field;
        _definition = override;
    }

    /**
     * The field we're defining. The Query object uses this for indexing.
     */
    public String getField ()
    {
        return _field;
    }

    public SQLExpression getDefinition ()
    {
        return _definition;
    }

    // from SQLExpression
    public void addClasses (Collection<Class<? extends PersistentRecord>> classSet)
    {
        _definition.addClasses(classSet);
    }

    // from SQLExpression
    public void accept (ExpressionVisitor visitor)
    {
        visitor.visit(this);
    }

    /** The name of the field on the persistent object to override. */
    protected String _field;

    /** The defining expression. */
    protected SQLExpression _definition;

}
