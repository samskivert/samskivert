//
// $Id: SetFieldRule.java,v 1.5 2004/02/25 13:16:32 mdb Exp $
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

import java.lang.NoSuchFieldException;
import java.lang.reflect.Field;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;

import com.samskivert.util.ValueMarshaller;

/**
 * Sets a field in the object on the top of the stack with a value parsed
 * from the body of an element.
 */
public class SetFieldRule extends Rule
{
    /**
     * Constructs a set field rule for the specified field.
     */
    public SetFieldRule (Digester digester, String fieldName)
    {
        super(digester);

        // keep this for later
        _fieldName = fieldName;
    }

    public void body (String bodyText)
        throws Exception
    {
        _bodyText = bodyText.trim();
    }

    public void end ()
        throws Exception
    {
	Object top = digester.peek();
	if (digester.getDebug() >= 1) {
            digester.log("  Setting '" + _fieldName + "' to '" +
                         _bodyText + "' on '" + top + "'.");
        }

        // convert the source string into an object and set the field
        try {
            Field field = top.getClass().getField(_fieldName);
            Object value = ValueMarshaller.unmarshal(
                field.getType(), _bodyText);
            field.set(top, value);
        } catch (NoSuchFieldException nsfe) {
            digester.log("No such field: " + top.getClass() + "." + _fieldName);
        }
    }

    protected String _fieldName;
    protected String _bodyText;
}
