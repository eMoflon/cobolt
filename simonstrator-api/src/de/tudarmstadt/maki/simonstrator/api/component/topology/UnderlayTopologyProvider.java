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

package de.tudarmstadt.maki.simonstrator.api.component.topology;

import de.tudarmstadt.maki.simonstrator.api.component.network.NetworkComponent.NetInterfaceName;

/**
 * This {@link TopologyProvider} returns graphs based on the physical underlays
 * of nodes. In case the underlay is based on wireless communication, the range
 * can be taken into account. Please note, that the provider operates on global
 * knowledge, it maintains only one graph. The graph can be obtained by any of
 * the components, but there is no need to further aggregate local views, as
 * they will all be the same for a given PHY.
 * 
 * @author Bjoern Richerzhagen
 *
 */
public interface UnderlayTopologyProvider extends TopologyProvider {

	/**
	 * Returns a topology ID for the given {@link NetInterfaceName} (i.e., the
	 * physical interface). Can additionally be filtered to only contain nodes
	 * that are online (which might be the most common use case).
	 * 
	 * In a wireless setting, this method will rely on the neighbors as reported
	 * by the respective underlay (1-hop) to create the graph.
	 * 
	 * @param netName
	 *            the physical interface to be considered
	 * @param onlyOnline
	 *            true, if only online nodes are to be contained
	 * @return
	 */
	public TopologyID getTopologyID(NetInterfaceName netName, boolean onlyOnline);

	/**
	 * Returns a topologyID for the given {@link NetInterfaceName}, based on the
	 * passed communication range, completely IGNORING the real range of the
	 * interface!
	 * 
	 * @param netName
	 * @param onlyOnline
	 * @param range
	 *            an assumed communication range
	 * @return
	 */
	public TopologyID getTopologyID(NetInterfaceName netName,
			boolean onlyOnline, double range);

}
