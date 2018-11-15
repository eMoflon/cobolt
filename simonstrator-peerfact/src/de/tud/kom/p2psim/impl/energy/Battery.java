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

package de.tud.kom.p2psim.impl.energy;

/**
 * This interface implements basic methods used by the EnergyModel to simulate a
 * battery.
 * 
 * @author Fabio Zöllner
 * @version 1.0, 20.04.2012
 */
public interface Battery extends Cloneable {

	/**
	 * Returns the current status of the Battery as a Percentage-Value
	 * 
	 * @return double between 0 and 100
	 */
	public double getCurrentPercentage();

	/**
	 * Returns the current energy level in uJ.
	 * 
	 * @return the current capacity of the battery in uJ
	 */
	public double getCurrentEnergyLevel();

	/**
	 * Returns the consumed energy in uJoule
	 * 
	 * @return The consumed Energy in uJoule
	 */
	public double getConsumedEnergy();

	/**
	 * Resets the Battery to the initial Energy-Level
	 */
	public void reset();

	/**
	 * Sets the battery to a given percentage.
	 * 
	 * @param double between 0 and 100
	 */
	public void setToPercentage(double percentage);

	/**
	 * Consumes the given amount of energy in uJoule (10^-6)
	 * 
	 * @param energy
	 *            Energy in uJoule
	 */
	public void consumeEnergy(double energy);

	/**
	 * Returns true, if the battery is empty. In such a case, there should be no
	 * further calls to consumeEnergy.
	 * 
	 * @return
	 */
	public boolean isEmpty();

	/**
	 * Clone a battery, used during configuration
	 * 
	 * @param battery
	 * @return
	 */
	public Battery clone();

}
