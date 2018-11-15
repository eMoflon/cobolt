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

package de.tudarmstadt.maki.simonstrator.tc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import de.tudarmstadt.maki.simonstrator.api.Graphs;
import de.tudarmstadt.maki.simonstrator.api.common.graph.BasicGraph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.DirectedEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.EdgeID;
import de.tudarmstadt.maki.simonstrator.api.common.graph.GenericGraphElementProperties;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INode;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Node;
import de.tudarmstadt.maki.simonstrator.api.util.UtilityClassNotInstantiableException;
import de.tudarmstadt.maki.simonstrator.tc.graph.algorithm.TarjanSCC;
import de.tudarmstadt.maki.simonstrator.tc.underlay.EdgeState;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyProperties;

/**
 * Utility functions for graphs
 */
public final class GraphUtil {

	public static final int INFINITE_DISTANCE = -1;

	private GraphUtil() {
	    throw new UtilityClassNotInstantiableException();
	}

	public static boolean isStronglyConnected(final Graph graph) {
		return new TarjanSCC(graph).getNumberOfSccs() == 1;
	}

	/**
	 * Returns whether for each edge in the graph, a reverse edge exists.
	 *
	 * Note: A symmetric graph needs not have an even edge count if we permit
	 * loops.
	 */
	public static boolean isSymmetric(final Graph graph) {
		/*
		 * final Set<IEdge> reverseEdgeSet = new HashSet<>(); for (final IEdge
		 * edge : graph.getEdges()) { reverseEdgeSet.add(new
		 * DirectedEdge(edge.toId(), edge.fromId())); }
		 */
		for (final IEdge edge : graph.getEdges()) {
			if (!graph.containsEdge(new DirectedEdge(edge.toId(), edge.fromId()))) {
				return false;
			}

		}
		return true;
	}

	/**
	 * Returns the set of nodes that are not end nodes of any edge.
	 */
	public static Iterable<INodeID> findIsolatedNodes(final Graph graph) {
		return graph.getIsolatedNodes();
	}

	/**
	 * Calculates the minimal hop distance from the given root node to every
	 * node in the graph. Unreachable nodes have the distance
	 * {@link #INFINITE_DISTANCE}.
	 */
	public static Map<INodeID, Integer> calculateNodeDepths(final INodeID root, final Graph graph) {
		return calculateNodeDepths(root, graph, true);
	}

	/**
	 * Calculates the minimal hop distance from the given root node to every
	 * node in the graph. Unreachable nodes have the distance
	 * {@link #INFINITE_DISTANCE}.
	 *
	 * @param directed
	 *            true if edges are only traversed in forwards direction, false
	 *            if edge are assumed to be undirected
	 */
	public static Map<INodeID, Integer> calculateNodeDepths(final INodeID root, final Graph graph,
			final boolean directed) {
		final Map<INodeID, Integer> nodeDepths = new HashMap<>();
		final Map<INodeID, Boolean> isVisited = new HashMap<>();

		// Set depth to infinity for every node
		for (final INode node : graph.getNodes()) {
			nodeDepths.put(node.getId(), INFINITE_DISTANCE);
			isVisited.put(node.getId(), false);
		}

		// Initialize BFS queue with root node
		nodeDepths.put(root, 0);
		final Deque<INodeID> toBeVisited = new LinkedList<>();
		toBeVisited.addLast(root);
		while (!toBeVisited.isEmpty()) {
			final INodeID node = toBeVisited.pollFirst();
			final Integer currentDepth = nodeDepths.get(node);
			isVisited.put(node, true);

			for (final INodeID neighbor : graph.getNeighbors(node, directed)) {
				final Integer oldNeighborDepth = nodeDepths.get(neighbor);
				int newDist;
				if (oldNeighborDepth == INFINITE_DISTANCE) {
					newDist = currentDepth + 1;
				} else {
					newDist = Math.min(oldNeighborDepth, currentDepth + 1);
				}
				nodeDepths.put(neighbor, newDist);
				if (!isVisited.get(neighbor) && !toBeVisited.contains(neighbor)) {
					toBeVisited.add(neighbor);
				}
			}
		}

		return nodeDepths;
	}

	public static Graph createGraph(final Iterable<? extends INode> nodes, final Iterable<? extends IEdge> edges) {
		final Graph graph = Graphs.createGraph(nodes, edges);
		return graph;
	}

	/**
	 * Returns the set of start and end nodes of the given set of edges
	 */
	public static Set<INodeID> collectEndnodes(final Iterable<? extends IEdge> edges) {
		final Set<INodeID> nodes = new HashSet<>();
		for (final IEdge edge : edges) {
			nodes.add(edge.fromId());
			nodes.add(edge.toId());
		}
		return nodes;
	}

