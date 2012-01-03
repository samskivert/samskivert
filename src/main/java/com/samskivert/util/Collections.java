//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import java.util.*;

import com.samskivert.annotation.ReplacedBy;

/**
 * Provides functionality for the samskivert collections that the <code>java.util</code> class of
 * the same name provides for the standard Java collections. Collections-related functionality that
 * is different from the standard support provided for the Java collections should go into {@link
 * CollectionUtil}.
 */
public class Collections
{
    /**
     * Returns an Iterator that iterates over all the elements contained within the Iterators
     * within the specified Iterable.
     *
     * @param metaIterable an iterable of Iterators.
     */
    @ReplacedBy("com.google.common.collect.Iterators#concat() or com.google.common.collect.Iterables#concat()")
    public static <T> Iterator<T> getMetaIterator (Iterable<Iterator<T>> metaIterable)
    {
        return new MetaIterator<T>(metaIterable);
    }

    /**
     * Get an Iterator over the supplied Collection that returns the elements in their natural
     * order.
     */
    public static <T extends Comparable<? super T>> Iterator<T> getSortedIterator (Iterable<T> coll)
    {
        return getSortedIterator(coll.iterator(), new Comparator<T>() {
            public int compare (T o1, T o2) {
                if (o1 == o2) { // catches null == null
                    return 0;
                } else if (o1 == null) {
                    return 1;
                } else if (o2 == null) {
                    return -1;
                }
                return o1.compareTo(o2); // null-free
            }
        });
    }

    /**
     * Get an Iterator over the supplied Collection that returns the elements in the order dictated
     * by the supplied Comparator.
     */
    public static <T> Iterator<T> getSortedIterator (Iterable<T> coll, Comparator<T> comparator)
    {
        return getSortedIterator(coll.iterator(), comparator);
    }

    /**
     * Get an Iterator that returns the same elements returned by the supplied Iterator, but in
     * their natural order.
     */
    public static <T extends Comparable<? super T>> Iterator<T> getSortedIterator (Iterator<T> itr)
    {
        return getSortedIterator(itr, new Comparator<T>() {
            public int compare (T o1, T o2) {
                if (o1 == o2) { // catches null == null
                    return 0;
                } else if (o1 == null) {
                    return 1;
                } else if (o2 == null) {
                    return -1;
                }
                return o1.compareTo(o2); // null-free
            }
        });
    }

    /**
     * Get an Iterator that returns the same elements returned by the supplied Iterator, but in the
     * order dictated by the supplied Comparator.
     */
    public static <T> Iterator<T> getSortedIterator (Iterator<T> itr, Comparator<T> comparator)
    {
        SortableArrayList<T> list = new SortableArrayList<T>();
        CollectionUtil.addAll(list, itr);
        list.sort(comparator);
        return getUnmodifiableIterator(list);
    }

    /**
     * Get an Iterator over the supplied Iterable that returns the elements in a completely random
     * order. Normally Iterators return elements in an undefined order, but it is usually the same
     * between different invocations as long as the underlying Iterable has not changed. This
     * method mixes things up.
     */
    public static <T> Iterator<T> getRandomIterator (Iterable<T> c)
    {
        return getRandomIterator(c.iterator());
    }

    /**
     * Get an Iterator that returns the same elements returned by the supplied Iterator, but in a
     * completely random order.
     */
    public static <T> Iterator<T> getRandomIterator (Iterator<T> itr)
    {
        ArrayList<T> list = new ArrayList<T>();
        CollectionUtil.addAll(list, itr);
        java.util.Collections.shuffle(list);
        return getUnmodifiableIterator(list);
    }

    /**
     * Get an Iterator that returns the elements in the supplied Iterable but blocks removal.
     */
    public static <T> Iterator<T> getUnmodifiableIterator (Iterable<T> c)
    {
        return getUnmodifiableIterator(c.iterator());
    }

    /**
     * Get an iterator that returns the same elements as the supplied iterator but blocks removal.
     */
    @ReplacedBy("com.google.common.collect.Iterators#unmodifiableIterator()")
    public static <T> Iterator<T> getUnmodifiableIterator (final Iterator<T> itr)
    {
        return new Iterator<T>() {
            public boolean hasNext () {
                return itr.hasNext();
            }
            public T next () {
                return itr.next();
            }
            public void remove () {
                throw new UnsupportedOperationException(
                    "Cannot remove from an UnmodifiableIterator!");
            }
        };
    }

