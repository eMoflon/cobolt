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

package de.tud.kom.p2psim.api.network;

import de.tudarmstadt.maki.simonstrator.api.Message;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;


/**
 * A less-coupled version of the {@link NetMsgEvent}, where the source
 * NetLayer-Object is not needed.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 29.02.2012
 */
public interface NetMessageEvent {

	/**
	 * Returns the data which was encapsulated in the network message
	 * 
	 * @return the data which was encapsulated in the network message
	 */
	public Message getPayload();

	/**
	 * Returns the NetID of the sender of the received network message
	 * 
	 * @return the NetID of sender of the received network message
	 */
	public NetID getSender();

	/**
	 * Receiver NetID
	 * 
	 * @return
	 */
	public NetID getReceiver();

	/**
	 * Returns the used network protocol
	 * 
	 * @return the used network protocol
	 * 
	 */
	public NetProtocol getNetProtocol();

}
