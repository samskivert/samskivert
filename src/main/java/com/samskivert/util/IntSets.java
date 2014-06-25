//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import java.util.Collection;
import java.util.NoSuchElementException;

/**
 * Utility methods for working with IntSets.
 */
public class IntSets
{
    /** Uninstantiable. */
    private IntSets () {}

    /**
     * Create a new IntSet, initially empty.
     */
    public static IntSet create ()
    {
        return new ArrayIntSet();
    }

    /**
     * Create a new IntSet, initially containing the specified ints.
     */
    public static IntSet create (int... ints)
    {
        checkNotNull(ints);
        return new ArrayIntSet(ints);
    }

    /**
     * Create a new IntSet, initially containing the ints in the specified IntSet.
     */
    public static IntSet create (IntSet from)
    {
        checkNotNull(from);
        return new ArrayIntSet(from);
    }

    /**
     * Create a new IntSet, initially containing the ints in the specified collection.
     */
    public static IntSet create (Collection<Integer> from)
    {
        checkNotNull(from);
        return new ArrayIntSet(from);
    }

    /**
     * Return an <b>immutable</b> empty IntSet.
     */
    public static IntSet emptyIntSet ()
    {
        return EMPTY_INTSET;
    }

    /**
     * Return an unmodifiable view of the specified IntSet
     */
    public static IntSet unmodifiableIntSet (IntSet s)
    {
        checkNotNull(s);
        return new UnmodifiableIntSetView(s);
    }

    /**
     * Create a new IntSet containing the ints that are contained in <b>all</b>
     * of the specified sets. The returned set may be further modified per your needs.
     */
    public static IntSet and (IntSet... sets)
    {
        checkNotNull(sets);
        IntSet result = create();
        int len = sets.length;
        if (len > 0) {
            OUTER:
            for (Interator it = sets[0].interator(); it.hasNext(); ) {
                int val = it.nextInt();
                for (int ii = 1; ii < len; ii++) {
                    if (!sets[ii].contains(val)) {
                        continue OUTER;
                    }
                }
                result.add(val);
            }
        }
        return result;
    }

    /**
     * Returns an <b>immutable</b> view of the underlying sets.
     */
    public static IntSet andView (IntSet... sets)
    {
        checkNotNull(sets);
        return new AndIntSetView(sets);
    }

    /**
     * Create a new IntSet containing the ints that are contained in <b>any</b>
     * of the specified sets. The returned set may be further modified per your needs.
     */
    public static IntSet or (IntSet... sets)
    {
        checkNotNull(sets);
        IntSet result = create();
        for (IntSet set : sets) {
            result.addAll(set);
        }
        return result;
    }

    /**
     * Returns an <b>immutable</b> view of the underlying sets.
     */
    public static IntSet orView (IntSet... sets)
    {
        checkNotNull(sets);
        return new OrIntSetView(sets);
    }

    /**
     * Creates a new IntSet, initially populated with ints contained in set1 but not in set2.
     * Set2 may also contain elements not present in set1, these are ignored.
     */
    public static IntSet difference (IntSet set1, IntSet set2)
    {
        return and(set1, notView(set2));
    }

    /**
     * Returns an <b>immutable</b> view containing the ints contained in set1 but not in set2.
     * Set2 may also contain elements not present in set1, these are ignored.
     */
    public static IntSet differenceView (IntSet set1, IntSet set2)
    {
        return andView(set1, notView(set2));
    }

    /**
     * Returns a <b>immutable</b> view of the underlying set.
     *
     * TODO: not yet public because iteration and size are weird.
     */
    protected static IntSet notView (IntSet set)
    {
        checkNotNull(set);
        // TODO
        return new NotIntSetView(set);
    }

    /**
     * Validate the specified argument.
     */
    protected static void checkNotNull (Object o)
    {
        if (o == null) {
            throw new NullPointerException();
        }
    }

    /**
     * Validate the specified arguments.
     */
    protected static void checkNotNull (Object[] array)
    {
        checkNotNull((Object)array);
        for (Object o : array) {
            checkNotNull(o);
        }
    }

