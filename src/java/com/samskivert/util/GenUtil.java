//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2007 Michael Bayne
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

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

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
        String cname;
        Class<?> clazz = field.getType();
        if (clazz.isArray()) {
            if (field.getGenericType() instanceof GenericArrayType) {
                GenericArrayType atype = (GenericArrayType)field.getGenericType();
                cname = atype.getGenericComponentType().toString();
            } else {
                return simpleName(clazz.getComponentType(), field.getGenericType()) + "[]";
            }
        } else if (field.getGenericType() instanceof ParameterizedType) {
            cname = field.getGenericType().toString();
        } else {
            cname = clazz.getName();
        }
        return simpleName(clazz, cname);
    }

    /**
     * Returns the name of the supplied class as it would likely appear in code using the class (no
     * package prefix, arrays specified as <code>type[]</code>).
     */
    public static String simpleName (Class<?> clazz, Type type)
    {
        if (clazz.isArray()) {
            return simpleName(clazz.getComponentType(), type) + "[]";
        }
        String cname = clazz.getName();
        if (type instanceof ParameterizedType) {
            cname = type.toString();
        }
        return simpleName(clazz, cname);
    }

    /**
     * A helper function for the public simpleName methods.
     */
    protected static String simpleName (Class<?> clazz, String cname)
    {
        Package pkg = clazz.getPackage();
        int offset = (pkg == null) ? 0 : pkg.getName().length()+1;
        return StringUtil.replace(cname.substring(offset), "$", ".");
    }
}
