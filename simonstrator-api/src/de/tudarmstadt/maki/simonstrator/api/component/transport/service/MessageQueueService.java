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


/**
 * This service enables you to limit the sending-rate on a given connection or
 * to implement any other fancy kind of message queue for the outgoing
 * connections of a node (e.g., priority handling of specific messages...)
 * 
 * @author Bjoern Richerzhagen
 * 
 */
public interface MessageQueueService extends TransportService {

	public void onOutgoingMessage(Message msg, NetID to, int toPort,
			int onPort, MessageQueueCallback callback);

	/**
	 * This is provided by the TransportComponent. Allows your service to send
	 * or drop the message asynchronously.
	 * 
	 * @author Bjoern Richerzhagen
	 * 
	 */
	public interface MessageQueueCallback {

		/**
		 * Triggers the send-method for the message
		 */
		public void sendMessage();

		/**
		 * Drops the message (e.g., after max time in the queue)
		 */
		public void dropMessage();

	}

}
