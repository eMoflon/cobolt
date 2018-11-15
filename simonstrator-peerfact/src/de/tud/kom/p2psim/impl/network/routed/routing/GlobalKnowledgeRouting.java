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

package de.tud.kom.p2psim.impl.network.routed.routing;

import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.linklayer.mac.Link;
import de.tud.kom.p2psim.api.linklayer.mac.MacAddress;
import de.tud.kom.p2psim.api.linklayer.mac.MacLayer;
import de.tud.kom.p2psim.api.linklayer.mac.PhyType;
import de.tud.kom.p2psim.api.network.NetMessage;
import de.tud.kom.p2psim.api.topology.views.TopologyView;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;

/**
 * This routing-algorithm <i>cheats</i> in that it uses the Information provided
 * by the {@link TopologyView} to forward messages along a path to the
 * destination that is considered <i>optimal</i> by the View.
 * 
 * It is nice for functional testing or for a baseline to compare other routing
 * algorithms against.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 04.03.2012
 */
public class GlobalKnowledgeRouting extends AbstractRoutingAlgorithm {

	private PhyType phy;

	private TopologyView view;

	public GlobalKnowledgeRouting(SimHost host, PhyType phy) {
		super(host, RoutingType.REACTIVE, phy.getNetInterfaceName());
		this.phy = phy;
	}

	@Override
	public PhyType getPhyType() {
		return phy;
	}

	@Override
	public void initialize() {
		MacLayer mac = getHost().getLinkLayer().getMac(phy);
		if (mac == null) {
			throw new AssertionError(
					"For GlobalKnowledgeRouting you need to specify a TopologyView of the same PHY");
		}
		view = getHost().getLinkLayer().getMac(phy).getTopologyView();
	}

	@Override
	protected void start() {
		// not interested
	}

	@Override
	protected void stop() {
		// not interested
	}

	@Override
	public void handleMessage(NetMessage msg, PhyType phy, MacAddress lastHop) {
		if (msg.getReceiver().equals(getNetID())
				|| msg.getReceiver().equals(
						getNetInterface().getBroadcastAddress())) {
			notifyNetLayer(msg);
			return;
		}

		MacAddress sender = getHost().getLinkLayer().addressResolution(
				msg.getSender(), phy);
		MacAddress receiver = getHost().getLinkLayer().addressResolution(
				msg.getReceiver(), phy);
		MacAddress currentHop = getHost().getLinkLayer().getMac(phy)
				.getMacAddress();
		Link link = view.getBestNextLink(sender, lastHop, currentHop, receiver);
		if (link == null || !link.isConnected()) {
			messageDropped(DropReason.NO_PATH_FOUND, msg);
		} else {
			NetID nextHop = view.getMac(link.getDestination()).getHost()
					.getNetworkComponent().getByName(phy.getNetInterfaceName())
					.getLocalInetAddress();
			forwardNetMessage(msg, nextHop, phy);
		}
	}

	@Override
	public void route(NetMessage msg) {
		if (msg.getReceiver().equals(getNetInterface().getBroadcastAddress())) {
			forwardNetMessage(msg, msg.getReceiver(), phy);
		} else {
			handleMessage(msg, phy, getHost().getLinkLayer().getMac(phy)
					.getMacAddress());
		}
	}

}
