//
// $Id: ClassUtil.java,v 1.3 2001/10/03 02:22:58 mdb Exp $
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001 Michael Bayne
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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Class object related utility routines.
 *
 * <p> This code was adapted from code provided by Paul Hosler in <a
 * href="http://www.javareport.com/html/from_pages/article.asp?id=4276">an
 * article</a> for Java Report Online.
 */
public class ClassUtil
{
    /**
     * @param clazz a class.
     *
     * @return true if the class is accessible, false otherwise.
     * Presently returns true if the class is declared public.
     */
    public static boolean classIsAccessible (Class clazz)
    {
        return Modifier.isPublic(clazz.getModifiers());
    }

    /**
     * @param args an object array.
     *
     * @return an array of Class objects representing the classes of the
     * objects in the given Object array.  If args is null, a zero-length
     * Class array is returned.  If an element in args is null, then
     * Void.TYPE is the corresponding Class in the return array.
     */
    public static Class[] getParameterTypesFrom (Object[] args)
    {
        Class[] argTypes = null;

        if (args != null) {
            argTypes = new Class[args.length];

            for (int i = 0; i < args.length; ++i) {
                argTypes[i] = (args[i] == null) ?
                    Void.TYPE : args[i].getClass();
            }

        } else {
            argTypes = new Class[0];
        }

        return argTypes;
    }

    /**
     * Tells whether instances of the classes in the 'rhs' array could be
     * used as parameters to a reflective method invocation whose
     * parameter list has types denoted by the 'lhs' array.
     * 
     * @param lhs Class array representing the types of the formal
     * parameters of a method.
     * @param rhs Class array representing the types of the actual
     * parameters of a method.  A null value or Void.TYPE is considered to
     * match a corresponding Object or array class in lhs, but not a
     * primitive.
     *
     * @return true if compatible, false otherwise.
     */
    public static boolean compatibleClasses (Class[] lhs, Class[] rhs)
    {
        if (lhs.length != rhs.length) {
            return false;
        }

        for (int i = 0; i < lhs.length; ++i) {
            if (rhs[i] == null || rhs[i].equals(Void.TYPE)) {
                if (lhs[i].isPrimitive())
                    return false;
                else
                    continue;
            }

            if (! lhs[i].isAssignableFrom(rhs[i])) {
                Class lhsPrimEquiv = primitiveEquivalentOf(lhs[i]);
                Class rhsPrimEquiv = primitiveEquivalentOf(rhs[i]);

                if (! primitiveIsAssignableFrom(lhsPrimEquiv, rhsPrimEquiv))
                    return false;
            }
        }

        return true;
    }

    /**
     * Searches for the method with the given name and formal parameter
     * types that is in the nearest accessible class in the class
     * hierarchy, starting with clazz's superclass. The superclass and
     * implemented interfaces of clazz are searched, then their
     * superclasses, etc. until a method is found. Returns null if there
     * is no such method.
     *
     * @param clazz a class.
     * @param methodName name of a method.
     * @param paramTypes Class array representing the types of a method's
     * formal parameters.
     *
     * @return the nearest method located, or null if there is no such
     * method.
     */
    public static Method getAccessibleMethodFrom (
        Class clazz, String methodName, Class[] parameterTypes)
    {
        // Look for overridden method in the superclass.
        Class superclass = clazz.getSuperclass();
        Method overriddenMethod = null;

        if (superclass != null && classIsAccessible(superclass)) {
            try {
                overriddenMethod =
                    superclass.getMethod(methodName, parameterTypes);
            } catch (NoSuchMethodException _) {
            }

            if (overriddenMethod != null) {
                return overriddenMethod;
            }
        }

        // If here, then clazz represents Object, or an interface, or the
        // superclass did not have an override.  Check implemented
        // interfaces.
        Class[] interfaces = clazz.getInterfaces();

        for (int i = 0; i < interfaces.length; ++i) {
            overriddenMethod = null;

            if (classIsAccessible(interfaces[i])) {
                try {
                    overriddenMethod =
                        interfaces[i].getMethod(methodName, parameterTypes);
                } catch (NoSuchMethodException _) {
                }

                if (overriddenMethod != null) {
                    return overriddenMethod;
                }
            }
        }

        overriddenMethod = null;

        // Try superclass's superclass and implemented interfaces.
        if (superclass != null) {
            overriddenMethod = getAccessibleMethodFrom(
                superclass, methodName, parameterTypes);

            if (overriddenMethod != null) {
                return overriddenMethod;
            }
        }

        // Try implemented interfaces' extended interfaces...
        for (int i = 0; i < interfaces.length; ++i) {
            overriddenMethod = getAccessibleMethodFrom(
                interfaces[i], methodName, parameterTypes);

            if (overriddenMethod != null) {
                return overriddenMethod;
            }
        }

        // Give up.
        return null;
    }

