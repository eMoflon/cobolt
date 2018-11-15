/*
 * Copyright (c) 2005-2010 KOM - Multimedia Communications Lab
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

import java.util.Collection;

import de.tudarmstadt.maki.simonstrator.api.common.graph.INode;

/**
 * This class extends {@link TopologyProvider} with methods for manipulating a
 * topology (e.g., adding or removing neighbors in a topology)
 *
 * @author Michael Stein
 */
public interface AdaptableTopologyProvider extends TopologyProvider {

	/**
	 * Adds the given node as a neighbor for the specified topology
	 *
	 * @param topologyIdentifier
	 * @param node
	 */
	public void addNeighbor(TopologyID topologyIdentifier, INode node);

	/**
	 * Removes the given neighbor node for the specified topology. Has no effect if
	 * the node is not a neighbor
	 *
	 * @param topologyIdentifier
	 * @param node
	 */
	public void removeNeighbor(TopologyID topologyIdentifier, INode node);

	/**
	 * Returns the collection of all possible interactions which may be done in
	 * order to adapt the topology. Returns only those actions that can later be
	 * conducted by this component via the performOperation() method
	 *
	 * @param topologyIdentifier
	 * @param node
	 */
	public Collection<OperationalEdge> getPossibleEdgeOperations(
			TopologyID topologyIdentifier);

	/**
	 * Performs the given edge operation. Must be one of the operation returned by
	 * getPossibleEdgeOperations()
	 *
	 * @param topologyidentifier
	 * @param edgeOperation
	 */
	public void performOperation(TopologyID topologyidentifier,
			OperationalEdge edgeOperation);

}