    /**
     * Returns a synchronized (thread-safe) int map backed by the specified int map.  In order to
     * guarantee serial access, it is critical that <strong>all</strong> access to the backing int
     * map is accomplished through the returned int map.
     *
     * <p> It is imperative that the user manually synchronize on the returned int map when
     * iterating over any of its collection views:
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
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p> The returned map will be serializable if the specified map is serializable.
     *
     * @param m the int map to be "wrapped" in a synchronized int map.
     *
     * @return a synchronized view of the specified int map.
     */
    public static <V> IntMap<V> synchronizedIntMap (IntMap<V> m) {
        return new SynchronizedIntMap<V>(m);
    }

    /**
     * Returns a synchronized (thread-safe) int set backed by the specified int set.  In order to
     * guarantee serial access, it is critical that <strong>all</strong> access to the backing int
     * map is accomplished through the returned int map.
     *
     * <p> It is imperative that the user manually synchronize on the returned int map when
     * iterating over any of its collection views:
     *
     * <pre>
     *  IntSet s = Collections.synchronizedIntSet(new ArrayIntSet());
     *      ...
     *  synchronized(s) {  // Synchronizing on s!
     *      Interator i = s.interator(); // Must be in synchronized block
     *      while (i.hasNext())
     *          foo(i.nextInt());
     *  }
     * </pre>
     *
     * Failure to follow this advice may result in non-deterministic behavior.
     *
     * <p> The returned set will be serializable if the specified set is serializable.
     *
     * @param s the int set to be "wrapped" in a synchronized int set.
     *
     * @return a synchronized view of the specified int set.
     */
    public static IntSet synchronizedIntSet (IntSet s) {
        return new SynchronizedIntSet(s);
    }

    /**
     * Horked from the Java util class and extended for <code>IntMap</code>.
     */
    protected static class SynchronizedIntMap<V> implements IntMap<V>
    {
        private IntMap<V> m; // Backing Map
        private Object mutex; // Object on which to synchronize

        private transient IntSet keySet = null;
        private transient Set<Map.Entry<Integer,V>> entrySet = null;
        private transient Collection<V> values = null;

        SynchronizedIntMap (IntMap<V> m) {
            if (m == null) {
                throw new NullPointerException();
            }
            this.m = m;
            mutex = this;
        }

        SynchronizedIntMap(IntMap<V> m, Object mutex) {
            if (m == null) {
                throw new NullPointerException();
            }
            this.m = m;
            this.mutex = mutex;
        }

        public int size () {
            synchronized(mutex) {return m.size();}
        }

        public boolean isEmpty (){
            synchronized(mutex) {return m.isEmpty();}
        }

        public boolean containsKey (int key) {
            synchronized(mutex) {return m.containsKey(key);}
        }

        public boolean containsKey (Object key) {
            synchronized(mutex) {return m.containsKey(key);}
        }

        public boolean containsValue (Object value){
            synchronized(mutex) {return m.containsValue(value);}
        }

        public V get (int key) {
            synchronized(mutex) {return m.get(key);}
        }

        public V get (Object key) {
            synchronized(mutex) {return m.get(key);}
        }

        public V put (int key, V value) {
            synchronized(mutex) {return m.put(key, value);}
        }

        public V put (Integer key, V value) {
            synchronized(mutex) {return m.put(key, value);}
        }

        public V remove (int key) {
            synchronized(mutex) {return m.remove(key);}
        }

        public V remove (Object key) {
            synchronized(mutex) {return m.remove(key);}
        }

        public void putAll (Map<? extends Integer,? extends V> map) {
            synchronized(mutex) {m.putAll(map);}
        }

        public void clear () {
            synchronized(mutex) {m.clear();}
        }

        public Set<Integer> keySet () {
            return intKeySet();
        }

        public IntSet intKeySet () {
            synchronized(mutex) {
                if (keySet == null) {
                    keySet = new SynchronizedIntSet(m.intKeySet(), mutex);
                }
                return keySet;
            }
        }

        public Set<Map.Entry<Integer,V>> entrySet () {
            synchronized(mutex) {
                if (entrySet == null) {
                    entrySet = new SynchronizedSet<Map.Entry<Integer,V>>(m.entrySet(), mutex);
                }
                return entrySet;
            }
        }

        public Set<IntEntry<V>> intEntrySet () {
            synchronized(mutex) {
                return new SynchronizedSet<IntEntry<V>>(m.intEntrySet(), mutex);
            }
        }

        public Collection<V> values () {
            synchronized(mutex) {
                if (values == null) {
                    values = new SynchronizedCollection<V>(m.values(), mutex);
                }
                return values;
            }
        }

