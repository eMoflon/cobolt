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

package de.tud.kom.p2psim.impl.network.routed;

import java.util.List;
import java.util.Vector;

import de.tud.kom.p2psim.api.network.NetProtocol;
import de.tud.kom.p2psim.api.network.routing.RoutingMessage;
import de.tud.kom.p2psim.impl.network.AbstractNetMessage;
import de.tud.kom.p2psim.impl.transport.TCPMessage;
import de.tud.kom.p2psim.impl.transport.UDPMessage;
import de.tudarmstadt.maki.simonstrator.api.Message;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;

/**
 * Implements Message sizes for the {@link NetProtocol}s handled by the
 * {@link RoutedNetLayer}. Payload may be a {@link UDPMessage}, a
 * {@link TCPMessage} or a {@link RoutingMessage}.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 01.03.2012
 */
public class RoutedNetMessage extends AbstractNetMessage {

	/**
	 * HopCount as part of the IP-Header
	 */
	private short hopCount = 0;

	private int totalNumberOfFragments = 0;

	private int fragmentNumber = 0;
	
	private FragmentReceivedInfo fragmentReceiverInfo = null;

	/**
	 * We do not operate on the payload directly to ease compution in the
	 * Netlayer and to allow for virtual fragmentation of a message.
	 */
	private long payloadSize = 0;

	private List<NetID> hops = new Vector<NetID>();

	/**
	 * Create a new, not fragmented End-to-End Message (sender and receiver are
	 * on IP-Level, intermediate hops are addressed with their MacAddress and
	 * this message is not changed!)-
	 * 
	 * @param payload
	 * @param receiver
	 * @param sender
	 * @param netProtocol
	 */
	public RoutedNetMessage(Message payload, NetID receiver, NetID sender,
			NetProtocol netProtocol) {
		this(payload, receiver, sender, netProtocol, payload.getSize(), 1, 1, new FragmentReceivedInfo());
	}

	/**
	 * A fragmented NetMessage (if both fragmentNumber and
	 * TotalNumberOfFragments is set to one, the message is not fragmented at
	 * all)
	 * 
	 * @param payload
	 * @param receiver
	 * @param sender
	 * @param netProtocol
	 * @param fragmentSize
	 *            virtual payload size of this fragment, do NOT include the
	 *            header size. This may be related to the MTU of the chosen PHY
	 * @param fragmentNumber
	 *            starting at 1
	 * @param fragmentInfo used to track fragments at the receiver
	 * @param totalNumberOfFragments
	 */
	public RoutedNetMessage(Message payload, NetID receiver, NetID sender,
			NetProtocol netProtocol, long fragmentSize, int fragmentNumber,
			int totalNumberOfFragments, FragmentReceivedInfo fragmentInfo) {
		super(payload, receiver, sender, netProtocol);
		this.payloadSize = fragmentSize;
		this.fragmentNumber = fragmentNumber;
		this.totalNumberOfFragments = totalNumberOfFragments;
		this.fragmentReceiverInfo = fragmentInfo;

		assert totalNumberOfFragments > 0;
		assert fragmentNumber > 0;
	}

	@Override
	public long getSize() {
		return getNetProtocol().getHeaderSize() + payloadSize;
	}

	/**
	 * The sequence of this fragment.
	 * 
	 * @return
	 */
	public int getFragmentNumber() {
		return fragmentNumber;
	}

	/**
	 * Number of Fragments for the complete Message, if == 1 there was no
	 * fragmentation
	 * 
	 * @return
	 */
	public int getTotalNumberOfFragments() {
		return totalNumberOfFragments;
	}

	/**
	 * Increment the HopCount on each intermediate hop
	 */
	public void incrementRoutedNetMessageHopCount() {
		hopCount++;
	}

	/**
	 * Hops this message already took.
	 * 
	 * @return
	 */
	public short getRoutedNetMessageHopCount() {
		return hopCount;
	}

	/**
	 * Use this object to keep track of received fragments at the receiver(s)
	 * 
	 * @return
	 */
	public FragmentReceivedInfo getFragmentReceiverInfo() {
		return fragmentReceiverInfo;
	}

	@Override
	public String toString() {
		return "RoutedNetMsg from " + getSender() + " to " + getReceiver()
				+ " via " + hops.toString();
	}

}
