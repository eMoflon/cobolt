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
import de.tud.kom.p2psim.impl.network.modular.ModularNetLayer;
import de.tud.kom.p2psim.impl.network.modular.db.NetMeasurementDB;

/**
 * <p>
 * Determines the jitter that shall be used for a given network message.
 * </p>
 * <p>
 * Jitter is the variation of the message propagation delay in a network.
 * In the Modular Net Layer, the total time the message is delayed is defined as the message propagation delay
 * (defined by the LatencyStrategy) plus the jitter (defined by this class), plus the time the message stays in
 * sender and receiver queues defined by the traffic control strategy (if the 
 * traffic control mechanism delays the messages at all).
 * </p>
 * 
 * @author Leo Nobach
 *
 */
public interface JitterStrategy extends ModNetLayerStrategy {

	/**
	 * Returns the jitter that shall be used for the given network message.
	 * 
	 * @param cleanMsgPropagationDelay : the message propagation delay that was previously calculated by the LatencyStrategy
	 * @param msg : the network message that shall be jittered
	 * @param nlSender : the network layer of the sender
	 * @param nlReceiver : the network layer of the receiver
	 * @param db : the network measurement database (may be null if none set)
	 * @return the jitter in simulation time units
	 */
	public long getJitter(long cleanMsgPropagationDelay, NetMessage msg,
			ModularNetLayer nlSender, ModularNetLayer nlReceiver,
			NetMeasurementDB db);
	
	
}
