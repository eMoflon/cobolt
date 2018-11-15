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
 * NetMessage listeners acts as event handlers for incoming NetMsgEvents
 * triggered by the NetLayer. In particular, NetMsgEvents comprises among other
 * things a payload message which in turn comprises data necessary for the
 * transport layer. In other words, the network layers strips off the header
 * information of the network message and passes all relevant data to transport
 * layer in terms of NetMsgEvents.
 * 
 * @author Sebastian Kaune
 * @author Konstantin Pussep
 * @version 3.0, 11/29/2007
 * 
 */
public interface NetMessageListener {
	/**
	 * Upon receiving a NetMessage, the NetLayer strips off the header
	 * information of this message and passes all relevant data to a given
	 * NetMessageListener in terms of NetMsgEvents.
	 * 
	 * @param nme
	 *            the NetMsgEvent passed from the NetLayer
	 * 
	 */
	public void messageArrived(NetMessageEvent nme);
}
