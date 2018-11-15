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

import de.tudarmstadt.maki.simonstrator.api.component.LifecycleComponent;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetworkComponent;

/**
 * Extension of the {@link NetworkComponent} to enable easier access to the
 * {@link NetLayer}-interface within the simulator. This is, for example, used
 * by the ChurnGenerator and/or Energy model.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, Aug 5, 2013
 */
public interface SimNetworkComponent extends NetworkComponent, LifecycleComponent {

	/**
	 * Returns the NetInterface that corresponds to the local IP.
	 * 
	 * @param netID
	 * @return
	 */
	public SimNetInterface getByNetId(NetID netID);

	/**
	 * Returns the NetInterface that is using the given physical technology.
	 * 
	 * @param name
	 * @return
	 */
	public SimNetInterface getByName(NetInterfaceName name);

	/**
	 * Returns all network interfaces available on this Host.
	 * 
	 * @return
	 */
	public Iterable<SimNetInterface> getSimNetworkInterfaces();

}
