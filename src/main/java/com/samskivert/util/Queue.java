//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.util;

/**
 * A queue implementation that is more efficient than a wrapper around
 * java.util.Vector. Allows adding and removing elements to/from the
 * beginning, without the unneccessary System.arraycopy overhead of
 * java.util.Vector.
 */
public class Queue<T>
{
    public static <T> Queue<T> newQueue ()
    {
        return new Queue<T>();
    }

    public Queue (int suggestedSize)
    {
        _size = _suggestedSize = suggestedSize;
        _items = newArray(_size);
    }

    public Queue ()
    {
        this(4);
    }

    public synchronized void clear ()
    {
        _count = _start = _end = 0;
        _size = _suggestedSize;
        _items = newArray(_size);
    }

    public synchronized boolean hasElements ()
    {
        return (_count != 0);
    }

    public synchronized int size ()
    {
        return _count;
    }

    public synchronized void prepend (T item)
    {
        if (_count == _size) {
            makeMoreRoom();
        }

        if (_start == 0) {
            _start = _size - 1;
        } else {
            _start--;
        }

        _items[_start] = item;
        _count++;

        if (_count == 1) {
            notify();
        }
    }

    /**
     * Appends the supplied item to the end of the queue, and notify a
     * consumer if and only if the queue was previously empty.
     */
    public synchronized void append (T item)
    {
        // only notify if the queue was previously empty
        append0(item, _count == 0);
    }

    /**
     * Appends an item to the queue without notifying anyone. Useful for
     * appending a bunch of items and then waking up the listener.
     */
    public synchronized void appendSilent (T item)
    {
        append0(item, false);
    }

    /**
     * Appends an item to the queue and notify a listener regardless of
     * how many items are on the queue. Use this for the last item you
     * append to a queue in a batch via <code>appendSilent</code> because
     * the regular <code>append</code> will think it doesn't need to
     * notify anyone because the queue size isn't zero prior to this add.
     * You should also use this method if you have mutiple consumers
     * listening waiting on the queue, to guarantee that one will be woken
     * for every element added.
     */
    public synchronized void appendLoud (T item)
    {
        append0(item, true);
    }

    /**
     * Internal append method. If subclassing queue, be sure to call
     * this method from inside a synchronized block.
     */
    protected void append0 (T item, boolean notify)
    {
        if (_count == _size) {
            makeMoreRoom();
        }
        _items[_end] = item;
        _end = (_end + 1) % _size;
        _count++;

        if (notify) {
            notify();
        }
    }

    /**
     * Returns the next item on the queue or null if the queue is
     * empty. This method will not block waiting for an item to be added
     * to the queue.
     */
    public synchronized T getNonBlocking ()
    {
        if (_count == 0) {
            return null;
        }

        // pull the object off, and clear our reference to it
        T retval = _items[_start];
        _items[_start] = null;

        _start = (_start + 1) % _size;
        _count--;

        return retval;
    }

    /**
     * Blocks the current thread waiting for an item to be added to the
     * queue. If the queue is currently non-empty, this function will
     * return immediately.
     */
    public synchronized void waitForItem ()
    {
        while (_count == 0) {
            try { wait(); } catch (InterruptedException e) {}
        }
    }

    /**
     * Gets the next item from the queue blocking for no longer than
     * <code>maxwait</code> milliseconds waiting for an item to be added
     * to the queue if it is empty at the time of invocation.
     */
    public synchronized T get (long maxwait)
    {
        if (_count == 0) {
            try { wait(maxwait); } catch (InterruptedException e) {}

            // if count's still null when we pull out, we waited ourmaxwait time.
            if (_count == 0) {
                return null;
            }
        }

        return get();
    }

    /**
     * Gets the next item from the queue, blocking until an item is added
     * to the queue if the queue is empty at time of invocation.
     */
    public synchronized T get ()
    {
        while (_count == 0) {
            try { wait(); } catch (InterruptedException e) {}
        }

        // pull the object off, and clear our reference to it
        T retval = _items[_start];
        _items[_start] = null;

        _start = (_start + 1) % _size;
        _count--;

        // if we are only filling 1/8th of the space, shrink by half
        if ((_size > MIN_SHRINK_SIZE) && (_size > _suggestedSize) &&
            (_count < (_size >> 3))) shrink();

        return retval;
    }

