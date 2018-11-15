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

package de.tud.kom.p2psim.impl.linklayer.mac.wifi;

import de.tud.kom.p2psim.api.linklayer.LinkLayerMessage;
import de.tud.kom.p2psim.api.linklayer.mac.MacAddress;
import de.tudarmstadt.maki.simonstrator.api.Message;

/**
 * This is a Ieee80211 MAC Message, which contains the Message from the upper
 * layer and adds the size of the headers to this message. The fields and their
 * sizes are taken from "802.11 Wireless Networks - Definitive Guide" from
 * Matthew Gast (p.47,53). In total, the header of the message has a size of 34
 * bytes, while the data can be up to 2312 bytes. However
 * "802.11 can transmit frames with a maximum payload of 2304 bytes" from higher
 * levels.
 * 
 * @author Christoph Muenker
 * @version 1.0, 22.02.2013
 */
public class Ieee80211MacMessage implements LinkLayerMessage {

	private static final long FRAME_CONTROL = 2;

	private static final long DURATION_ID = 2;

	private static final long ADDR1 = 6;

	private static final long ADDR2 = 6;

	private static final long ADDR3 = 6;

	private static final long SEQUENCE_CONTROL = 2;

	private static final long ADDR4 = 6;

	private static final long FCS = 4;

	/**
	 * Specifies the overall size of the header of the
	 * {@link Ieee80211MacMessage}, which is 34 bytes.
	 */
	private static final long HEADER_SIZE = FRAME_CONTROL + DURATION_ID + ADDR1
			+ ADDR2 + ADDR3 + SEQUENCE_CONTROL + ADDR4 + FCS;

	private Message payload;
	
	private MacAddress sender;
	
	private MacAddress receiver;

	public Ieee80211MacMessage(MacAddress sender, MacAddress receiver,
			Message msg) {
		this.sender = sender;
		this.receiver = receiver;
		this.payload = msg;
	}

	@Override
	public long getSize() {
		return payload.getSize() + HEADER_SIZE;
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
		return "IEEE80211LinkLayerMessage from " + sender.toString() + " to "
				+ receiver.toString() + ", carrying " + payload.toString();
	}

}
