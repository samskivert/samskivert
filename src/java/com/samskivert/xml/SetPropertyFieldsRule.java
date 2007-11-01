//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2007 Michael Bayne
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
import java.util.HashMap;

import org.xml.sax.Attributes;
import org.apache.commons.digester.Rule;

import com.samskivert.util.StringUtil;
import com.samskivert.util.ValueMarshaller;

/**
 * Sets the fields in the object on the top of the stack from the attributes available in the
 * matched element.
 */
public class SetPropertyFieldsRule extends Rule
{
    /**
     * Used to supplied custom code to parse a property.
     *
     * @see #addFieldParser
     */
    public static interface FieldParser
    {
        public Object parse (String property) throws Exception;
    }

    /**
     * Constructs a set property fields rule.
     */
    public SetPropertyFieldsRule ()
    {
        this(true);
    }

    /**
     * Constructs a set property fields rule.
     */
    public SetPropertyFieldsRule (boolean warnNonFields)
    {
        _warnNonFields = warnNonFields;
    }

    /**
     * Adds a custom parser for the specified named field.
     */
    public void addFieldParser (String property, FieldParser parser)
    {
        if (_parsers == null) {
            _parsers = new HashMap<String,FieldParser>();
        }
        _parsers.put(property, parser);
    }

    /**
     * Configures this rule to warn or not when it skips properties for which there are no
     * associated object fields.
     */
    public void setWarnNonFields (boolean warnNonFields)
    {
        _warnNonFields = warnNonFields;
    }

    public void begin (String namespace, String name, Attributes attrs)
        throws Exception
    {
        Object top = digester.peek();
        Class topclass = top.getClass();

        // iterate over the attributes, setting public fields where applicable
	for (int i = 0; i < attrs.getLength(); i++) {
	    String lname = attrs.getLocalName(i);
            if (StringUtil.isBlank(lname)) {
                lname = attrs.getQName(i);
            }

            // look for a public field with this lname
            Field field = null;
            try {
                field = topclass.getField(lname);
            } catch (NoSuchFieldException nsfe) {
                if (_warnNonFields) {
                    digester.getLogger().warn(
                        "Skipping property '" + lname + "' for which there is no field.");
                }
                continue;
            } 

            // convert the value into the appropriate object type
	    String valstr = attrs.getValue(i);
            FieldParser parser = null;
            Object value;

            // look for a custom field parser
            if ((_parsers != null) &&
                ((parser = _parsers.get(lname)) != null)) {
                value = parser.parse(valstr);
            } else {
                // otherwise use the value marshaller to parse the property based on the type of
                // the target object field
                value = ValueMarshaller.unmarshal(field.getType(), valstr);
            }

            if (digester.getLogger().isDebugEnabled()) {
                digester.getLogger().debug(
                    "  Setting property '" + lname + "' to '" + valstr + "'");
            }

            // and finally set the field
            field.set(top, value);
        }
    }

    protected HashMap<String,FieldParser> _parsers;
    protected boolean _warnNonFields;
}
