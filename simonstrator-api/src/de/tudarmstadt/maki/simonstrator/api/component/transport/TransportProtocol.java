/*
 * Copyright (c) 2005-2010 KOM – Multimedia Communications Lab
 *
 * This file is part of Simonstrator.KOM.
 * 
 * Simonstrator.KOM is free software: you can redistribute it and/or modify
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

package de.tudarmstadt.maki.simonstrator.api.component.transport;

import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetInterface;

/**
 * Core Protocol interface for transport protocols
 * 
 * @author Bjoern Richerzhagen
 * 
 */
public interface TransportProtocol {

	/**
	 * Local port this protocol is listening on.
	 * 
	 * @return
	 */
	public int getLocalPort();

	/**
	 * The NetInterface that is being used by this Protocol.
	 * 
	 * @return
	 */
	public NetInterface getNetInterface();

	/**
	 * Returns the local {@link TransInfo}
	 * 
	 * @param netId
	 * @param port
	 * @return
	 */
	public TransInfo getTransInfo();

	/**
	 * Creates a TransInfo out of the given NetID and the binded protocol's port
	 * 
	 * @param net
	 * @param port
	 * @return
	 */
	public TransInfo getTransInfo(NetID net);

	/**
	 * Creates a TransInfo out of the given NetID and the given port
	 * 
	 * @param net
	 * @param port
	 * @return
	 */
	public TransInfo getTransInfo(NetID net, int port);

	/**
	 * Header size in byte
	 * 
	 * @return
	 * @deprecated check, if this is used at all
	 */
	public int getHeaderSize();

}
