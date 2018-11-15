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

package de.tud.kom.p2psim.impl.network.routed.routing.aodv.state;

import java.util.HashSet;
import java.util.Set;

import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;

/**
 * AODV route table entry.
 * 
 * @author Christoph Neumann
 */
public class AodvRouteTableEntry {
	public NetID destination;				// Destination IP Address
	public int destinationSeqNo;			// Destination Sequence Number

	public byte hopCount; // Hop Count (number of hops needed to reach
							// destination)
	public NetID nextHop;					// Next Hop
	public long lifetime = -1;
	public Set<NetID> precursors = new HashSet<NetID>();

	public AodvRouteTableEntry() {}

	/**
	 * Copy constructor.
	 * @param rte the object to copy
	 */
	public AodvRouteTableEntry(AodvRouteTableEntry rte) {
		destination = rte.destination;
		destinationSeqNo = rte.destinationSeqNo;
		hopCount = rte.hopCount;
		nextHop = rte.nextHop;
		lifetime = rte.lifetime;
		precursors = new HashSet<NetID>(rte.precursors);
	}

	public boolean isValid() {
		return lifetime >= Time.getCurrentTime();
	}

	@Override
	public String toString() {
		return "AODV-RTE to " + destination + " nextHop: " + nextHop
				+ " hops: " + hopCount + " destSeq.: " + destinationSeqNo;
	}
}