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



package de.tud.kom.p2psim.impl.network;

import de.tud.kom.p2psim.api.network.NetMessage;
import de.tud.kom.p2psim.api.network.NetProtocol;
import de.tudarmstadt.maki.simonstrator.api.Message;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;

/**
 * This abstract class provides a skeletal implementation of the
 * <code>NetMessage<code> interface to lighten the effort for implementing this interface.
 * 
 * @author Sebastian Kaune
 * @author Konstantin Pussep
 * @version 3.0, 11/29/2007
 */
public abstract class AbstractNetMessage implements NetMessage {

	/**
	 * The payload of the NetMessage.
	 */
	private Message payload;

	/**
	 * The NetID of the receiver.
	 */
	private NetID receiver;

	/**
	 * The NetID of the sender.
	 */
	private NetID sender;

	/**
	 * The NetProtocol of the NetMessage.
	 */
	private NetProtocol netProtocol;

	/**
	 * The Number of Fragments.
	 */
	protected int noOfFragments = 0;
	

	/**
	 * Constructor called by subclasses of this
	 * 
	 * @param payload
	 *            The payload of the ComplexNetworkMessage.
	 * @param receiver
	 *            The NetID of the receiver.
	 * @param sender
	 *            The NetID of the sender.
	 * @param netProtocol
	 *            The ServiceCategory of the ComplexNetworkMessage.
	 */
	public AbstractNetMessage(Message payload, NetID receiver, NetID sender,
			NetProtocol netProtocol) {
		this.payload = payload;
		this.receiver = receiver;
		this.sender = sender;
		this.netProtocol = netProtocol;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.tud.kom.p2psim.api.network.NetMessage#getReceiver()
	 */
	public NetID getReceiver() {
		return this.receiver;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.tud.kom.p2psim.api.network.NetMessage#getSender()
	 */
	public NetID getSender() {
		return this.sender;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.tud.kom.p2psim.api.network.NetMessage#getNetProtocol()
	 */
	public NetProtocol getNetProtocol() {
		return this.netProtocol;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.tud.kom.p2psim.api.common.Message#getPayload()
	 */
	public Message getPayload() {
		return this.payload;
	}
	
	/**
	 * Returns the number of fragments the message is split into.
	 * @return
	 */
	public int getNoOfFragments() {
		return noOfFragments;
	}

}
