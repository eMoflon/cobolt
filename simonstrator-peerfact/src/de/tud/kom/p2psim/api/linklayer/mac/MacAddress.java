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

import de.tud.kom.p2psim.api.scenario.ConfigurationException;
import de.tudarmstadt.maki.simonstrator.api.common.Transmitable;

/**
 * The MAC-Address is a unique address on the LinkLayer, identifying a
 * communication interface on a host.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 20.02.2012
 */
public class MacAddress implements Transmitable {

	/**
	 * The Broadcast-MAC-Address
	 */
	public final static MacAddress BROADCAST = new MacAddress(true);

	private boolean isBroadcast = false;

	/**
	 * Internally used: long instead of byte-array.
	 */
	// private Long addressLong = 0l;

	/**
	 * Int-Space is huge enough
	 */
	private Integer address = 0;

	/**
	 * Create a MacAddress from a long. Long.MAX_VALUE is reserved for
	 * broadcasts.
	 * 
	 * @param id
	 */
	public MacAddress(int id) {
		if (id == Integer.MAX_VALUE) {
			throw new ConfigurationException(
					"MAX_VALUE is reserved for the broadcast address!");
		}
		address = id;
	}

	/**
	 * Just used to create the broadcast Address
	 * 
	 * @param broadcast
	 */
	private MacAddress(boolean broadcast) {
		address = Integer.MAX_VALUE;
		isBroadcast = true;
	}

	/**
	 * Return the long-representation of the Mac-Address
	 * 
	 * @return
	 */
	protected int getAddressAsLong() {
		return address;
	}

	@Override
	public boolean equals(Object obj) {
		/*
		 * As the MacAddress-Objects are created during configuration (and ONLY
		 * there), it is suitable to check for equality by just comparing the
		 * pointers to speed up the simulation process. In a distributed
		 * simulator communication will happen above the LinkLayer, so we do not
		 * care about MacAddr.
		 */
		// return this == obj;
		if (this == obj) {
			return true;
		}
		if (obj instanceof MacAddress) {
			return ((MacAddress) obj).address.equals(address);
		}
		return false;
	}

	/**
	 * @return The hashcode
	 */
	@Override
	public int hashCode() {
		return this.address.hashCode();
	}

	@Override
	public String toString() {
		return String.valueOf(address);
	}

	@Override
	public int getTransmissionSize() {
		return 6; // The "real deal" is a 6 byte address
	}

	/**
	 * Use this rather than equals BROADCAST
	 * 
	 * @return
	 */
	public boolean isBroadcast() {
		return isBroadcast;
	}

}
