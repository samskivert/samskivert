//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import java.lang.reflect.*;
import java.util.*;

/**
 * Finds methods and constructors that can be invoked reflectively.  Attempts to address some of
 * the limitations of the JDK's {@link Class#getMethod} and {@link Class#getConstructor}, and other
 * JDK reflective facilities.
 *
 * <p> Because those methods only match exact method signatures, one is unable to perform the same
 * method matching that the compiler does at compile time (e.g. matching the method
 * <code>foo(Exception)</code> when the user wants to call a method named <code>foo</code> with an
 * <code>IOException</code> argument) with the basic reflection services.  This class implements
 * the method resolution process according to the same rules used by a Java compiler. These rules
 * are outlined in the Java Language Specification, variously in sections 5.1.2, 5.1.4, 5.3, and
 * 15.12.2.
 *
 * <p> This code was adapted from code provided by Paul Hosler in <a
 * href="http://www.javareport.com/html/from_pages/article.asp?id=4276">an article</a> for Java
 * Report Online.
 */
public class MethodFinder
{
    /**
     * Constructs a method finder for the supplied class.
     *
     * @param clazz Class in which I will look for methods and constructors.
     *
     * @exception IllegalArgumentException if clazz is null, or represents a primitive, or
     * represents an array type.
     */
    public MethodFinder (Class<?> clazz)
    {
        if (clazz == null) {
            throw new IllegalArgumentException("null Class parameter");
        }
        if (clazz.isPrimitive()) {
            throw new IllegalArgumentException("primitive Class parameter");
        }
        if (clazz.isArray()) {
            throw new IllegalArgumentException("array Class parameter");
        }
        _clazz = clazz;
    }

