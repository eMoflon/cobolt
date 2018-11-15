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

package de.tud.kom.p2psim.api.linklayer.mac;

import de.tud.kom.p2psim.api.topology.views.TopologyView;

/**
 * This replaces the Channel-concept. A Link is a connection between two hosts
 * in a given PHY-Medium. It is unidirectional and holds properties such as the
 * bandwidth and drop-rate. Links are created/provided by the Topology and the
 * corresponding {@link TopologyView} upon request of the LinkLayer (MAC).
 * 
 * We want full transparency from the representation of the network (graph,
 * db...).
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 21.02.2012
 */
public interface Link {

	/**
	 * Source
	 * 
	 * @return
	 */
	public MacAddress getSource();

	/**
	 * Destination
	 * 
	 * @return
	 */
	public MacAddress getDestination();

	/**
	 * If we send in the MAC, we decide based on this method whether the
	 * destination is reachable or not. This does not take into account if the
	 * mac is offline, as this is done in the MAC itself. However, if source ==
	 * destination, this method HAS to return false!
	 * 
	 * @return
	 */
	public boolean isConnected();

	/**
	 * Probability that a frame is dropped on this link. This might result in a
	 * retransmission (for example in WIFI). The drop-probability could depend
	 * on the length of the link (distance of the hosts) or the density of the
	 * neighborhood. All relevant information is contained in the topology so we
	 * will decide there which drop rate we assume for the link.
	 * 
	 * @return p (probability that <b>one frame</b> is dropped)
	 */
	public double getDropProbability();

	/**
	 * Bandwidth we can assume on this link in bit/s - we use this bandwidth and
	 * calculate the real number of bytes that we need to transmit, including
	 * redundancy and retransmits, to get the actual bandwidth visible to the
	 * user.
	 * 
	 * <b>This is in bit/s</b>
	 * 
	 * @param isBroadcast
	 *            for a broadcast the BW might be slower, as a more robust
	 *            scheme might be used
	 * 
	 * @return BW in bit/s as defined in Rate (api)
	 */
	public long getBandwidth(boolean isBroadcast);

	/**
	 * Latency on a link (for the ethernet-scenario "cloud" this might be
	 * GNP-based), wireless latency is close to 0 (very close)
	 * 
	 * @return latency in simulation units
	 */
	public long getLatency();

	/**
	 * Maximum number of bytes we can transmit in one frame. This will be
	 * evaluated against the Packet size. If the packet size exceeds MTU, we
	 * will assume there is some partitioning mechanism and we will evaluate the
	 * drop-rate for each "virtual" frame (ie. no. of frames + no. of
	 * retransmissions)
	 * 
	 * @return MTU in byte
	 */
	public int getMTU();

}
