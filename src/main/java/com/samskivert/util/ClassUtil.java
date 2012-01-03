//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Class object related utility routines.
 *
 * <p> This code was adapted from code provided by Paul Hosler in <a
 * href="http://www.javareport.com/html/from_pages/article.asp?id=4276">an article</a> for Java
 * Report Online.
 */
public class ClassUtil
{
    /**
     * @param clazz a class.
     *
     * @return true if the class is accessible, false otherwise.  Presently returns true if the
     * class is declared public.
     */
    public static boolean classIsAccessible (Class<?> clazz)
    {
        return Modifier.isPublic(clazz.getModifiers());
    }

    /**
     * Get the fields contained in the class and its superclasses.
     */
    public static Field[] getFields (Class<?> clazz)
    {
        ArrayList<Field> list = new ArrayList<Field>();
        getFields(clazz, list);
        return list.toArray(new Field[list.size()]);
    }

    /**
     * Add all the fields of the specifed class (and its ancestors) to the list. Note, if we are
     * running in a sandbox, this will only enumerate public members.
     */
    public static void getFields (Class<?> clazz, List<Field> addTo)
    {
        // first get the fields of the superclass
        Class<?> pclazz = clazz.getSuperclass();
        if (pclazz != null && !pclazz.equals(Object.class)) {
            getFields(pclazz, addTo);
        }

        // then reflect on this class's declared fields
        Field[] fields;
        try {
            fields = clazz.getDeclaredFields();
        } catch (SecurityException se) {
            System.err.println("Unable to get declared fields of " + clazz.getName() + ": " + se);
            fields = new Field[0];
        }

        // override the default accessibility check for the fields
        try {
            AccessibleObject.setAccessible(fields, true);
        } catch (SecurityException se) {
            // ah well, only publics for us
        }

        for (Field field : fields) {
            int mods = field.getModifiers();
            if (Modifier.isStatic(mods) || Modifier.isTransient(mods)) {
                continue; // skip static and transient fields
            }
            addTo.add(field);
        }
    }

    /**
     * @param args an object array.
     *
     * @return an array of Class objects representing the classes of the objects in the given
     * Object array.  If args is null, a zero-length Class array is returned.  If an element in
     * args is null, then Void.TYPE is the corresponding Class in the return array.
     */
    public static Class<?>[] getParameterTypesFrom (Object[] args)
    {
        Class<?>[] argTypes = null;
        if (args != null) {
            argTypes = new Class<?>[args.length];
            for (int i = 0; i < args.length; ++i) {
                argTypes[i] = (args[i] == null) ? Void.TYPE : args[i].getClass();
            }
        } else {
            argTypes = new Class<?>[0];
        }
        return argTypes;
    }

