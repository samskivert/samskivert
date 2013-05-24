//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static com.samskivert.util.UtilLog.log;

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
                method.setAccessible(true); // isAccessible() check is too slow
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
