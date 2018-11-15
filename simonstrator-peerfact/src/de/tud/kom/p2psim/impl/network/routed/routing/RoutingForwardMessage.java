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

package de.tud.kom.p2psim.impl.network.routed.routing;

import java.util.List;
import java.util.Vector;

import de.tud.kom.p2psim.api.network.NetProtocol;
import de.tud.kom.p2psim.impl.network.routed.RoutedNetMessage;
import de.tudarmstadt.maki.simonstrator.api.Message;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;

/**
 * Container message for payload that has to travel via multiple hops
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 05/07/2011
 */
public class RoutingForwardMessage extends RoutedNetMessage {

	private List<NetID> path = new Vector<NetID>();

	private NetID finalTarget;

	/**
	 * Create a new Forward Message
	 * 
	 * @param payload
	 *            the forwarded Message
	 * @param receiver
	 *            next Hop-Receiver
	 * @param sender
	 *            last Hop-Sender
	 * @param finalTarget
	 *            final Target of this message
	 */
	public RoutingForwardMessage(Message payload, NetID receiver, NetID sender,
			NetID finalTarget, NetProtocol protocol) {
		super(payload, receiver, sender, protocol);
		this.finalTarget = finalTarget;
	}

	/**
	 * A forward message with a partial or full path included, depending on the
	 * algorithm used.
	 * 
	 * @param payload
	 * @param receiver
	 * @param sender
	 * @param finalTarget
	 * @param path
	 */
	public RoutingForwardMessage(Message payload, NetID receiver, NetID sender,
			NetID finalTarget, List<NetID> path, NetProtocol protocol) {
		this(payload, receiver, sender, finalTarget, protocol);
		this.path = path;
	}

	/**
	 * Clone a ForwardMessage with a new NextHop. This will add the Message to
	 * 
	 * @param msg
	 * @param nextReceiver
	 */
	public RoutingForwardMessage(RoutingForwardMessage msg, NetID nextReceiver) {
		this(msg.getPayload(), nextReceiver, msg.getReceiver(), msg
				.getFinalTarget(), msg.getPath(), msg.getNetProtocol());
	}

	/**
	 * Target this Routing-Message has to reach
	 * 
	 * @return
	 */
	public NetID getFinalTarget() {
		return finalTarget;
	}

	/**
	 * if supported, your routing algorithm may calculate part of the path or
	 * the whole path and provide a sorted list (first element = first hop)
	 * 
	 * @return
	 */
	public NetID getNextHopOnPath() {
		if (path.isEmpty())
			return null;
		else
			return path.remove(0);
	}

	protected List<NetID> getPath() {
		return path;
	}

	@Override
	public long getSize() {
		if (path != null) {
			return super.getSize() + path.size()
					* finalTarget.getTransmissionSize();
		} else {
			return super.getSize();
		}
	}

	@Override
	public String toString() {
		return "RoutingForwardMessage";
	}

}
