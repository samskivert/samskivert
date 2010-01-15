//
// $Id$

package com.samskivert.util;

import java.util.NoSuchElementException;

/**
 * Is able to store every single int, using sub ArrayIntSets.
 */
public class CompleteIntSet extends AbstractIntSet
{
    public CompleteIntSet ()
    {
        for (int ii = 0; ii < 4; ii++) {
            // start each with 0-length arrays, they'll grow when used
            _subsets[ii] = new ArrayIntSet(0);
        }
    }

    @Override
    public boolean contains (int value)
    {
        return getSet(value).contains(value);
    }

    @Override
    public boolean add (int value)
    {
        return getSet(value).add(value);
    }

    @Override
    public boolean remove (int value)
    {
        return getSet(value).remove(value);
    }

    @Override
    public int size ()
    {
        long size = 0;
        for (IntSet set : _subsets) {
            size += set.size();
        }
        return (size > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int)size;
    }

    public Interator interator ()
    {
        return new AbstractInterator() {
            public boolean hasNext ()
            {
                while (true) {
                    if (_current == null) {
                        if (_index < 4) {
                            _current = _subsets[_index++].interator();
                        } else {
                            return false;
                        }
                    }
                    if (_current.hasNext()) {
                        return true;
                    }
                    _current = null;
                }
            }

            public int nextInt ()
            {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                _removeFrom = _current;
                return _current.nextInt();
            }

            @Override public void remove ()
            {
                if (_removeFrom == null) {
                    throw new IllegalStateException();
                }
                _removeFrom.remove();
                _removeFrom = null;
            }

            protected Interator _current;
            protected Interator _removeFrom;
            protected int _index = 0;
        };
    }

    protected IntSet getSet (int value)
    {
        return _subsets[valueToIndex(value)];
    }

    protected int valueToIndex (int value)
    {
        // the indexes don't matter much, as long as they're distinct..
        return (value >>> 30);
//        if (value < 0) {
//            return (value < Integer.MIN_VALUE/2) ? 0 : 1;
//        } else {
//            return (value < Integer.MAX_VALUE/2) ? 2 : 3;
//        }
    }

    /** The subsets. 3 would work, but 4 is a nicer split... */
    protected IntSet[] _subsets = new IntSet[4];
}
