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

package de.tud.kom.p2psim.impl.linklayer;

import de.tud.kom.p2psim.api.linklayer.LinkLayerMessage;
import de.tud.kom.p2psim.api.linklayer.mac.MacAddress;
import de.tudarmstadt.maki.simonstrator.api.Message;

/**
 * Basic implementation of a LinkLayerMessage, this is a message that is
 * transmitted between different LinkLayers (or more precisely between
 * MAC-Layers)
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 01.03.2012
 */
public class DefaultLinkLayerMessage implements LinkLayerMessage {

	private MacAddress sender;

	private MacAddress receiver;

	private Message payload;

	private long size = -1;

	public DefaultLinkLayerMessage(Message payload, MacAddress sender,
			MacAddress receiver) {
		this.payload = payload;
		this.sender = sender;
		this.receiver = receiver;
	}

	@Override
	public long getSize() {
		if (size == -1) {
			size = payload.getSize() + sender.getTransmissionSize()
				+ receiver.getTransmissionSize();
		}
		return size;
	}

	@Override
	public Message getPayload() {
		return payload;
	}

	@Override
	public MacAddress getReceiver() {
		return receiver;
	}

	@Override
	public MacAddress getSender() {
		return sender;
	}

	@Override
	public String toString() {
		return "DefaultLinkLayerMessage from " + sender.toString() + " to "
				+ receiver.toString() + ", carrying " + payload.toString();
	}

}
