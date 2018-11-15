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

/**
 * This class is the state of the stations of the {@link IdealRemoteStation}. It
 * stores the last SNR of a received Message of this station.
 * 
 * @author Christoph Muenker
 * @version 1.0, 22.02.2013
 */
public class IdealRemoteStation implements IWifiRemoteStation {

	private double lastSnr = Double.MIN_VALUE;

	private MacAddress address;

	public IdealRemoteStation(MacAddress address) {
		this.address = address;
	}

	public void setLastSnr(double lastSnr) {
		this.lastSnr = lastSnr;
	}

	public double getLastSnr() {
		return lastSnr;
	}

	@Override
	public MacAddress getMacAddress() {
		return address;
	}
}
