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

package de.tudarmstadt.maki.simonstrator.api;

import de.tudarmstadt.maki.simonstrator.api.common.graph.BasicGraph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.DirectedEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.EdgeID;
import de.tudarmstadt.maki.simonstrator.api.common.graph.GenericGraphElementProperties;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INode;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Node;
import de.tudarmstadt.maki.simonstrator.api.component.ComponentNotAvailableException;
import de.tudarmstadt.maki.simonstrator.api.component.core.GraphComponent;

/**
 * The convenient access method to all the Graph-Component interfaces. The
 * respective graph component could/should be implemented by the runtime or a
 * "graph-utils"-project.
 * 
 * @author Bjoern Richerzhagen
 *
 */
public final class Graphs {

	/**
	 * 
	 */
	public static GraphComponent bindedGraph = null;

	private static GraphComponent getGraphComponent() {
		if (bindedGraph == null) {
			try {
				bindedGraph = Binder.getComponent(GraphComponent.class);
			} catch (ComponentNotAvailableException e) {
				System.err
						.println("Simonstrator-API INFO: The default implementation of the GraphComponent is used.");
				bindedGraph = new DefaultGraphComponent();
			}
		}
		return bindedGraph;
	}

	/**
	 * Returns a new Graph-Instance created by the GraphComponent (or its
	 * default implementation).
	 * 
	 * @return
	 */
	public static Graph createGraph() {
		return getGraphComponent().createGraph();
	}

	/**
	 * The default implementation of a GraphComponent, relying on the API
	 * classes as defined in api.common.graph.
	 * 
	 * @author Bjoern Richerzhagen
	 *
	 */
	private static class DefaultGraphComponent implements GraphComponent {

		@Override
		public Graph createGraph() {
			return new BasicGraph();
		}

	}

	public static Graph createGraph(Iterable<? extends INode> nodes,
			Iterable<? extends IEdge> edges) {
		Graph graph = getGraphComponent().createGraph();
		graph.addNodes(nodes);
		graph.addEdges(edges);
		return graph;
	}

	public static IEdge createDirectedWeightedEdge(final INodeID source,
			final INodeID target, final Double weight) {
		DirectedEdge edge = new DirectedEdge(source, target);
		if (weight != null && !Double.isNaN(weight)) {
			edge.setProperty(GenericGraphElementProperties.WEIGHT, weight);
		}
		return edge;
	}

	public static IEdge createDirectedWeightedEdge(final INode source,
			final INode target, final double weight) {
		return createDirectedWeightedEdge(source.getId(), target.getId(),
				weight);
	}

	public static IEdge createDirectedWeightedEdge(final INodeID source,
			final INodeID target, final EdgeID edgeId, final double weight) {
		final DirectedEdge directedEdge = new DirectedEdge(source, target,
				edgeId);
		directedEdge.setProperty(GenericGraphElementProperties.WEIGHT, weight);
		return directedEdge;
	}

	public static IEdge createDirectedEdge(INode n1, INode n2) {
		return new DirectedEdge(n1.getId(), n2.getId());
	}

	public static IEdge createDirectedEdge(INodeID sourceId, INodeID targetId) {
		return new DirectedEdge(sourceId, targetId);
	}

	public static IEdge createDirectedEdge(EdgeID edgeId, INodeID sourceId, INodeID targetId) {
		return new DirectedEdge(sourceId, targetId, edgeId);
	}

	public static Node createNode(long id) {
		return new Node(INodeID.get(id));
	}

	public static Node createNode(String id) {
		return new Node(INodeID.get(id));
	}

	public static INode createNode(INodeID id) {
		return new Node(id);
	}

}
