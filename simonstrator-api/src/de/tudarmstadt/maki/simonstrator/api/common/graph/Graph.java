/*
 * Copyright (c) 2005-2010 KOM – Multimedia Communications Lab
 *
 * This file is part of PeerfactSim.KOM.
 * 
 * PeerfactSim.KOM is free software: you can redistribute it and/or modify
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

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Represents a graph structure of an arbitrary topology (updated by BR, divided
 * into a general interface and a basic implementation {@link BasicGraph}).
 * 
 * @author michael.stein, Bjoern Richerzhagen
 * @version 1.0, 02.11.2014
 */
public interface Graph extends Cloneable {

	/**
	 * Create and return a node. The node is <strong>not</strong> yet added to
	 * the graph. If a node with the given ID is already in the graph, that node
	 * is returned instead and no new instance is created.
	 * 
	 * @param id
	 * @return
	 * 
	 * @deprecated The semantics of this method is arguably strange. Please use
	 *             {@link #createAndAddNode(INodeID)}. (rkluge)
	 */
	@Deprecated
	public INode createNode(INodeID id);

	/**
	 * Create and return an unweighted edge. The edge is <strong>not</strong>
	 * yet added to the graph. In case the same edge already exists in the
	 * graph, that edge is returned instead and no new instance is created.
	 * 
	 * @param from
	 *            the ID of the source node
	 * @param to
	 *            the ID of the target node
	 * @return
	 * @deprecated The semantics of this method is arguably strange. Please use
	 *             {@link #createAndAddEdge(INodeID, INodeID)}. (rkluge)
	 */
	@Deprecated
	public IEdge createEdge(INodeID from, INodeID to);

	/**
	 * Create and return a weighted edge. The edge is <strong>not</strong> yet
	 * added to the graph.
	 * 
	 * @param from
	 * @param to
	 * @param weight
	 *            weight of the edge
	 * @return
	 * @deprecated add weights via the respective {@link IElement} properties.
	 */
	@Deprecated
	public IEdge createEdge(INodeID from, INodeID to, double weight);

	/**
	 * Creates and adds an edge to the graph if no edge with identical source
	 * and target is contained in the graph.
	 * 
	 * In any case, the corresponding edge is returned.
	 * 
	 * @param from
	 * @param to
	 * @return the created or existing edge
	 */
	public IEdge createAndAddEdge(INodeID from, INodeID to);

	/**
	 * Creates and adds an edge to the graph if no edge with identical source
	 * and target is contained in the graph.
	 * 
	 */
	public IEdge createAndAddEdge(INodeID from, INodeID to, boolean allowMultiEdges);

	/**
	 * Adds an element ({@link IEdge} or {@link INode}) to the graph. If the
	 * element is already contained in the graph, the graph remains unchanged.
	 * If an edge is added and the respective nodes are not present in the
	 * graph, they are created.
	 * 
	 * Please note: properties annotated to elements are not considered when
	 * checking for already present elements.
	 * 
	 * @param element
	 * @return false, if the element was already present.
	 */
	public boolean addElement(IElement element);

	/**
	 * @deprecated Only during the reconciliation phase for the new graph API
	 */
	public boolean add(IElement element);

	/**
	 * Adds the given elements to the graph.
	 * 
	 * An implementation should typically call {@link #addElement(IElement)} on
	 * each element of the given sequence.
	 * 
	 */
	public void addElements(Iterable<? extends IElement> elements);

	/**
	 * Adds the given edge to the graph if the graph does not contain an edge
	 * with the the new edge's ID yet.
	 * 
	 * @param edge
	 * 
	 * @return whether the edge could be added (true: edge count has increased,
	 *         false: nothing has changed)
	 * 
	 * @precondition the graph needs to contain the source and target nodes
	 *               already
	 */
	boolean addEdge(IEdge edge);

