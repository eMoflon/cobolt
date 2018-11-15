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

/**
 * The {@link EnergyModel} registers via this listener with each
 * {@link EnergyComponent}.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 27.02.2012
 */
public interface EnergyEventListener {

	
	
	/**
	 * Notify the Model that a component switched its state
	 * 
	 * @param component
	 * @param oldState
	 * @param newState
	 * @param timeSpentInOldState
	 */
	public void switchedState(EnergyComponent component, EnergyState oldState,
			EnergyState newState, long timeSpentInOldState);

	/**
	 * Allows the energy model to check, if a component is able to be turned on.
	 * Return false, if the battery is empty or there is any other reason for
	 * the component not to be online.
	 * 
	 * @param component
	 * @return
	 */
	public boolean turnOn(EnergyComponent component);

}
