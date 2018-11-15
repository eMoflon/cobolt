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
import java.io.ObjectOutputStream;

import de.tud.kom.p2psim.api.transport.TransMessage;
import de.tud.kom.p2psim.impl.network.IPv4Message;
import de.tud.kom.p2psim.impl.network.IPv4NetID;
import de.tud.kom.p2psim.impl.transport.TCPMessage;
import de.tud.kom.p2psim.impl.transport.UDPMessage;
import de.tudarmstadt.maki.simonstrator.api.Message;

public class RealNetworkingSerializer {

	public static final int NETMSG_PROTOCOL_NUMBER_TCP = 6;
	public static final int NETMSG_PROTOCOL_NUMBER_UDP = 17;

	public static void serialize(ObjectOutputStream oos, Message msg) throws IOException {
		
			/* Net Message */
			final IPv4Message netMsg = (IPv4Message) msg;
			if( netMsg.getPayload() instanceof UDPMessage ) {
				oos.writeByte(NETMSG_PROTOCOL_NUMBER_UDP);
			} else if( netMsg.getPayload() instanceof TCPMessage ) {
				oos.writeByte(NETMSG_PROTOCOL_NUMBER_TCP);
			} else {
				throw new IOException("Unable to parse transport protocol.");
			}
			oos.writeLong(((IPv4NetID) netMsg.getSender()).getID());
			oos.writeLong(((IPv4NetID) netMsg.getReceiver()).getID());
			
			
			/* Transport Message */
			final TransMessage transMsg = (TransMessage) netMsg.getPayload();
			oos.writeShort(transMsg.getSenderPort());
			oos.writeShort(transMsg.getReceiverPort());
			oos.writeInt(transMsg.getCommId());
			oos.writeBoolean(transMsg.isReply());
			if( transMsg instanceof TCPMessage ) {
				oos.writeLong(((TCPMessage)transMsg).getSequenzNumber());
			}
			
			
			/* User Message */
			/* TODO: Add special cases here. */
			oos.writeObject(transMsg.getPayload());			

	}
}
