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


public class IEEE80211DummyCTSMessage extends IEEE80211AbstractDummyMessage {

	/**
	 * Size of Frame Check Sequence in Bytes
	 */
	protected static int FCS = 4;

	/**
	 * CTS message size in Bytes
	 */
	protected static int CTS_MSG_SIZE = 10 + FCS; // macHeader + FCS
	
	@Override
	public long getSize() {
		return CTS_MSG_SIZE;
	}

}
