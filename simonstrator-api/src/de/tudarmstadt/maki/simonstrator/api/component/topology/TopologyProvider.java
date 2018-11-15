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

package de.tudarmstadt.maki.simonstrator.api.component.topology;

import java.util.Set;

import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INode;
import de.tudarmstadt.maki.simonstrator.api.component.HostComponent;

/**
 * This interface is intended to be implemented by HostComponents that define a
 * topology.
 * 
 * Assumptions made: Each instance of a TopologyProvider of the same type
 * corresponds to exactly one node in a topology graph. Each instance of a
 * TopologyProvider is able to provide a 1-local view of its surround topology.
 * 
 * @author michael.stein
 * @version 1.0, 02.11.2014
 * 
 */
public interface TopologyProvider extends HostComponent {
	/**
	 * 
	 * @return the node that represents this HostComponent
	 */
	public INode getNode(TopologyID identifier);

	/**
	 * This method provides the 1-hop neighborhood of this TopologyProvider
	 * 
	 * @param topologyIdentifier
	 */
	public Set<IEdge> getNeighbors(TopologyID topologyIdentifier);

	/**
	 * This method provides the complete local view of this topology provider,
	 * i.e., it is not bounded by the 1-hop neighborhood. It depends on the
	 * concrete implementation how large the local view is.
	 */
	public Graph getLocalView(TopologyID topologyIdentifier);

	/**
	 * 
	 * @return the types of topologies provided by the component
	 */
	public Iterable<TopologyID> getTopologyIdentifiers();

}
