/*
 * Copyright (c) 2005-2010 KOM – Multimedia Communications Lab
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

package de.tudarmstadt.maki.simonstrator.api.common.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.tudarmstadt.maki.simonstrator.api.Graphs;
import de.tudarmstadt.maki.simonstrator.api.common.datastructures.Pair;

/**
 * A basic class realizing the Graph-Interface
 * 
 * @author Michael Stein
 * @author Roland Kluge
 * @author Bjoern Richerzhagen
 *
 */
public class BasicGraph implements Graph {

	private final Set<INode> nodes = new LinkedHashSet<>();

	private Map<INodeID, INode> nodesById = new LinkedHashMap<>();

	private final Set<IEdge> edges = new LinkedHashSet<>();

	private final Set<Pair<INodeID>> edgesAsNodeIdPairs = new LinkedHashSet<>();

	// The list of all edges. For performance reasons. Edge IDs are always unique within one graph.
	private final Map<EdgeID, IEdge> edgesById = new LinkedHashMap<>();

	// Assigns to each node the set of outgoing edges of this node
	private final Map<INodeID, Set<IEdge>> outgoingAdjacencyList = new LinkedHashMap<>();

	// Assigns to each node the set of incoming edges of this node
	private final Map<INodeID, Set<IEdge>> incomingAdjacencyList = new LinkedHashMap<>();

	// Assigns to each node the set of successors of this node
	// The successors of a node n1 are all nodes n2 that can be reached via an
	// outgoing edge of n1
	private final Map<INodeID, HashSet<INodeID>> successorNodes = new LinkedHashMap<>();

	// Assigns to each node the set of predecessors of this node
	// The successors of a node n2 are all nodes n2 that can be reached (in
	// reverse direction) via an incoming edge of n1
	private final Map<INodeID, HashSet<INodeID>> predecessorNodes = new LinkedHashMap<>();

	// Maps an edge to its inverse edge. Each pair of edges should only be
	// contained once (i.e., check both directions).
	private Map<IEdge, Collection<IEdge>> inverseEdgeMapping = new LinkedHashMap<>();

	/**
	 * Creates an empty graph. Components should create new graphs by using the
	 * {@link Graphs} class in the API
	 */
	public BasicGraph() {
	}

	/**
	 * Creates a graph from the given nodes and edges.
	 * 
	 * Duplicate nodes and edges will be ignored
	 * 
	 * @param nodes
	 *            The set of nodes contained in the graph.
	 * @param edges
	 *            Set of edges contained in the graph. Assumes that every start
	 *            and end node in the edges is also contained in the nodes set
	 */
	public BasicGraph(Iterable<? extends INode> nodes, Iterable<? extends IEdge> edges) {
		this();

		for (final INode node : nodes)
			this.addNode(node);

		for (final IEdge edge : edges)
			this.addEdge(edge);
	}

	/**
	 * Creates an edge-induced graph.
	 * 
	 * The nodes are derived from the edges.
	 * 
	 * @param edges
	 *            the edges of the graph
	 */
	public BasicGraph(Iterable<? extends IEdge> edges) {
		this();
		for (final IEdge edge : edges) {
			this.createAndAddNode(edge.fromId());
			this.createAndAddNode(edge.toId());
		}
		this.addElements(edges);
	}

	public static BasicGraph createGraphFromNodeIdsAndEdges(Iterable<? extends INodeID> nodes,
			Iterable<? extends IEdge> edges) {
		final BasicGraph graph = new BasicGraph();
		for (INodeID nodeId : nodes) {
			graph.addNode(graph.createNode(nodeId));
		}
		for (IEdge edge : edges) {
			graph.addEdge(edge);
		}
		return graph;
	}

	/**
	 * Creates an edge-induced graph.
	 * 
	 * The nodes are derived from the end nodes of the edges.
	 * 
	 * @param edges
	 *            the edges of the graph
	 * 
	 * @deprecated Use {@link #BasicGraph(Iterable)}
	 */
	@SafeVarargs
	@Deprecated
	public BasicGraph(IEdge... edges) {
		this(Arrays.asList(edges));
	}

	@Override
	public IEdge createEdge(INodeID from, INodeID to) {
		IEdge edge = getEdge(from, to);
		return (edge == null ? new DirectedEdge(from, to) : edge);
	}

