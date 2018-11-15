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

package de.tud.kom.p2psim.api.network.routing;

import de.tud.kom.p2psim.api.common.SimHostComponent;
import de.tud.kom.p2psim.api.linklayer.LinkLayer;
import de.tud.kom.p2psim.api.linklayer.mac.MacAddress;
import de.tud.kom.p2psim.api.linklayer.mac.PhyType;
import de.tud.kom.p2psim.api.network.NetMessage;
import de.tud.kom.p2psim.api.network.NetProtocol;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetInterface;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetworkComponent.NetInterfaceName;
import de.tudarmstadt.maki.simonstrator.api.component.transport.ConnectivityListener;

/**
 * Interface for a Routing Algorithm on the Network-Layer. A routing algorithm
 * might span multiple PHYs, for example WIFI (infrastructure) + ETHERNET, to
 * provide routing from end-system to end-system or it might just work on one
 * PHY, for example on Bluetooth-devices. Which algorithm to choose depends on
 * the {@link NetProtocol} that is chosen by the TransLayer.
 * 
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 25.02.2012
 */
public interface RoutingAlgorithm extends SimHostComponent, ConnectivityListener {

	public static enum RoutingType {
		/**
		 * The algorithm is adaptive, but tries to maintain a routing table all
		 * the time.
		 */
		PROACTIVE,
		/**
		 * The algorithm does not maintain a routing-table all the time but
		 * instead creates a route as soon as it is requested
		 */
		REACTIVE,
		/**
		 * The algorithm uses static routing tables (entries do not change)
		 */
		STATIC
	}

	/**
	 * Returns the Type of the RoutingAlgorithm
	 * 
	 * @return
	 */
	public RoutingType getType();
	
	public void initialize();

	/**
	 * 
	 * @param net
	 */
	public void setNetInterface(NetInterface net);

	/**
	 * 
	 * @return
	 */
	public NetInterfaceName getNetInterfaceName();

	/**
	 * @deprecated later on, remove PhyType completely in favor of
	 *             NetInterfaceName?
	 * @return
	 */
	public PhyType getPhyType();

	/**
	 * The NetLayer will register itself as a Listener here. It will be notified
	 * as soon as a payload-message arrives that is destined for this NetLayer
	 * 
	 * @param listener
	 */
	public void setMessageListener(RoutingListener listener);
	
	/**
	 * Route a message through the network towards the provided destination.
	 * Uses the information and methods of the {@link LinkLayer} to send
	 * messages to fulfill this task. This is called on the first node of the route towards the destination.
	 * 
	 * @param msg
	 *            beware, this might be a broadcast - you might use it to
	 *            piggyback information but you also have to ensure that it is
	 *            not "routed" but instead just executed by passing it to the
	 *            linkLayer
	 */
	public void route(NetMessage msg);
	
	/**
	 * The NetLayer received a Message that has to be handled by this routing
	 * Algorithm.
	 * 
	 * @param msg
	 * @param phy
	 *            the PHY over which this message arrived. Might be important to
	 *            an Algorithm that spans multiple PHYs
	 * @param lastHop
	 *            the MacAddress of the last Hop of this message
	 */
	public void handleMessage(NetMessage msg, PhyType phy, MacAddress lastHop);

}
