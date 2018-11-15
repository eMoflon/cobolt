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

import de.tud.kom.p2psim.api.energy.EnergyState;

/**
 * Basic implementation of an EnergyState
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 03.03.2012
 */
public class DefaultEnergyState implements EnergyState {

	private String name;

	private double energyConsumption;

	/**
	 * 
	 * @param name
	 *            identifier of this state
	 * @param energyConsumption
	 *            consumption in uW
	 */
	public DefaultEnergyState(String name, double energyConsumption) {
		this.name = name;
		this.energyConsumption = energyConsumption;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public double getEnergyConsumption() {
		return energyConsumption;
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(energyConsumption);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DefaultEnergyState other = (DefaultEnergyState) obj;
		if (Double.doubleToLongBits(energyConsumption) != Double
				.doubleToLongBits(other.energyConsumption))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
