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
 */
package de.tudarmstadt.maki.simonstrator.tc.lomba.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import de.tudarmstadt.maki.simonstrator.api.Graphs;
import de.tudarmstadt.maki.simonstrator.api.common.graph.BasicGraph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.DirectedEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INode;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Node;
import de.tudarmstadt.maki.simonstrator.api.component.topology.OperationalEdge;
import de.tudarmstadt.maki.simonstrator.api.component.topology.OperationalEdge.EdgeOperationType;
import de.tudarmstadt.maki.simonstrator.tc.GraphUtil;

/**
 * Static utility-class, which holds often used graph operations.
 *
 * @author Julian M. Klomp
 *
 */
public class GraphOperations {
   
	/**
	 * Helper method to create graphs conveniently from strings. A graph is
	 * represented by the edges which are separated by commas: 0-1,1-2,2-0 A
	 * normal hyphen generates a bidirectional edge (in & out), whereas an arrow
	 * creates a directional edge: 0->1,1->2,2->0
	 *
	 * @param edgesAsString
	 * @return the graph
	 */
	public static Graph graphFromString(final String edgesAsString) {
		// init vars
		final Set<Node> nodes = new HashSet<>();
		final Set<IEdge> edges = new HashSet<>();
		// split string by commas
		final String[] sEdges = edgesAsString.split(",");
		for (final String e : sEdges) {
			String[] dNodes = e.split("->");
			Node n1, n2;
			// if it was a hyphen, split again and add incoming edge as well
			if (dNodes[0].equals(e)) {
				dNodes = e.split("-");
				n1 = Graphs.createNode(Integer.valueOf(dNodes[0]));
				n2 = Graphs.createNode(Integer.valueOf(dNodes[1]));
				edges.add(Graphs.createDirectedEdge(n2, n1));
			} else {
				n1 = Graphs.createNode(Integer.valueOf(dNodes[0]));
				n2 = Graphs.createNode(Integer.valueOf(dNodes[1]));
			}
			// add outgoing edge
			edges.add(Graphs.createDirectedEdge(n1, n2));
			nodes.add(n1);
			nodes.add(n2);
		}
		return GraphUtil.createGraph(nodes, edges);
	}

	/**
	 * Returns all outgoing edges of a given node {@code n} in graph {@code g}
	 *
	 * @param g
	 *            the graph containing {@code n}
	 * @param n
	 *            the node of which to extract all successors
	 * @return a set of all neighbors which have an incoming link of {@code n}
	 */
	public static Set<INodeID> getSuccessors(final Graph g, final INode n) {
		return g.getNeighbors(n, true);
	}

	/**
	 * Returns all incoming edges of a given node {@code n} in graph {@code g}
	 *
	 * @param g
	 *            the graph containing {@code n}
	 * @param n
	 *            the node of which to extract all successors
	 * @return a set of all neighbors pointing to node n
	 */
	public static Set<INodeID> getPredecessors(final Graph g, final INode n) {
		return ((BasicGraph)g).getPredecessorNodes(n.getId());
	}

	/**
	 * Returns all edges which are connected to the given node.
	 *
	 * @param localKnowledge
	 *            the graph of the node
	 * @param node
	 *            the node
	 * @return a the set of edges in- and outgoing from {@code n}
	 */

	public static Set<IEdge> getEdgesOfNode(final Graph localKnowledge, final INode node) {
		final HashSet<IEdge> edges = new HashSet<>();
		final Iterator<?> eIt = localKnowledge.getEdges().iterator();
		// TODO: quite inefficient ..
		while (eIt.hasNext()) {
			final IEdge e = (DirectedEdge) eIt.next();
			if (e.toId().equals(node.getId()) || e.fromId().equals(node.getId())) {
				edges.add(e);
			}
		}
		return edges;
	}

	/**
	 * Returns all outgoing edges from the given node.
	 *
	 * @param g
	 *            the graph of the node
	 * @param n
	 *            the node
	 * @return a the set of edges in- and outgoing from {@code n}
	 */
	public static Set<IEdge> getOutgoingEdgesOfNode(final Graph g, final INode n) {
		final HashSet<IEdge> edges = new HashSet<>();
		final Iterator<? extends IEdge> eIt = g.getEdges().iterator();
		while (eIt.hasNext()) {
			final IEdge e = eIt.next();
			if (e.fromId().equals(n.getId())) {
				edges.add(e);
			}
		}
		return edges;
	}

