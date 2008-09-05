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
 * An expression for a function, e.g. FLOOR(blah).
 */
public class FunctionExp implements SQLExpression
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
    public void accept (ExpressionVisitor builder)
    {
        builder.visit(this);
    }

    // from SQLExpression
    public void addClasses (Collection<Class<? extends PersistentRecord>> classSet)
    {
        for (int ii = 0; ii < _arguments.length; ii ++) {
            _arguments[ii].addClasses(classSet);
        }
    }

    public String getFunction ()
    {
        return _function;
    }

    public SQLExpression[] getArguments ()
    {
        return _arguments;
    }

    /** The literal name of this function, e.g. FLOOR */
    protected String _function;

    /** The arguments to this function */
    protected SQLExpression[] _arguments;
}