    /** The immutable empty IntSet. */
    protected static final IntSet EMPTY_INTSET = new EmptyIntSet();

    /**
     * An extension to AbstractIntSet that implements a "true" immutable IntSet.
     * That is, calling removeAll() with an empty Collection will still throw an Exception.
     */
    protected static abstract class AbstractImmutableIntSet extends AbstractIntSet
    {
        // the following are overridden so that they don't appear to work if given empty collections
        // or an improper Object key
        @Override public boolean remove (Object o) { throw new UnsupportedOperationException(); }
        @Override public boolean addAll (Collection<? extends Integer> c) {
            throw new UnsupportedOperationException();
        }
        @Override public boolean removeAll (Collection<?> c) {
            throw new UnsupportedOperationException();
        }
        @Override public boolean retainAll (Collection<?> c) {
            throw new UnsupportedOperationException();
        }
        @Override public void clear () {
            throw new UnsupportedOperationException();
        }
    } // end: class AbstractImmutableIntSet

    /**
     * The empty IntSet.
     */
    protected static class EmptyIntSet extends AbstractImmutableIntSet
    {
        @Override public boolean contains (int value) { return false; }
        @Override public int size () { return 0; }
        @Override public boolean isEmpty () { return true; }

        public Interator interator () {
            return EMPTY_INTERATOR;
        }

        protected static final Interator EMPTY_INTERATOR = new AbstractInterator() {
            public boolean hasNext () { return false; }
            public int nextInt () { throw new NoSuchElementException(); }
        };
    } // end: class EmptyIntSet

    /**
     * An immutable view of another IntSet.
     */
    protected static class UnmodifiableIntSetView extends AbstractImmutableIntSet
    {
        public UnmodifiableIntSetView (IntSet s) {
            _s = s;
        }

        @Override public boolean contains (int value) { return _s.contains(value); }
        @Override public int size () { return _s.size(); }
        @Override public boolean isEmpty () { return _s.isEmpty(); }

        public Interator interator () {
            return new AbstractInterator() {
                public boolean hasNext () { return _i.hasNext(); }
                public int nextInt () { return _i.nextInt(); }

                protected Interator _i = _s.interator();
            };
        }

        protected IntSet _s;
    } // end: class UnmodifiableIntSetView

    /**
     * A building-block Interator that utilizes a findNext() method to locate the next int
     * to return.
     */
    protected static abstract class FindingInterator extends AbstractInterator
    {
        public boolean hasNext () {
            return _hasNext || (_hasNext = findNext());
        }

        public int nextInt () {
            if (_hasNext) {
                _hasNext = false;
            } else if (!findNext()) { // support calling nextInt() without using hasNext() (tsk tsk)
                throw new NoSuchElementException();
            }
            return _next;
        }

        /**
         * Populate _next with the next value and return true, or return false if there are
         * no more ints in this Interator.
         */
        protected abstract boolean findNext ();

        /** Does _next contain a valid value? */
        protected boolean _hasNext;

        /** The next value to return, iff _hasNext is true. */
        protected int _next;
    } // end: class FindingInterator

    /**
     * An "and" view of the specified sources.
     */
    protected static class AndIntSetView extends AbstractImmutableIntSet
    {
        public AndIntSetView (IntSet... sources) {
            if (sources.length == 0) {
                _sources = new IntSet[] { IntSets.emptyIntSet() };

            } else {
                // TODO: sort sources by size (smallest first) to optimize?
                // TODO: copy to prevent befuckery?
                _sources = sources;
            }
        }

        @Override
        public boolean contains (int value) {
            for (IntSet src : _sources) {
                if (!src.contains(value)) {
                    return false;
                }
            }
            return true;
        }

        // TODO: smarter size() (Right now we're using inherited, which counts interated)