    /**
     * @param clazz a Class.
     *
     * @return the class's primitive equivalent, if clazz is a primitive
     * wrapper.  If clazz is primitive, returns clazz.  Otherwise,
     * returns null.
     */
    public static Class primitiveEquivalentOf (Class clazz)
    {
        return clazz.isPrimitive() ?
            clazz : (Class)_objectToPrimitiveMap.get(clazz);
    }

    /**
     * Tells whether an instance of the primitive class represented by
     * 'rhs' can be assigned to an instance of the primitive class
     * represented by 'lhs'.
     * 
     * @param lhs assignee class.
     * @param rhs assigned class.
     *
     * @return true if compatible, false otherwise. If either argument is
     * <code>null</code>, or one of the parameters does not represent a
     * primitive (e.g. Byte.TYPE), returns false.
     */
    public static boolean primitiveIsAssignableFrom (Class lhs, Class rhs)
    {
        if (lhs == null || rhs == null)
            return false;

        if (! (lhs.isPrimitive() && rhs.isPrimitive()))
            return false;

        if (lhs.equals(rhs))
            return true;

        Set wideningSet = (Set)_primitiveWideningsMap.get(rhs);
        if (wideningSet == null)
            return false;

        return wideningSet.contains(lhs);
    }

    /**
     * Mapping from primitive wrapper Classes to their corresponding
     * primitive Classes.
     */
    private static final Map _objectToPrimitiveMap = new HashMap(13);

    static {
        _objectToPrimitiveMap.put(Boolean.class, Boolean.TYPE);
        _objectToPrimitiveMap.put(Byte.class, Byte.TYPE);
        _objectToPrimitiveMap.put(Character.class, Character.TYPE);
        _objectToPrimitiveMap.put(Double.class, Double.TYPE);
        _objectToPrimitiveMap.put(Float.class, Float.TYPE);
        _objectToPrimitiveMap.put(Integer.class, Integer.TYPE);
        _objectToPrimitiveMap.put(Long.class, Long.TYPE);
        _objectToPrimitiveMap.put(Short.class, Short.TYPE);
    }

    /**
     * Mapping from primitive wrapper Classes to the sets of primitive
     * classes whose instances can be assigned an instance of the first.
     */
    private static final Map _primitiveWideningsMap = new HashMap(11);

    static {
        Set set = new HashSet();
        set.add(Short.TYPE);
        set.add(Integer.TYPE);
        set.add(Long.TYPE);
        set.add(Float.TYPE);
        set.add(Double.TYPE);
        _primitiveWideningsMap.put(Byte.TYPE, set);

        set = new HashSet();
        set.add(Integer.TYPE);
        set.add(Long.TYPE);
        set.add(Float.TYPE);
        set.add(Double.TYPE);
        _primitiveWideningsMap.put(Short.TYPE, set);
        _primitiveWideningsMap.put(Character.TYPE, set);

        set = new HashSet();
        set.add(Long.TYPE);
        set.add(Float.TYPE);
        set.add(Double.TYPE);
        _primitiveWideningsMap.put(Integer.TYPE, set);

        set = new HashSet();
        set.add(Float.TYPE);
        set.add(Double.TYPE);
        _primitiveWideningsMap.put(Long.TYPE, set);

        set = new HashSet();
        set.add(Double.TYPE);
        _primitiveWideningsMap.put(Float.TYPE, set);
    }
}