	@Override
	public IEdge createEdge(INodeID from, INodeID to, double weight) {
		IEdge edge = createEdge(from, to);
		if (edge == null) {
			return Graphs.createDirectedWeightedEdge(from, to, weight);
		} else {
			edge.setProperty(GenericGraphElementProperties.WEIGHT, weight);
			return edge;
		}
	}

	@Override
	public IEdge createAndAddEdge(INodeID from, INodeID to) {
		return createAndAddEdge(from, to, false);
	}

	@Override
	public IEdge createAndAddEdge(INodeID from, INodeID to, boolean allowMultiEdges) {
		if (!this.containsEdge(from, to)) {
			IEdge newEdge = this.createEdge(from, to);
			this.addEdge(newEdge);
			return newEdge;
		} else {
			return this.getEdge(from, to);
		}
	}

	@Override
	public boolean containsEdge(INodeID from, INodeID to) {
		return this.edgesAsNodeIdPairs.contains(createNodeIdPair(from, to));
	}

	@Override
	public INode createNode(INodeID id) {
		if (this.containsNode(id)) {
			return this.getNode(id);
		} else {
			return new Node(id);
		}
	}

	@Override
	@Deprecated // Use the strongly typed methods instead
	public boolean addElement(IElement element) {
		if (element instanceof INode) {
			return this.addNode((Node) element);
		} else if (element instanceof IEdge) {
			return this.addEdge((IEdge) element);
		} else {
			throw new AssertionError("Unknown graph element type.");
		}
	}

	@Override
	@Deprecated // Use the strongly typed methods instead
	public boolean add(IElement element) {
		return this.addElement(element);
	}