        public Interator interator () {
            return new FindingInterator() {
                @Override protected boolean findNext () {
                    OUTER:
                    while (_i.hasNext()) {
                        _next = _i.nextInt();
                        // make sure it's in all the other sets
                        for (int ii = 1; ii < _sources.length; ii++) {
                            if (!_sources[ii].contains(_next)) {
                                continue OUTER;
                            }
                        }
                        return true;
                    }
                    return false;
                }

                protected Interator _i = _sources[0].interator();
            };
        }

        protected IntSet[] _sources;
    } // end: class AndIntSetView

    /**
     * An "or" view of the specified sources.
     */
    protected static class OrIntSetView extends AbstractImmutableIntSet {
        public OrIntSetView (IntSet... sources) {
            _sources = sources;
            // TODO: sort sources by size (largest first), to optimize general checks?
            // TODO: copy array to prevent befuckery?
        }

        @Override
        public boolean contains (int value) {
            for (IntSet src : _sources) {
                if (src.contains(value)) {
                    return true;
                }
            }
            return false;
        }

        // TODO: smarter size() (right now we're using inherited, which

        public Interator interator () {
            return new FindingInterator() {
                @Override
                protected boolean findNext () {
                    while (true) {
                        if (_i == null) {
                            if (_srcIdx >= _sources.length - 1) {
                                return false;
                            }
                            _i = _sources[++_srcIdx].interator();
                        }
                        OUTER: while (_i.hasNext()) {
                            _next = _i.nextInt();
                            // make sure it's not in any of the previous sources
                            for (int ii = 0; ii < _srcIdx; ii++) {
                                if (_sources[ii].contains(_next)) {
                                    continue OUTER;
                                }
                            }
                            return true;
                        }
                        _i = null;
                    }
                }

                protected Interator _i;
                protected int _srcIdx = -1;
            };
        }

        protected IntSet[] _sources;
    } // end: class OrIntSetView

    /**
     * An "or" view of the specified sources.
     */
    protected static class NotIntSetView extends AbstractImmutableIntSet
    {
        /**
         * Construct a ComplementIntSet with the specified view source.
         */
        public NotIntSetView (IntSet source) {
            _source = source;
        }

        @Override
        public int size () {
            // Even if our source was an ArrayIntSet with exactly MAX_VALUE elements,
            // there are still over MAX_VALUE ints in the complement!
            if (_source.size() < Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            }
            // else: iterate
            return super.size();
        }

        @Override // from IntSet
        public boolean contains (int value) {
            return !_source.contains(value);
        }

        /**
         * Returns the positive integers first, starting with 0.
         */
        // from IntSet
        public Interator interator () {
            return new FindingInterator() {
                @Override
                protected boolean findNext () {
                    if (_positive) {
                        // first go from 0 to MAX_VALUE
                        while (_next != Integer.MAX_VALUE) {
                            if (contains(++_next)) {
                                return true;
                            }
                        }
                        // prepare to go negative.. (preparing to go negative, sir!)
                        _positive = false;
                        _next = 0;
                    }
                    // now go down from -1 to MIN_VALUE
                    while (_next != Integer.MIN_VALUE) {
                        if (contains(--_next)) {
                            return true;
                        }
                    }
                    return false;
                }

                { // initializer
                    _next = -1;
                }

                protected boolean _positive = true;
            };
        }

        @Override // from IntSet
        public int[] toIntArray () {
            Interator it = interator();
            if (!it.hasNext()) {
                return new int[0];
            }
            int[] array = new int[Integer.MAX_VALUE];
            int index = 0;
            do {
                array[index++] = it.nextInt();
            } while ((index < Integer.MAX_VALUE) && it.hasNext());
            // we may need to trim the array down to size
            // 1.6ism: return (index == Integer.MAX_VALUE) ? array : Arrays.copyOf(array, index);
            if (index == Integer.MAX_VALUE) {
                return array;

            } else {
                int[] trimmed = new int[index];
                System.arraycopy(array, 0, trimmed, 0, index);
                return trimmed;
            }
        }

        /** The ints we <b>don't</b> contain. */
        protected IntSet _source;
    } // end: class NotIntSetView
}
