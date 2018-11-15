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

import de.tudarmstadt.maki.simonstrator.api.util.XMLConfigurableConstructor;

/**
 * Internally, all Energy-Related values are calculated in uJ (10^-6). Only for
 * the construction of batteries, the value is passed in J.
 * 
 * @author Bjoern Richerzhagen, Fabio Zöllner
 * @version 1.0, 24.04.2012
 */
public class SimpleBattery implements Battery {

	private double capacity;

	private double initialEnergy;

	private double currentEnergy;

	private boolean isEmpty = false;

	/**
	 * Create a new battery with the given initial energy and total capacity in
	 * Joule (<b>not</b> uJ).
	 * 
	 * @param capacity
	 * @param initalEnergy
	 */
	@XMLConfigurableConstructor({ "capacity", "initialEnergy" })
	public SimpleBattery(double capacity, double initalEnergy) {
		this.capacity = capacity * 1000000;
		this.initialEnergy = initalEnergy * 1000000;
		this.currentEnergy = initalEnergy * 1000000;
	}

	@Override
	public double getCurrentPercentage() {
		if (isEmpty) {
			return 0.0;
		}
		double percent = 100.0 * currentEnergy / capacity;
		assert percent <= 100 && percent >= 0;
		return percent;
	}

	@Override
	public double getCurrentEnergyLevel() {
		if (isEmpty) {
			return 0.0;
		}
		return currentEnergy;
	}

	@Override
	public double getConsumedEnergy() {
		return capacity - currentEnergy;
	}

	@Override
	public void reset() {
		currentEnergy = initialEnergy;
		isEmpty = false;
	}

	@Override
	public void setToPercentage(double percentage) {
		currentEnergy = (capacity / 100.0) * percentage;
		isEmpty = false;
	}

	@Override
	public void consumeEnergy(double energy) {
		if (isEmpty || energy >= currentEnergy) {
			isEmpty = true;
		} else {
			currentEnergy = currentEnergy - energy;
		}
	}

	@Override
	public boolean isEmpty() {
		return isEmpty;
	}

	@Override
	public SimpleBattery clone() {
		return new SimpleBattery(capacity / 1000000, initialEnergy / 1000000);
	}

}