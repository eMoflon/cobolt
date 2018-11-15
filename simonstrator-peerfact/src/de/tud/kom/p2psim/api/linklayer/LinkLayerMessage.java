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

package de.tud.kom.p2psim.api.linklayer;

import de.tud.kom.p2psim.api.linklayer.mac.MacAddress;
import de.tudarmstadt.maki.simonstrator.api.Message;

/**
 * A Message (Frame) on the LinkLayer. According to 802.xx this consists at
 * least of both MAC-Addresses and a Checksum.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 21.02.2012
 */
public interface LinkLayerMessage extends Message {

	/**
	 * MAC-Address of the Receiver
	 * 
	 * @return
	 */
	public MacAddress getReceiver();

	/**
	 * MAC-Address of the Sender
	 * 
	 * @return
	 */
	public MacAddress getSender();

}
