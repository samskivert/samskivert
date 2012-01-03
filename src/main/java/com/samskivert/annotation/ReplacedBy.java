//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation for indicating that something has a preferred implementation elsewhere, without
 * going so far as to deprecate it.
 */
@Documented
@Inherited
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
public @interface ReplacedBy
{
    /**
     * A human-readable String containing a reference to the replacement Class, method, or field.
     * It is suggested that you follow the "@see" javadoc semantics for specifying a reference.
     */
    String value ();

    /**
     * The reason the replacement is suggested, in case it's not obvious.
     */
    String reason () default "";
}
