//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

/**
 * Utility methods for code that generates code.
 */
public class GenUtil
{
    /**
     * Returns the name of the supplied class as it would likely appear in code using the class (no
     * package prefix, arrays specified as <code>type[]</code>).
     */
    public static String simpleName (Field field)
    {
        return simpleName(field.getGenericType());
    }

    /**
     * Returns the name of the supplied class as it would likely appear in code using the class (no
     * package prefix, arrays specified as <code>type[]</code>).
     */
    public static String simpleName (Type type)
    {
        if (type instanceof GenericArrayType) {
            return simpleName(((GenericArrayType)type).getGenericComponentType()) + "[]";
        } else if (type instanceof Class<?>) {
            Class<?> clazz = (Class<?>)type;
            if (clazz.isArray()) {
                return simpleName(clazz.getComponentType()) + "[]";
            } else {
                Package pkg = clazz.getPackage();
                int offset = (pkg == null) ? 0 : pkg.getName().length()+1;
                return clazz.getName().substring(offset).replace('$', '.');
            }

        } else if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType)type;
            StringBuilder buf = new StringBuilder();
            for (Type arg : pt.getActualTypeArguments()) {
                if (buf.length() > 0) {
                    buf.append(", ");
                }
                buf.append(simpleName(arg));
            }
            return simpleName(pt.getRawType()) + "<" + buf + ">";

        } else if (type instanceof WildcardType) {
            WildcardType wt = (WildcardType)type;
            if (wt.getLowerBounds().length > 0) {
                String errmsg = "Generation of simple name for wildcard type with lower bounds " +
                    "not implemented [type=" + type +
                    ", lbounds=" + StringUtil.toString(wt.getLowerBounds()) + "]";
                throw new IllegalArgumentException(errmsg);
            }
            if (wt.getUpperBounds().length > 1) {
                String errmsg = "Generation of simple name for wildcard type with multiple upper " +
                    "bounds not implemented [type=" + type +
                    ", ubounds=" + StringUtil.toString(wt.getUpperBounds()) + "]";
                throw new IllegalArgumentException(errmsg);
            }
            StringBuilder buf = new StringBuilder("?");
            if (!Object.class.equals(wt.getUpperBounds()[0])) {
                buf.append(" extends ").append(simpleName(wt.getUpperBounds()[0]));
            }
            return buf.toString();

        } else if (type instanceof TypeVariable) {
            return ((TypeVariable<?>)type).getName();

        } else {
            throw new IllegalArgumentException("Can't generate simple name [type=" + type +
                                               ", tclass=" + StringUtil.shortClassName(type) + "]");
        }
    }
}
