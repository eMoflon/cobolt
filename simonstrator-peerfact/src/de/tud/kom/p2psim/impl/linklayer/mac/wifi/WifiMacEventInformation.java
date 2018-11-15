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

package de.tud.kom.p2psim.impl.linklayer.mac.wifi;

import de.tud.kom.p2psim.api.linklayer.mac.MacAddress;
import de.tud.kom.p2psim.api.topology.views.wifi.phy.WifiMode;
import de.tud.kom.p2psim.impl.linklayer.mac.DefaultMacEventInformation;

/**
 * This class extends the {@link DefaultMacEventInformation}. This added
 * information are specific needed for the {@link Ieee80211AdHocMac}, because we
 * need this information at the receiver of the message.
 * 
 * @author Christoph Muenker
 * @version 1.0, 22.02.2013
 */
public class WifiMacEventInformation extends DefaultMacEventInformation {

	private long transmissionDuration;

	private long ackDuration = 0;

	private WifiMode ackMode = null;

	private WifiMode mode = null;

	public WifiMacEventInformation(Ieee80211MacMessage msg, MacAddress sender,
			MacAddress receiver, long timeInQueue) {
		super(msg, sender, receiver, timeInQueue);
	}

	@Override
	public Ieee80211MacMessage getMessage() {
		return (Ieee80211MacMessage) super.getMessage();
	}

	public void setTransmissionDuration(long transmissionDuration) {
		this.transmissionDuration = transmissionDuration;
	}

	public void setAckDuration(long ackDuration) {
		this.ackDuration = ackDuration;
	}

	public long getTransmissionDuration() {
		return transmissionDuration;
	}

	public long getAckDuration() {
		return ackDuration;
	}

	public void setAckMode(WifiMode ackMode) {
		this.ackMode = ackMode;
	}

	public WifiMode getAckMode() {
		return ackMode;
	}

	public void setMode(WifiMode mode) {
		this.mode = mode;
	}

	public WifiMode getMode() {
		return mode;
	}

}