	/**
	 * Simply checks, whether the given edge exists in this graph or not
	 */
	public static boolean hasEdge(final Graph g, final IEdge e) {
		return g.containsEdge(e);
	}
	
	/**
	 * Simply checks, whether the given edge exists in this graph or not
	 */
	public static boolean hasEdge(final Graph g, final INodeID from, final INodeID to) {
		//return g.containsEdge(new DirectedEdge(from, to));
		return ((BasicGraph)g).getPredecessorNodes(to).contains(from);
	}

	/**
	 * Creates and returns the node induced subgraph of {@code g} defined by the
	 * given nodes {@code nodes}
	 *
	 * @param g
	 *            The original graph. It should contain all nodes in
	 *            {@code nodes}.
	 * @param nodes
	 *            The nodes which form the induced subgraph. Duplicated entries
	 *            are ignored.
	 * @return the node induced subgraph as a copy of g.
	 */
	public static Graph getNodeInducedSubgraph(final Graph g, final Set<INodeID> nodes) {
		final Set<INode> nodes2 = new HashSet<INode>();
		for(INodeID n : nodes)
			nodes2.add(g.getNode(n));
		return GraphUtil.createGraph(nodes2, ((BasicGraph)g).getEdges(nodes));
	}

	/**
	 * Creates and returns the graph only specified by the given edges. The
	 * nodes are extracted from the edge list.
	 *
	 * @param edges
	 *            a collection of edges
	 * @return the graph consisting of all nodes and edges which were found in
	 *         {@code edges}
	 */
	public static Graph getGraphFromEdges(final Collection<IEdge> edges) {
		final Set<INode> nodes = new HashSet<>();
		for (final IEdge e : edges) {
			nodes.add(new Node(e.fromId()));
			nodes.add(new Node(e.toId()));
		}
		return GraphUtil.createGraph(nodes, edges);
	}

	/**
	 * Returns a random node of the given graph {@code graph}.
	 *
	 * @param graph
	 *            the graph from which the random node should be taken
	 * @return a random node from {@code graph}
	 */
	public static INode getRandomNode(final Graph graph) {
		final Random rnd = new Random(System.nanoTime());
		final Iterator<? extends INode> it = graph.getNodes().iterator();
		INode rndNode = null;
		int pos = 0;
		final int end = rnd.nextInt(graph.getNodes().size());
		while (it.hasNext() && pos <= end) {
			rndNode = it.next();
			pos++;
		}
		return rndNode;
	}

	/**
	 * Gets the first node which is returned by the iterator of the nodes. The
	 * ordering is defined by the iterator itself, no guarantee is given that it
	 * will always be the same node.
	 *
	 * @param graph
	 * @return
	 */
	public static INode getFirstNode(final Graph graph) {
		return graph.getNodes().iterator().next();
	}

	/**
	 * Checks, whether a graph is weakly connected (i.e., direccted edges are
	 * treated as if they were undirected). This means, that the graph can be
	 * traversed completely from any node in the graph.
	 *
	 * @param g
	 *            the graph to be checked
	 * @return true, if the graph could be traversed completely.
	 */
	public static boolean isConnected(final Graph g) {
		final Set<INodeID> traversedNodes = new HashSet<>();
		final INodeID curNode = GraphOperations.getFirstNode(g).getId();
		//traversedNodes.add(curNode);
		visitNeighbors(g, curNode, traversedNodes);
		// the graph is connected if all nodes were reached
		return traversedNodes.size() == g.getNodes().size();
	}
	
	/**
	 * Recursive utility function to traverse the graph. It is a depth-first
	 * algorithm. Treats directed edges as if they were undirected
	 *
	 * @param g
	 *            the complete graph of which to extract the neighborhood. Is
	 *            not changed during iteration.
	 * @param node
	 *            the current node which is examined. Will change in each call.
	 * @param traversedNodes
	 *            already visited nodes. note: this does not need to be copied.
	 */
	private static void visitNeighbors(final Graph g, final INodeID node, final Set<INodeID> traversedNodes) {
		traversedNodes.add(node);		
		for (final INodeID neighborId : g.getNeighbors(node, false)) {
			if(traversedNodes.size() == g.getNodeCount())
				return;
			if (traversedNodes.contains(neighborId)) {
				continue;
			}
			visitNeighbors(g, neighborId, traversedNodes);
		}
	}

