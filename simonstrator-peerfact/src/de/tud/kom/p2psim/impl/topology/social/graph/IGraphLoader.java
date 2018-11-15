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
 * Generate or load a graph from an extern source. This Graph can be an extended
 * Graph! This mean, that is contain activity and interactions with values
 * between 0 and 1. In other case, the activity and interactions has the default
 * value of 1.
 * <p>
 * A normal graph can be transfrom with a subclasses of {@link IGraphExtender}
 * to an extended Graph!
 * 
 * @author Christoph Muenker
 * @version 1.0, 07.06.2013
 */
public interface IGraphLoader {
	/**
	 * Returns a directed Graph with the given number of Nodes!
	 * 
	 * @param numberOfNodes
	 *            The number of Nodes/Vertexes which should be have the graph.
	 * @return A directed graph with the number of Nodes
	 */
	public DirectedGraph<SocialNode, SocialEdge> getGraph(int numberOfNodes);

	/**
	 * An extended graph is a graph with interactions and activities not equal
	 * 1!
	 * 
	 * @return <code>true</code> if the graph is an extended Graph, otherwise
	 *         <code>false</code>.
	 */
	public boolean isExtendedGraph();

	/**
	 * Sets the seed, to load the same graph.
	 */
	public void setSeed(long seed);
}
