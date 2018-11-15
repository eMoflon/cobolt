/*
 * Copyright (c) 2005-2011 KOM - Multimedia Communications Lab
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


package de.tud.kom.p2psim.impl.network.modular.st;

import de.tud.kom.p2psim.api.network.NetMessage;
import de.tud.kom.p2psim.impl.network.AbstractNetLayer;
import de.tud.kom.p2psim.impl.network.modular.db.NetMeasurementDB;

/**
 * Strategy to determine whether a given packet shall be dropped or not.
 * 
 * Packet Loss at the Network Layer can have many causes, like interference on the 
 * physical layer or congestion in intermediate routers which causes them to drop
 * subsequent packets.
 * 
 * @author Leo Nobach
 *
 */
public interface PLossStrategy extends ModNetLayerStrategy {

	/**
	 * Returns if the given network message shall be dropped because of network layer
	 * packet loss.
	 * 
	 * @param msg : the message to be dropped or not
	 * @param nlSender : the sender's network layer of the message
	 * @param nlReceiver : the receiver's network layer of the message
	 * @param db : the network measurement database
	 * @return whether the network message shall be dropped or not
	 */
	public boolean shallDrop(NetMessage msg, AbstractNetLayer nlSender,
			AbstractNetLayer nlReceiver, NetMeasurementDB db);

}
