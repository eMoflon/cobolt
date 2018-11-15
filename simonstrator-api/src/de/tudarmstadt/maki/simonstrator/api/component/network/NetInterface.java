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

import de.tudarmstadt.maki.simonstrator.api.component.network.NetworkComponent.NetInterfaceName;
import de.tudarmstadt.maki.simonstrator.api.component.transport.ConnectivityListener;

/**
 * A NetInterface, corresponding to a phsical network connection.
 * 
 * @author Bjoern Richerzhagen
 * 
 */
public interface NetInterface {

	/**
	 * Name of the Interface
	 * 
	 * @return
	 */
	public NetInterfaceName getName();

	/**
	 * MTU in byte
	 * 
	 * @return
	 */
	public int getMTU();
	
	/**
	 * We assume that only one local IP per interface is allowed to ease
	 * development of apps.
	 * 
	 * @return The local NetID of the given interface
	 */
	public NetID getLocalInetAddress();

	/**
	 * Returns the broadcast address that can be used to send broadcasts via the
	 * given NetInterface.
	 * 
	 * @return
	 */
	public NetID getBroadcastAddress();

	/**
	 * Translates a human-readable address (either IP or hostname) into the
	 * respective NetID-object.
	 * 
	 * @param address
	 * @return
	 */
	public NetID getByName(String name);

	/**
	 * True, if this network interface is up and running (i.e., the host is
	 * "online" via this interface)
	 * 
	 * @return
	 */
	public boolean isUp();

	public Bandwidth getCurrentBandwidth();

	public Bandwidth getMaxBandwidth();

	/**
	 * This Listener is notified if the availability of the {@link NetInterface}
	 * changes (i.e., due to churn)
	 * 
	 * @param listener
	 */
	public void addConnectivityListener(ConnectivityListener listener);

	/**
	 * This Listener is notified if the availability of the {@link NetInterface}
	 * changes (i.e., due to churn)
	 * 
	 * @param listener
	 */
	public void removeConnectivityListener(ConnectivityListener listener);

}