	/**
	 * Adds all of the given edges to the graph.
	 * 
	 * The result should be equal to adding all contained edges via
	 * {@link #addEdge(IEdge)}
	 * 
	 * @param edges
	 * 
	 * @precondition the graph needs to contain the source and target nodes
	 *               already
	 */
	void addEdges(Iterable<? extends IEdge> edges);

	/**
	 * Adds the given node to the graph
	 *
	 * If the graph already contains a node with the same ID, nothing happens.
	 * 
	 * @param node
	 * @return true if the node has been added, false if the graph already
	 *         contained a node with the same ID
	 * 
	 * @see #containsNode(INode)
	 */
	boolean addNode(INode node);

	/**
	 * Adds all of the given nodes to the graph
	 * 
	 * @param nodes
	 */
	void addNodes(Iterable<? extends INode> nodes);

	/**
	 * Creates a node with the given ID and adds it to the graph using
	 * {@link #addNode(INode)}.
	 * 
	 * @param nodeId
	 * @return the created node.
	 */
	INode createAndAddNode(INodeID nodeId);

	/**
	 * Creates and adds the given node IDs in order
	 * 
	 * @param nodeIds
	 *            the ids of the new nodes
	 * @return the created nodes in the order corresponding to the provided IDs
	 */
	List<INode> createAndAddNodes(Iterable<INodeID> nodeIds);

	/**
	 * Remove the given {@link IElement} from the graph. In case of nodes, all
	 * edges referring to the given node are also deleted. Orphaned nodes, e.g.,
	 * nodes that do not have any edges, are <strong>NOT</strong> removed
	 * automatically.
	 * 
	 * @param element
	 * @return
	 */
	boolean removeElement(IElement element);

	/**
	 * @deprecated
	 * @param element
	 * @return
	 */
	boolean remove(IElement element);

	/**
	 * Removes the node with the given id from the graph. If no such node
	 * exists, nothing happens.
	 * 
	 * @return whether the graph has changed
	 */
	boolean removeNode(final INodeID node);

	/**
	 * Removes the edge with the given id from the graph. If no such edge
	 * exists, nothing happens.
	 * 
	 * @return whether the graph has changed
	 */
	boolean removeEdge(final IEdge edgeID);

	/**
	 * Removes all nodes and edges from the graph
	 */
	void clear();

	/**
	 * Returns whether the element is contained in the graph
	 */
	boolean contains(IElement element);

	/**
	 * True, if the given node is contained in the graph
	 * 
	 * @param nodeId
	 * @return
	 */
	boolean containsNode(INodeID nodeId);

	/**
	 * True, if the given node is contained in the graph
	 * 
	 * @param nodeId
	 * @return
	 */
	boolean containsNode(INode node);

	/**
	 * True, if the given edge is contained in the graph
	 */
	boolean containsEdge(IEdge edge);

	/**
	 * True if there is at least one edge with the given from and to node IDs
	 */
	boolean containsEdge(INodeID from, INodeID to);

	/*
	 * Graph Interface and convenience methods
	 */

	/**
	 * Returns all edges in the graph
	 * 
	 * @return
	 */
	Set<? extends IEdge> getEdges();

	/**
	 * Returns the number of edges in this graph.
	 * 
	 * Should be equal to calling {@link Collection#size()} on the result of
	 * {@link #getEdges()}.
	 * 
	 * @return
	 */
	int getEdgeCount();

	/**
	 * Returns the node with the given ID if existent in the graph. null
	 * otherwise
	 * 
	 * @param node
	 * @return
	 */
	INode getNode(INodeID nodeId);

	/**
	 * Returns some edge (if present) between from and to, null otherwise
	 * 
	 * @param from
	 * @param to
	 * @return edge or null
	 */
	IEdge getEdge(INodeID from, INodeID to);

	/**
	 * Returns all edges (if present) between from and to, an empty collection
	 * otherwise
	 * 
	 * @param from
	 * @param to
	 * @return edge or null
	 */
	Collection<IEdge> getEdges(INodeID from, INodeID to);

	/**
	 * Returns the edge with the given ID.
	 * 
	 * @return edge or null
	 */
	IEdge getEdge(EdgeID edgeID);

