//
// $Id: SetFieldRule.java,v 1.1 2001/11/17 03:45:52 mdb Exp $
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
import java.util.HashMap;

import org.xml.sax.Attributes;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;

import com.samskivert.util.StringUtil;

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
        // make sure we've got something to set
        if (StringUtil.blank(_bodyText)) {
            if (digester.getDebug() >= 3) {
                digester.log("  Skipping set field for lack of " +
                             "body text [field=" + _fieldName + "].");
            }
            return;
        }

	Object top = digester.peek();
	if (digester.getDebug() >= 1) {
            digester.log("  Setting '" + _fieldName + "' to '" +
                         _bodyText + "' on '" + top + "'.");
        }

        Field field = top.getClass().getField(_fieldName);

        // look up an argument parser for the field type
        Parser parser = (Parser)_parsers.get(field.getType());
        if (parser == null) {
            String errmsg = "Don't know how to convert strings into " +
                "fields of type '" + field.getType() +
                "' [field=" + _fieldName + ", target=" + top + "].";
            throw new Exception(errmsg);
        }

        // parse the text and set the field
        field.set(top, parser.parse(_bodyText));
    }

    protected static interface Parser
    {
        public Object parse (String source) throws Exception;
    }

    protected String _fieldName;
    protected String _bodyText;

    protected static HashMap _parsers;

    protected static final int[] INT_ARRAY_PROTOTYPE = new int[0];
    protected static final String[] STRING_ARRAY_PROTOTYPE = new String[0];

    static {
        _parsers = new HashMap();

        // we can parse strings
        _parsers.put(String.class, new Parser() {
            public Object parse (String source) throws Exception {
                return source;
            }
        });

        // and ints
        _parsers.put(Integer.TYPE, new Parser() {
            public Object parse (String source) throws Exception {
                return Integer.valueOf(source);
            }
        });

        // and integers
        _parsers.put(Integer.class, new Parser() {
            public Object parse (String source) throws Exception {
                return Integer.valueOf(source);
            }
        });

        // and int arrays
        _parsers.put(INT_ARRAY_PROTOTYPE.getClass(), new Parser() {
            public Object parse (String source) throws Exception {
                return StringUtil.parseIntArray(source);
            }
        });

        // and string arrays, oh my!
        _parsers.put(STRING_ARRAY_PROTOTYPE.getClass(), new Parser() {
            public Object parse (String source) throws Exception {
                return StringUtil.parseStringArray(source);
            }
        });
    }
}
