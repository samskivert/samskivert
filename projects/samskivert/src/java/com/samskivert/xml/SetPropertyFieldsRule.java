//
// $Id: SetPropertyFieldsRule.java,v 1.2 2001/12/13 01:31:23 mdb Exp $
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

import org.xml.sax.Attributes;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;

import com.samskivert.util.StringUtil;
import com.samskivert.util.ValueMarshaller;

/**
 * Sets the fields in the object on the top of the stack from the
 * attributes available in the matched element.
 */
public class SetPropertyFieldsRule extends Rule
{
    /**
     * Constructs a set property fields rule.
     */
    public SetPropertyFieldsRule (Digester digester)
    {
        super(digester);
    }

    public void begin (Attributes attrs)
        throws Exception
    {
        Object top = digester.peek();
        Class topclass = top.getClass();

        // iterate over the attributes, setting public fields where
        // applicable
	for (int i = 0; i < attrs.getLength(); i++) {
	    String name = attrs.getLocalName(i);
            if (StringUtil.blank(name)) {
                name = attrs.getQName(i);
            }

            // look for a public field with this name
            Field field = null;
            try {
                field = topclass.getField(name);
            } catch (NoSuchFieldException nsfe) {
                digester.log("Skipping property '" + name +
                             "' for which there is no field.");
                continue;
            } 

            // convert the value into the appropriate object type
	    String valstr = attrs.getValue(i);
            Object value = ValueMarshaller.unmarshal(field.getType(), valstr);
            if (digester.getDebug() >= 9) {
                digester.log("  Setting property '" + name + "' to '" +
                             valstr + "'");
            }

            // and finally set the field
            field.set(top, value);
	}
    }
}
