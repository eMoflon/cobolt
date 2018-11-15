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

import de.tudarmstadt.maki.simonstrator.api.Graphs;

/**
 * This class provides a fluent interface for constructing graphs
 * 
 * Start by calling {@link #create()}.
 * 
 * Then use methods for adding nodes ({@link #n(String)} and edges
 * ({@link #e(String, String)}).
 * 
 * Finally, create the graph by calling {@link #done()}.
 * 
 * @author Roland Kluge - Initial implementation
 *
 */
public class GraphBuilder {

	private Graph graph;
	private INode mostRecentNode;
	private IEdge mostRecentEdge;

	private GraphBuilder() {
		this.graph = Graphs.createGraph();
	}

	public static GraphBuilder create() {
		return new GraphBuilder();
	}

	public Graph done() {
		return this.graph;
	}

	public GraphBuilder n(final String nodeId) {
		final Node node = Graphs.createNode(nodeId);
		this.graph.addNode(node);
		this.mostRecentNode = node;
		return this;
	}

	public GraphBuilder e(final String sourceId, String targetId) {
		final DirectedEdge edge = new DirectedEdge(INodeID.get(sourceId), INodeID.get(targetId));
		this.graph.addEdge(edge);
		this.mostRecentEdge = edge;
		return this;
	}

	public GraphBuilder e(final String sourceId, final String targetId, final String edgeId) {
		final INodeID sourceNodeId = INodeID.get(sourceId);
		final INodeID targetNodeId = INodeID.get(targetId);
		if (!this.graph.containsNode(sourceNodeId)) {
			this.graph.createAndAddNode(sourceNodeId);
		}
		if (!this.graph.containsNode(targetNodeId)) {
			this.graph.createAndAddNode(targetNodeId);
		}
		final DirectedEdge edge = new DirectedEdge(sourceNodeId, targetNodeId, EdgeID.get(edgeId));
		this.graph.addEdge(edge);
		this.mostRecentEdge = edge;
		return this;
	}

	public GraphBuilder e(final String sourceId, final String targetId, final String edgeId, final double weight) {
		INodeID sourceNodeId = INodeID.get(sourceId);
		INodeID targetNodeId = INodeID.get(targetId);
		if (!this.graph.containsNode(sourceNodeId)) {
			this.graph.createAndAddNode(sourceNodeId);
		}
		if (!this.graph.containsNode(targetNodeId)) {
			this.graph.createAndAddNode(targetNodeId);
		}
		final IEdge edge = Graphs.createDirectedWeightedEdge(INodeID.get(sourceId), INodeID.get(targetId),
				EdgeID.get(edgeId), weight);
		this.graph.addEdge(edge);
		this.mostRecentEdge = edge;
		return this;
	}

	public <T> GraphBuilder pn(final GraphElementProperty<T> property, final T value) {
		if (null == this.mostRecentNode)
			throw new IllegalStateException("Cannot set property: No node added, yet");

		this.mostRecentNode.setProperty(property, value);
		return this;
	}

	public <T> GraphBuilder pe(final GraphElementProperty<T> property, final T value) {
		if (null == this.mostRecentEdge)
			throw new IllegalStateException("Cannot set property: No edge added, yet");

		this.mostRecentEdge.setProperty(property, value);
		return this;
	}

	public IEdge getMostRecentEdge() {
		return mostRecentEdge;
	}

	public INode getMostRecentNode() {
		return mostRecentNode;
	}
}
