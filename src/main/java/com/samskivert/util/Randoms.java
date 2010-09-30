//
// $Id$

package com.samskivert.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Provides utility routines to simplify obtaining randomized values.
 */
public class Randoms
{
    /** A default Randoms that can be safely shared by any caller. */
    public static final Randoms RAND = with(new Random());

    /**
     * A factory to create a new Randoms object.
     */
    public static Randoms with (Random rand)
    {
        return new Randoms(rand);
    }

    /**
     * Returns a pseudorandom, uniformly distributed <code>int</code> value between 0 (inclusive)
     * and the specified value (exclusive).
     *
     * @param high the high value limiting the random number sought.
     *
     * @throws IllegalArgumentException if <code>high</code> is not positive.
     */
    public int getInt (int high)
    {
        return _r.nextInt(high);
    }

    /**
     * Returns a pseudorandom, uniformly distributed <code>int</code> value between
     * <code>low</code> (inclusive) and <code>high</code> (exclusive).
     *
     * @throws IllegalArgumentException if <code>high - low</code> is not positive.
     */
    public int getInRange (int low, int high)
    {
        return low + _r.nextInt(high - low);
    }

    /**
     * Returns a pseudorandom, uniformly distributed float value between 0.0 (inclusive) and the
     * specified value (exclusive).
     *
     * @param high the high value limiting the random number sought.
     */
    public float getFloat (float high)
    {
        return _r.nextFloat() * high;
    }
    
    /**
     * Returns a pseudorandom, uniformly distributed <code>float</code> value between
     * <code>low</code> (inclusive) and <code>high</code> (exclusive).
     */
    public float getInRange (float low, float high)
    {
        return low + (_r.nextFloat() * (high - low));
    }

    /**
     * Returns true approximately one in n times.
     */
    public boolean getChance (int n)
    {
        return (0 == _r.nextInt(n));
    }

    /**
     * Has a probability p of returning true.
     */
    public boolean getProbability (float p)
    {
        return _r.nextFloat() < p;
    }

    /**
     * Returns true or false with approximately even distribution.
     */
    public boolean getBoolean ()
    {
        return _r.nextBoolean();
    }

    /**
     * Returns a key from the supplied map according to a probability computed as
     * the key's value divided by the total of all the key's values.
     *
     * @throws NullPointerException if the map is null.
     * @throws IllegalArgumentException if the sum of the weights is not positive.
     */
    public <T> T getWeighted (Map<T, Integer> valuesToWeights)
    {
        // TODO: validation?
        int idx = _r.nextInt(Folds.sum(0, valuesToWeights.values()));
        for (Map.Entry<T, Integer> entry : valuesToWeights.entrySet()) {
            idx -= entry.getValue();
            if (idx < 0) {
                return entry.getKey();
            }
        }
        throw new AssertionError("Not possible");
    }

    /**
     * Pick a random element from the specified Iterator, or return <code>ifEmpty</code>
     * if it is empty.
     *
     * <p><b>Implementation note:</b> because the total size of the Iterator is not known,
     * the random number generator is queried after the second element and every element
     * thereafter.
     *
     * @throws NullPointerException if the iterator is null.
     */
    public <T> T pick (Iterator<? extends T> iterator, T ifEmpty)
    {
        if (!iterator.hasNext()) {
            return ifEmpty;
        }
        T pick = iterator.next();
        for (int count = 2; iterator.hasNext(); count++) {
            T next = iterator.next();
            if (0 == _r.nextInt(count)) {
                pick = next;
            }
        }
        return pick;
    }

    /**
     * Pick a random element from the specified Iterable, or return <code>ifEmpty</code>
     * if it is empty.
     *
     * <p><b>Implementation note:</b> optimized implementations are used if the Iterable
     * is a List or Collection. Otherwise, it behaves as if calling {@link #pick(Iterator)} with
     * the Iterable's Iterator.
     *
     * @throws NullPointerException if the iterable is null.
     */
    public <T> T pick (Iterable<? extends T> iterable, T ifEmpty)
    {
        return pickPluck(iterable, ifEmpty, false);
    }

    /**
     * Pluck (remove) a random element from the specified Iterable, or return <code>ifEmpty</code>
     * if it is empty.
     *
     * <p><b>Implementation note:</b> optimized implementations are used if the Iterable
     * is a List or Collection. Otherwise, two Iterators are created from the Iterable
     * and a random number is generated after the second element and all beyond.
     *
     * @throws NullPointerException if the iterable is null.
     * @throws UnsupportedOperationException if the iterable is unmodifiable or its Iterator
     * does not support {@link Iterator#remove()}.
     */
    public <T> T pluck (Iterable<? extends T> iterable, T ifEmpty)
    {
        return pickPluck(iterable, ifEmpty, true);
    }

    /**
     * Construct a Randoms.
     */
    protected Randoms (Random rand)
    {
        _r = rand;
    }

    /**
     * Shared code for pick and pluck.
     */
    @SuppressWarnings("unchecked")
    protected <T> T pickPluck (Iterable<? extends T> iterable, T ifEmpty, boolean remove)
    { 
        if (iterable instanceof Collection) {
            // optimized path for Collection
            Collection<? extends T> coll = (Collection<? extends T>)iterable;
            int size = coll.size();
            if (size == 0) {
                return ifEmpty;
            }
            if (coll instanceof List) {
                // extra-special optimized path for Lists
                List<? extends T> list = (List<? extends T>)coll;
                int idx = _r.nextInt(size);
                return (T)(remove ? list.remove(idx) : list.get(idx));
            }
            // for other Collections, we must iterate
            Iterator<? extends T> it = coll.iterator();
            for (int idx = _r.nextInt(size); idx > 0; idx--) {
                it.next();
            }
            try {
                return it.next();
            } finally {
                if (remove) {
                    it.remove();
                }
            }
        }

        if (!remove) {
            return pick(iterable.iterator(), ifEmpty);
        }

        // from here on out, we're doing a pluck with a complicated two-iterator solution
        Iterator<? extends T> it = iterable.iterator();
        if (!it.hasNext()) {
            return ifEmpty;
        }
        Iterator<? extends T> lagIt = iterable.iterator();
        T pick = it.next();
        lagIt.next();
        for (int count = 2, lag = 1; it.hasNext(); count++, lag++) {
            T next = it.next();
            if (0 == _r.nextInt(count)) {
                pick = next;
                for ( ; lag > 0; lag--) {
                    lagIt.next();
                }
            }
        }
        lagIt.remove();
        return pick;
    }

    /** The random number generator. */
    protected final Random _r;
}
