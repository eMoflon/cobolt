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

package de.tud.kom.p2psim.api.linklayer.mac;

import de.tud.kom.p2psim.api.linklayer.LinkLayerMessage;
import de.tudarmstadt.maki.simonstrator.api.Message;

/**
 * Container for an event in the MAC - this may include only the message but
 * further information depending on the MAC-Layer may be added.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 22.02.2012
 */
public interface MacEventInformation {

	/**
	 * The transmitted Message, should be an instance of
	 * {@link LinkLayerMessage} in most cases.
	 * 
	 * @return
	 */
	public Message getMessage();

	/**
	 * Returns true, if the message was received as a broadcast
	 * 
	 * @return
	 */
	public boolean isBroadcast();

	/**
	 * MacAddress of the sender
	 * 
	 * @return
	 */
	public MacAddress getSender();

	/**
	 * MacAddress of the receiver
	 * 
	 * @return
	 */
	public MacAddress getReceiver();

	/**
	 * Time this message waited in the outgoing queue of the sender
	 * 
	 * @return
	 */
	public long getTimeInQueue();

	/**
	 * Beware, this might be called multiple times if this is a broadcast! It
	 * will also be called if the LinkLayer-Message was dropped - in this case,
	 * wasDropped will be true.
	 * 
	 * This is a callback that the MAC may use to implement a scheduling
	 * mechanism that does not need ACK-Messages on the LinkLayer but instead
	 * relies on this callback.
	 * 
	 * @param receiver
	 * @param wasDropped
	 */
	public void arrivedAt(MacLayer receiver, boolean wasDropped);

}
