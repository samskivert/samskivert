//
// $Id: Collections.java,v 1.1 2001/09/15 17:22:11 mdb Exp $

package com.samskivert.util;

import java.util.*;

/**
 * Like the <code>java.util</code> class of the same name, the
 * <code>Collections</code> class provides utility functions related to
 * the collections provided by the <code>com.samskivert.util</code>
 * package.
 */
public class Collections
{
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

	private transient Set keySet = null;
	private transient Set entrySet = null;
	private transient Collection values = null;

	public Set keySet() {
            synchronized(mutex) {
                if (keySet==null)
                    keySet = new SynchronizedSet(m.keySet(), mutex);
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
}
