//
// $Id: SetNextFieldRule.java,v 1.3 2004/02/25 13:16:32 mdb Exp $
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001 Walter Korman
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

package com.samskivert.xml;

import java.lang.reflect.Field;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;

/**
 * Like the <code>SetNextRule</code> except that the object on the top of
 * the stack is placed into a field of the penultimate object.
 */
public class SetNextFieldRule extends Rule
{
    /**
     * Constructs a set field rule for the specified field.
     */
    public SetNextFieldRule (Digester digester, String fieldName)
    {
        super(digester);

        // keep this for later
        _fieldName = fieldName;
    }

    public void end ()
        throws Exception
    {
	// identify the objects to be used
	Object child = digester.peek(0);
	Object parent = digester.peek(1);
        Class pclass = parent.getClass();

	if (digester.getDebug() >= 1) {
	    digester.log("Set " + pclass.getName() + "." + _fieldName +
                         " = " + child);
        }

        // stuff the child object into the field of the parent
        Field field = pclass.getField(_fieldName);
        field.set(parent, child);
    }

    /**
     * Render a printable version of this rule.
     */
    public String toString ()
    {
        StringBuffer sb = new StringBuffer("SetNextFieldRule[");
        sb.append("fieldName=");
        sb.append(_fieldName);
        sb.append("]");
        return (sb.toString());
    }

    protected String _fieldName;
}
