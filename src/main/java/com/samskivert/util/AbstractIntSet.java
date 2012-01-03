//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;

/**
 * A base class for {@link IntSet} implementations.<p>
 *
 * All you really need to do is implement <tt>interable</tt>, but you'll almost certainly want
 * to implement <tt>size</tt> and <tt>contains</tt> for enhanced performance.<p>
 *
 * To implement a modifiable IntSet, the programmer must additionally override this class's
 * <tt>add</tt> and <tt>remove</tt> methods, which will otherwise throw an
 * <tt>UnsupportedOperationException</tt>.<p>
 */
public abstract class AbstractIntSet extends AbstractSet<Integer>
    implements IntSet
{
    /**
     * Add all of the values in the supplied array to the set.
     *
     * @param values elements to be added to this set.
     *
     * @return <tt>true</tt> if this set did not already contain all of the specified elements.
     */
    public boolean add (int[] values)
    {
        boolean modified = false;
        int vlength = values.length;
        for (int i = 0; i < vlength; i++) {
            modified = (add(values[i]) || modified);
        }
        return modified;
    }

    /**
     * Removes all values in the supplied array from the set. Any values that are in the array but
     * not in the set are simply ignored.
     *
     * @param values elements to be removed from the set.
     *
     * @return <tt>true</tt> if this set contained any of the specified elements (which will have
     * been removed).
     */
    public boolean remove (int[] values)
    {
        boolean modified = false;
        int vcount = values.length;
        for (int i = 0; i < vcount; i++) {
            modified = (remove(values[i]) || modified);
        }
        return modified;
    }

    /**
     * {@inheritDoc}
     *
     * <p>This implementation iterates over the ints in the collection, checking each one in turn
     * to see if it's the specified value.
     */
    // from IntSet
    public boolean contains (int value)
    {
        // dumb implementation. You should override.
        for (Interator it = interator(); it.hasNext(); ) {
            if (it.nextInt() == value) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * <p>This implementation simply counts the elements in the interator.
     */
    @Override
    public int size ()
    {
        // dumb implementation. You should override.
        int size = 0;
        for (Interator it = interator(); (size < Integer.MAX_VALUE) && it.hasNext(); it.nextInt()) {
            size++;
        }
        return size;
    }

    /**
     * {@inheritDoc}
     *
     * <p>This implementation simply checks to see if the interator has a first element.
     */
    @Override
    public boolean isEmpty ()
    {
        // possibly dumb implementation. Override if you can do better.
        return !interator().hasNext();
    }

    // from IntSet
    public boolean add (int value)
    {
        throw new UnsupportedOperationException();
    }

    // from IntSet
    public boolean remove (int value)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     *
     * <p>This implementation returns an array containing all the elements returned by the
     * interator.
     */
    // from IntSet
    public int[] toIntArray ()
    {
        int[] vals = new int[size()];
        int ii = 0;
        for (Interator it = interator(); (ii < Integer.MAX_VALUE) && it.hasNext(); ) {
            vals[ii++] = it.nextInt();
        }
        return vals;
    }

    @Override // from AbstractSet<Integer>
    public Iterator<Integer> iterator ()
    {
        return interator();
    }

    @Override // from AbstractSet<Integer>
    public boolean contains (Object o)
    {
        // cope with null or non-Integer
        return (o instanceof Integer) && contains(((Integer)o).intValue());
    }

    @Override // from AbstractSet<Integer>
    public boolean add (Integer i)
    {
        return add(i.intValue()); // will NPE
    }

    @Override // from AbstractSet<Integer>
    public boolean remove (Object o)
    {
        // cope with null or non-Integer
        return (o instanceof Integer) && remove(((Integer)o).intValue());
    }

    @Override // from AbstractSet<Integer>
    public boolean equals (Object o)
    {
        if (o == this) {
            return true;
        }

        if (o instanceof IntSet) {
            IntSet that = (IntSet)o;
            return (this.size() == that.size()) && this.containsAll(that);
        }
        return super.equals(o);
    }

    @Override // from AbstractSet<Integer>
    public int hashCode ()
    {
        int h = 0;
        for (Interator it = interator(); it.hasNext(); ) {
            h += it.nextInt();
        }
        return h;
    }

    @Override // from AbstractSet<Integer>
    public String toString ()
    {
        StringBuilder sb = new StringBuilder("[");
        Interator it = interator();
        if (it.hasNext()) {
            sb.append(it.nextInt());
            while (it.hasNext()) {
                sb.append(", ").append(it.nextInt());
            }
        }
        return sb.append(']').toString();
    }

    @Override // from AbstractSet<Integer>
    public boolean containsAll (Collection<?> c)
    {
        if (c instanceof Interable) {
            for (Interator it = ((Interable) c).interator(); it.hasNext(); ) {
                if (!contains(it.nextInt())) {
                    return false;
                }
            }
            return true;
        }
        return super.containsAll(c);
    }

    @Override // from AbstractSet<Integer>
    public boolean addAll (Collection<? extends Integer> c)
    {
        if (c instanceof Interable) {
            boolean modified = false;
            for (Interator it = ((Interable) c).interator(); it.hasNext(); ) {
                if (add(it.nextInt())) {
                    modified = true;
                }
            }
            return modified;
        }
        return super.addAll(c);
    }

    @Override // from AbstractSet<Integer>
    public boolean removeAll (Collection<?> c)
    {
        if (c instanceof Interable) {
            boolean modified = false;
            for (Interator it = ((Interable)c).interator(); it.hasNext(); ) {
                if (remove(it.nextInt())) {
                    modified = true;
                }
            }
            return modified;
        }
        return super.removeAll(c);
    }

    @Override // from AbstractSet<Integer>
    public boolean retainAll (Collection<?> c)
    {
        if (c instanceof IntSet) {
            IntSet that = (IntSet)c;
            boolean modified = false;
            for (Interator it = interator(); it.hasNext(); ) {
                if (!that.contains(it.nextInt())) {
                    it.remove();
                    modified = true;
                }
            }
            return modified;
        }
        return super.retainAll(c);
    }
}