	@Override
	public boolean addEdge(final IEdge edge) {
		final INodeID toId = edge.toId();
		final INodeID fromId = edge.fromId();
		this.ensureThatNodeIsInGraph(toId);
		this.ensureThatNodeIsInGraph(fromId);
		if (!this.edgesById.containsKey(edge.getId())) {
			this.edges.add(edge);
			this.edgesById.put(edge.getId(), edge);
			this.edgesAsNodeIdPairs.add(createNodeIdPair(edge));
			this.outgoingAdjacencyList.get(fromId).add(edge);
			this.incomingAdjacencyList.get(toId).add(edge);
			this.successorNodes.get(fromId).add(toId);
			this.predecessorNodes.get(toId).add(fromId);
			this.inverseEdgeMapping.put(edge, new LinkedList<IEdge>());
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean addNode(INode node) {
		if (!this.containsNode(node)) {
			this.nodes.add(node);
			this.nodesById.put(node.getId(), node);
			this.incomingAdjacencyList.put(node.getId(), new LinkedHashSet<>());
			this.outgoingAdjacencyList.put(node.getId(), new LinkedHashSet<>());
			this.successorNodes.put(node.getId(), new LinkedHashSet<>());
			this.predecessorNodes.put(node.getId(), new LinkedHashSet<>());
			return true;
		} else {
			return false;
		}
	}

	@Override
	public INode createAndAddNode(INodeID nodeId) {
		if (!this.containsNode(nodeId)) {
			final INode node = this.createNode(nodeId);
			this.addNode(node);
			return node;
		} else {
			return this.getNode(nodeId);
		}
	}

	@Override
	public List<INode> createAndAddNodes(Iterable<INodeID> nodeIds) {
		final List<INode> createdNodes = new ArrayList<>();
		for (final INodeID nodeId : nodeIds) {
			createdNodes.add(this.createAndAddNode(nodeId));
		}
		return createdNodes;
	}

	@Override
	@Deprecated // Use the strongly typed methods instead
	public boolean removeElement(IElement element) {
		if (element instanceof INode) {
			return removeNode((INode) element);
		} else if (element instanceof IEdge) {
			return removeEdge((IEdge) element);
		} else {
			throw new AssertionError("Unknown graph element type.");
		}
	}

	@Override
	public boolean removeNode(INodeID node) {
		if (this.containsNode(node))
			return this.removeNode(this.getNode(node));
		return false;
	}

	private boolean removeNode(INode node) {
		if (!this.containsNode(node.getId()))
			return false;

		final List<IEdge> edgesToRemove = new ArrayList<>();
		for (final IEdge edge : this.getOutgoingEdges(node.getId()))
			edgesToRemove.add(edge);

		for (final IEdge edge : this.getIncomingEdges(node.getId()))
			edgesToRemove.add(edge);

		for (final IEdge edgeToRemove : edgesToRemove)
			this.removeEdge(edgeToRemove);

		nodesById.remove(node.getId());
		nodes.remove(node);

		return true;
	}

	@Override
	public boolean removeEdge(final IEdge edge) {
		if (!this.containsEdge(edge))
			return false;

		this.incomingAdjacencyList.get(edge.toId()).remove(edge);
		this.outgoingAdjacencyList.get(edge.fromId()).remove(edge);
		this.successorNodes.get(edge.fromId()).remove(edge.toId());
		this.predecessorNodes.get(edge.toId()).remove(edge.fromId());
		this.edges.remove(edge);
		this.edgesAsNodeIdPairs.remove(createNodeIdPair(edge));
		this.edgesById.remove(edge.getId());
		for (final IEdge inverseEdge : new ArrayList<>(this.getInverseEdges(edge)))
			this.destroyInverseEdges(edge, inverseEdge);
		this.inverseEdgeMapping.remove(edge);
		return true;
	}

	@Override
	public Set<INode> getNodes() {
		return nodes;
	}

	@Override
	public Iterable<INodeID> getNodeIds() {
		return this.nodesById.keySet();
	}

	public Set<INodeID> getNodeIdsSet() {
		return this.nodesById.keySet();
	}

	@Override
	public Set<EdgeID> getEdgeIds() {
		return this.edgesById.keySet();
	}

	@Override
	public int getNodeCount() {
		return this.getNodes().size();
	}

	@Override
	public Set<IEdge> getEdges() {
		return Collections.unmodifiableSet(this.edges);
	}

	@Override
	public int getEdgeCount() {
		return this.getEdges().size();
	}

	@Override
	@Deprecated // Use the strongly typed methods instead
	public boolean contains(IElement element) {
		if (element instanceof INode)
			return containsNode((INode) element);
		else if (element instanceof IEdge)
			return containsEdge((IEdge) element);
		else
			throw new IllegalArgumentException("Unsupported kind of element: " + element);
	}

	@Override
	public boolean containsNode(INodeID nodeId) {
		return nodesById.containsKey(nodeId);
	}

	@Override
	public boolean containsNode(INode node) {
		return this.containsNode(node.getId());
	}

	@Override
	public INode getNode(INodeID nodeId) {
		return nodesById.get(nodeId);
	}

	@Override
	public IEdge getEdge(INodeID from, INodeID to) {

		if (!this.containsNode(from))
			return null;

		if (!this.containsNode(to))
			return null;

		if (!this.containsEdge(from, to))
			return null;

		final Set<IEdge> outgoingEdgesOfFrom = this.outgoingAdjacencyList.get(from);
		for (final IEdge outgoingEdgeOfFrom : outgoingEdgesOfFrom) {
			if (outgoingEdgeOfFrom.toId().equals(to)) {
				return outgoingEdgeOfFrom;
			}
		}
		return null;
	}

	@Override
	public Collection<IEdge> getEdges(INodeID from, INodeID to) {

		final List<IEdge> resultSet = new ArrayList<>();

		if (!this.containsNode(from))
			throw new IllegalArgumentException(String.format("Unknown 'from' ID: %s", from.toString()));

		if (!this.containsNode(to))
			throw new IllegalArgumentException(String.format("Unknown 'to' ID: %s", to.toString()));

		if (!this.containsEdge(from, to))
			return resultSet;

		final Set<IEdge> outgoingEdgesOfFrom = this.outgoingAdjacencyList.get(from);
		for (final IEdge outgoingEdgeOfFrom : outgoingEdgesOfFrom) {
			if (outgoingEdgeOfFrom.toId().equals(to)) {
				resultSet.add(outgoingEdgeOfFrom);
			}
		}

		return resultSet;
	}

	@Override
	public IEdge getEdge(EdgeID edgeID) {
		return this.edgesById.get(edgeID);
	}

	@Override
	public IEdge getInverseEdge(EdgeID edgeId) {
		final IEdge edge = getEdge(edgeId);
		if (edge == null)
			throw new IllegalArgumentException("Cannot retrieve inverse edge of " + edgeId + " because " + edgeId
					+ " is not contained in this graph");
		return getInverseEdge(edge);
	}

	@Override
	public Collection<IEdge> getInverseEdges(IEdge edge) {
		return Collections.unmodifiableCollection(inverseEdgeMapping.get(edge));
	}

	@Override
	public IEdge getInverseEdge(IEdge edge) {
		final Iterator<IEdge> forwardLookup = inverseEdgeMapping.get(edge).iterator();
		if (forwardLookup.hasNext()) {
			return forwardLookup.next();
		}
		return null;
	}

	@Override
	public void makeInverseEdges(IEdge forwardEdge, IEdge backwardEdge) {
		if (!areInverseEdges(forwardEdge, backwardEdge))
		{
			this.inverseEdgeMapping.get(forwardEdge).add(backwardEdge);
			this.inverseEdgeMapping.get(backwardEdge).add(forwardEdge);
		}

	}

	@Override
	public boolean areInverseEdges(IEdge forwardEdge, IEdge backwardEdge) {
		return forwardEdge.equals(this.getInverseEdge(backwardEdge));
	}

	@Override
	public void destroyInverseEdges(IEdge forwardEdge, IEdge backwardEdge)
	{
		this.inverseEdgeMapping.get(forwardEdge).remove(backwardEdge);
	}

	/**
	 * Computes subgraph of the currently known local view. This operation
	 * changes the current graph instead of cloning it.
	 */
	@Override
	public BasicGraph getLocalView(INodeID node, int k) {
		return getLocalView(node, k, false);
	}

	/**
	 * Computes subgraph of the currently known local view. The clone parameter
	 * specifies whether this Graph instance is changed. If clone is true, a new
	 * Graph instance is generated. Elsewise, this Graph instance is modified.
	 */
	@Override
	public BasicGraph getLocalView(INodeID center, int k, boolean clone) {
		BasicGraph g = clone ? this.clone() : this;

		// filter nodes
		Set<INodeID> kHopNodes = getNeighbors(center, k, false);

		// determine edges
		Set<IEdge> kHopEdges = new LinkedHashSet<>();
		for (final INodeID kHopNode : kHopNodes) {
			for (final IEdge outgoingEdge : this.getOutgoingEdges(kHopNode)) {
				if (kHopNodes.contains(outgoingEdge.toId())) {
					kHopEdges.add(outgoingEdge);
				}
			}
		}

		List<INode> nodesToBeRemoved = new ArrayList<>();
		for (final INode node : this.getNodes()) {
			if (!kHopNodes.contains(node.getId())) {
				nodesToBeRemoved.add(node);
			}
		}
		for (final INode node : nodesToBeRemoved) {
			g.removeElement(node);
		}

		assert checkGraphConsistency();

		return g;
	}

	@Override
	public void clear() {
		this.nodes.clear();
		this.nodesById.clear();
		this.edges.clear();
		this.edgesAsNodeIdPairs.clear();
		this.edgesById.clear();
		this.incomingAdjacencyList.clear();
		this.outgoingAdjacencyList.clear();
		this.predecessorNodes.clear();
		this.successorNodes.clear();
		this.inverseEdgeMapping.clear();
	}

	/**
	 * Returns the nodes contained in the k-hop neighborhood of the specified
	 * node. This includes the node itself.
	 * 
	 * If the directedNeighborhood flag is true, edge directions are considered
	 * for the neighborhood
	 */
	@Override
	public Set<INodeID> getNeighbors(INodeID center, int k, boolean directedNeighborhood) {
		// assert (getNeighbors(center, k, directedNeighborhood, new
		// LinkedHashSet<INodeID>())
		// .equals(getNeighborsIter(center, k, directedNeighborhood)));
		return getNeighborsIter(center, k, directedNeighborhood);

	}

	/**
	 * Checks that every start and end node in the edges is also contained in
	 * the nodes set
	 */
	private boolean checkGraphConsistency() {
		for (IEdge directedEdge : edges) {
			if (!nodesById.containsKey(directedEdge.fromId()) || !nodesById.containsKey(directedEdge.toId())) {
				return false;
			}
		}

		return true;
	}

	// helper method
	private Set<INodeID> getNeighborsIter(INodeID node, int k, boolean directedNeighborhood) {

		HashSet<INodeID> visited = new HashSet<INodeID>();

		// visited.add(node);

		if (k < 0)
			throw new IllegalArgumentException("k must be >=0, but was " + k);

		if (k == 0) {
			return visited;
		}

		HashSet<INodeID> neighbors = new HashSet<INodeID>();
		HashSet<INodeID> newNeighbors = new HashSet<INodeID>();
		neighbors.add(node);

		for (int i = k; i > 0; i--) {
			for (INodeID neighbor : neighbors) {
				if (visited.add(neighbor))
					newNeighbors.addAll(getNeighbors(neighbor, directedNeighborhood));
			}
			neighbors = new HashSet<INodeID>(newNeighbors);
			newNeighbors.clear();
		}
		visited.addAll(neighbors);
		return visited;
	}

	// helper method, replaced by getNeighborsIter which is thought to be more
	// efficient
	@SuppressWarnings("unused")
	private Set<INodeID> getNeighbors(INodeID node, int k, boolean directedNeighborhood, HashSet<INodeID> visited) {

		if (k < 0)
			throw new IllegalArgumentException("k must be >=0, but was " + k);

		if (k == 0) {
			visited.add(node);
			HashSet<INodeID> oneNodeNeighborSet = new HashSet<INodeID>();
			oneNodeNeighborSet.add(node);
			return oneNodeNeighborSet;
		}

		visited.add(node);

		HashSet<INodeID> neighbors = new HashSet<INodeID>();
		neighbors.add(node);

		for (INodeID neighbor : getNeighbors(node, directedNeighborhood)) {
			if (!visited.contains(neighbor))
				neighbors.addAll(getNeighbors(neighbor, k - 1, directedNeighborhood, visited));
		}

		return neighbors;
	}

	@Override
	public Set<IEdge> getOutgoingEdges(INodeID node) {
		ensureThatNodeIsInGraph(node);
		return Collections.unmodifiableSet(this.outgoingAdjacencyList.get(node));
	}

	@Override
	public Set<IEdge> getIncomingEdges(INodeID node) {
		ensureThatNodeIsInGraph(node);
		return Collections.unmodifiableSet(this.incomingAdjacencyList.get(node));
	}

	@Override
	public Set<INodeID> getNeighbors(INodeID node, boolean directedNeighborhood) {
		ensureThatNodeIsInGraph(node);
		final HashSet<INodeID> successorSet = successorNodes.get(node);
		if (directedNeighborhood)
			return Collections.unmodifiableSet(successorSet);
		@SuppressWarnings("unchecked")
		Set<INodeID> neighbors = (Set<INodeID>) successorSet.clone();
		neighbors.addAll(predecessorNodes.get(node));
		return neighbors;
	}

	@Override
	public Set<INodeID> getNeighbors(INodeID node) {
		return getNeighbors(node, true);
	}

	@Override
	public Set<INodeID> getNeighbors(INode node) {
		return getNeighbors(node.getId());
	}

	private void ensureThatNodeIsInGraph(INodeID node) {
		if (!this.nodesById.containsKey(node))
			throw new IllegalStateException(String.format("Node %s is not contained in the graph %s", node, this));
	}

	@Override
	public BasicGraph clone() {
		/*
		 * more efficient, but less clean than inserting each node and edge
		 *
		 * Map<INodeID, Set<IEdge>> outs = new HashMap<INodeID, Set<IEdge>>();
		 * Map<INodeID, Set<IEdge>> ins = new HashMap<INodeID, Set<IEdge>>();
		 * Map<INodeID, Set<INodeID>> pres = new HashMap<INodeID,
		 * Set<INodeID>>(); Map<INodeID, Set<INodeID>> succ = new
		 * HashMap<INodeID, Set<INodeID>>(); for (INodeID n : getNodeIds()) {
		 * outs.put(n, new HashSet<>(outgoingAdjacencyList.get(n))); ins.put(n,
		 * new HashSet<>(incomingAdjacencyList.get(n))); pres.put(n, new
		 * HashSet<>(predecessorNodes.get(n))); succ.put(n, new
		 * HashSet<>(successorNodes.get(n))); } return new
		 * BasicGraph(this.nodes, this.nodesById, this.edges, this.edgesById,
		 * outs, ins, succ, pres);
		 *
		 * As per the API contract, the inner objects are NOT cloned. This
		 * cloned graph is just a new view.
		 */

		BasicGraph g = new BasicGraph();
		g.addNodes(this.nodes);
		g.addEdges(this.edges);
		return g;

	}

	@Override
	public void addEdges(Iterable<? extends IEdge> edges) {
		for (IEdge edge : edges) {
			this.addEdge(edge);
		}
	}

	@Override
	public void addNodes(Iterable<? extends INode> nodes) {
		for (INode node : nodes) {
			this.addNode(node);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Graph [V= ");
		sb.append(this.nodes);
		sb.append(", E= ");
		sb.append(this.edgesById);
		sb.append("]");
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((edges == null) ? 0 : edges.hashCode());
		result = prime * result + ((nodes == null) ? 0 : nodes.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BasicGraph other = (BasicGraph) obj;
		if (edges == null) {
			if (other.edges != null)
				return false;
		} else if (!edges.equals(other.edges))
			return false;
		if (nodes == null) {
			if (other.nodes != null)
				return false;
		} else if (!nodes.equals(other.nodes))
			return false;
		return true;
	}

	@Override
	@Deprecated // Use the strongly typed methods instead
	public void addElements(Iterable<? extends IElement> elements) {
		for (final IElement element : elements) {
			if (element instanceof INode)
				this.addNode((INode) element);
		}
		for (final IElement element : elements) {
			if (element instanceof IEdge)
				this.addEdge((IEdge) element);
		}
	}

	@Override
	public boolean containsEdge(IEdge edge) {
		return this.edges.contains(edge);
	}

	@Override
	public int getOutdegree(INodeID id) {
		return this.getOutgoingEdges(id).size();
	}

	@Override
	public int getIndegree(INodeID node) {
		return this.getIncomingEdges(node).size();
	}

	@Override
	public int getDegree(INodeID nodeID) {
		return this.getIndegree(nodeID) + this.getOutdegree(nodeID);
	}

	@Override
	public Set<INodeID> getNeighbors(INode node, int k, boolean directedNeighborhood) {
		return getNeighbors(node.getId(), k, directedNeighborhood);
	}

	@Override
	public Set<INodeID> getNeighbors(INode node, boolean directedNeighborhood) {
		return getNeighbors(node.getId(), directedNeighborhood);
	}

	@Override
	public Iterable<INodeID> getIsolatedNodes() {
		final List<INodeID> isolatedNodes = new ArrayList<>();
		for (final INode node : this.getNodes()) {
			if (this.getOutdegree(node.getId()) == 0 && this.getIndegree(node.getId()) == 0) {
				isolatedNodes.add(node.getId());
			}
		}
		return isolatedNodes;
	}

	@Override
	public Iterable<INodeID> getRootNodes() {
		final List<INodeID> rootNodes = new ArrayList<>();
		for (final INode node : this.getNodes()) {
			if (this.getIndegree(node.getId()) == 0) {
				rootNodes.add(node.getId());
			}
		}
		return rootNodes;
	}

	@Override
	public Iterable<INodeID> getLeafNodes() {
		final List<INodeID> leafNodes = new ArrayList<>();
		for (final INode node : this.getNodes()) {
			if (this.getOutdegree(node.getId()) == 0) {
				leafNodes.add(node.getId());
			}
		}
		return leafNodes;
	}

	@Override
	public Set<IEdge> getOutgoingEdges(INode node) {
		return this.getOutgoingEdges(node.getId());
	}

	@Override
	public Set<IEdge> getIncomingEdges(INode node) {
		return this.getIncomingEdges(node.getId());
	}

	@Override
	@Deprecated // Use the strongly typed methods instead
	public boolean remove(IElement element) {
		return this.removeElement(element);
	}

	@Override
	public Set<INodeID> getPredecessorNodes(INodeID n) {
		return Collections.unmodifiableSet(predecessorNodes.get(n));
	}

	@Override
	public Collection<IEdge> getEdges(final Collection<INodeID> nodes) {

		final List<IEdge> resultList = new ArrayList<>();
		final Set<INodeID> nodesSet = new HashSet<INodeID>(nodes);
		final Set<IEdge> newEdges = new LinkedHashSet<IEdge>();
		for (final INodeID id : nodesSet) {
			if (!this.containsNode(id))
				return new ArrayList<>();
			newEdges.addAll(this.outgoingAdjacencyList.get(id));
		}
		for (final IEdge e : newEdges) {
			if (nodesSet.contains(e.toId())) {
				resultList.add(e);
			}
		}
		return resultList;
	}

	/**
	 * Creates a {@link Pair} that contains the source and target IDs of the
	 * given edge
	 */
	private Pair<INodeID> createNodeIdPair(final IEdge edge) {
		return createNodeIdPair(edge.fromId(), edge.toId());
	}

	/**
	 * Creates a {@link Pair} that contains the given IDs in order
	 */
	private Pair<INodeID> createNodeIdPair(final INodeID from, INodeID to) {
		return new Pair<INodeID>(from, to);
	}

}