    /**
     * Tells whether instances of the classes in the 'rhs' array could be used as parameters to a
     * reflective method invocation whose parameter list has types denoted by the 'lhs' array.
     *
     * @param lhs Class array representing the types of the formal parameters of a method.
     * @param rhs Class array representing the types of the actual parameters of a method.  A null
     * value or Void.TYPE is considered to match a corresponding Object or array class in lhs, but
     * not a primitive.
     *
     * @return true if compatible, false otherwise.
     */
    public static boolean compatibleClasses (Class<?>[] lhs, Class<?>[] rhs)
    {
        if (lhs.length != rhs.length) {
            return false;
        }

        for (int i = 0; i < lhs.length; ++i) {
            if (rhs[i] == null || rhs[i].equals(Void.TYPE)) {
                if (lhs[i].isPrimitive()) {
                    return false;
                } else {
                    continue;
                }
            }

            if (!lhs[i].isAssignableFrom(rhs[i])) {
                Class<?> lhsPrimEquiv = primitiveEquivalentOf(lhs[i]);
                Class<?> rhsPrimEquiv = primitiveEquivalentOf(rhs[i]);
                if (!primitiveIsAssignableFrom(lhsPrimEquiv, rhsPrimEquiv)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Searches for the method with the given name and formal parameter types that is in the
     * nearest accessible class in the class hierarchy, starting with clazz's superclass. The
     * superclass and implemented interfaces of clazz are searched, then their superclasses,
     * etc. until a method is found. Returns null if there is no such method.
     *
     * @param clazz a class.
     * @param methodName name of a method.
     * @param parameterTypes Class array representing the types of a method's formal parameters.
     *
     * @return the nearest method located, or null if there is no such method.
     */
    public static Method getAccessibleMethodFrom (
        Class<?> clazz, String methodName, Class<?>[] parameterTypes)
    {
        // Look for overridden method in the superclass.
        Class<?> superclass = clazz.getSuperclass();
        Method overriddenMethod = null;

        if (superclass != null && classIsAccessible(superclass)) {
            try {
                overriddenMethod = superclass.getMethod(methodName, parameterTypes);
            } catch (NoSuchMethodException nsme) {
                // no problem
            }

            if (overriddenMethod != null) {
                return overriddenMethod;
            }
        }

        // If here, then clazz represents Object, or an interface, or the superclass did not have
        // an override.  Check implemented interfaces.
        Class<?>[] interfaces = clazz.getInterfaces();
        for (int i = 0; i < interfaces.length; ++i) {
            if (classIsAccessible(interfaces[i])) {
                try {
                    overriddenMethod = interfaces[i].getMethod(methodName, parameterTypes);
                } catch (NoSuchMethodException nsme) {
                    // no problem
                }
                if (overriddenMethod != null) {
                    return overriddenMethod;
                }
            }
        }

        // Try superclass's superclass and implemented interfaces.
        if (superclass != null) {
            overriddenMethod = getAccessibleMethodFrom(superclass, methodName, parameterTypes);
            if (overriddenMethod != null) {
                return overriddenMethod;
            }
        }

        // Try implemented interfaces' extended interfaces...
        for (int i = 0; i < interfaces.length; ++i) {
            overriddenMethod = getAccessibleMethodFrom(interfaces[i], methodName, parameterTypes);
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
     * @return the class's primitive equivalent, if clazz is a primitive wrapper.  If clazz is
     * primitive, returns clazz.  Otherwise, returns null.
     */
    public static Class<?> primitiveEquivalentOf (Class<?> clazz)
    {
        return clazz.isPrimitive() ? clazz : _objectToPrimitiveMap.get(clazz);
    }

    /**
     * @return the class's object equivalent if the class is a primitive type.
     */
    public static Class<?> objectEquivalentOf (Class<?> clazz)
    {
        return clazz.isPrimitive() ? _primitiveToObjectMap.get(clazz) : clazz;
    }

    /**
     * Tells whether an instance of the primitive class represented by 'rhs' can be assigned to an
     * instance of the primitive class represented by 'lhs'.
     *
     * @param lhs assignee class.
     * @param rhs assigned class.
     *
     * @return true if compatible, false otherwise. If either argument is <code>null</code>, or one
     * of the parameters does not represent a primitive (e.g. Byte.TYPE), returns false.
     */
    public static boolean primitiveIsAssignableFrom (Class<?> lhs, Class<?> rhs)
    {
        if (lhs == null || rhs == null) {
            return false;
        }
        if (!(lhs.isPrimitive() && rhs.isPrimitive())) {
            return false;
        }
        if (lhs.equals(rhs)) {
            return true;
        }
        Set<Class<?>> wideningSet = _primitiveWideningsMap.get(rhs);
        if (wideningSet == null) {
            return false;
        }
        return wideningSet.contains(lhs);
    }

    /** Mapping from primitive wrapper Classes to their corresponding
     * primitive Classes. */
    protected static final Map<Class<?>,Class<?>> _objectToPrimitiveMap =
        new HashMap<Class<?>,Class<?>>(13);
    protected static final Map<Class<?>,Class<?>> _primitiveToObjectMap =
        new HashMap<Class<?>,Class<?>>(13);

    static {
        _objectToPrimitiveMap.put(Boolean.class, Boolean.TYPE);
        _primitiveToObjectMap.put(Boolean.TYPE, Boolean.class);
        _objectToPrimitiveMap.put(Byte.class, Byte.TYPE);
        _primitiveToObjectMap.put(Byte.TYPE, Byte.class);
        _objectToPrimitiveMap.put(Character.class, Character.TYPE);
        _primitiveToObjectMap.put(Character.TYPE, Character.class);
        _objectToPrimitiveMap.put(Double.class, Double.TYPE);
        _primitiveToObjectMap.put(Double.TYPE, Double.class);
        _objectToPrimitiveMap.put(Float.class, Float.TYPE);
        _primitiveToObjectMap.put(Float.TYPE, Float.class);
        _objectToPrimitiveMap.put(Integer.class, Integer.TYPE);
        _primitiveToObjectMap.put(Integer.TYPE, Integer.class);
        _objectToPrimitiveMap.put(Long.class, Long.TYPE);
        _primitiveToObjectMap.put(Long.TYPE, Long.class);
        _objectToPrimitiveMap.put(Short.class, Short.TYPE);
        _primitiveToObjectMap.put(Short.TYPE, Short.class);
    }

    /** Mapping from primitive wrapper Classes to the sets of primitive classes whose instances can
     * be assigned an instance of the first. */
    protected static final Map<Class<?>,Set<Class<?>>> _primitiveWideningsMap =
        new HashMap<Class<?>,Set<Class<?>>>(11);

    static {
        Set<Class<?>> set = new HashSet<Class<?>>();
        set.add(Short.TYPE);
        set.add(Integer.TYPE);
        set.add(Long.TYPE);
        set.add(Float.TYPE);
        set.add(Double.TYPE);
        _primitiveWideningsMap.put(Byte.TYPE, set);

        set = new HashSet<Class<?>>();
        set.add(Integer.TYPE);
        set.add(Long.TYPE);
        set.add(Float.TYPE);
        set.add(Double.TYPE);
        _primitiveWideningsMap.put(Short.TYPE, set);
        _primitiveWideningsMap.put(Character.TYPE, set);

        set = new HashSet<Class<?>>();
        set.add(Long.TYPE);
        set.add(Float.TYPE);
        set.add(Double.TYPE);
        _primitiveWideningsMap.put(Integer.TYPE, set);

        set = new HashSet<Class<?>>();
        set.add(Float.TYPE);
        set.add(Double.TYPE);
        _primitiveWideningsMap.put(Long.TYPE, set);

        set = new HashSet<Class<?>>();
        set.add(Double.TYPE);
        _primitiveWideningsMap.put(Float.TYPE, set);
    }
}
