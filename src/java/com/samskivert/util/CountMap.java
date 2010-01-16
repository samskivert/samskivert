//
// $Id$

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A CountMap maps keys to non-null Integers and provides methods for efficiently adding
 * to the count.
 */
public class CountMap<K> extends AbstractMap<K, Integer>
{
    /**
     * Create a new CountMap backed by a HashMap.
     */
    public CountMap ()
    {
        this(new HashMap<K, int[]>());
    }

    /**
     * For subclassing, etc. Not yet public.
     */
    protected CountMap (Map<K, int[]> backing)
    {
        if (!backing.isEmpty()) {
            throw new IllegalArgumentException("Map is non-empty");
        }
        _backing = backing;
    }

    /**
     * Add 1 to the count for the specified key.
     */
    public int increment (K key)
    {
        return add(key, 1);
    }

    /**
     * Subtract 1 from the count for the specified key.
     */
    public int decrement (K key)
    {
        return add(key, -1);
    }

    /**
     * Add the specified amount to the count for the specified key.
     */
    public int add (K key, int amount)
    {
        int[] val = _backing.get(key);
        if (val == null) {
            _backing.put(key, val = new int[1]);
        }
        val[0] += amount;
        return val[0];
    }

    /**
     * Get the count for the specified key. If the key is not present, 0 is returned.
     */
    public int getCount (K key)
    {
        int[] val = _backing.get(key);
        return (val == null) ? 0 : val[0];
    }

    /**
     * Remove any keys for which the count is currently 0.
     */
    public void compress ()
    {
        for (Iterator<int[]> it = _backing.values().iterator(); it.hasNext(); ) {
            if (it.next()[0] == 0) {
                it.remove();
            }
        }
    }

    @Override
    public Set<Map.Entry<K, Integer>> entrySet ()
    {
        if (_entrySet == null) {
            _entrySet = new EntrySet();
        }
        return _entrySet;
    }

    @Override
    public Integer put (K key, Integer value)
    {
        return integer(_backing.put(key, new int[] { value.intValue() }));
    }

    @Override
    public boolean containsKey (Object key)
    {
        return _backing.containsKey(key);
    }

    @Override
    public Integer get (Object key)
    {
        return integer(_backing.get(key));
    }

    @Override
    public Integer remove (Object key)
    {
        return integer(_backing.remove(key));
    }

    @Override
    public void clear ()
    {
        _backing.clear();
    }

    @Override
    public int size ()
    {
        return _backing.size();
    }

    @Override
    public boolean isEmpty ()
    {
        return _backing.isEmpty();
    }

    /**
     * Our EntrySet.
     */
    protected class EntrySet extends AbstractSet<Map.Entry<K, Integer>>
    {
        public Iterator<Map.Entry<K, Integer>> iterator () {
            return new Iterator<Map.Entry<K, Integer>>() {
                public boolean hasNext () {
                    return _it.hasNext();
                }

                public Map.Entry<K, Integer> next () {
                    // I don't see any way around creating a new Entry here
                    return adaptEntry(_it.next());
                }

                public void remove () {
                    _it.remove();
                }
                protected Iterator<Map.Entry<K, int[]>> _it = _backing.entrySet().iterator();
            };
        }

        public boolean contains (Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry<?, ?> entry = (Map.Entry<?, ?>) o;
            Integer value = CountMap.this.get(entry.getKey());
            // we don't allow storing null, so getting a null means there's no mapping,
            // and we don't have to check containsKey
            return (value != null) && value.equals(entry.getValue());
        }

        public boolean remove (Object o) {
            if (contains(o)) {
                CountMap.this.remove(((Map.Entry<?, ?>) o).getKey());
                return true;
            }
            return false;
        }

        public int size () {
            return CountMap.this.size();
        }

        public void clear () {
            CountMap.this.clear();
        }
    }

    /**
     * Return null or the boxed value contained in the count.
     */
    protected static final Integer integer (int[] val)
    {
        return (val == null) ? null : val[0];
    }

    /**
     * Adapt an entry from our internal backing map to one visible to users of this class.
     * Grumble.
     */
    protected static <K> Map.Entry<K, Integer> adaptEntry (final Map.Entry<K, int[]> entry)
    {
        return new Map.Entry<K, Integer>() {
            public K getKey () {
                return entry.getKey();
            }
            public Integer getValue () {
                return integer(entry.getValue());
            }
            public Integer setValue (Integer newVal) {
                int[] val = entry.getValue();
                Integer ret = integer(val);
                val[0] = newVal.intValue();
                return ret;
            }
            public int hashCode () {
                K key = getKey();
                return ((key == null) ? 0 : key.hashCode()) ^ getValue().hashCode();
            }
            public boolean equals (Object o) {
                if (!(o instanceof Map.Entry)) {
                    return false;
                }
                Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
                K key = getKey();
                Object key2 = e.getKey();
                return ((key == null) ? (key2 == null) : key.equals(key2)) &&
                    getValue().equals(e.getValue());
            }
            public String toString () {
                return getKey() + "=" + entry.getValue()[0];
            }
        };
    }

    /** Our backing map */
    protected Map<K, int[]> _backing;

    /** The entrySet, if created. */
    protected transient volatile Set<Map.Entry<K, Integer>> _entrySet = null;
}
