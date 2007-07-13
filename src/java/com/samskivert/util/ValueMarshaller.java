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

package com.samskivert.util;

import java.awt.Color;
import java.util.HashMap;

/**
 * Provides a mechanism for converting a string representation of a value
 * into a Java object when provided with the type of the target object.
 * This is used to do things like populate object fields with values
 * parsed from an XML file and the like.
 */
public class ValueMarshaller
{
    /**
     * Attempts to convert the specified value to an instance of the
     * specified object type.
     *
     * @exception Exception thrown if no field parser exists for the
     * target type or if an error occurs while parsing the value.
     */
    public static Object unmarshal (Class type, String source)
        throws Exception
    {
        // look up an argument parser for the field type
        Parser parser = _parsers.get(type);
        if (parser == null) {
            String errmsg = "Don't know how to convert strings into " +
                "values of type '" + type + "'.";
            throw new Exception(errmsg);
        }
        return parser.parse(source);
    }

    protected static interface Parser
    {
        public Object parse (String source) throws Exception;
    }

    protected static HashMap<Class,Parser> _parsers;

    static {
        _parsers = new HashMap<Class,Parser>();

        // we can parse strings
        _parsers.put(String.class, new Parser() {
            public Object parse (String source) throws Exception {
                return source;
            }
        });

        // and bytes
        _parsers.put(Byte.TYPE, new Parser() {
            public Object parse (String source) throws Exception {
                return Byte.valueOf(source);
            }
        });

        // and shorts
        _parsers.put(Short.TYPE, new Parser() {
            public Object parse (String source) throws Exception {
                return Short.valueOf(source);
            }
        });

        // and ints
        _parsers.put(Integer.TYPE, new Parser() {
            public Object parse (String source) throws Exception {
                return Integer.valueOf(source);
            }
        });

        // and longs
        _parsers.put(Long.TYPE, new Parser() {
            public Object parse (String source) throws Exception {
                return Long.valueOf(source);
            }
        });
        
        // and floats
        _parsers.put(Float.TYPE, new Parser() {
            public Object parse (String source) throws Exception {
                return Float.valueOf(source);
            }
        });

        // and booleans
        _parsers.put(Boolean.TYPE, new Parser() {
            public Object parse (String source) throws Exception {
                return Boolean.valueOf(source);
            }
        });

        // and integers
        _parsers.put(Integer.class, new Parser() {
            public Object parse (String source) throws Exception {
                return Integer.valueOf(source);
            }
        });

        // and byte arrays
        _parsers.put(byte[].class, new Parser() {
            public Object parse (String source) throws Exception {
                int[] values = StringUtil.parseIntArray(source);
                int vcount = values.length;
                byte[] bytes = new byte[vcount];
                for (int ii = 0; ii < vcount; ii++) {
                    bytes[ii] = (byte)values[ii];
                }
                return bytes;
            }
        });

        // and int arrays
        _parsers.put(int[].class, new Parser() {
            public Object parse (String source) throws Exception {
                return StringUtil.parseIntArray(source);
            }
        });

        // and float arrays
        _parsers.put(float[].class, new Parser() {
            public Object parse (String source) throws Exception {
                return StringUtil.parseFloatArray(source);
            }
        });

        // and string arrays, oh my!
        _parsers.put(String[].class, new Parser() {
            public Object parse (String source) throws Exception {
                return StringUtil.parseStringArray(source);
            }
        });

        // and Color objects
        _parsers.put(Color.class, new Parser() {
            public Object parse (String source) throws Exception {
                if (source.length() == 0 || source.charAt(0) != '#') {
                    return new Color(Integer.parseInt(source, 16));
                } else {
                    return new Color(Integer.parseInt(source.substring(1), 16));
                }
            }
        });
    }
}
