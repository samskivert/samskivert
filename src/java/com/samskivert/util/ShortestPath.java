//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2007 Michael Bayne
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.samskivert.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * Implements Dijkstra's algorithm for finding the shortest path between two nodes in a weighted
 * graph. The code assumes that the caller represents their nodes and edges as objects which can
 * be compared and hashed (via {@link Object#equals} and {@link Object#hashCode}) other necessary
 * information is obtained through a special interface {@link Graph} implemented by the caller to
 * enumerate edges and compute weights.
 */
public class ShortestPath
{
    /** A caller must implement this interface to provide the information needed to define the
     * graph and compute the shortest path.
     */
    public interface Graph<T, V>
    {
        /** Enumerates all nodes in the graph. */
        public Iterator<T> enumerateNodes ();

        /** Returns the list of the edges for the specified node. */
        public List<V> getEdges (T node);

        /** Returns the weight associated with the supplied edge in the direction established by
         * the supplied starting node. */
        public int computeWeight (V edge, T start);

        /** Returns the node opposite the supplied node on the supplied edge. */
        public T getOpposite (V edge, T node);
    }

    /**
     * Computes the shortest path between the specified starting and ending nodes using Dijkstra's
     * algorithm. This implementation assumes that the graph is properly formed and may behave
     * strangely or throw an exception if provided with an invalid graph.
     *
     * @return a list of the edges that must be followed to traverse from the starting node to the
     * ending node. This list may be empty if the graph is improperly formed.
     */
    public static <T, V> List<V> compute (Graph<T, V> graph, T start, T end)
    {
        HashMap<T, NodeInfo<T, V>> nodes = new HashMap<T, NodeInfo<T, V>>();
        HashSet<T> relaxed = new HashSet<T>();
        ComparableArrayList<NodeInfo<T, V>> uptight = new ComparableArrayList<NodeInfo<T, V>>();

        // initialize our searching info
        for (Iterator<T> iter = graph.enumerateNodes(); iter.hasNext(); ) {
            NodeInfo<T, V> info = new NodeInfo<T, V>();
            info.node = iter.next();
            if (info.node == start) {
                info.weightTo = 0;
            }
            uptight.add(info);
            nodes.put(info.node, info);
        }
        uptight.sort();

        // now execute the main part of the search
        while (uptight.size() > 0) {
            // remove the cheapest known node
            NodeInfo<T, V> info = uptight.remove(uptight.size()-1);
            // make a note that it is now relaxed
            relaxed.add(info.node);
            // relax its uptight neighbors
            List<V> edges = graph.getEdges(info.node);
            for (int ii = 0, ll = edges.size(); ii < ll; ii++) {
                V edge = edges.get(ii);
                T onode = graph.getOpposite(edge, info.node);
                if (relaxed.contains(onode)) {
                    continue;
                }
                // if the path through this node to its neighbor is cheaper than the existing known
                // shortest path, update the neighbor to reflect this new shorter path
                NodeInfo<T, V> oinfo = nodes.get(onode);
                int weight = graph.computeWeight(edge, info.node);
                if (oinfo.weightTo > info.weightTo + weight) {
                    oinfo.weightTo = info.weightTo + weight;
                    oinfo.edgeTo = edge;
                }
            }
            // now resort the uptight list
            uptight.sort();
        }

        // now trace the path from the final node back to the start
        ArrayList<V> path = new ArrayList<V>();
        NodeInfo<T, V> info = nodes.get(end);
        while (info.edgeTo != null) {
            path.add(0, info.edgeTo);
            info = nodes.get(graph.getOpposite(info.edgeTo, info.node));
        }
        return path;
    }

    /** Used to maintain information during the shortest path search. */
    protected static final class NodeInfo<T, V> implements Comparable<NodeInfo<T, V>>
    {
        /** The node for which we're representing information. */
        public T node;

        /** The cumulative weight to this node from the source. */
        public int weightTo = Integer.MAX_VALUE / 4;

        /** The edge followed to reach this node along the shortest path. */
        public V edgeTo;

        /** We order ourselves by the cumulative weight to this node. */
        public int compareTo (NodeInfo<T, V> o) {
            return o.weightTo - weightTo;
        }
    }
}
