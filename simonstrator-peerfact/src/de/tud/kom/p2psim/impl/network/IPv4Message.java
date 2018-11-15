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


package de.tud.kom.p2psim.impl.network;

import de.tud.kom.p2psim.api.network.NetProtocol;
import de.tudarmstadt.maki.simonstrator.api.Message;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;

/**
 * Implements the NetworkMessage-Interface for the complex network model.
 * 
 * @author Sebastian Kaune
 */
public class IPv4Message extends AbstractNetMessage {

	/** Packet header size. */
	public static final int HEADER_SIZE = 20;

	public static final int MAX_IP_PACKET_SIZE = 65535;

	public static final int MTU_SIZE = 1500;

	private int noOfFragments = 1;

	private long size = 0;

	public IPv4Message(Message payload, NetID receiver, NetID sender) {
		super(payload, receiver, sender, NetProtocol.IPv4);

		noOfFragments = (int) Math.ceil((double) getPayload().getSize()
				/ (double) (MTU_SIZE - HEADER_SIZE));
		size = getPayload().getSize() + HEADER_SIZE * noOfFragments;	//FIXME: why "* noOfFragments"?

	}

	public long getSize() {
		return size;
	}

	public int getNoOfFragments() {
		return noOfFragments;
	}

	@Override
	public String toString() {
		return "[ IP " + super.getSender() + " -> " + super.getReceiver()
				+ " | size: " + getSize() + " ( " + noOfFragments + "*"
				+ HEADER_SIZE + " + " + getPayload().getSize()
				+ " ) bytes | payload: " + getPayload() + " ]";
	}

}