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

import de.tudarmstadt.maki.simonstrator.api.EventHandler;

/**
 * A component that consumes energy (ie. a radio or a GPS-receiver).
 * State-switching is event-based in this new version of the Model to allow more
 * fine-grained analyzing.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 21.02.2012
 */
public interface EnergyComponent extends EventHandler {

	/**
	 * Type of this Component. Multiple components of a given type are allowed,
	 * for example to add multiple Communication Interfaces.
	 * 
	 * @return
	 */
	public ComponentType getType();

	/**
	 * Generic method to deactivate a component. <b>DO NOT use this method on a
	 * CommunicationComponent directly. Call goOffline() on the corresponding
	 * Mac-Layer instead!</b>
	 */
	public void turnOff();

	/**
	 * Generic method to activate a component. <b>DO NOT use this method on a
	 * CommunicationComponent directly. Call goOffline() on the corresponding
	 * Mac-Layer instead!</b>
	 * 
	 * @return true, if the component was switched on. False, if this operation
	 *         was not successful (battery might be empty)
	 */
	public boolean turnOn();

	/**
	 * Return false, if this component is currently in the OFF-State
	 * 
	 * @return
	 */
	public boolean isOn();

	/**
	 * Event-based approach for an {@link EnergyModel}. The model registers via
	 * this method with each component. Interested Analyzers should directly
	 * register with the EnergyModel.
	 * 
	 * @param listener
	 */
	public void setEnergyEventListener(EnergyEventListener listener);

}
