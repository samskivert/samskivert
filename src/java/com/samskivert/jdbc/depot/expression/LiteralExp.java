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

package com.samskivert.jdbc.depot.expression;

import java.util.Collection;

import com.samskivert.jdbc.depot.PersistentRecord;

/**
 * An expression for things we don't support natively, e.g. COUNT(*).
 */
public class LiteralExp
    implements SQLExpression
{
    public LiteralExp (String text)
    {
        super();
        _text = text;
    }

    // from SQLExpression
    public void accept (ExpressionVisitor builder) throws Exception
    {
        builder.visit(this);
    }

    // from SQLExpression
    public void addClasses (Collection<Class<? extends PersistentRecord>> classSet)
    {
    }

    public String getText ()
    {
        return _text;
    }

    /** The literal text of this expression, e.g. COUNT(*) */
    protected String _text;
}
