//
// $Id: Queue.java,v 1.1 2001/03/02 00:47:10 mdb Exp $

package com.samskivert.util;

/**
 * A queue implementation that is more efficient than a wrapper around
 * java.util.Vector. Allows adding and removing elements to/from the
 * beginning, without the unneccessary System.arraycopy overhead of
 * java.util.Vector.
 */
public class Queue
{
    public Queue (int suggestedSize)
    {
        _size = _suggestedSize = suggestedSize;
        _items = new Object[_size];
    }

    public Queue ()
    {
        this(4);
    }

    public synchronized void clear ()
    {
        _count = _start = _end = 0;
        _size = _suggestedSize;
        _items = new Object[_size];
    }

    public synchronized boolean hasElements ()
    {
        return (_count != 0);
    }

    public synchronized int size ()
    {
        return _count;
    }

    public void appendItem (Object item)
    {
        addItem(item);
    }

    public synchronized void prependItem (Object item)
    {
        if (_count == _size) makeMoreRoom();

        if (_start == 0) {
            _start = _size - 1;
        } else {
            _start--;
        }

        _items[_start] = item;
        _count++;

        if (_count == 1) notify();
    }

    public synchronized void addItem (Object item)
    {
        if (_count == _size) makeMoreRoom();

        _items[_end] = item;
        _end = (_end + 1) % _size;
        _count++;

        if (_count == 1) notify();
    }

    /**
     * Adds an item to the queue without notifying anyone. Useful for
     * adding a bunch of items and then waking up the listener.
     */
    public synchronized void addItemSilent (Object item)
    {
        if (_count == _size) makeMoreRoom();
        _items[_end] = item;
        _end = (_end + 1) % _size;
        _count++;
    }

    /**
     * Adds an item to the queue and notify any listeners regardless of
     * how many items are on the queue. Use this for the last item you add
     * to a queue in a batch via addItemSilent() because the regular
     * addItem() will think it doesn't need to notify anyone because the
     * queue size isn't zero prior to this add.
     */
    public synchronized void addItemLoud (Object item)
    {
        if (_count == _size) makeMoreRoom();
        _items[_end] = item;
        _end = (_end + 1) % _size;
        _count++;
        notify();
    }

    public synchronized Object getItemNonBlocking ()
    {
        if (_count == 0) return null;

        // pull the object off, and clear our reference to it
        Object retval = _items[_start];
        _items[_start] = null;

        _start = (_start + 1) % _size;
        _count--;

        return retval;
    }

    public synchronized void waitForItem ()
    {
        while (_count == 0) {
            try { wait(); } catch (InterruptedException e) {}
        }
    }

    public synchronized Object getItem (long maxwait)
    {
	if (_count == 0) {
	    try { wait(maxwait); } catch (InterruptedException e) {}

	    // if count's still null when we pull out, we waited
	    // ourmaxwait time.
	    if (_count == 0) {
		return null;
	    }
	}

	return getItem();
    }

    public synchronized Object getItem ()
    {
        while (_count == 0) {
            try { wait(); } catch (InterruptedException e) {}
        }

        // pull the object off, and clear our reference to it
        Object retval = _items[_start];
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
        Object[] items = new Object[_size * 2];
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
        Object[] items = new Object[_size / 2];

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

    public String toString ()
    {
        StringBuffer buf = new StringBuffer();

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
//             queue.addItem(new Integer(value++));
//         }
//         System.out.println("Add three: " + queue);

//         // now add three more items and cause it to expand and see how it
//         // does
//         for (int i = 0; i < 3; i++) {
//             queue.addItem(new Integer(value++));
//         }
//         System.out.println("Add three more: " + queue);

//         // now remove three items and move the queue pointer up a bit
//         for (int i = 0; i < 3; i++) {
//             queue.getItem();
//         }
//         System.out.println("Remove three: " + queue);

//         // now add three again and cause the queue to wrap around
//         for (int i = 0; i < 3; i++) {
//             queue.addItem(new Integer(value++));
//         }
//         System.out.println("Add three more: " + queue);

//         // now add three more and cause it to expand while wrapped
//         for (int i = 0; i < 3; i++) {
//             queue.addItem(new Integer(value++));
//         }
//         System.out.println("Add three more: " + queue);

//         // now remove three elements and add 8 to cause it to wrap around
//         // again
//         for (int i = 0; i < 3; i++) {
//             queue.getItem();
//         }
//         for (int i = 0; i < 8; i++) {
//             queue.addItem(new Integer(value++));
//         }
//         System.out.println("-3 and +8: " + queue);

//         // now add 2030 elements, cause it to expand a great deal
//         for (int i = 0; i < 2030; i++) {
//             queue.addItem(new Integer(value++));
//         }
//         // now remove some from the front so that we may wrap
//         for (int i = 0; i < 8; i++) {
//             queue.getItem();
//         }
//         // now add a few more to the end to cause us to wrap
//         for (int i = 0; i < 8; i++) {
//             queue.addItem(new Integer(value++));
//         }
//         System.out.println("+2030 -8 and +8: " + queue);

//         // finally, remove 2030 and see where we end up after the shrink
//         for (int i = 0; i < 2030; i++) {
//             queue.getItem();
//         }
//         System.out.println("Remove 2030: " + queue);

//         // now add and remove two elements to make sure the append and
//         // remove pointers are in the right place
//         for (int i = 0; i < 2; i++) {
//             queue.addItem(new Integer(value++));
//             queue.getItem();
//         }
//         System.out.println("Add two and remove two: " + queue);
//     }

    protected final static int MIN_SHRINK_SIZE = 1024;

    protected Object[] _items;
    protected int _count = 0;
    protected int _start = 0, _end = 0;
    protected int _suggestedSize, _size = 0;
}
