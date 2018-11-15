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

package de.tud.kom.p2psim.api.transport;

import de.tudarmstadt.maki.simonstrator.api.Message;

/**
 * Providing the basic API for every TransportLayer-Message
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 07.05.2012
 */
public interface TransMessage extends Message {

	/**
	 * The port that was used to send the Message
	 * 
	 * @return
	 */
	public int getSenderPort();

	/**
	 * The port on the receiver where the message is dispatched to
	 * 
	 * @return
	 */
	public int getReceiverPort();

	/**
	 * The used TransProtocol
	 * 
	 * @return
	 */
	public TransProtocol getProtocol();

	/**
	 * True, if this message is a reply (used for the Callback-based send-reply
	 * scenario)
	 * 
	 * @return
	 */
	public boolean isReply();

	/**
	 * Communication ID - unique for a sender
	 * 
	 * @return
	 */
	public int getCommId();

}