	/**
	 * Returns the inverse edge of the edge with the given ID.
	 * 
	 * @return the inverse edge if exists. Otherwise, null.
	 * @throws IllegalArgumentException
	 *             if no edge with the given ID is known
	 * @since 2.5
	 */
	IEdge getInverseEdge(EdgeID edgeId);

	/**
	 * Returns the inverse edge of the edge with the given ID.
	 * 
	 * <b>Note</b> Edges need to be marked <b>explicitly</b> as inverse edge of
	 * one another by using {@link #makeInverseEdges(EdgeID, EdgeID)} or
	 * {@link #makeInverseEdges(IEdge, IEdge)}
	 * 
	 * @return the inverse edge if exists. Otherwise, null.
	 * @throws IllegalArgumentException
	 *             if no edge with the given ID is known
	 * @since 2.5
	 */
	IEdge getInverseEdge(IEdge edge);

	/**
	 * Returns the set of all edges for which
	 * {@link #makeInverseEdges(IEdge, IEdge)} was has been called together with
	 * edge.
	 * 
	 * @param edge
	 * @return
	 */
	Collection<IEdge> getInverseEdges(IEdge edge);

	/**
	 * Marks the given pair of edges as inverse edges of each other
	 *
	 * Only edges that have been passed to
	 * {@link #makeInverseEdges(IEdge, IEdge)} will afterwards be returned by
	 * {@link #getInverseEdge(IEdge)}.
	 * 
	 * @param forwardEdge
	 *            the first edge
	 * @param backwardEdge
	 *            the second edge
	 * @since 2.5
	 */
	void makeInverseEdges(IEdge forwardEdge, IEdge backwardEdge);

	/*
	 * Convenience methods
	 */

	/**
	 * Returns the unmodifiable set of nodes of this graph
	 * 
	 * @return the nodes
	 */
	Set<? extends INode> getNodes();

	/**
	 * Returns the number of nodes in this graph.
	 * 
	 * Should be equal to calling {@link Collection#size()} on the result of
	 * {@link #getNodes()}.
	 * 
	 * @return
	 */
	int getNodeCount();

	/*
	 * Convenience methods
	 */
	/**
	 * Computes subgraph of the currently known local view. This operation
	 * changes the current graph instead of cloning it.
	 */
	Graph getLocalView(INodeID node, int k);

	/**
	 * Computes subgraph of the currently known local view. The clone parameter
	 * specifies whether this Graph instance is changed. If clone is true, a new
	 * Graph instance is generated. Elsewise, this Graph instance is modified.
	 */
	Graph getLocalView(INodeID node, int k, boolean clone);

	/**
	 * For the given node, compute the outgoing edges in the graph
	 * 
	 * @param node
	 *            a node contained in the graph
	 * 
	 * @return
	 */
	Set<IEdge> getOutgoingEdges(INodeID node);

	/**
	 * @deprecated
	 * @param node
	 * @return
	 */
	Set<IEdge> getOutgoingEdges(INode node);

	/**
	 * Returns the number of outgoing links of this node. Should be equal to
	 * calling {@link Set#size()} on the result of
	 * {@link #getOutgoingEdges(Node)}.
	 */
	int getOutdegree(INodeID node);

	/**
	 * For the given node, compute the incoming edges in the graph
	 * 
	 * @param node
	 *            a node contained in the graph
	 * 
	 * @return
	 */
	Set<IEdge> getIncomingEdges(INodeID node);

	/**
	 * @deprecated
	 * @param node
	 * @return
	 */
	Set<IEdge> getIncomingEdges(INode node);

	/**
	 * Returns the number of incoming links to this node. Should be equal to
	 * calling {@link Set#size()} on the result of
	 * {@link #getIncomingEdges(Node)}.
	 */
	int getIndegree(INodeID node);

