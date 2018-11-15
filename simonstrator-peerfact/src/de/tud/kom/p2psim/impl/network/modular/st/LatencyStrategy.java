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
 * This strategy defines the message propagation delay of a given network message. This is the
 * total time the network message is delayed minus the jitter, minus the time the message stays in
 * sender and receiver queues defined by the traffic control strategy (if the 
 * traffic control mechanism delays the messages at all).
 * 
 * 
 * @author Leo Nobach
 *
 */
public interface LatencyStrategy extends ModNetLayerStrategy {

	/**
	 * <p>
	 * Returns the message propagation delay in simulation time units. This is the
	 * total time the network message is delayed minus the jitter, minus the time the message stays in
	 * sender and receiver queues defined by the traffic control strategy (if the 
	 * traffic control mechanism delays the messages at all).
	 * </p>
	 * <p>
	 * Note that the message to be delayed may be split into multiple IP fragments, please
	 * implement an appropriate propagation delay.
	 * </p>
	 * 
	 * @param msg , the message to be delayed
	 * @param nlSender , the sender's network layer
	 * @param nlReceiver , the receiver's network layer
	 * @param db , the measurement database
	 * 
	 * @return the message propagation delay in simulation time units. 
	 */
	public long getMessagePropagationDelay(NetMessage msg,
			AbstractNetLayer nlSender, AbstractNetLayer nlReceiver,
			NetMeasurementDB db);
	
}
