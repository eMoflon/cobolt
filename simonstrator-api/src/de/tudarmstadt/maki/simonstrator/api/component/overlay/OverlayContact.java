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

package de.tudarmstadt.maki.simonstrator.api.component.overlay;

import java.util.Collection;

import de.tudarmstadt.maki.simonstrator.api.common.Transmitable;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetworkComponent.NetInterfaceName;

/**
 * API-interface for OverlayContacts. Updated to support multiple netInterfaces
 * for one single contact (e.g., BT + WiFi + Cell).
 * 
 * @author Bjoern Richerzhagen
 *
 */
public interface OverlayContact extends Transmitable {

	/**
	 * The {@link INodeID} of the host this contact runs on (equal for all
	 * overlays). Optionally, a contact might contain an ID that is specific to
	 * the overlay, but this is not part of this generic interface.
	 * 
	 * @return
	 */
	public INodeID getNodeID();

	/**
	 * Returns the NetID of an overlay node for the given netInterface (to
	 * support overlays with, e.g., cell + wifi ad hoc connectivity).
	 * 
	 * 
	 * @return null, if the contact is not known on the given interface
	 */
	public NetID getNetID(NetInterfaceName netInterface);

	/**
	 * Returns the (listening) port for the respective netInterface.
	 * 
	 * @param netId
	 * @return -1, if the contact is not known on the given interface
	 */
	public int getPort(NetInterfaceName netInterface);

	/**
	 * A collection of all interfaces we have contact information on.
	 * 
	 * @return
	 */
	public Collection<NetInterfaceName> getInterfaces();

}
