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

package de.tud.kom.p2psim.impl.network;

import de.tud.kom.p2psim.api.network.NetMessageEvent;
import de.tud.kom.p2psim.api.network.NetProtocol;
import de.tudarmstadt.maki.simonstrator.api.Message;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;

/**
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 29.02.2012
 */
public class DefaultNetMessageEvent implements NetMessageEvent {

	private Message payload;

	private NetID sender;

	private NetID receiver;

	private NetProtocol netProtocol;

	/**
	 * Constructs NetMsgEvent
	 * 
	 * @param netMsg
	 *            the NetMessage received by the NetLayer
	 * @param source
	 *            the source of this event
	 */
	public DefaultNetMessageEvent(NetProtocol protocol, NetID sender,
			NetID receiver, Message payload) {
		this.netProtocol = protocol;
		this.sender = sender;
		this.receiver = receiver;
		this.payload = payload;
	}

	/**
	 * Returns the data which was encapsulated in the network message
	 * 
	 * @return the data which was encapsulated in the network message
	 */
	@Override
	public Message getPayload() {
		return payload;
	}

	/**
	 * Returns the NetID of the sender of the received network message
	 * 
	 * @return the NetID of sender of the received network message
	 */
	@Override
	public NetID getSender() {
		return sender;
	}

	@Override
	public NetID getReceiver() {
		return receiver;
	}

	/**
	 * Returns the used network protocol
	 * 
	 * @return the used network protocol
	 * 
	 */
	@Override
	public NetProtocol getNetProtocol() {
		return netProtocol;
	}

}
