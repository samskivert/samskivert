//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

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
    public static Object unmarshal (Class<?> type, String source)
        throws Exception
    {
        if (type.isEnum()) {
            // we need to use a dummy enum type here as there's no way to ask Enum.valueOf to
            // execute on an existentially typed enum; it all works out under the hood
            @SuppressWarnings("unchecked") Class<Dummy> etype = (Class<Dummy>)type;
            return Enum.valueOf(etype, source); // may throw an exception
        }
        // look up an argument parser for the field type
        Parser parser = _parsers.get(type);
        if (parser == null) {
            throw new Exception(
                "Don't know how to convert strings into values of type '" + type + "'.");
        }
        return parser.parse(source);
    }

    protected static interface Parser
    {
        public Object parse (String source) throws Exception;
    }

    protected static Map<Class<?>, Parser> _parsers = new HashMap<Class<?>, Parser>();
    static {
        Parser p;
        // we can parse strings
        _parsers.put(String.class, new Parser() {
            public Object parse (String source) throws Exception {
                return source;
            }
        });

        // and bytes
        p = new Parser() {
            public Object parse (String source) throws Exception {
                return Byte.valueOf(source);
            }
        };
        _parsers.put(Byte.class, p);
        _parsers.put(Byte.TYPE, p);

        // and shorts
        p = new Parser() {
            public Object parse (String source) throws Exception {
                return Short.valueOf(source);
            }
        };
        _parsers.put(Short.class, p);
        _parsers.put(Short.TYPE, p);

        // and ints
        p = new Parser() {
            public Object parse (String source) throws Exception {
                return Integer.valueOf(source);
            }
        };
        _parsers.put(Integer.class, p);
        _parsers.put(Integer.TYPE, p);

        // and longs
        p = new Parser() {
            public Object parse (String source) throws Exception {
                return Long.valueOf(source);
            }
        };
        _parsers.put(Long.class, p);
        _parsers.put(Long.TYPE, p);

        // and floats
        p = new Parser() {
            public Object parse (String source) throws Exception {
                return Float.valueOf(source);
            }
        };
        _parsers.put(Float.class, p);
        _parsers.put(Float.TYPE, p);

        // and booleans
        p = new Parser() {
            public Object parse (String source) throws Exception {
                return Boolean.valueOf(source);
            }
        };
        _parsers.put(Boolean.class, p);
        _parsers.put(Boolean.TYPE, p);

        // and byte arrays
        _parsers.put(byte[].class, new Parser() {
            public Object parse (String source) throws Exception {
                String[] strs = StringUtil.parseStringArray(source);
                int count = strs.length;
                byte[] bytes = new byte[count];
                for (int ii = 0; ii < count; ii++) {
                    bytes[ii] = Byte.valueOf(strs[ii]);
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
                if (source.startsWith("#")) {
                    source = source.substring(1);
                }
                return new Color(Integer.parseInt(source, 16));
            }
        });
    }

    protected static enum Dummy {};
}
