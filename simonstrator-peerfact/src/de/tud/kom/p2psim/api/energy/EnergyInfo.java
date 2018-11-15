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

import de.tud.kom.p2psim.impl.energy.Battery;

/**
 * This interface is exported by the {@link EnergyModel} of a host to allow
 * access to status information of the model and the battery. Within an
 * application one can use this information to decide based on battery level,
 * for example.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 03.03.2012
 */
public interface EnergyInfo {

	/**
	 * Gets the current status of the {@link Battery} as a Percentage-Value
	 * 
	 * @return double between 0 and 100
	 */
	public double getCurrentPercentage();

	/**
	 * Gets the current energy level of the {@link Battery} in uJ.
	 * 
	 * @return double - the current capacity of the battery in uJ
	 */
	public double getCurrentEnergyLevel();

	/**
	 * Access the Battery - do not use this for functional parts, it is mainly
	 * needed for analyzing
	 * 
	 * @return
	 */
	public Battery getBattery();

}
