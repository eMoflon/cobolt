/*
 * Copyright (c) 2005-2011 KOM - Multimedia Communications Lab
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

package de.tud.kom.p2psim.impl.network;

import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;

/**
 * Implementation of the NetID-Interface for IPv4-Addresses
 * 
 * @author Gerald Klunker
 */
public class IPv4NetID implements NetID {

	/**
	 * 32bit IP Address (with respect to the algebraic sign of 32bit Integers in
	 * a 64bit Long Value).
	 */
	private Long id;

	/**
	 * Broadcast-Adress which is used by the subnet
	 */
	public static IPv4NetID LOCAL_BROADCAST = new IPv4NetID("255.255.255.255");

	/**
	 * Creates an Instance of IPv4NetID.
	 * 
	 * @param id
	 *            The Long-ID.
	 */
	public IPv4NetID(Long id) {
		this.id = id;
	}

	public IPv4NetID(String id) {
		try {
			long ip = Long.parseLong(id);
			this.id = ip;
		} catch (Exception e) {
			long ip = IPv4NetID.ipToLong(id);
			this.id = ip;
		}
	}

	/**
	 * @return The Long-ID.
	 */
	public long getID() {
		return this.id;
	}

	/**
	 * @param obj
	 *            An object.
	 * @return Whether the parameter is equal to this IPv4NetID or not.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof IPv4NetID) {
			return getID() == ((IPv4NetID) obj).getID();
		} else {
			return false;
		}
	}

	/**
	 * @return The hashcode of this IPv4NetID.
	 */
	@Override
	public int hashCode() {
		return this.id.hashCode();
	}

	/**
	 * @return A string representing this InternetProtocolNetID.
	 */
	@Override
	public String toString() {
		return IPv4NetID.ipToString(this.id);
	}

	// TODO Throw exceptions on invalid values for the parameters

	/**
	 * @param ip
	 *            32bit IP-Address
	 * @return A readable IP-String like "192.168.0.1"
	 */
	public static String ipToString(Long ip) {
		String returnString = "";
		returnString += Long.toString((ip << 32) >>> 56) + ".";
		returnString += Long.toString((ip << 40) >>> 56) + ".";
		returnString += Long.toString((ip << 48) >>> 56) + ".";
		returnString += Long.toString((ip << 56) >>> 56);
		return returnString;
	}

	/**
	 * 
	 * @param ip
	 *            readable IP-String like "192.168.0.1"
	 * @return A 32bit IP-Address
	 */
	public static Long ipToLong(String ip) {
		String[] ipBytes = ip.split("\\.");
		Long ipLong = new Long(0);
		try {
			ipLong += (Long.valueOf(ipBytes[0])) << 24;
			ipLong += (Long.valueOf(ipBytes[1])) << 16;
			ipLong += (Long.valueOf(ipBytes[2])) << 8;
			ipLong += Long.valueOf(ipBytes[3]);
		} catch (Exception e) {
			return null;
		}

		return ipLong;
	}

	public static int ipToInt(String ip) {
		String[] ipBytes = ip.split("\\.");
		int ipInt = 0;
		ipInt += (Integer.valueOf(ipBytes[0])) << 24;
		ipInt += (Integer.valueOf(ipBytes[1])) << 16;
		ipInt += (Integer.valueOf(ipBytes[2])) << 8;
		ipInt += Integer.valueOf(ipBytes[3]);
		return ipInt;
	}

	/**
	 * @param ip
	 *            32bit IP-Address
	 * @return A readable IP-String like "192.168.0.1"
	 */
	public static String intToIP(int ip) {
		String returnString = "";
		returnString += Integer.toString((ip << 32) >>> 56) + ".";
		returnString += Integer.toString((ip << 40) >>> 56) + ".";
		returnString += Integer.toString((ip << 48) >>> 56) + ".";
		returnString += Integer.toString((ip << 56) >>> 56);
		return returnString;
	}

	public static long intToLong(int ip) {
		return Long.parseLong(Integer.toBinaryString(ip), 2);
	}

	public static int longToInt(long ip) {
		return (int) ip;
	}

	@Override
	public int getTransmissionSize() {
		return 4;
	}

}
