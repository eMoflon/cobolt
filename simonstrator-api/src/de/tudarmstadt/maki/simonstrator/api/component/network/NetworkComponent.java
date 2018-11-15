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

package de.tudarmstadt.maki.simonstrator.api.component.network;

import de.tudarmstadt.maki.simonstrator.api.component.HostComponent;

/**
 * This is losely based on the java.net definitions and concepts.
 * 
 * @author Bjoern Richerzhagen
 * 
 */
public interface NetworkComponent extends HostComponent {

	/**
	 * To ease interface detection for a given type of communication, we rely on
	 * these high-level definitions. Binding is provided by the respective
	 * Platform runtime.
	 * 
	 * @author Bjoern Richerzhagen
	 * 
	 */
	public enum NetInterfaceName {
		/**
		 * WiFi Access Point or Ad Hoc
		 */
		WIFI,
		/**
		 * Ethernet
		 */
		ETHERNET,
		/**
		 * UMTS/LTE
		 */
		MOBILE,
		/**
		 * Bluetooth
		 */
		BLUETOOTH
	}

	/**
	 * Returns a list of all available Network Interfaces. Most Overlays and
	 * apps should use the convenience method getByName().
	 * 
	 * @return
	 */
	public Iterable<NetInterface> getNetworkInterfaces();
	
	/**
	 * Returns the NetInterface that corresponds to the local IP.
	 * 
	 * @param netID
	 * @return
	 */
	public NetInterface getByNetId(NetID netID);

	/**
	 * Returns the NetInterface that is using the given physical technology.
	 * 
	 * @param name
	 * @return
	 */
	public NetInterface getByName(NetInterfaceName name);

}
