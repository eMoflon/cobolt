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

package de.tud.kom.p2psim.impl.network.realNetworking;

import java.util.HashMap;

import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.impl.network.AbstractNetLayerFactory;
import de.tud.kom.p2psim.impl.simengine.Simulator;
import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;

public class RealNetworkingNetLayerFactory extends AbstractNetLayerFactory {
	
	/** The routing table. */
	private RealNetworkRoutingTable routingTable;
	
	/** The net layer manager. */
	private static RealNetworkingNetLayerManager netLayerManager = RealNetworkingNetLayerManager.getInstance();

	/** The hostID to netIP translation hashmap. */
	public static HashMap<Long, NetID> HOST_ID_TO_NETIP_TRANSLATION = new HashMap<Long, NetID>();
		
	/**
	 * Instantiates a new real networking net layer factory.
	 */
	public RealNetworkingNetLayerFactory() {
		routingTable = RealNetworkRoutingTable.getInstance();
		netLayerManager.goOnline();
		Simulator.getScheduler().setSimulationSpeedLocked(true);
		Simulator.getScheduler().setRealTime(true);
	}
	
	/* (non-Javadoc)
	 * @see de.tud.kom.p2psim.api.common.ComponentFactory#createComponent(de.tud.kom.p2psim.api.common.Host)
	 */
	@Override
	public RealNetworkingNetLayer createComponent(Host phost) {

		SimHost host = (SimHost) phost;

		NetID id = HOST_ID_TO_NETIP_TRANSLATION.get(host.getHostId());	
		final RealNetworkingNetLayer newHost = new RealNetworkingNetLayer(host,
				id);

		if( routingTable.getRealInetAddrAndPortForVirtualIP(id) == null ) {
			Monitor.log(RealNetworkingNetLayerFactory.class, Level.WARN,
					"Host not known to routing table. Not created.");
			return null;
		}
				
		return newHost;
		
	}

}
