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

package de.tud.kom.p2psim.impl.transport.modular;

import de.tud.kom.p2psim.api.transport.TransMessage;
import de.tudarmstadt.maki.simonstrator.api.Message;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;
import de.tudarmstadt.maki.simonstrator.api.component.transport.TransInfo;

/**
 * Basic interface for a TransmissionProtocol inside peerfactSim
 * 
 * @author Bjoern
 * @version 1.0, Jul 8, 2013
 */
public interface ITransProtocol {

	/**
	 * Called when a message is passed from the application/overlay to this
	 * protocol
	 * 
	 * @param msg
	 * @param receiverNet
	 * @param receiverPort
	 * @param senderPort
	 * @param commId
	 * @param isReply
	 */
	public void send(Message msg, NetID receiverNet, int receiverPort,
			int senderPort, int commId, boolean isReply);

	/**
	 * Called when a message is received by the Netlayer and has to be processed
	 * by this protocol
	 * 
	 * @param transMsg
	 * @param senderTransInfo
	 * @return
	 */
	public TransMessage receive(Message transMsg, TransInfo senderTransInfo);

	/**
	 * Size of the header of this protocol in byte
	 * 
	 * @return
	 */
	public int getHeaderSize();

}
