//
// $Id: ValueMarshaller.java,v 1.8 2003/02/06 22:47:41 mdb Exp $

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

    protected static final byte[] BYTE_ARRAY_PROTOTYPE = new byte[0];
    protected static final int[] INT_ARRAY_PROTOTYPE = new int[0];
    protected static final float[] FLOAT_ARRAY_PROTOTYPE = new float[0];
    protected static final String[] STRING_ARRAY_PROTOTYPE = new String[0];

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
        _parsers.put(BYTE_ARRAY_PROTOTYPE.getClass(), new Parser() {
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
        _parsers.put(INT_ARRAY_PROTOTYPE.getClass(), new Parser() {
            public Object parse (String source) throws Exception {
                return StringUtil.parseIntArray(source);
            }
        });

        // and float arrays
        _parsers.put(FLOAT_ARRAY_PROTOTYPE.getClass(), new Parser() {
            public Object parse (String source) throws Exception {
                return StringUtil.parseFloatArray(source);
            }
        });

        // and string arrays, oh my!
        _parsers.put(STRING_ARRAY_PROTOTYPE.getClass(), new Parser() {
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
