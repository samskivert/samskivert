//
// $Id: Collections.java,v 1.4 2002/11/07 18:40:40 ray Exp $

package com.samskivert.util;

import java.util.*;

/**
 * Provides functionality for the samskivert collections that the
 * <code>java.util</code> class of the same name provides for the standard
 * Java collections. Collections-related functionality that is different
 * from the standard support provided for the Java collections should go
 * into {@link CollectionUtil}.
 */
public class Collections
{
    /**
     * Returns an Iterator that iterates over all the elements contained
     * within the Collections within the specified Collection.
     *
     * @param metaCollection a collection of either other Collections and/or
     * of Iterators.
     */
    public static Iterator getMetaIterator (Collection metaCollection)
    {
        return new MetaIterator(metaCollection);
    }

    /**
     * Get an Iterator over the supplied Collection that returns the
     * elements in their natural order.
     */
    public static Iterator getSortedIterator (Collection coll)  
    {
        return getSortedIterator(coll.iterator(), Comparators.COMPARABLE);
    }

    /**
     * Get an Iterator over the supplied Collection that returns the
     * elements in the order dictated by the supplied Comparator.
     */
    public static Iterator getSortedIterator (Collection coll,
                                              Comparator comparator)
    {
        return getSortedIterator(coll.iterator(), comparator);
    }

    /**
     * Get an Iterator that returns the same elements returned by
     * the supplied Iterator, but in their natural order.
     */
    public static Iterator getSortedIterator (Iterator itr)
    {
        return getSortedIterator(itr, Comparators.COMPARABLE);
    }

    /**
     * Get an Iterator that returns the same elements returned by
     * the supplied Iterator, but in the order dictated by the supplied
     * Comparator.
     */
    public static Iterator getSortedIterator (Iterator itr,
                                              Comparator comparator)
    {
        SortableArrayList list = new SortableArrayList();
        CollectionUtil.addAll(list, itr);
        list.sort(comparator);
        return getUnmodifiableIterator(list);
    }

    /**
     * Get an Iterator over the supplied Collection that returns
     * the elements in a completely random order. Normally Iterators
     * return elements in an undefined order, but it is usually the same
     * between different invocations as long as the underlying Collection
     * has not changed. This method mixes things up.
     */
    public static Iterator getRandomIterator (Collection c)
    {
        return getRandomIterator(c.iterator());
    }

    /**
     * Get an Iterator that returns the same elements returned by
     * the supplied Iterator, but in a completely random order.
     */
    public static Iterator getRandomIterator (Iterator itr)
    {
        ArrayList list = new ArrayList();
        CollectionUtil.addAll(list, itr);
        java.util.Collections.shuffle(list);
        return getUnmodifiableIterator(list);
    }

    /**
     * Get an Iterator that returns the elements in the supplied
     * Collection but blocks removal.
     */
    public static Iterator getUnmodifiableIterator (Collection c)
    {
        return getUnmodifiableIterator(c.iterator());
    }

    /**
     * Get an iterator that returns the same elements as the supplied
     * iterator but blocks removal.
     */
    public static Iterator getUnmodifiableIterator (final Iterator itr)
    {
        return new Iterator() {
            public boolean hasNext ()
            {
                return itr.hasNext();
            }

            public Object next ()
            {
                return itr.next();
            }

            public void remove ()
            {
                throw new UnsupportedOperationException(
                    "Cannot remove from an UnmodifiableIterator!");
            }
        };
    }

    /**
     * Returns a synchronized (thread-safe) int map backed by the
     * specified int map.  In order to guarantee serial access, it is
     * critical that <strong>all</strong> access to the backing int map is
     * accomplished through the returned int map.
     *
     * <p> It is imperative that the user manually synchronize on the
     * returned int map when iterating over any of its collection views:
     *
     * <pre>
     *  IntMap m = Collections.synchronizedIntMap(new HashIntMap());
     *      ...
     *  Set s = m.keySet();  // Needn't be in synchronized block
     *      ...
     *  synchronized(m) {  // Synchronizing on m, not s!
     *      Iterator i = s.iterator(); // Must be in synchronized block
     *      while (i.hasNext())
     *          foo(i.next());
     *  }
     * </pre>
     *
     * Failure to follow this advice may result in non-deterministic
     * behavior.
     *
     * <p> The returned map will be serializable if the specified map is
     * serializable.
     *
     * @param m the int map to be "wrapped" in a synchronized int map.
     *
     * @return a synchronized view of the specified int map.
     */
    public static IntMap synchronizedIntMap(IntMap m) {
	return new SynchronizedIntMap(m);
    }

