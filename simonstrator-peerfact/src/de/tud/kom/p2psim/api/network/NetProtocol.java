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



package de.tud.kom.p2psim.api.network;

/**
 * This enumeration types describe the service properties chosen for message
 * transmission at the network layer
 * 
 * @author Sebastian Kaune
 * @author Konstantin Pussep
 * @version 3.0, 11/29/2007
 * 
 */
public enum NetProtocol {
	/**
	 * Common known IPv4 network protocol, with a 20 byte header
	 */
	IPv4(20);

	private final int headerSize;

	private NetProtocol(int headerSize) {
		this.headerSize = headerSize;
	}

	/**
	 * Default Header Size of a Message within this Protocol
	 * 
	 * @return
	 */
	public int getHeaderSize() {
		return headerSize;
	}

}
