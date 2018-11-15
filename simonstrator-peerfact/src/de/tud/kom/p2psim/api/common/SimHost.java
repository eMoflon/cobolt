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

package de.tud.kom.p2psim.api.common;

import de.tud.kom.p2psim.api.energy.EnergyModel;
import de.tud.kom.p2psim.api.linklayer.LinkLayer;
import de.tud.kom.p2psim.api.network.SimNetworkComponent;
import de.tud.kom.p2psim.api.topology.TopologyComponent;
import de.tudarmstadt.maki.simonstrator.api.Host;

/**
 * Host represents an end-system in a (p2p) network. A host contains several
 * layers (network, transport, overlay and application) while one layer may
 * contain several components (e.g. two overlays in the same host). Further, a
 * host stores additional properties in a HostProperites object and the user
 * object which may represent .
 * <p>
 * Before a simulation can be started a host may receive some basic actions
 * (=operations) to be performed during the simulation. These actions are
 * specified outside of the host and the host schedule them when the
 * <code>scheduleEvents()</code> method is called.
 * 
 * @author Konstantin Pussep
 * @author Sebastian Kaune
 * @version 3.0, 03.12.2007
 */
public interface SimHost extends Host {

	/**
	 * Convenience method for the NetComponent within the Simulator.
	 * 
	 * @return the NetworkComponent
	 */
	@Override
	public SimNetworkComponent getNetworkComponent();

	/**
	 * Returns the current LinkLayer
	 * 
	 * @return a LinkLayer
	 */
	public LinkLayer getLinkLayer();

	/**
	 * Returns the Information about this Host in the current Topology. Via
	 * this, the physical position of the Host can be accessed
	 * 
	 * @return
	 */
	public TopologyComponent getTopologyComponent();

	/**
	 * Returns the currently used energy model
	 * 
	 * @return
	 */
	public EnergyModel getEnergyModel();

	/**
	 * Returns host properties
	 * 
	 * @return Properties of an Host
	 */
	public HostProperties getProperties();

	/**
	 * Gets an unique identifier for the host.
	 * 
	 * @return A unique identifier for the host
	 */
	public long getHostId();

	/**
	 * Called, after all components for this host have been configured
	 * successfully. This should be used by the components themselves to perform
	 * binding with other layers, as these might have been null beforehand.
	 */
	public void initialize();
}
