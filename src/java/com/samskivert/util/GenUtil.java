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
        } else if (type instanceof Class) {
            Class clazz = (Class)type;
            if (clazz.isArray()) {
                return simpleName(clazz.getComponentType()) + "[]";
            } else {
                Package pkg = clazz.getPackage();
                int offset = (pkg == null) ? 0 : pkg.getName().length()+1;
                return StringUtil.replace(clazz.getName().substring(offset), "$", ".");
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
        } else {
            throw new IllegalArgumentException("Can't generate simple name [type=" + type +
                                               ", tclass=" + StringUtil.shortClassName(type) + "]");
        }
    }
}
