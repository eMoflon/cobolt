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


package de.tud.kom.p2psim.impl.transport;

import de.tud.kom.p2psim.api.transport.TransProtocol;
import de.tudarmstadt.maki.simonstrator.api.Message;

/**
 * This class is the default implementation of a transport layer message of type
 * UDP.
 * 
 * Note: If you implement a UDP message type on your own, be sure to assign the
 * protocol type <code>TransProtocol.UDP</code>.
 */
public class UDPMessage extends AbstractTransMessage {

	/**
	 * Create a new UDP message
	 * 
	 * @param payload
	 * @param senderPort
	 * @param receiverPort
	 * @param commId
	 */
	public UDPMessage(Message payload, int senderPort, int receiverPort,
                      int commId, boolean isReply) {
        super(TransProtocol.UDP, payload, senderPort, receiverPort, commId,
                isReply);
    }

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return ("[ UDP " + this.getSenderPort() + " -> "
				+ this.getReceiverPort() + " | size: "
				+ TransProtocol.UDP.getHeaderSize() + " + "
				+ this.getPayload().getSize() + " bytes | payload-hash: "
				+ this.getPayload().hashCode() + " ]");
	}

}