	/**
	 * Utility method for creating a node whose ID is generated from the given
	 * string.
	 * 
    * The node is isolated and should be added to a graph using {@link Graph#addNode(INode)}
	 * 
	 * @see INodeID#get(String)
	 */
	public static INode createNode(final String id) {
		return new Node(INodeID.get(id));
	}

	/**
    * Utility method for creating a node whose ID is generated from the given
    * integer.
    * 
    * The node is isolated and should be added to a graph using {@link Graph#addNode(INode)}
    * 
    * @param value
    * 
    * @see INodeID#get(long)
    */
   public static INode createNode(final Integer value) {
   	return new Node(INodeID.get(value));
   }

   /**
	 * Creates an edge-induced graph.
	 *
	 * The nodes are derived from the end nodes of the edges.
	 * 
    * Multi-edges are allowed, i.e., a pair of nodes may be connected by more than one edge.
    * Each multi-edge counts separately when calling {@link Graph#getEdgeCount()}
	 *
	 * @param edges
	 *            the edges of the graph.
	 *            
	 *            @see #createGraph(Iterable, Iterable)
	 */
	public static Graph createGraph(final Iterable<? extends IEdge> edges) {
		return createGraph(collectEndnodes(edges), edges);
	}

   /**
    * Creates a graph based on lists of nodes and edges
    *
    * Multi-edges are allowed, i.e., a pair of nodes may be connected by more than one edge.
    * Each multi-edge counts separately when calling {@link Graph#getEdgeCount()}
    *
    * @param edges
    *            the edges of the graph.
    *            
    *            @see #createGraph(Iterable, Iterable)
    */
	public static Graph createGraph(final Set<INodeID> nodes, final Iterable<? extends IEdge> edges) {
		final Graph g = new BasicGraph();
		g.createAndAddNodes(nodes);
		g.addEdges(edges);
		return g;
	}

	/**
	 * Creates and returns the reverse edge of the given edge.
	 */
	public static IEdge reverse(final IEdge edge) {
		if (edge.getProperty(GenericGraphElementProperties.WEIGHT) != null) {
			return Graphs.createDirectedWeightedEdge(edge.toId(), edge.fromId(),
					edge.getProperty(GenericGraphElementProperties.WEIGHT));
		} else {
			return new DirectedEdge(edge.toId(), edge.fromId());
		}
	}

	/**
	 * Returns the average degree (in plus out) of the graph.
	 * 
	 * If the graph does not contain any nodes, an
	 * {@link IllegalArgumentException} is thrown.
	 */
	public static double getAverageDegree(final Graph graph) {
		return calculateAveragePerNodeMetric(graph, new Function<INode, Double>() {
			@Override
			public Double apply(INode arg0) {
				return (double) graph.getDegree(arg0.getId());
			};
		});
	}

	/**
	 * Returns the average in-degree of the graph.
	 * 
	 * If the graph does not contain any nodes, an
	 * {@link IllegalArgumentException} is thrown.
	 */
	public static double getAverageIndegree(final Graph graph) {
		return calculateAveragePerNodeMetric(graph, new Function<INode, Double>() {
			@Override
			public Double apply(INode node) {
				return (double) graph.getIndegree(node.getId());
			};
		});
	}

	/**
	 * Returns the average out-degree of the graph.
	 * 
	 * If the graph does not contain any nodes, an
	 * {@link IllegalArgumentException} is thrown.
	 */
	public static double getAverageOutdegree(final Graph graph) {
		return calculateAveragePerNodeMetric(graph, new Function<INode, Double>() {
			@Override
			public Double apply(INode node) {
				return (double) graph.getOutdegree(node.getId());
			};
		});
	}

	public static String formatEdgeStateSummary(final Graph graph) {

		final Map<EdgeState, Integer> stateCounts = new HashMap<>();
		stateCounts.put(EdgeState.ACTIVE, 0);
		stateCounts.put(EdgeState.INACTIVE, 0);
		stateCounts.put(EdgeState.UNCLASSIFIED, 0);

		for (final IEdge edge : graph.getEdges()) {
			final EdgeState linkState = edge.getProperty(UnderlayTopologyProperties.EDGE_STATE);
			stateCounts.put(linkState, stateCounts.get(linkState) + 1);
		}

		return String.format("#A : %d || #I : %d || #U : %d || Sum : %d", //
				stateCounts.get(EdgeState.ACTIVE), //
				stateCounts.get(EdgeState.INACTIVE), //
				stateCounts.get(EdgeState.UNCLASSIFIED), //
				stateCounts.get(EdgeState.ACTIVE) + stateCounts.get(EdgeState.INACTIVE)
						+ stateCounts.get(EdgeState.UNCLASSIFIED)//
		);
	}

