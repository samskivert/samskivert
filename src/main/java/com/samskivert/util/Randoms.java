//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Provides utility routines to simplify obtaining randomized values.
 *
 * <p>Each instance of Randoms contains an underlying {@link java.util.Random} instance and is
 * only as thread-safe as that is. If you wish to have a private stream of pseudorandom numbers,
 * use the {@link #with} factory.
 */
public class Randoms
{
    /** A default Randoms that is thread-safe and can be safely shared by any caller. */
    public static final Randoms RAND = with(new Random());

    /**
     * A factory to create a new Randoms object.
     */
    public static Randoms with (Random rand)
    {
        return new Randoms(rand);
    }

    /**
     * Get a thread-local Randoms instance that will not contend with any other thread
     * for random number generation.
     *
     * <p><b>Note:</b> This method will return a Randoms instance that is not thread-safe.
     * It can generate random values with less overhead, however it may be dangerous to share
     * the reference. Instead you should probably always use it immediately as in the following
     * example:
     * <pre style="code">
     *     Puppy pick = Randoms.threadLocal().pick(Puppy.LITTER, null);
     * </pre>
     */
    public static Randoms threadLocal ()
    {
        return _localRandoms.get();
    }

    /**
     * An interface for weighing pickable elements.
     */
    public interface Weigher<T>
    {
        /**
         * Get the weight of the specified value.
         */
        public double getWeight (T value);
    }

    /**
     * Returns a pseudorandom, uniformly distributed <code>int</code> value between <code>0</code>
     * (inclusive) and <code>high</code> (exclusive).
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
     * Returns a pseudorandom, uniformly distributed <code>float</code> value between
     * <code>0.0</code> (inclusive) and the <code>high</code> (exclusive).
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
     * Returns a pseudorandom, uniformly distributed <code>double</code> value between
     * <code>0.0</code> (inclusive) and the <code>high</code> (exclusive).
     *
     * @param high the high value limiting the random number sought.
     */
    public double getDouble (double high)
    {
        return _r.nextDouble() * high;
    }

    /**
     * Returns a pseudorandom, uniformly distributed <code>double</code> value between
     * <code>low</code> (inclusive) and <code>high</code> (exclusive).
     */
    public double getInRange (double low, double high)
    {
        return low + (_r.nextDouble() * (high - low));
    }

    /**
     * Returns true approximately one in <code>n</code> times.
     *
     * @throws IllegalArgumentException if <code>n</code> is not positive.
     */
    public boolean getChance (int n)
    {
        return (0 == _r.nextInt(n));
    }

    /**
     * Has a probability <code>p</code> of returning true.
     */
    public boolean getProbability (double p)
    {
        return _r.nextDouble() < p;
    }

    /**
     * Returns <code>true</code> or <code>false</code> with approximately even probability.
     */
    public boolean getBoolean ()
    {
        return _r.nextBoolean();
    }

    /**
     * Returns a pseudorandom, normally distributed <code>float</code> value around the
     * <code>mean</code> with the standard deviation <code>dev</code>.
     */
    public float getNormal (float mean, float dev)
    {
        return (float)_r.nextGaussian() * dev + mean;
    }

    /**
     * Shuffle the specified list using our Random.
     */
    public void shuffle (List<?> list)
    {
        Collections.shuffle(list, _r);
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
     * is a List or Collection. Otherwise, it behaves as if calling {@link #pick(Iterator, Object)}
     * with the Iterable's Iterator.
     *
     * @throws NullPointerException if the iterable is null.
     */
    public <T> T pick (Iterable<? extends T> iterable, T ifEmpty)
    {
        return pickPluck(iterable, ifEmpty, false);
    }

    /**
     * Pick a random <em>key</em> from the specified mapping of weight values, or return
     * <code>ifEmpty</code> if no mapping has a weight greater than <code>0</code>. Each
     * weight value is evaluated as a double.
     *
     * <p><b>Implementation note:</b> a random number is generated for every entry with a
     * non-zero weight after the first such entry.
     *
     * @throws NullPointerException if the map is null.
     * @throws IllegalArgumentException if any weight is less than 0.
     */
    public <T> T pick (Map<? extends T, ? extends Number> weightMap, T ifEmpty)
    {
        Map.Entry<? extends T, ? extends Number> pick = pick(
                weightMap.entrySet(), null, new Weigher<Map.Entry<?, ? extends Number>>() {
                    public double getWeight (Map.Entry<?, ? extends Number> entry) {
                        return entry.getValue().doubleValue();
                    }
                });
        return (pick == null) ? ifEmpty : pick.getKey();
    }

    /**
     * Pick a random element from the specified iterable, or return
     * <code>ifEmpty</code> if no element has a weight greater than <code>0</code>.
     *
     * <p><b>Implementation note:</b> a random number is generated for every entry with a
     * non-zero weight after the first such entry.
     *
     * @throws NullPointerException if values is null.
     * @throws IllegalArgumentException if any weight is less than 0.
     */
    public <T> T pick (Iterable<? extends T> values, T ifEmpty, Weigher<? super T> weigher)
    {
        return pick(values.iterator(), ifEmpty, weigher);
    }

    /**
     * Pick a random element from the specified iterator, or return
     * <code>ifEmpty</code> if no element has a weight greater than <code>0</code>.
     *
     * <p><b>Implementation note:</b> a random number is generated for every entry with a
     * non-zero weight after the first such entry.
     *
     * @throws NullPointerException if values is null.
     * @throws IllegalArgumentException if any weight is less than 0.
     */
    public <T> T pick (Iterator<? extends T> values, T ifEmpty, Weigher<? super T> weigher)
    {
        T pick = ifEmpty;
        double total = 0.0;
        while (values.hasNext()) {
            T val = values.next();
            double weight = weigher.getWeight(val);
            if (weight > 0.0) {
                total += weight;
                if ((total == weight) || ((_r.nextDouble() * total) < weight)) {
                    pick = val;
                }
            } else if (weight < 0.0) {
                throw new IllegalArgumentException("Weight less than 0: " + val);
            }
            // else: weight == 0.0 is OK
        }
        return pick;
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
    protected <T> T pickPluck (Iterable<? extends T> iterable, T ifEmpty, boolean remove)
    {
        if (iterable instanceof Collection) {
            // optimized path for Collection
            @SuppressWarnings("unchecked")
            Collection<? extends T> coll = (Collection<? extends T>)iterable;
            int size = coll.size();
            if (size == 0) {
                return ifEmpty;
            }
            if (coll instanceof List) {
                // extra-special optimized path for Lists
                @SuppressWarnings("unchecked")
                List<? extends T> list = (List<? extends T>)coll;
                int idx = _r.nextInt(size);
                if (remove) { // ternary conditional causes warning here with javac 1.6, :(
                    return list.remove(idx);
                }
                return list.get(idx);
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
                // catch up lagIt so that it has just returned 'pick' as well
                for ( ; lag > 0; lag--) {
                    lagIt.next();
                }
            }
        }
        lagIt.remove(); // remove 'pick' from the lagging iterator
        return pick;
    }

    /** The random number generator. */
    protected final Random _r;

    /** A ThreadLocal for accessing a thread-local version of Randoms. */
    protected static final ThreadLocal<Randoms> _localRandoms = new ThreadLocal<Randoms>() {
        @Override
        public Randoms initialValue () {
            return with(new ThreadLocalRandom());
        }
    };

    /*
     * TODO: This can be updated and this inner class removed
     * when the samskivert library is JDK 1.7 compatible.
     *-----------------------------------------------------------------
     *
     * Written by Doug Lea with assistance from members of JCP JSR-166
     * Expert Group and released to the public domain, as explained at
     * http://creativecommons.org/licenses/publicdomain
     *
     * A random number generator isolated to the current thread.  Like the
     * global {@link java.util.Random} generator used by the {@link
     * java.lang.Math} class, a {@code ThreadLocalRandom} is initialized
     * with an internally generated seed that may not otherwise be
     * modified. When applicable, use of {@code ThreadLocalRandom} rather
     * than shared {@code Random} objects in concurrent programs will
     * typically encounter much less overhead and contention.  Use of
     * {@code ThreadLocalRandom} is particularly appropriate when multiple
     * tasks (for example, each a {@link ForkJoinTask}) use random numbers
     * in parallel in thread pools.
     *
     * <p>This class also provides additional commonly used bounded random
     * generation methods.
     *
     * @since 1.7
     * @author Doug Lea
     */
    protected static class ThreadLocalRandom extends Random {
        // same constants as Random, but must be redeclared because private
        private final static long multiplier = 0x5DEECE66DL;
        private final static long addend = 0xBL;
        private final static long mask = (1L << 48) - 1;

        /**
         * The random seed. We can't use super.seed.
         */
        private long rnd;

        /**
         * Initialization flag to permit calls to setSeed to succeed only
         * while executing the Random constructor.  We can't allow others
         * since it would cause setting seed in one part of a program to
         * unintentionally impact other usages by the thread.
         */
        boolean initialized;

        // Padding to help avoid memory contention among seed updates in
        // different TLRs in the common case that they are located near
        // each other.
        @SuppressWarnings("unused")
        private long pad0, pad1, pad2, pad3, pad4, pad5, pad6, pad7;

        /**
         * Constructor called only by localRandom.initialValue.
         */
        ThreadLocalRandom() {
            super();
            initialized = true;
        }

        /**
         * Throws {@code UnsupportedOperationException}.  Setting seeds in
         * this generator is not supported.
         *
         * @throws UnsupportedOperationException always
         */
        @Override
        public void setSeed(long seed) {
            if (initialized)
                throw new UnsupportedOperationException();
            rnd = (seed ^ multiplier) & mask;
        }

        @Override
        protected int next(int bits) {
            rnd = (rnd * multiplier + addend) & mask;
            return (int) (rnd >>> (48-bits));
        }

// as of JDK 1.6, this method does not exist in java.util.Random
//        /**
//         * Returns a pseudorandom, uniformly distributed value between the
//         * given least value (inclusive) and bound (exclusive).
//         *
//         * @param least the least value returned
//         * @param bound the upper bound (exclusive)
//         * @throws IllegalArgumentException if least greater than or equal
//         * to bound
//         * @return the next value
//         */
//        public int nextInt(int least, int bound) {
//            if (least >= bound)
//                throw new IllegalArgumentException();
//            return nextInt(bound - least) + least;
//        }

        /**
         * Returns a pseudorandom, uniformly distributed value
         * between 0 (inclusive) and the specified value (exclusive).
         *
         * @param n the bound on the random number to be returned.  Must be
         *        positive.
         * @return the next value
         * @throws IllegalArgumentException if n is not positive
         */
        public long nextLong(long n) {
            if (n <= 0)
                throw new IllegalArgumentException("n must be positive");
            // Divide n by two until small enough for nextInt. On each
            // iteration (at most 31 of them but usually much less),
            // randomly choose both whether to include high bit in result
            // (offset) and whether to continue with the lower vs upper
            // half (which makes a difference only if odd).
            long offset = 0;
            while (n >= Integer.MAX_VALUE) {
                int bits = next(2);
                long half = n >>> 1;
                long nextn = ((bits & 2) == 0) ? half : n - half;
                if ((bits & 1) == 0)
                    offset += n - nextn;
                n = nextn;
            }
            return offset + nextInt((int) n);
        }

        /**
         * Returns a pseudorandom, uniformly distributed value between the
         * given least value (inclusive) and bound (exclusive).
         *
         * @param least the least value returned
         * @param bound the upper bound (exclusive)
         * @return the next value
         * @throws IllegalArgumentException if least greater than or equal
         * to bound
         */
        public long nextLong(long least, long bound) {
            if (least >= bound)
                throw new IllegalArgumentException();
            return nextLong(bound - least) + least;
        }

        /**
         * Returns a pseudorandom, uniformly distributed {@code double} value
         * between 0 (inclusive) and the specified value (exclusive).
         *
         * @param n the bound on the random number to be returned.  Must be
         *        positive.
         * @return the next value
         * @throws IllegalArgumentException if n is not positive
         */
        public double nextDouble(double n) {
            if (n <= 0)
                throw new IllegalArgumentException("n must be positive");
            return nextDouble() * n;
        }

        /**
         * Returns a pseudorandom, uniformly distributed value between the
         * given least value (inclusive) and bound (exclusive).
         *
         * @param least the least value returned
         * @param bound the upper bound (exclusive)
         * @return the next value
         * @throws IllegalArgumentException if least greater than or equal
         * to bound
         */
        public double nextDouble(double least, double bound) {
            if (least >= bound)
                throw new IllegalArgumentException();
            return nextDouble() * (bound - least) + least;
        }

        private static final long serialVersionUID = -5851777807851030925L;
    }
}
