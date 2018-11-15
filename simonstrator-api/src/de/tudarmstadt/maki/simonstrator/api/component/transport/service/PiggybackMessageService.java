/*
 * Copyright (c) 2005-2010 KOM – Multimedia Communications Lab
 *
 * This file is part of Simonstrator.KOM.
 * 
 * Simonstrator.KOM is free software: you can redistribute it and/or modify
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

package de.tudarmstadt.maki.simonstrator.api.component.transport.service;

import de.tudarmstadt.maki.simonstrator.api.Message;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;
import de.tudarmstadt.maki.simonstrator.api.component.overlay.Serializer;
import de.tudarmstadt.maki.simonstrator.api.component.transport.TransInfo;
import de.tudarmstadt.maki.simonstrator.api.component.transport.TransportProtocol;

/**
 * Opportunity for a component to piggyback additional data at the end of a
 * message that is being sent by the Transport Protocol.
 * 
 * This extends Serializer, as the additional payload is serialized using this
 * component as well. This special serializer does not need to register with a
 * given port! Usually, serializers are not used for simulations.
 * 
 * @author Bjoern Richerzhagen
 * 
 */
public interface PiggybackMessageService extends Serializer, TransportService {

	/**
	 * Has to return a service ID. Negative values are reserved. If a collision
	 * occurs, an exception will be thrown upon component initialization.
	 * 
	 * @return
	 */
	public byte getPiggybackServiceID();

	/**
	 * If you want to piggyback data to a message that is currently being sent
	 * to the given receiver on the given port via the given protocol, just
	 * return the message that is to be piggybacked. Otherwise, return null.
	 * 
	 * @param to
	 * @param receiverPort
	 * @param protocol
	 * @return
	 */
	public Message piggybackOnSendMessage(NetID to, int receiverPort,
			TransportProtocol protocol);

	/**
	 * Called, if the transport layer detected piggybacked information that is
	 * to be consumed by this service instance. Piggybacked information is
	 * stripped from the message before it is delivered, i.e., this service gets
	 * called prior to the delivery of the original message!
	 * 
	 * @param msg
	 */
	public void onReceivedPiggybackedMessage(Message msg, TransInfo sender);

}