	/***
	 * Computes the maximum number of hops and thus the local knowledge states
	 * as hop-counts.
	 *
	 * @param g
	 * @param node
	 * @return
	 */
	public static int getLocalViewHopCount(final Graph g, final INode node) {
		final Set<INode> nodesToVisit = new HashSet<>();
		nodesToVisit.add(node);
		// return visitNeighbors(g,
		// new HashSet<Node<T>>(), visitedNodes, 0);
		final Set<IEdge> hopsToTake = new HashSet<>();
		hopsToTake.addAll(GraphOperations.getEdgesOfNode(g, node));

		return hopFurther(g, new HashSet<IEdge>(), nodesToVisit, 0);
	}

	public static Graph getLocalView(final Graph g, final INode node, final int k) {
		Set<INodeID> nodes = g.getNeighbors(node, k, false);
		Graph g2 = getNodeInducedSubgraph(g, nodes);
		return g2;
	}

	@SuppressWarnings("unused")
	private static Graph visitNeighbors(final Graph g, final Graph nG, final Set<? extends INode> nodesToVisit,
			int hopCount) {

		// nG.getNodes().addAll(nodesToVisit);
		nG.addElements(nodesToVisit);

		if (hopCount <= 0 || nodesToVisit.isEmpty()) {
			return nG;
		}

		final Set<INode> newNodes = new HashSet<>();

		for (final INode nodeToVisit : nodesToVisit) {
			for (final IEdge e : g.getEdges()) {
				final INode fromNode = g.getNode(e.fromId());
				final INode toNode = g.getNode(e.toId());
				if (fromNode.getId().equals(nodeToVisit.getId())) {
					if (!nG.contains(toNode)) {
						newNodes.add(toNode);
						nG.addNode(toNode);
					}
					nG.addEdge(e);
				} else if (toNode.getId().equals(nodeToVisit.getId())) {
					if (!nG.contains(fromNode)) {
						newNodes.add(fromNode);
						nG.addNode(fromNode);
					}
					nG.addEdge(e);
				}
			}
		}

		return visitNeighbors(g, nG, newNodes, --hopCount);
	}

	private static <T> int hopFurther(final Graph g, final Set<IEdge> takenHops, final Set<INode> nodesToVisit,
			final int hopCount) {

		if (nodesToVisit.isEmpty()) {
			return hopCount - 1;
		}

		final Set<INode> newNodes = new HashSet<>();
		for (final INode nodeToVisit : nodesToVisit) {
			for (final IEdge hopToTake : g.getOutgoingEdges(nodeToVisit.getId())) {
				if (takenHops.add(hopToTake)) {
					newNodes.add(g.getNode(hopToTake.toId()));
				}
			}
			for (final IEdge hopToTake : g.getIncomingEdges(nodeToVisit.getId())) {
				if (takenHops.add(hopToTake)) {
					newNodes.add(g.getNode(hopToTake.fromId()));
				}
			}
		}
		return hopFurther(g, takenHops, newNodes, hopCount + 1);
	}

	/**
	 * Removes all mutual edges from the given collection of edges. Always the
	 * first edge returned by the iterator of the given set is kept. If the set
	 * does not guarantee a ordering, then it is not guaranteed which edge is
	 * kept, respectively removed.
	 *
	 * @param edges
	 *            the set of edges which should be checked for mutual edges
	 * @return a set of edges without any mutual edges
	 */
	public static Set<IEdge> removeMutualEdges(final Collection<IEdge> edges) {
		final Set<IEdge> newEdgeSet = new HashSet<>(edges);
		final Iterator<IEdge> it = newEdgeSet.iterator();
		while (it.hasNext()) {
			final IEdge e = it.next();
			final IEdge mutualEdge = new DirectedEdge(e.toId(), e.fromId());
			if (newEdgeSet.contains(mutualEdge)) {
				it.remove();
			}
		}
		return newEdgeSet;
	}