	/**
	 * Returns the nodes contained in the k-hop neighborhood of the specified
	 * node. This includes the node itself.
	 * 
	 * If the directedNeighborhood flag is true, edge directions are considered
	 * for the neighborhood
	 */
	Set<INodeID> getNeighbors(INodeID node, int k, boolean directedNeighborhood);

	/**
	 * Returns the nodes contained in the k-hop neighborhood of the specified
	 * node. This includes the node itself.
	 * 
	 * If the directedNeighborhood flag is true, edge directions are considered
	 * for the neighborhood
	 */
	Set<INodeID> getNeighbors(INode node, int k, boolean directedNeighborhood);

	/**
	 * returns the strict 1-hop neighbors of the specified node.
	 * 
	 * If the directedNeighborhood flag is true, only outgoing neighbors are
	 * considered. If the directedNeighborhood flag is false, incoming and
	 * outgoing neighbors are considered.
	 * 
	 * @precondition node is contained in the graph
	 */
	Set<INodeID> getNeighbors(INodeID node, boolean directedNeighborhood);

	Set<INodeID> getNeighbors(INode node, boolean directedNeighborhood);

	/**
	 * Returns the union of all graph elemtents, that is, all nodes and edges.
	 */

	/**
	 * Returns the strict 1-hop neighbors of the specified node, considering
	 * nodes reachable via directed edges only.
	 */
	Set<INodeID> getNeighbors(INodeID node);

	/**
	 * Returns the strict 1-hop neighbors of the specified node, considering
	 * nodes reachable via directed edges only.
	 */
	Set<INodeID> getNeighbors(INode node);

	/**
	 * Clones the graph (returns a new view that can be modified). Please note:
	 * the underlying objects (nodes, edges) are <strong>NOT</strong> cloned!
	 * Reason behind that: {@link Graph} contains the structure of nodes and
	 * edges (potentially a subview on a network), while nodes and edges within
	 * these subviews remain immutable.
	 * 
	 * @return
	 */
	Graph clone();

	/**
	 * Returns a sequence of isolated nodes, that is, nodes that have incident
	 * edges.
	 */
	Iterable<INodeID> getIsolatedNodes();

	/**
	 * Returns the set of nodes that have only incoming edges.
	 */
	Iterable<INodeID> getLeafNodes();

	/**
	 * Returns the sequence of nodes that have only outgoing edges.
	 * 
	 * @return
	 */
	Iterable<INodeID> getRootNodes();

	/**
	 * Returns the sequence of node IDs of this graph.
	 */
	Iterable<INodeID> getNodeIds();

	/**
	 * Returns the sequence of node IDs
	 * 
	 * @return
	 */
	Set<EdgeID> getEdgeIds();

	/**
	 * Returns the degree of a node, which is equal to the sum of its indegree
	 * and outdegree
	 * 
	 * @see #getIndegree(INodeID)
	 * @see #getOutdegree(INodeID)
	 */
	int getDegree(INodeID nodeID);

	/**
	 * get all nodes that precede n, i.e., for each returned node m, there is an
	 * edge m -> n
	 * 
	 * @param n
	 * @return all nodes with an edge to n
	 */
	Set<INodeID> getPredecessorNodes(INodeID nodeID);

	/**
	 * Get all edges between nodes of the input set.
	 * 
	 * This provides all edges for the node-induced subgraph
	 * 
	 * @param nodes
	 *            the nodes that induce the set of output edges
	 * @return all edges m -> n where m,n in nodes
	 */
	Collection<IEdge> getEdges(Collection<INodeID> nodes);

	/**
	 * Returns whether the given edges are marked as 'inverse edges'
	 * 
	 * @param forwardEdge
	 * @param backwardEdge
	 * @return
	 */
	boolean areInverseEdges(IEdge forwardEdge, IEdge backwardEdge);

	/**
	 * Inverse operation of {@link #makeInverseEdges(IEdge, IEdge)}
	 * 
	 * @param forwardEdge
	 * @param backwardEdge
	 */
	void destroyInverseEdges(IEdge forwardEdge, IEdge backwardEdge);

}
