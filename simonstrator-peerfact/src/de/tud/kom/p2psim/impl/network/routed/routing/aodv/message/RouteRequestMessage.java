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
 * AODV Route Request (RREQ) Message
 * 
 * @author Christoph Neumann
 */
public class RouteRequestMessage extends AodvMessage {

	private byte hopCount;

	private int rreqId;
	private int destinationSeqNo, originatorSeqNo;
	private NetID destination, originator;

	// the unknown sequence number flag is implemented as
	// sequence number AodvConstants.UNKNOWN_SEQ_NO
	
	/*
	 * Creates a new RREQ message.
	 */
	public RouteRequestMessage() {
		super();
	}

	@Override
	public long getSize() {
		// 24 bytes (16 + 4 + 4) as defined in RFC 3561 section 5.1 for usage with
		// IPv4. But since maybe this is to be used with IPv6 some day,
		// the length of an ip address is considered here.
		return 16 + destination.getTransmissionSize() + originator.getTransmissionSize();
	}

	@Override
	public String toString() {
		return "RREQ: dest: " + destination.toString() + " orig: "
				+ originator.toString() + " hop: " + hopCount + " TTL: "
				+ getTtl() + " destSeq: " + destinationSeqNo + " origSeq: "
				+ originatorSeqNo;
	}

	/**
	 * RREQ messages are uniquely identified by their rreqId
	 * in conjunction with their originator. See RFC Section 5.1.
	 * 
	 * @param obj object to check
	 * @return true if equal
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final RouteRequestMessage other = (RouteRequestMessage) obj;
		if (this.rreqId != other.rreqId) {
			return false;
		}
		if (this.originator != other.originator && (this.originator == null || !this.originator.equals(other.originator))) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 37 * hash + this.rreqId;
		hash = 37 * hash + (this.originator != null ? this.originator.hashCode() : 0);
		return hash;
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

	public NetID getOriginator() {
		return originator;
	}

	public void setOriginator(NetID originator) {
		this.originator = originator;
	}

	public int getOriginatorSeqNo() {
		return originatorSeqNo;
	}

	public void setOriginatorSeqNo(int originatorSeqNo) {
		this.originatorSeqNo = originatorSeqNo;
	}

	public int getRreqId() {
		return rreqId;
	}

	public void setRreqId(int rreqId) {
		this.rreqId = rreqId;
	}
}
