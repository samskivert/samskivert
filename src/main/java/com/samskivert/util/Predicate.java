//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.samskivert.annotation.ReplacedBy;

/**
 * Arbitrates membership. Example usage. Find the best kind of peanut butter:
 * <pre>{@code
 * public Iterator<PeanutButter> findBestKinds (Collection<PeanutButter> pbs)
 * {
 *     return new Predicate<PeanutButter>() {
 *         public boolean isMatch (PeanutButter pb)
 *         {
 *              return pb.isCreamy() && !pb.containsPartiallyHydrogenatedOils();
 *         }
 *     }.filter(pbs.iterator());
 * }
 * }</pre>
 * An interface like this may someday be in the Java library, but until then...
 */
@ReplacedBy("com.google.common.base.Predicate")
public abstract class Predicate<T>
{
    //--------------------------------------------------------------------
    // But first, some handy utility classes:

    /**
     * A simple predicate that includes an object if it is an instance
     * of the specified class.
     */
    public static class InstanceOf<T> extends Predicate<T>
    {
        public InstanceOf (Class<?> clazz)
        {
            _class = clazz;
        }

        @Override public boolean isMatch (T obj)
        {
            return _class.isInstance(obj);
        }

        /** The class that must be implemented by applicants. */
        protected Class<?> _class;
    }

    /**
     * Includes an object if and only if all the predicates with which it
     * was constructed also include the object. The predicates
     * are tested in the order specified and testing is halted as soon
     * as one does not include the object. If the Predicate is constructed
     * with no arguments, it will behave like TRUE.
     */
    public static class And<T> extends Predicate<T>
    {
        public And (Predicate<T> ... preds)
        {
            _preds = preds;
        }

        @Override public boolean isMatch (T obj)
        {
            for (Predicate<T> pred : _preds) {
                if (!pred.isMatch(obj)) {
                    return false;
                }
            }
            return true;
        }

        /** The predicates that all must be satisfied. */
        protected Predicate<T>[] _preds;
    }

    /**
     * Includes an object if at least one of the predicates specified in the
     * constructor include the object. The predicates are tested in the
     * order specified and testing is halted as soon as one includes
     * the object. If the predicate is constructed with no arguments,
     * it will behave like FALSE.
     */
    public static class Or<T> extends Predicate<T>
    {
        public Or (Predicate<T> ... preds)
        {
            _preds = preds;
        }

        @Override public boolean isMatch (T obj)
        {
            for (Predicate<T> pred : _preds) {
                if (pred.isMatch(obj)) {
                    return true;
                }
            }
            return false;
        }

        protected Predicate<T>[] _preds;
    }

    /**
     * Negates any other predicate.
     */
    public static class Not<T> extends Predicate<T>
    {
        public Not (Predicate<T> negated)
        {
            _pred = negated;
        }

        @Override public boolean isMatch (T obj)
        {
            return !_pred.isMatch(obj);
        }

        protected Predicate<T> _pred;
    }

    /**
     * Returns a type-safe reference to the shared instance of a predicate that always returns
     * <code>true</code>.
     */
    public static <T> Predicate<T> trueInstance ()
    {
        @SuppressWarnings("unchecked") Predicate<T> pred = (Predicate<T>)TRUE_INSTANCE;
        return pred;
    }

    /**
     * Returns a type-safe reference to the shared instance of a predicate that always returns
     * <code>false</code>.
     */
    public static <T> Predicate<T> falseInstance ()
    {
        @SuppressWarnings("unchecked") Predicate<T> pred = (Predicate<T>)FALSE_INSTANCE;
        return pred;
    }

    //--------------------------------------------------------------------
    // Here's the sole abstract method in the Predicate class:

    /**
     * Does the specified object belong to the special set that we test for?
     */
    public abstract boolean isMatch (T obj);

    //--------------------------------------------------------------------
    // And finally, some handy utility methods:

    /**
     * Return a new iterator that contains only matching elements from
     * the input iterator.
     */
    public <E extends T> Iterator<E> filter (final Iterator<E> input)
    {
        return new Iterator<E>() {
            // from Iterator
            public boolean hasNext ()
            {
                return _found || findNext();
            }

            // from Iterator
            public E next ()
            {
                if (_found || findNext()) {
                    _found = false;
                    return _last;

                } else {
                    throw new NoSuchElementException();
                }
            }

            // from Iterator
            public void remove ()
            {
                if (_removeable) {
                    _removeable = false;
                    input.remove();

                } else {
                    throw new IllegalStateException();
                }
            }

            private boolean findNext ()
            {
                boolean result = false;
                while (input.hasNext()) {
                    E candidate = input.next();
                    if (isMatch(candidate)) {
                        _last = candidate;
                        result = true;
                        break;
                    }
                }

                _found = result;
                _removeable = result;
                return result;
            }

            private E _last;
            private boolean _found; // because _last == null is a valid element
            private boolean _removeable;
        };
    }

    /**
     * Remove non-matching elements from the specified collection.
     */
    public <E extends T> void filter (Collection<E> coll)
    {
        for (Iterator<E> itr = coll.iterator(); itr.hasNext(); ) {
            if (!isMatch(itr.next())) {
                itr.remove();
            }
        }
    }

    /**
     * Create an Iterable view of the specified Iterable that only contains
     * elements that match the predicate.
     * This Iterable can be iterated over at any time in the future to
     * view the current predicate-matching elements of the input Iterable.
     */
    public <E extends T> Iterable<E> createView (final Iterable<E> input)
    {
        return new Iterable<E>() {
            public Iterator<E> iterator() {
                return filter(input.iterator());
            }
        };
    }

    /**
     * Create a view of the specified collection that only contains elements
     * that match the predicate.
     * This collection can be examined at any time in the future to view
     * the current predicate-matching elements of the input collection.
     *
     * Note that the view is not modifiable and currently has poor
     * implementations of some methods.
     */
    public <E extends T> Collection<E> createView (final Collection<E> input)
    {
        // TODO: create a collection of the same type?
        return new AbstractCollection<E>() {
            @Override public int size ()
            {
                // oh god, oh god: we iterate and count
                int size = 0;
                for (Iterator<E> iter = iterator(); iter.hasNext(); ) {
                    iter.next();
                    size++;
                }
                return size;
            }

            @Override public boolean add (E element)
            {
                return input.add(element);
            }

            @Override public boolean remove (Object element)
            {
                return input.remove(element);
            }

            @Override public boolean contains (Object element)
            {
                try {
                    @SuppressWarnings("unchecked")
                    E elem = (E) element;
                    return isMatch(elem) && input.contains(elem);

                } catch (ClassCastException cce) {
                    // since it's not an E, it can't be a member of our view
                    return false;
                }
            }

            @Override public Iterator<E> iterator ()
            {
                return filter(input.iterator());
            }
        };
    }

    /** A shared predicate instance that always matches its input. */
    protected static final Predicate<Object> TRUE_INSTANCE = new Predicate<Object>() {
        @Override public boolean isMatch (Object object) {
            return true;
        }
    };

    /** A shared predicate instance that never matches its input. */
    protected static final Predicate<Object> FALSE_INSTANCE = new Predicate<Object>() {
        @Override public boolean isMatch (Object object) {
            return false;
        }
    };
}
