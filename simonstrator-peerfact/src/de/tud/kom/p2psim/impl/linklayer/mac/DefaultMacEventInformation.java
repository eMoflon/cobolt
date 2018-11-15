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

package de.tud.kom.p2psim.impl.linklayer.mac;

import de.tud.kom.p2psim.api.linklayer.mac.MacAddress;
import de.tud.kom.p2psim.api.linklayer.mac.MacEventInformation;
import de.tud.kom.p2psim.api.linklayer.mac.MacLayer;
import de.tudarmstadt.maki.simonstrator.api.Message;

/**
 * Basic implementation of the {@link MacEventInformation}. Extending classes
 * could for example use the arrivedAt-Callback to implement some kind of
 * Scheduling mechanism that does not need to send "real" messages.
 * 
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 27.02.2012
 */
public class DefaultMacEventInformation implements MacEventInformation {

	private Message msg;

	private boolean isBroadcast;

	private MacAddress sender;

	private MacAddress receiver;

	private long timeInQueue;

	public DefaultMacEventInformation(Message msg, MacAddress sender,
			MacAddress receiver, long timeInQueue) {
		this.msg = msg;
		this.sender = sender;
		this.receiver = receiver;
		this.isBroadcast = receiver.isBroadcast();
		this.timeInQueue = timeInQueue;
	}

	@Override
	public void arrivedAt(MacLayer receiver, boolean wasDropped) {
		// not interested.
	}

	@Override
	public Message getMessage() {
		return msg;
	}

	@Override
	public boolean isBroadcast() {
		return isBroadcast;
	}

	@Override
	public MacAddress getSender() {
		return sender;
	}

	@Override
	public MacAddress getReceiver() {
		return receiver;
	}

	@Override
	public long getTimeInQueue() {
		return timeInQueue;
	}

}