    private void makeMoreRoom ()
    {
        T[] items = newArray(_size * 2);
        System.arraycopy(_items, _start, items, 0, _size - _start);
        System.arraycopy(_items, 0, items, _size - _start, _end);
        _start = 0;
        _end = _size;
        _size *= 2;
        _items = items;
    }

    // shrink by half
    private void shrink ()
    {
        T[] items = newArray(_size / 2);

        if (_start > _end) {
            // the data wraps around
            System.arraycopy(_items, _start, items, 0, _size - _start);
            System.arraycopy(_items, 0, items, _size - _start, _end + 1);

        } else {
            // the data does not wrap around
            System.arraycopy(_items, _start, items, 0, _end - _start+1);
        }

        _size = _size / 2;
        _start = 0;
        _end = _count;
        _items = items;
    }

    @SuppressWarnings("unchecked")
    private T[] newArray (int size)
    {
        return (T[])new Object[size];
    }

    @Override
    public String toString ()
    {
        StringBuilder buf = new StringBuilder();

        buf.append("[count=").append(_count);
        buf.append(", size=").append(_size);
        buf.append(", start=").append(_start);
        buf.append(", end=").append(_end);
        buf.append(", elements={");

        for (int i = 0; i < _count; i++) {
            int pos = (i + _start) % _size;
            if (i > 0) buf.append(", ");
            buf.append(_items[pos]);
        }

        return buf.append("}]").toString();
    }

//     public static void main (String[] args)
//     {
//         Queue queue = new Queue();
//         int value = 0;

//         // add three items and dump the queue
//         for (int i = 0; i < 3; i++) {
//             queue.append(new Integer(value++));
//         }
//         System.out.println("Add three: " + queue);

//         // now add three more items and cause it to expand and see how it
//         // does
//         for (int i = 0; i < 3; i++) {
//             queue.append(new Integer(value++));
//         }
//         System.out.println("Add three more: " + queue);

//         // now remove three items and move the queue pointer up a bit
//         for (int i = 0; i < 3; i++) {
//             queue.get();
//         }
//         System.out.println("Remove three: " + queue);

//         // now add three again and cause the queue to wrap around
//         for (int i = 0; i < 3; i++) {
//             queue.append(new Integer(value++));
//         }
//         System.out.println("Add three more: " + queue);

//         // now add three more and cause it to expand while wrapped
//         for (int i = 0; i < 3; i++) {
//             queue.append(new Integer(value++));
//         }
//         System.out.println("Add three more: " + queue);

//         // now remove three elements and add 8 to cause it to wrap around
//         // again
//         for (int i = 0; i < 3; i++) {
//             queue.get();
//         }
//         for (int i = 0; i < 8; i++) {
//             queue.append(new Integer(value++));
//         }
//         System.out.println("-3 and +8: " + queue);

//         // now add 2030 elements, cause it to expand a great deal
//         for (int i = 0; i < 2030; i++) {
//             queue.append(new Integer(value++));
//         }
//         // now remove some from the front so that we may wrap
//         for (int i = 0; i < 8; i++) {
//             queue.get();
//         }
//         // now add a few more to the end to cause us to wrap
//         for (int i = 0; i < 8; i++) {
//             queue.append(new Integer(value++));
//         }
//         System.out.println("+2030 -8 and +8: " + queue);

//         // finally, remove 2030 and see where we end up after the shrink
//         for (int i = 0; i < 2030; i++) {
//             queue.get();
//         }
//         System.out.println("Remove 2030: " + queue);

//         // now add and remove two elements to make sure the append and
//         // remove pointers are in the right place
//         for (int i = 0; i < 2; i++) {
//             queue.append(new Integer(value++));
//             queue.get();
//         }
//         System.out.println("Add two and remove two: " + queue);
//     }

    protected final static int MIN_SHRINK_SIZE = 1024;

    protected T[] _items;
    protected int _count = 0;
    protected int _start = 0, _end = 0;
    protected int _suggestedSize, _size = 0;
}
