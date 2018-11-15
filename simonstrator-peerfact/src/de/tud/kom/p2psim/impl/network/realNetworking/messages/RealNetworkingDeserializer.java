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

package de.tud.kom.p2psim.impl.network.realNetworking.messages;

import java.io.IOException;
import java.io.ObjectInputStream;

import de.tud.kom.p2psim.api.transport.TransMessage;
import de.tud.kom.p2psim.impl.network.IPv4Message;
import de.tud.kom.p2psim.impl.network.IPv4NetID;
import de.tud.kom.p2psim.impl.transport.UDPMessage;
import de.tudarmstadt.maki.simonstrator.api.Message;

public class RealNetworkingDeserializer {

	/**
	 * Deserialize overall message.
	 *
	 * @param ois the ois
	 * @return the message
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public static Message deserialize(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		
		int transMsgType = ois.readByte();
		final IPv4NetID sender = new IPv4NetID(ois.readLong());
		final IPv4NetID receiver = new IPv4NetID(ois.readLong());
		final TransMessage transMsg = deserializeTransMsg(ois, transMsgType);
		return new IPv4Message(transMsg, receiver, sender);

	}

	/**
	 * Deserialize transport msg.
	 *
	 * @param ois the ois
	 * @param transMsgType the trans msg type
	 * @return the trans message
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ClassNotFoundException the class not found exception
	 */
	private static TransMessage deserializeTransMsg(ObjectInputStream ois, int transMsgType) throws IOException, ClassNotFoundException {
		
		if( transMsgType == RealNetworkingSerializer.NETMSG_PROTOCOL_NUMBER_UDP ) {

			short senderPort = ois.readShort();
			short receiverPort = ois.readShort();
			int commId = ois.readInt();
			boolean isReply = ois.readBoolean();
			Message msg = (Message) ois.readObject();

			return new UDPMessage(msg, senderPort, receiverPort, commId, isReply);
			
		} else if( transMsgType == RealNetworkingSerializer.NETMSG_PROTOCOL_NUMBER_TCP ) {
			
			return null;
			// Not yet.
		} 
		
		throw new IOException("Cannot parse protocol field in packet.");				
	}
}