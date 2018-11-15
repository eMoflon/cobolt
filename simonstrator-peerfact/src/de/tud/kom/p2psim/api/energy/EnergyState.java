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
 * A State of a {@link EnergyComponent} in the {@link EnergyModel}.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 25.02.2012
 */
public interface EnergyState {

	/**
	 * Return a name for this state, this should used in debugging and for
	 * analyzers. Examples are IDLE, ON, OFF...
	 * 
	 * @return
	 */
	public String getName();

	/**
	 * This method has to return the energy consumption per Second in uW in this
	 * state. An energy model will then perform the calculation of the total
	 * consumption. As energy consumption in real life is not just the sum of
	 * all active components, the energy model may perform a more complex
	 * calculation based on other components states.
	 * 
	 * @return
	 */
	public double getEnergyConsumption();

}
