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

package de.tud.kom.p2psim.impl.network.routed.routing.aodv.message;

import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;

/**
 * AODV Route Reply (RREP) Message
 * 
 * @author Christoph Neumann
 */
public class RouteReplyMessage extends AodvMessage {

	private byte hopCount;
	private int destinationSeqNo;
	private long lifetime;	// simulator operates with long values
	private NetID destination, originator;
	
	/*
	 * Creates a new RREP message.
	 */
	public RouteReplyMessage() {
		super();
	}

	@Override
	public long getSize() {
		// 20 bytes (12 + 4 + 4) as defined in RFC 3561 section 5.2 for usage with
		// IPv4. But since maybe this is to be used with IPv6 some day,
		// the length of an ip address is considered here.
		return 12 + destination.getTransmissionSize() + originator.getTransmissionSize();
	}

	public NetID getDestination() {
		return destination;
	}

	public void setDestination(NetID destination) {
		this.destination = destination;
	}

	public int getDestinationSeqNo() {
		return destinationSeqNo;
	}

	public void setDestinationSeqNo(int destinationSeqNo) {
		this.destinationSeqNo = destinationSeqNo;
	}

	public byte getHopCount() {
		return hopCount;
	}

	public void setHopCount(byte hopCount) {
		this.hopCount = hopCount;
	}

	public long getLifetime() {
		return lifetime;
	}

	public void setLifetime(long lifetime) {
		this.lifetime = lifetime;
	}

	public NetID getOriginator() {
		return originator;
	}

	public void setOriginator(NetID originator) {
		this.originator = originator;
	}

	@Override
	public String toString() {
		return "RREP: originator: " + originator.toString() + " hops: "
				+ hopCount + " dest: " + destination + " destSeq: "
				+ destinationSeqNo + " lifetime: " + lifetime;
	}
}
