/*
 * Copyright (c) 2005-2010 KOM - Multimedia Communications Lab
 *
 * This file is part of Simonstrator.KOM.
 * 
 * Simonstrator.KOM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 * 
 * PeerfactSim.KOM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with PeerfactSim.KOM.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package de.tudarmstadt.maki.simonstrator.tc.graph.algorithm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INode;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Node;

/******************************************************************************
 *  Compilation:  javac TarjanSCC.java
 *  Execution:    Java TarjanSCC V E
 *  Dependencies: Graph.java Stack.java TransitiveClosure.java StdOut.java
 *
 *  Compute the strongly-connected components of a Graph using 
 *  Tarjan's algorithm.
 *
 *  Runs in O(E + V) time.
 *
 *  % java TarjanSCC tinyDG.txt
 *  5 components
 *  1 
 *  0 2 3 4 5
 *  9 10 11 12
 *  6 8
 *  7 
 *
 ******************************************************************************/

/**
 * The <tt>TarjanSCC</tt> class represents a data type for determining the
 * strong components in a Graph. The <em>id</em> operation determines in which
 * strong component a given vertex lies; the <em>areStronglyConnected</em>
 * operation determines whether two vertices are in the same strong component;
 * and the <em>count</em> operation determines the number of strong components.
 * 
 * The <em>component identifier</em> of a component is one of the vertices in
 * the strong component: two vertices have the same component identifier if and
 * only if they are in the same strong component.
 * 
 * <p>
 * This implementation uses Tarjan's algorithm. The constructor takes time
 * proportional to <em>V</em> + <em>E</em> (in the worst case), where <em>V</em>
 * is the number of vertices and <em>E</em> is the number of edges. Afterwards,
 * the <em>id</em>, <em>count</em>, and <em>areStronglyConnected</em> operations
 * take constant time. For alternate implementations of the same API, see
 * {@link KosarajuSharirSCC} and {@link GabowSCC}.
 * <p>
 * For additional documentation, see <a href="/algs4/42Graph">Section 4.2</a> of
 * <i>Algorithms, 4th Edition</i> by Robert Sedgewick and Kevin Wayne.
 *
 * @author Robert Sedgewick
 * @author Kevin Wayne
 * 
 * @see http://algs4.cs.princeton.edu/42directed/TarjanSCC.java.html
 */
public class TarjanSCC {

	private boolean[] marked; // marked[v] = has v been visited?
	private int[] sccId; // id[v] = id of strong component containing v
	private int[] low; // low[v] = low number of v
	private int pre; // preorder number counter
	private int count; // number of strongly-connected components
	private Stack<Integer> stack;
	private Map<INodeID, Integer> nodeIdToInternalId = new HashMap<>();

	/**
	 * Computes the strong components of the Graph <tt>G</tt>.
	 * 
	 * @param graph
	 *            the Graph
	 */
	public TarjanSCC(Graph graph) {
		int nodeCount = graph.getNodeCount();
		marked = new boolean[nodeCount];
		stack = new Stack<Integer>();
		sccId = new int[nodeCount];
		low = new int[nodeCount];
		for (final INode node : graph.getNodes()) {
			nodeIdToInternalId.put(node.getId(), nodeIdToInternalId.size());
		}
		for (final INode node : graph.getNodes()) {
			int v = this.nodeIdToInternalId.get(node.getId());
			if (!marked[v])
				dfs(graph, node.getId());
		}
	}

	private void dfs(Graph graph, INodeID node) {
		int v = this.nodeIdToInternalId.get(node);
		marked[v] = true;
		low[v] = pre++;
		int min = low[v];
		stack.push(v);
		for (final INodeID neighbor : graph.getNeighbors(node)) {
			int w = this.nodeIdToInternalId.get(neighbor);
			if (!marked[w])
				dfs(graph, neighbor);
			if (low[w] < min)
				min = low[w];
		}
		if (min < low[v]) {
			low[v] = min;
			return;
		}
		int w;
		do {
			w = stack.pop();
			sccId[w] = count;
			low[w] = graph.getNodeCount();
		} while (w != v);
		count++;
	}

	/**
	 * Returns the number of strong components.
	 * 
	 * @return the number of strong components
	 */
	public int getNumberOfSccs() {
		return count;
	}

	/**
	 * Are vertices <tt>v</tt> and <tt>w</tt> in the same strong component?
	 * 
	 * @param v
	 *            one vertex
	 * @param w
	 *            the other vertex
	 * @return <tt>true</tt> if vertices <tt>v</tt> and <tt>w</tt> are in the
	 *         same strong component, and <tt>false</tt> otherwise
	 */
	public boolean stronglyConnected(Node v, Node w) {
		return sccId[this.nodeIdToInternalId.get(v.getId())] == sccId[this.nodeIdToInternalId.get(w.getId())];
	}

	/**
	 * Returns the component id of the strong component containing vertex
	 * <tt>v</tt>.
	 * 
	 * @param v
	 *            the vertex
	 * @return the component id of the strong component containing vertex
	 *         <tt>v</tt>
	 */
	public int getSccId(INodeID v) {
		return sccId[this.nodeIdToInternalId.get(v)];
	}

	public Iterable<Set<INodeID>> getSCCs() {
		Map<Integer, Set<INodeID>> sccIdToNodes = new HashMap<>();
		for (int scc = 0; scc < this.getNumberOfSccs(); ++scc) {
			sccIdToNodes.put(scc, new HashSet<INodeID>());
		}
		for (final INodeID v : this.nodeIdToInternalId.keySet()) {
			sccIdToNodes.get(this.getSccId(v)).add(v);
		}
		return sccIdToNodes.values();
	}
}
