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

package de.tud.kom.p2psim.impl.topology.social.graph;

import edu.uci.ics.jung.graph.DirectedGraph;

/**
 * Extends the Graph, with the information of activity and interactions.
 * Activity is a property of a Node/Vertex. The interaction is a property of a
 * Edge.
 * 
 * @author Christoph Muenker
 * @version 1.0, 07.06.2013
 */
public interface IGraphExtender {
	/**
	 * Modify the given graph and return this. The Graph will be prepared with
	 * activities at the nodes and interactions at the edges!
	 * <p>
	 * Please note, that the given reference of this graph will be manipulated!
	 * 
	 * @param graph
	 *            The graph, which should be prepared
	 * @return The extend graph.
	 */
	public DirectedGraph<SocialNode, SocialEdge> extendGraph(
			DirectedGraph<SocialNode, SocialEdge> graph);
}
