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
import de.tud.kom.p2psim.api.linklayer.mac.MacAddress;
import de.tud.kom.p2psim.api.linklayer.mac.PhyType;
import de.tud.kom.p2psim.api.network.NetMessage;

/**
 * This uses global knowledge to determine the MacAddr of a NetID and will then
 * just try to send. If the receiver is within the 1-hop-range delivery will
 * succeed, otherwise it will fail.
 * 
 * This is basically relying on other Application-Level routing algorithms.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 29.02.2012
 */
public class OneHopRouting extends AbstractRoutingAlgorithm {

	private PhyType phy;

	public OneHopRouting(SimHost host, PhyType phy) {
		super(host, RoutingType.STATIC, phy.getNetInterfaceName());
		this.phy = phy;
	}

	@Override
	public PhyType getPhyType() {
		return phy;
	}

	@Override
	public void handleMessage(NetMessage msg, PhyType phy, MacAddress lastHop) {
		/*
		 * In this algorithm, it can only be the final NetMessage we wanted to
		 * deliver via this one hop
		 */
		notifyNetLayer(msg);
	}

	@Override
	public void initialize() {
		// not interested
	}

	@Override
	public void route(NetMessage msg) {
		forwardNetMessage(msg, msg.getReceiver(), phy);
	}

	@Override
	protected void start() {
		// not interested
	}

	@Override
	protected void stop() {
		// not interested
	}

}
