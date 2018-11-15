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



package de.tud.kom.p2psim.api.network;

import de.tudarmstadt.maki.simonstrator.api.Message;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;

/**
 * NetMessages are used to realize the communication between two NetLayers and
 * encapsulate the necessary information such as the used network protocol and
 * the NetID of the sender and receiver of a given message.
 * 
 * @author Sebastian Kaune
 * @author Konstantin Pussep
 * @version 3.0, 11/29/2007
 * 
 */
public interface NetMessage extends Message {

	/**
	 * Returns the NetID of the sender of a NetMessage
	 * 
	 * @return The NetID of the sender.
	 */
	public NetID getSender();

	/**
	 * Returns the NetID of the receiver of a NetMessage
	 * 
	 * @return The NetID of the receiver.
	 */
	public NetID getReceiver();

	/**
	 * Returns the network protocol used to send this message
	 * 
	 * @return The network protocol used to send this message
	 */
	public NetProtocol getNetProtocol();
	
	/**
	 * Returns the number of fragments the message is split into.
	 * @return
	 */
	public int getNoOfFragments();
	
}