    /**
     * Horked from the Java util class and extended for <code>IntMap</code>.
     */
    protected static class SynchronizedIntMap implements IntMap
    {
	private IntMap m; // Backing Map
        private Object mutex; // Object on which to synchronize

	SynchronizedIntMap(IntMap m) {
            if (m == null) {
                throw new NullPointerException();
            }
            this.m = m;
            mutex = this;
        }

	SynchronizedIntMap(IntMap m, Object mutex) {
            if (m == null) {
                throw new NullPointerException();
            }
            this.m = m;
            this.mutex = mutex;
        }

	public int size() {
	    synchronized(mutex) {return m.size();}
        }

        public boolean isEmpty(){
	    synchronized(mutex) {return m.isEmpty();}
        }

        public boolean containsKey(int key) {
	    synchronized(mutex) {return m.containsKey(key);}
        }

        public boolean containsKey(Object key) {
	    synchronized(mutex) {return m.containsKey(key);}
        }

        public boolean containsValue(Object value){
	    synchronized(mutex) {return m.containsValue(value);}
        }

        public Object get(int key) {
	    synchronized(mutex) {return m.get(key);}
        }

        public Object get(Object key) {
	    synchronized(mutex) {return m.get(key);}
        }

	public Object put(int key, Object value) {
	    synchronized(mutex) {return m.put(key, value);}
        }

	public Object put(Object key, Object value) {
	    synchronized(mutex) {return m.put(key, value);}
        }

	public Object remove(int key) {
	    synchronized(mutex) {return m.remove(key);}
        }

	public Object remove(Object key) {
	    synchronized(mutex) {return m.remove(key);}
        }

	public void putAll(Map map) {
	    synchronized(mutex) {m.putAll(map);}
        }

	public void clear() {
	    synchronized(mutex) {m.clear();}
        }

	private transient IntSet keySet = null;
	private transient Set entrySet = null;
	private transient Collection values = null;

	public Set keySet() {
            return intKeySet();
	}

        public IntSet intKeySet () {
            synchronized(mutex) {
                if (keySet==null)
                    keySet = new SynchronizedIntSet(m.intKeySet(), mutex);
                return keySet;
            }
        }

        public Set entrySet() {
            synchronized(mutex) {
                if (entrySet==null)
                    entrySet = new SynchronizedSet(m.entrySet(), mutex);
                return entrySet;
            }
	}

	public Collection values() {
            synchronized(mutex) {
                if (values==null)
                    values = new SynchronizedCollection(m.values(), mutex);
                return values;
            }
        }

	public boolean equals(Object o) {
            synchronized(mutex) {return m.equals(o);}
        }

	public int hashCode() {
            synchronized(mutex) {return m.hashCode();}
        }

	public String toString() {
	    synchronized(mutex) {return m.toString();}
        }
    }

    /**
     * I wish I could use this from the <code>java.util.Collections</code>
     * class, but those crazy kids at Sun are always using private and
     * default access and pointlessly preventing people from properly
     * reusing their code. Yay!
     */
    protected static class SynchronizedSet
        extends SynchronizedCollection implements Set {
	SynchronizedSet(Set s) {
            super(s);
        }
	SynchronizedSet(Set s, Object mutex) {
            super(s, mutex);
        }

	public boolean equals(Object o) {
	    synchronized(mutex) {return c.equals(o);}
        }
	public int hashCode() {
	    synchronized(mutex) {return c.hashCode();}
        }
    }