    @Override
    public boolean equals (Object o)
    {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        } else {
            MethodFinder other = (MethodFinder)o;
            return _clazz.equals(other._clazz);
        }
    }

    /**
     * Returns the most specific public constructor in my target class that accepts the number and
     * type of parameters in the given Class array in a reflective invocation.
     *
     * <p> A null value or Void.TYPE parameterTypes matches a corresponding Object or array
     * reference in a constructor's formal parameter list, but not a primitive formal parameter.
     *
     * @param parameterTypes array representing the number and types of parameters to look for in
     * the constructor's signature.  A null array is treated as a zero-length array.
     *
     * @return Constructor object satisfying the conditions.
     *
     * @exception NoSuchMethodException if no constructors match the criteria, or if the reflective
     * call is ambiguous based on the parameter types.
     */
    public Constructor<?> findConstructor (Class<?>[] parameterTypes)
        throws NoSuchMethodException
    {
        // make sure the constructor list is loaded
        maybeLoadConstructors();

        if (parameterTypes == null) {
            parameterTypes = new Class<?>[0];
        }

        return (Constructor<?>)findMemberIn(_ctorList, parameterTypes);
    }

    /**
     * Returns the most specific public method in my target class that has the given name and
     * accepts the number and type of parameters in the given Class array in a reflective
     * invocation.
     *
     * <p> A null value or Void.TYPE in parameterTypes will match a corresponding Object or array
     * reference in a method's formal parameter list, but not a primitive formal parameter.
     *
     * @param methodName name of the method to search for.
     * @param parameterTypes array representing the number and types of parameters to look for in
     * the method's signature.  A null array is treated as a zero-length array.
     *
     * @return Method object satisfying the conditions.
     *
     * @exception NoSuchMethodException if no methods match the criteria, or if the reflective call
     * is ambiguous based on the parameter types, or if methodName is null.
     */
    public Method findMethod (String methodName, Class<?>[] parameterTypes)
        throws NoSuchMethodException
    {
        // make sure the constructor list is loaded
        maybeLoadMethods();

        List<Member> methodList = _methodMap.get(methodName);
        if (methodList == null) {
            throw new NoSuchMethodException(
                "No method named " + _clazz.getName() + "." + methodName);
        }

        if (parameterTypes == null) {
            parameterTypes = new Class<?>[0];
        }

        return (Method)findMemberIn(methodList, parameterTypes);
    }

    /**
     * Like {@link #findMethod(String,Class[])} except that it takes the actual arguments that will
     * be passed to the found method and creates the array of class objects for you using {@link
     * ClassUtil#getParameterTypesFrom}.
     */
    public Method findMethod (String methodName, Object[] args)
        throws NoSuchMethodException
    {
        return findMethod(methodName, ClassUtil.getParameterTypesFrom(args));
    }

    /**
     * Basis of {@link #findConstructor} and {@link #findMethod}.  The member list fed to this
     * method will be either all {@link Constructor} objects or all {@link Method} objects.
     */
    protected Member findMemberIn (List<Member> memberList, Class<?>[] parameterTypes)
        throws NoSuchMethodException
    {
        List<Member> matchingMembers = new ArrayList<Member>();

        for (Iterator<Member> it = memberList.iterator(); it.hasNext();) {
            Member member = it.next();
            Class<?>[] methodParamTypes = _paramMap.get(member);

            // check for exactly equal method signature
            if (Arrays.equals(methodParamTypes, parameterTypes)) {
                return member;
            }

            if (ClassUtil.compatibleClasses(methodParamTypes, parameterTypes)) {
                matchingMembers.add(member);
            }
        }

        if (matchingMembers.isEmpty()) {
            throw new NoSuchMethodException(
                "No member in " + _clazz.getName() + " matching given args");
        }
        if (matchingMembers.size() == 1) {
            return matchingMembers.get(0);
        }

        return findMostSpecificMemberIn(matchingMembers);
    }

    /**
     * @param memberList a list of members (either all constructors or all methods).
     *
     * @return the most specific of all members in the list.
     *
     * @exception NoSuchMethodException if there is an ambiguity as to which is most specific.
     */
    protected Member findMostSpecificMemberIn (List<Member> memberList)
        throws NoSuchMethodException
    {
        List<Member> mostSpecificMembers = new ArrayList<Member>();

        for (Member member : memberList) {
            if (mostSpecificMembers.isEmpty()) {
                // First guy in is the most specific so far.
                mostSpecificMembers.add(member);

            } else {
                boolean moreSpecific = true;
                boolean lessSpecific = false;

                // Is member more specific than everyone in the most-specific set?
                for (Member moreSpecificMember : mostSpecificMembers) {
                    if (!memberIsMoreSpecific(member, moreSpecificMember)) {
                        // if the candidate member is not more specific than this member, then it's
                        // not more specific than the entire set, but it may still be equivalently
                        // specific, so we check that next
                        moreSpecific = false;

                        // we check for a member of equal specificity by checking to see if this
                        // most specific member is explicitly more specific than the candidate
                        // member. if it is more specific, the candidate member can be chucked,
                        // otherwise we need to add the candidate member to the most-specific set
                        lessSpecific = memberIsMoreSpecific(moreSpecificMember, member);
                        break;
                    }
                }

                if (moreSpecific) {
                    // Member is the most specific now.
                    mostSpecificMembers.clear();
                    mostSpecificMembers.add(member);

                } else if (!lessSpecific) {
                    // Add to ambiguity set if mutually unspecific.
                    mostSpecificMembers.add(member);
                }
            }
        }

        if (mostSpecificMembers.size() > 1) {
            throw new NoSuchMethodException(
                "Ambiguous request for member in " + _clazz.getName() + " matching given args" );
        }

        return mostSpecificMembers.get(0);
    }

    @Override // from Object
    public int hashCode ()
    {
        return _clazz.hashCode();
    }

    /**
     * Loads up the data structures for my target class's constructors.
     */
    protected void maybeLoadConstructors ()
    {
        if (_ctorList == null) {
            _ctorList = new ArrayList<Member>();
            Constructor<?>[] ctors = _clazz.getConstructors();
            for (int i = 0; i < ctors.length; ++i) {
                _ctorList.add(ctors[i]);
                _paramMap.put(ctors[i], ctors[i].getParameterTypes());
            }
        }
    }

    /**
     * Loads up the data structures for my target class's methods.
     */
    protected void maybeLoadMethods ()
    {
        if (_methodMap == null) {
            _methodMap = new HashMap<String,List<Member>>();
            Method[] methods = _clazz.getMethods();

            for (int i = 0; i < methods.length; ++i) {
                Method m = methods[i];
                String methodName = m.getName();
                Class<?>[] paramTypes = m.getParameterTypes();

                List<Member> list = _methodMap.get(methodName);
                if (list == null) {
                    list = new ArrayList<Member>();
                    _methodMap.put(methodName, list);
                }

                if (!ClassUtil.classIsAccessible(_clazz)) {
                    m = ClassUtil.getAccessibleMethodFrom(_clazz, methodName, paramTypes );
                }

                if (m != null) {
                    list.add(m);
                    _paramMap.put(m, paramTypes);
                }
            }
        }
    }

    /**
     * @param first a Member.
     * @param second a Member.
     *
     * @return true if the first Member is more specific than the second, false otherwise.
     * Specificity is determined according to the procedure in the Java Language Specification,
     * section 15.12.2.
     */
    protected boolean memberIsMoreSpecific (Member first, Member second)
    {
        Class<?>[] firstParamTypes = _paramMap.get(first);
        Class<?>[] secondParamTypes = _paramMap.get(second);
        return ClassUtil.compatibleClasses(secondParamTypes, firstParamTypes);
    }

    /** The target class to look for methods and constructors in. */
    protected Class<?> _clazz;

    /** Mapping from method name to the Methods in the target class with that name. */
    protected Map<String,List<Member>> _methodMap = null;

    /** List of the Constructors in the target class. */
    protected List<Member> _ctorList = null;

    /** Mapping from a Constructor or Method object to the Class objects representing its formal
     * parameters. */
    protected Map<Member,Class<?>[]> _paramMap = new HashMap<Member,Class<?>[]>();
}