        @Override
        public boolean equals (Object o) {
            synchronized(mutex) {return m.equals(o);}
        }

        @Override
        public int hashCode () {
            synchronized(mutex) {return m.hashCode();}
        }

        @Override
        public String toString () {
            synchronized(mutex) {return m.toString();}
        }
    }

    /**
     * I wish I could use this from the <code>java.util.Collections</code> class, but those crazy
     * kids at Sun are always using private and default access and pointlessly preventing people
     * from properly reusing their code. Yay!
     */
    protected static class SynchronizedSet<E> extends SynchronizedCollection<E> implements Set<E>
    {
        SynchronizedSet (Set<E> s) {
            super(s);
        }
        SynchronizedSet(Set<E> s, Object mutex) {
            super(s, mutex);
        }

        @Override
        public boolean equals (Object o) {
            synchronized(mutex) {return c.equals(o);}
        }
        @Override
        public int hashCode () {
            synchronized(mutex) {return c.hashCode();}
        }
    }

    protected static class SynchronizedIntSet extends SynchronizedSet<Integer>
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
            return _i.interator(); // must be manually sync'd by user
        }

        public int[] toIntArray () {
            synchronized(mutex) {return _i.toIntArray();}
        }

        @Override
        public boolean equals (Object o) {
            synchronized(mutex) {return _i.equals(o);}
        }

        @Override
        public int hashCode () {
            synchronized(mutex) {return _i.hashCode();}
        }

        /** Properly casted reference to our backing set. */
        protected IntSet _i;
    }

    /**
     * I wish I could use this from the <code>java.util.Collections</code> class, but those crazy
     * kids at Sun are always using private and default access and pointlessly preventing people
     * from properly reusing their code. Yay!
     */
    protected static class SynchronizedCollection<E> implements Collection<E>
    {
        protected Collection<E> c;   // Backing Collection
        protected Object mutex;  // Object on which to synchronize

        SynchronizedCollection (Collection<E> c) {
            if (c==null) {
                throw new NullPointerException();
            }
            this.c = c;
            mutex = this;
        }
        SynchronizedCollection(Collection<E> c, Object mutex) {
            this.c = c;
            this.mutex = mutex;
        }

        public int size () {
            synchronized(mutex) {return c.size();}
        }
        public boolean isEmpty () {
            synchronized(mutex) {return c.isEmpty();}
        }
        public boolean contains (Object o) {
            synchronized(mutex) {return c.contains(o);}
        }
        public Object[] toArray () {
            synchronized(mutex) {return c.toArray();}
        }
        public <T> T[] toArray (T[] a) {
            synchronized(mutex) {return c.toArray(a);}
        }

        public Iterator<E> iterator () {
            return c.iterator(); // Must be manually synched by user!
        }

        public boolean add (E o) {
            synchronized(mutex) {return c.add(o);}
        }
        public boolean remove (Object o) {
            synchronized(mutex) {return c.remove(o);}
        }

        public boolean containsAll (Collection<?> coll) {
            synchronized(mutex) {return c.containsAll(coll);}
        }
        public boolean addAll (Collection<? extends E> coll) {
            synchronized(mutex) {return c.addAll(coll);}
        }
        public boolean removeAll (Collection<?> coll) {
            synchronized(mutex) {return c.removeAll(coll);}
        }
        public boolean retainAll (Collection<?> coll) {
            synchronized(mutex) {return c.retainAll(coll);}
        }
        public void clear () {
            synchronized(mutex) {c.clear();}
        }
        @Override
        public String toString () {
            synchronized(mutex) {return c.toString();}
        }
    }

    /**
     * An iterator that iterates over the union of the iterators provided by a collection of
     * iterators.
     */
    protected static class MetaIterator<T> implements Iterator<T>
    {
        /**
         * @param iterable a Iterable containing more Iterables whose elements we are to iterate
         * over.
         */
        public MetaIterator (Iterable<Iterator<T>> iterable)
        {
            _meta = iterable.iterator();
        }

        // documentation inherited from interface Iterator
        public boolean hasNext ()
        {
            while ((_current == null) || (!_current.hasNext())) {
                if (_meta.hasNext()) {
                    _current = _meta.next();
                } else {
                    return false;
                }
            }
            return true;
        }

        // documentation inherited from interface Iterator
        public T next ()
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
        protected Iterator<Iterator<T>> _meta;

        /** The current sub-collection's iterator. */
        protected Iterator<T> _current;
    }
}
