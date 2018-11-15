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


package de.tud.kom.p2psim.impl.network.modular.st;

import de.tud.kom.p2psim.api.network.NetProtocol;
import de.tudarmstadt.maki.simonstrator.api.Message;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;

/**
 * A network layer usually splits up the SDU into multiple fragments, if it is too big.
 * This strategy determines how many fragments are needed to service the given payload.
 * 
 * @author Leo Nobach
 *
 */
public interface FragmentingStrategy extends ModNetLayerStrategy {

	/**
	 * Returns the number of fragments for the given payload in order to construct a NetMessage.
	 * @param payload , the payload passed by the transport layer
	 * @param receiver , the receiver address of the future net message
	 * @param sender , the sender address of the future net message
	 * @param netProtocol , the network protocol used of the future net message.
	 * @return
	 */
	public int getNoOfFragments(Message payload, NetID receiver, NetID sender, NetProtocol netProtocol);

}
