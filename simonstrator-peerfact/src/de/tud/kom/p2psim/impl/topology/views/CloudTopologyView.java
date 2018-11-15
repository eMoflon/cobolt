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

package de.tud.kom.p2psim.impl.topology.views;

import java.util.ArrayList;
import java.util.List;

import de.tud.kom.p2psim.api.linklayer.mac.Link;
import de.tud.kom.p2psim.api.linklayer.mac.MacAddress;
import de.tud.kom.p2psim.api.linklayer.mac.MacLayer;
import de.tud.kom.p2psim.api.linklayer.mac.PhyType;
import de.tud.kom.p2psim.api.network.BandwidthImpl;
import de.tud.kom.p2psim.api.topology.obstacles.ObstacleModel;
import de.tud.kom.p2psim.api.topology.waypoints.WaypointModel;
import de.tudarmstadt.maki.simonstrator.api.util.XMLConfigurableConstructor;

/**
 * A very simple topology view that provides connectivity between each
 * participating host. Mainly for testing purposes or for simulations that do
 * not care about very accurate Net-Level measurements.
 * 
 * This behaves in exactly the same way as the ModularNetLayer does - it
 * simulates end-to-end connections of hosts. In most cases it makes sense to
 * provide latency and bandwidth configurations that differ from the ones in
 * {@link PhyType}, as it is done in the ModularNetLayer-Strategies.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 29.02.2012
 */
public class CloudTopologyView extends AbstractTopologyView<DefaultLink> {

	private List<MacAddress> allMacs = new ArrayList<MacAddress>();

	public CloudTopologyView(PhyType phy) {
		super(phy);
		setHasRealLinkLayer(false);
	}

	@XMLConfigurableConstructor({ "phy" })
	public CloudTopologyView(String phy) {
		this(PhyType.ETHERNET);
		setPhy(phy);
	}

	@Override
	protected void addedMac(MacLayer mac) {
		allMacs.add(mac.getMacAddress());
	}


	@Override
	public Link getBestNextLink(MacAddress source, MacAddress lastHop,
			MacAddress currentHop, MacAddress destination) {
		return getLinkBetween(currentHop, destination);
	}

	@Override
	protected CloudLink createLink(MacAddress source, MacAddress destination) {
		/*
		 * All are connected, and if source == destination the Link itself will
		 * set isConnected to false.
		 */
		return new CloudLink(source, destination, true,
				determineLinkDropProbability(source, destination),
				determineLinkBandwidth(source, destination), getPhyType()
						.getDefaultMTU());
	}
	
	@Override
	protected long determineLinkBandwidth(MacAddress source,
			MacAddress destination) {
		BandwidthImpl sourceBandwidth = getMac(source).getMaxBandwidth();
		return sourceBandwidth.getUpBW();
	}

	@Override
	protected List<MacAddress> updateNeighborhood(MacAddress source) {
		List<MacAddress> neighbors = new ArrayList<MacAddress>(allMacs);
		neighbors.remove(source);
		return neighbors;
	}

	@Override
	protected void updateOutdatedLink(DefaultLink link) {
		// will not happen
	}

	@Override
	public void changedWaypointModel(WaypointModel model) {
		//
	}

	@Override
	public void changedObstacleModel(ObstacleModel model) {
		//
	}

	/**
	 * A link for the cloud-topology, that supports updates of latency/jitter
	 * (as we now consider End-to-End Transport connections rather than just
	 * one-hop MAC-links)
	 * 
	 * @author Bjoern Richerzhagen
	 * @version 1.0, May 7, 2014
	 */
	public class CloudLink extends DefaultLink {

		public CloudLink(MacAddress source, MacAddress destination,
				boolean isConnected, double dropProbability, long bandwidth,
				int mtu) {
			super(source, destination, isConnected, dropProbability, bandwidth,
					-1, mtu);
		}

		@Override
		public long getLatency() {
			return determineLinkLatency(getSource(), getDestination());
		}

	}

}
