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

import de.tudarmstadt.maki.simonstrator.api.component.HostComponent;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetInterface;
import de.tudarmstadt.maki.simonstrator.api.component.transport.service.TransportService;

/**
 * This component provides {@link TransportProtocol}s.
 * 
 * @author Bjoern Richerzhagen
 * 
 */
public interface TransportComponent extends HostComponent {

	/**
	 * Tries to bind the protocol to the {@link NetInterface} with the given
	 * {@link NetID} and port and returns the protocol. The object returned by
	 * this method should be cached!
	 * 
	 * @param protocolInterface
	 * @param localAddress
	 * @param localPort
	 * @return
	 * @throws ProtocolNotAvailableException
	 */
	public <T extends TransportProtocol> T getProtocol(
			Class<T> protocolInterface, NetID localAddress, int localPort)
			throws ProtocolNotAvailableException;

	/**
	 * Enables overlays, services, and apps to register themselves as handlers
	 * for special features of the Transport Component. Services usually listen
	 * on all ports and all protocols.
	 * 
	 * @param serviceInterface
	 * @param serviceImplementation
	 * @throws ServiceNotAvailableException
	 *             if the given service is not supported by the respective
	 *             runtime implementation
	 */
	public <T extends TransportService> void registerService(
			Class<T> serviceInterface, T serviceImplementation)
			throws ServiceNotAvailableException;

}