	public static Set<INode> getNotConnectedNodes(final Graph graph, final INode node) {
		final Set<INode> notConnectedNodes = new HashSet<>();
		for (final INode n : graph.getNodes()) {
			if (GraphOperations.areNeighbors(graph, node, n) || n.getId().equals(node.getId())) {
				continue;
			}
			notConnectedNodes.add(n);
		}
		return notConnectedNodes;
	}

	public static Graph getCompleteGraph(final Collection<Node> nodes) {
		final ArrayList<Node> rangedNodes = new ArrayList<Node>(nodes);
		final Set<IEdge> edges = new HashSet<>();
		for (int i = 0; i < rangedNodes.size(); i++) {
			for (int j = 0; j < rangedNodes.size(); j++) {
				if (i == j) {
					continue;
				}
				edges.add(Graphs.createDirectedEdge(rangedNodes.get(i), rangedNodes.get(j)));
			}
		}
		return GraphUtil.createGraph(nodes, edges);
	}

	public static boolean areNeighbors(final Graph graph, final INode node1, final INode node2) {
		return hasEdge(graph, node1.getId(), node2.getId()) || hasEdge(graph, node2.getId(), node1.getId());
	}
	
	public static boolean areNeighbors(final Graph graph, final INodeID node1, final INodeID node2) {
		return hasEdge(graph, node1, node2) || hasEdge(graph, node2, node1);
	}

	public static IEdge reverseEdge(final IEdge edge) {
		return new DirectedEdge(edge.toId(), edge.fromId());
	}

	public static int getAmountOfRemovedEdges(final Graph g1, final Graph g2) {
		int removedEdges = 0;
		for (final IEdge e : g1.getEdges()) {
			if (!g2.contains(e)) {
				removedEdges++;
			}
		}
		return removedEdges;
	}

	public static int getAmountOfAddedEdges(final Graph g1, final Graph g2) {
		int addedEdges = 0;
		for (final IEdge e : g2.getEdges()) {
			if (!g1.contains(e)) {
				addedEdges++;
			}
		}
		return addedEdges;
	}

	public static Set<IEdge> getMissingEdges(final Graph g1, final Graph g2) {
		final Set<IEdge> missingEdges = new HashSet<>();
		for (final IEdge e : g1.getEdges()) {
			if (!g2.contains(e)) {
				missingEdges.add(e);
			}
		}
		return missingEdges;
	}

	/**
	 * Removes all single edges in the given graph and returns the set of edges,
	 * which has been removed.
	 *
	 * @param g
	 * @return
	 */
	public static Set<IEdge> removeSingleEdges(final Graph graph) {
		final Set<IEdge> edgesToRemove = new HashSet<>();
		for (final IEdge e : graph.getEdges()) {
			if (!graph.contains(GraphOperations.reverseEdge(e))) {
				edgesToRemove.add(e);
			}
		}
		for (final IEdge e : edgesToRemove) {
			graph.removeEdge(e);
		}
		return edgesToRemove;
	}

	public static Graph executeOperations(final Graph originalGraph, final Set<OperationalEdge> operations) {
		if (operations == null || operations.isEmpty()) {
			return originalGraph;
		}
		final Graph graph = originalGraph.clone();
		for (final OperationalEdge e : operations) {
			if (e.getType() == EdgeOperationType.Add) {
				graph.addEdge(e.getEdge());
			} else {
				graph.removeEdge(e.getEdge());
			}
		}
		return graph;
	}
	
	public static Graph executeOperationsNoCopy(final Graph graph, final Set<OperationalEdge> operations) {
		if (operations == null || operations.isEmpty()) {
			return graph;
		}
		for (final OperationalEdge e : operations) {
			if (e.getType() == EdgeOperationType.Add) {
				graph.addEdge(e.getEdge());
			} else {
				graph.removeEdge(e.getEdge());
			}
		}
		return graph;
	}
	
	public static Graph undoOperations(final Graph graph, final Set<OperationalEdge> operations) {
		if (operations == null || operations.isEmpty()) {
			return graph;
		}
		for (final OperationalEdge e : operations) {
			if (e.getType() == EdgeOperationType.Add) {
				graph.removeEdge(e.getEdge());
			} else {
				graph.addEdge(e.getEdge());
			}
		}
		return graph;
	}
}
