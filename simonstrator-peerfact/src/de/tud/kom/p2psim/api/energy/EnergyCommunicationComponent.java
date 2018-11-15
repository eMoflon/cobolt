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

package de.tud.kom.p2psim.api.energy;

import de.tud.kom.p2psim.api.linklayer.mac.PhyType;
import de.tudarmstadt.maki.simonstrator.api.Message;

/**
 * An interface for an {@link EnergyComponent} that is used as a communication
 * medium. We need to additionally define the PHY-Type to support automatic
 * binding with the corresponding MAC-Layer.
 * 
 * All {@link EnergyCommunicationComponent}-Components have to return
 * COMMUNICATION as their {@link ComponentType}!
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 25.02.2012
 */
public interface EnergyCommunicationComponent extends EnergyComponent {

	/**
	 * Get the PHY-Medium this component works on (to allow automatic
	 * configuration of the MAC-Layer which will search for the right type)
	 * 
	 * @return
	 */
	public PhyType getPhyType();

	/**
	 * Energy consumption triggered by a SEND for the given duration
	 * 
	 * @param duration
	 * @param msg
	 * @param isBroadcast
	 */
	public void send(long duration, Message msg, boolean isBroadcast);
	
	/**
	 * Energy consumption triggered by a RECEIVE for the given duration
	 * 
	 * @param duration
	 * @param msg
	 * @param isBroadcast
	 * @param isIntendedReceiver
	 */
	public void receive(long duration, Message msg, boolean isBroadcast,
			boolean isIntendedReceiver);
	
	/**
	 * Just to ensure that the energy calculation is correct, once you rely on
	 * the energy level in an analyzer you should call this method prior to
	 * querying the battery.
	 */
	public void doFakeStateChange();

}
