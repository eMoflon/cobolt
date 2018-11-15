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

package de.tudarmstadt.maki.simonstrator.api.component.service;

import java.util.Collection;

import de.tudarmstadt.maki.simonstrator.api.component.HostComponent;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetworkComponent.NetInterfaceName;
import de.tudarmstadt.maki.simonstrator.api.component.overlay.OverlayContact;
import de.tudarmstadt.maki.simonstrator.api.component.transport.ProtocolNotAvailableException;

/**
 * Simple discovery service for services and devices in a network. Common
 * use-case: discovery of nearby devices in a mobile ad hoc network.
 * 
 * @author Bjoern Richerzhagen
 *
 */
public interface DiscoveryService extends HostComponent {

	/**
	 * Create a request object for the {@link DiscoveryService}.
	 * 
	 * @param serviceName
	 *            name of the service (a string identifier)
	 * @param network
	 *            network interface to use
	 * @param port
	 *            port for the service announcements (and listening port if this
	 *            node also offers the service itself)
	 * @param offerService
	 *            true, if the device starting the discovery also offers the
	 *            service (usually the case in an ad hoc networking scenario)
	 * 
	 * @return a configurable request object used to finally start the discovery
	 *         process.
	 */
	public DiscoveryRequest createRequest(String serviceName,
			NetInterfaceName network, int port, boolean offerService);

	/**
	 * Start service discovery on the given network and port.
	 * 
	 * @param request
	 *            request object for the service discovery.
	 * @param listener
	 *            listener to be informed if new service endpoints are found.
	 *            Has to be unique as it is also used for identification of a
	 *            discovery process.
	 * 
	 * @throws ProtocolNotAvailableException
	 *             if the given port is not available
	 */
	public void startDiscovery(DiscoveryRequest request,
			DiscoveryListener listener) throws ProtocolNotAvailableException;

	/**
	 * Stop service discovery (and, if enabled, service offerings) corresponding
	 * to the provided {@link DiscoveryRequest}.
	 * 
	 * @param request
	 */
	public void stopDiscovery(DiscoveryRequest request);

	/**
	 * Listener for the service discovery, operating on a given port and a given
	 * service name.
	 * 
	 * @author Bjoern Richerzhagen
	 *
	 */
	public interface DiscoveryListener {

		/**
		 * Called as soon as a service is discovered, together with a list of
		 * potential service endpoints (contacts). If we keep the discovery
		 * process active, this list can change over time if new endpoints are
		 * discovered or existing ones are lost, resulting in calls to
		 * <code>onServiceUpdate</code>.
		 * 
		 * @param serviceName
		 * @param serviceEndpoints
		 */
		public void onServiceFound(String serviceName,
				Collection<OverlayContact> serviceEndpoints);

		/**
		 * Called each time the list of service endpoints changes.
		 * 
		 * @param serviceName
		 * @param serviceEndpoints
		 */
		public void onServiceUpdate(String serviceName,
				Collection<OverlayContact> serviceEndpoints);

		/**
		 * Called, as soon as no endpoint for the given service is known anymore
		 * (the service is lost).
		 * 
		 * @param serviceName
		 */
		public void onServiceLost(String serviceName);

	}

	/**
	 * Object used for the configuration of the service discovery.
	 * 
	 * @author Bjoern Richerzhagen
	 *
	 */
	public interface DiscoveryRequest {

		/**
		 * Configuration option: set a discovery interval. The
		 * {@link DiscoveryService} implementation might alter this interval.
		 * The shorter the interval, the more aggressive the strategy.
		 * 
		 * @param discoveryInterval
		 *            a simulation time
		 */
		public void setDiscoveryInterval(long discoveryInterval);

	}

}
