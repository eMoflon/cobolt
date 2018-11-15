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

package de.tudarmstadt.maki.simonstrator.api.component.transport;

import de.tudarmstadt.maki.simonstrator.api.Message;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;

/**
 * Interface for message-based Transport Protocols
 * 
 * @author Bjoern Richerzhagen
 * 
 */
public interface MessageBasedTransport extends TransportProtocol {

	/**
	 * This Listener is notified upon new messages arriving on the port this
	 * protocol has been registered for.
	 * 
	 * @param listener
	 */
	public void setTransportMessageListener(TransMessageListener listener);

	/**
	 * Something similar to "unbind" - remove the transport message listener,
	 * allowing the protocol instance to be garbage collected.
	 */
	public void removeTransportMessageListener();

	/**
	 * Sends the given message to the receiver with the specified NetID on the
	 * given port.
	 * 
	 * @param msg
	 * @param receiverNet
	 * @param receiverPort
	 */
	public int send(Message msg, NetID receiverNet, int receiverPort);

	/**
	 * This method is used to implement a request-reply scenario. It sends the
	 * given message to a remote host by using the given <code>TransInfo</code>
	 * information of the receiver and calls the given TransMessageCallback when
	 * a reply for the given message is received by using
	 * {@link TransMessageCallback#receive(Message, TransInfo, int)} method.
	 * 
	 * In particular, the sendAndWait method returns a unique communication
	 * identifier which can be used to identify the above mentioned reply when
	 * implementing the TransMessageCallback interface. In addition to this, a
	 * timeout event occurs at the TransMessageCallback after
	 * <code>timeout</code> simulation units.
	 * 
	 * Note that the timeout interval must be adapted to the time units of the
	 * simulation framework. For instance, a real time time of two milliseconds
	 * is specified by 2 * Time.MILLISECOND;
	 * 
	 * @param msg
	 *            the message to be send
	 * @param receiverNet
	 *            the remote receiver which should receive the given message
	 * @param receiverPort
	 *            the port of the receiver
	 * @param senderCallback
	 *            the TransMessageCallback which is called when receiving a
	 *            reply to the given message
	 * @param timeout
	 *            the timeout interval which has to be adapted to the time units
	 *            of the simulation framework
	 */
	public int sendAndWait(Message msg, NetID receiverNet, int receiverPort,
			TransMessageCallback senderCallback, long timeout);

	/**
	 * Sends a reply to a specific message which has been received within a
	 * TransMsgEvent. It is recommended to use this method in order to implement
	 * a request-reply scenario.
	 * 
	 * @param reply
	 *            the given reply message
	 * @param inReplyTo
	 *            the request which triggered the reply
	 * 
	 */
	public int sendReply(Message reply, NetID receiver, int receiverPort,
			int commID);

}
