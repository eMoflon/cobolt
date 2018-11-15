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
import de.tud.kom.p2psim.api.linklayer.LinkMessageEvent;
import de.tud.kom.p2psim.api.linklayer.mac.MacAddress;
import de.tud.kom.p2psim.api.linklayer.mac.PhyType;
import de.tudarmstadt.maki.simonstrator.api.Message;

/**
 * Implementation of a {@link LinkMessageEvent}
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 27.02.2012
 */
public class DefaultLinkMessageEvent implements LinkMessageEvent {

	private Message payload;

	private PhyType phyType;

	private MacAddress sender;

	private boolean isBroadcast;

	private LinkLayerMessage linkLayerMessage;

	public DefaultLinkMessageEvent(LinkLayerMessage msg, PhyType phyType,
			MacAddress sender, boolean isBroadcast) {
		this.payload = msg.getPayload();
		this.linkLayerMessage = msg;
		this.phyType = phyType;
		this.sender = sender;
		this.isBroadcast = isBroadcast;
	}

	@Override
	public Message getPayload() {
		return payload;
	}

	@Override
	public PhyType getPhyType() {
		return phyType;
	}

	@Override
	public MacAddress getSender() {
		return sender;
	}

	@Override
	public boolean isBroadcast() {
		return isBroadcast;
	}

	@Override
	public LinkLayerMessage getLinkLayerMessage() {
		return linkLayerMessage;
	}

}
