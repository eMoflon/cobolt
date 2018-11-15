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



package de.tud.kom.p2psim.api.network;


/**
 * The <code>NetLatencyModel</code> is used to calculate message transmission
 * times. Depending on the degree of abstraction, computational cost and memory
 * consumption, a various number of different approaches can be provided such as
 * analytical functions, n-dimensional coordinate-based approaches (Global
 * Positioning Networking) or network topologies.
 * 
 * @author Sebastian Kaune
 * @author Konstantin Pussep
 * @version 3.0, 11/29/2007
 * 
 */
public interface NetLatencyModel {

	/**
	 * This method is invoked by a given Subnet and calculates the network
	 * transmission time for sending a message from a NetLayer of Host A
	 * (sender) to the NetLayer of Host B (receiver). It is recommended that the
	 * resulting transmission time is adapted to the simulation units used by
	 * the simulation framework. For instance, latency = calculated value *
	 * Simulator.MILLISECOND_UNIT.
	 * 
	 * @param sender
	 *            the sending NetLayer
	 * @param receiver
	 *            the receiving NetLayer
	 * @return time interval required to transmit a message.
	 */
	public abstract long getLatency(NetLayer sender, NetLayer receiver);

}
