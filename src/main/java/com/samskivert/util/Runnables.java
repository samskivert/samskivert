//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2011 Michael Bayne, et al.
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static com.samskivert.Log.log;

/**
 * Runnable related utility methods.
 */
public class Runnables
{
    /** A runnable that does nothing. Useful for use as the default value for optional callbacks. */
    public static Runnable NOOP = new Runnable() {
        public void run () {
            // noop!
        }
    };

    /**
     * Creates a runnable that invokes the specified method on the specified instance via
     * reflection.
     *
     * <p> NOTE: if you specify a protected or private method this method will call {@link
     * Method#setAccessible} to make the method accessible. If you're writing secure code or need
     * to run in a sandbox, don't use this functionality.
     *
     * @throws IllegalArgumentException if the supplied method name does not correspond to a
     * non-static zero-argument method of the supplied instance's class.
     */
    public static Runnable asRunnable (Object instance, String methodName)
    {
        Class<?> clazz = instance.getClass();
        Method method = findMethod(clazz, methodName);
        if (Modifier.isStatic(method.getModifiers())) {
            throw new IllegalArgumentException(
                clazz.getName() + "." + methodName + "() must not be static");
        }
        return new MethodRunner(method, instance);
    }

    /**
     * Creates a runnable that invokes the specified static method via reflection.
     *
     * <p> NOTE: if you specify a protected or private method this method will call {@link
     * Method#setAccessible} to make the method accessible. If you're writing secure code or need
     * to run in a sandbox, don't use this functionality.
     *
     * @throws IllegalArgumentException if the supplied method name does not correspond to a static
     * zero-argument method of the supplied class.
     */
    public static Runnable asRunnable (Class<?> clazz, String methodName)
    {
        Method method = findMethod(clazz, methodName);
        if (!Modifier.isStatic(method.getModifiers())) {
            throw new IllegalArgumentException(
                clazz.getName() + "." + methodName + "() must be static");
        }
        return new MethodRunner(method, null);
    }

    protected static Method findMethod (Class<?> clazz, String methodName)
    {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(methodName) && method.getParameterTypes().length == 0) {
                if (!method.isAccessible()) {
                    method.setAccessible(true);
                }
                return method;
            }
        }
        throw new IllegalArgumentException(clazz.getName() + "." + methodName + "() not found");
    }

    protected static class MethodRunner implements Runnable
    {
        public MethodRunner (Method method, Object instance) {
            _method = method;
            _instance = instance;
        }

        public void run () {
            try {
                _method.invoke(_instance);
            } catch (IllegalAccessException iae) {
                log.warning("Failed to invoke " + _method.getName() + ": " +  iae);
            } catch (InvocationTargetException ite) {
                log.warning("Invocation of " + _method.getName() + " failed", ite);
            }
        }

        protected Method _method;
        protected Object _instance;
    }

    protected Runnables ()
    {
        // no constructy
    }
}