    protected static class SynchronizedIntSet extends SynchronizedSet
        implements IntSet
    {
        SynchronizedIntSet (IntSet s) {
            super(s);
            _i = s;
        }

        SynchronizedIntSet (IntSet s, Object mutex) {
            super(s, mutex);
            _i = s;
        }

        public boolean contains (int value) {
            synchronized(mutex) {return _i.contains(value);}
        }

        public boolean add (int value) {
            synchronized(mutex) {return _i.add(value);}
        }

        public boolean remove (int value) {
            synchronized(mutex) {return _i.remove(value);}
        }

        public Interator interator () {
            // must be manually sync'd by user
            return _i.interator();
        }

        public int[] toIntArray () {
            synchronized(mutex) {return _i.toIntArray();}
        }

        /** Pre-casted version of our backing set. */
        protected IntSet _i;
    }

    /**
     * I wish I could use this from the <code>java.util.Collections</code>
     * class, but those crazy kids at Sun are always using private and
     * default access and pointlessly preventing people from properly
     * reusing their code. Yay!
     */
    protected static class SynchronizedCollection
        implements Collection {
	Collection c;	   // Backing Collection
	Object	   mutex;  // Object on which to synchronize

	SynchronizedCollection(Collection c) {
            if (c==null)
                throw new NullPointerException();
	    this.c = c;
            mutex = this;
        }
	SynchronizedCollection(Collection c, Object mutex) {
	    this.c = c;
            this.mutex = mutex;
        }

	public int size() {
	    synchronized(mutex) {return c.size();}
        }
	public boolean isEmpty() {
	    synchronized(mutex) {return c.isEmpty();}
        }
	public boolean contains(Object o) {
	    synchronized(mutex) {return c.contains(o);}
        }
	public Object[] toArray() {
	    synchronized(mutex) {return c.toArray();}
        }
	public Object[] toArray(Object[] a) {
	    synchronized(mutex) {return c.toArray(a);}
        }

	public Iterator iterator() {
            return c.iterator(); // Must be manually synched by user!
        }

	public boolean add(Object o) {
	    synchronized(mutex) {return c.add(o);}
        }
	public boolean remove(Object o) {
	    synchronized(mutex) {return c.remove(o);}
        }

	public boolean containsAll(Collection coll) {
	    synchronized(mutex) {return c.containsAll(coll);}
        }
	public boolean addAll(Collection coll) {
	    synchronized(mutex) {return c.addAll(coll);}
        }
	public boolean removeAll(Collection coll) {
	    synchronized(mutex) {return c.removeAll(coll);}
        }
	public boolean retainAll(Collection coll) {
	    synchronized(mutex) {return c.retainAll(coll);}
        }
	public void clear() {
	    synchronized(mutex) {c.clear();}
        }
	public String toString() {
	    synchronized(mutex) {return c.toString();}
        }
    }

    /**
     * An iterator that iterates over the union of the iterators provided by a
     * collection of collections.
     */
    protected static class MetaIterator implements Iterator
    {
        /**
         * @param collections a Collection containing more Collections
         * whose elements we are to iterate over.
         */
        public MetaIterator (Collection collections)
        {
            _meta = collections.iterator();
        }

        // documentation inherited from interface Iterator
        public boolean hasNext ()
        {
            while ((_current == null) || (!_current.hasNext())) {
                if (_meta.hasNext()) {
                    Object o = _meta.next();
                    if (o instanceof Iterator) {
                        _current = (Iterator) o;
                    // TODO: jdk1.5,
                    // (obsoletes the Collection case, below)
                    //} else if (o instanceof Iterable) {
                    //    _current = ((Iterable) o).iterator();
                    } else if (o instanceof Collection) {
                        _current = ((Collection) o).iterator();
                    } else {
                        throw new IllegalArgumentException(
                            "MetaIterator must be constructed with a " +
                            "collection of Iterators or other collections.");
                    }

                } else {
                    return false;
                }
            }
            return true;
        }

        // documentation inherited from interface Iterator
        public Object next ()
        {
            if (hasNext()) {
                return _current.next();
            } else {
                throw new NoSuchElementException();
            }
        }

        // documentation inherited from interface Iterator
        public void remove ()
        {
            if (_current != null) {
                _current.remove();
            } else {
                throw new IllegalStateException();
            }
        }

        /** The iterator through the collection we were constructed with. */
        protected Iterator _meta;

        /** The current sub-collection's iterator. */
        protected Iterator _current;
    }
}
