//
// $Id: ValueMarshaller.java,v 1.2 2001/12/11 01:38:08 mdb Exp $

package com.samskivert.util;

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
        Parser parser = (Parser)_parsers.get(type);
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

        // and booleans
        _parsers.put(Boolean.TYPE, new Parser() {
            public Object parse (String source) throws Exception {
                return Boolean.valueOf(source);
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