	public static String formatEdgeStateReport(final Graph graph) {
		final StringBuilder builder = new StringBuilder();
		final List<EdgeID> edgeIds = new ArrayList<>();
		final Set<EdgeID> processedIds = new HashSet<>();
		for (final IEdge edge : graph.getEdges()) {
			edgeIds.add(edge.getId());
		}
		final Map<EdgeState, Integer> stateCounts = new HashMap<>();
		stateCounts.put(EdgeState.ACTIVE, 0);
		stateCounts.put(EdgeState.INACTIVE, 0);
		stateCounts.put(EdgeState.UNCLASSIFIED, 0);
		Collections.sort(edgeIds, new Comparator<EdgeID>() {
			@Override
			public int compare(EdgeID o1, EdgeID o2) {
				return o1.valueAsString().compareTo(o2.valueAsString());
			}
		});

		for (final EdgeID id : edgeIds) {
			if (!processedIds.contains(id)) {
				final IEdge link = graph.getEdge(id);
				EdgeState linkState = link.getProperty(UnderlayTopologyProperties.EDGE_STATE);
				builder.append(String.format("%6s", link.getId()) + " : " + linkState.toString().substring(0, 1));
				processedIds.add(link.getId());
				stateCounts.put(linkState, stateCounts.get(linkState) + 1);

				final IEdge revLink = graph.getInverseEdge(link);
				if (revLink != null) {
					EdgeState revLinkState = revLink.getProperty(UnderlayTopologyProperties.EDGE_STATE);
					builder.append(" || " + String.format("%6s", revLink.getId()) + " : "
							+ revLinkState.toString().substring(0, 1));
					processedIds.add(revLink.getId());
					stateCounts.put(revLinkState, stateCounts.get(revLinkState) + 1);
				}

				builder.append("\n");

			}
		}

		builder.insert(0,
				String.format("#A : %d || #I : %d || #U : %d\n || Sum : %d", //
						stateCounts.get(EdgeState.ACTIVE), //
						stateCounts.get(EdgeState.INACTIVE), //
						stateCounts.get(EdgeState.UNCLASSIFIED), //
						stateCounts.get(EdgeState.ACTIVE) + stateCounts.get(EdgeState.INACTIVE)
								+ stateCounts.get(EdgeState.UNCLASSIFIED)//
		));

		return builder.toString().trim();

	}

	public static String createEdgeStateDifferenceReport(Graph graph, Graph graph2) {
		Set<EdgeID> edgeIdSet = new HashSet<>();
		edgeIdSet.addAll(graph.getEdgeIds());
		edgeIdSet.addAll(graph2.getEdgeIds());
		List<EdgeID> edgeIds = new ArrayList<>(edgeIdSet);
		Collections.sort(edgeIds, new Comparator<EdgeID>() {
			@Override
			public int compare(EdgeID o1, EdgeID o2) {
				return o1.valueAsString().compareTo(o2.valueAsString());
			}
		});

		StringBuilder sb = new StringBuilder();
		sb.append(String.format("Same edge sets? %b\n", graph.getEdgeIds().equals(graph2.getEdgeIds())));
		sb.append(String.format("%10s    %s  %10s  %10s\n", "Edge ID", "Diff", "State 1", "State 2"));

		int numberOfDifferences = 0;
		int numberOfSameStateLinks = 0;
		for (final EdgeID id : edgeIds) {
			final IEdge edge1 = graph.getEdge(id);
			final IEdge edge2 = graph2.getEdge(id);
			final EdgeState state1 = edge1 != null ? edge1.getProperty(UnderlayTopologyProperties.EDGE_STATE) : null;
			final EdgeState state2 = edge2 != null ? edge2.getProperty(UnderlayTopologyProperties.EDGE_STATE) : null;
			final String difference = state1 == state2 ? "SAME" : "DIFF";
			if (state1 != state2) {
				sb.append(String.format("%10s    %s  %10s  %10s %.3f  %.3f\n", id, difference, state1, state2,
						edge1.getProperty(UnderlayTopologyProperties.WEIGHT),
						edge2.getProperty(UnderlayTopologyProperties.WEIGHT)));
				++numberOfDifferences;
			} else {
				++numberOfSameStateLinks;
			}

		}

		sb.append(String.format("#DIFF: %d || #SAME: %s", numberOfDifferences, numberOfSameStateLinks));

		return sb.toString();
	}
	

	private static double calculateAveragePerNodeMetric(final Graph graph,
			Function<INode, Double> nodePropertyExtractor) {

		if (graph.getNodeCount() == 0)
			throw new IllegalArgumentException("Graph must have at least one node");
		
		return graph.getNodes().stream().map(nodePropertyExtractor).reduce(new BinaryOperator<Double>() {
			@Override
			public Double apply(Double x, Double y) {
				return x.doubleValue() + y.doubleValue();
			}
		}).get() / graph.getNodeCount();
	}
}
