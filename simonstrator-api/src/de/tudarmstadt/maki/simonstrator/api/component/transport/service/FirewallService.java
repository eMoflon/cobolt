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

package de.tudarmstadt.maki.simonstrator.api.component.transport.service;

import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;
import de.tudarmstadt.maki.simonstrator.api.component.transport.ServiceNotAvailableException;
import de.tudarmstadt.maki.simonstrator.api.component.transport.TransInfo;

/**
 * Enables a component to act as a Firewall-like component for Transport layer
 * connections. This component is notified upon incoming connections and might
 * decide to drop them.
 * 
 * This is an OPTIONAL feature of the respective runtime. Upon usage, you need
 * to check whether the service is registered - otherwise, a
 * {@link ServiceNotAvailableException} will be thrown.
 * 
 * @author Bjoern Richerzhagen
 * 
 */
public interface FirewallService extends TransportService {

	/**
	 * True, if the incoming connection from the given TransInfo is allowed.
	 * 
	 * @param from
	 * @return
	 */
	public boolean allowIncomingConnection(TransInfo from, int onPort);

	/**
	 * True, if the outgoing connection on the given port to the given netId is
	 * allowed.
	 * 
	 * @param to
	 * @param toPort
	 * @param onPort
	 * @return
	 */
	public boolean allowOutgoingConnection(NetID to, int toPort, int onPort);

}
