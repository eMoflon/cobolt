/*
 * Copyright (c) 2005-2011 KOM - Multimedia Communications Lab
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

package de.tud.kom.p2psim.impl.util.functiongenerator.functions;

/**
 * SawtoothFunction provides a sawtooth function for the function generator. (x
 * % frequency) * (frequency > 1 ? (magnitude / (frequency - 1)) : 0)
 * 
 * @author Fabio Zöllner
 * 
 */
public class SawtoothFunction extends Function {

	public SawtoothFunction() {
		/* Required for instantiation by a configurator */
	}

	/**
	 * Instantiates a new sawtooth function
	 * 
	 * @param maxMagnitude
	 *            Maximum magnitude of the function
	 * @param frequency
	 *            Frequency of the function
	 */
	public SawtoothFunction(double maxMagnitude, double frequency) {
		super(maxMagnitude, frequency);
	}

	@Override
	public double execute(long x) {
		double factor = 0.0;

		if (super.getFrequency() > 1) {
			factor = super.getMaxMagnitude() / (super.getFrequency() - 1);
		}

		return (x % this.getFrequency()) * factor;
	}

	@Override
	public double getDerivativeAt(long x) {
		double factor = 0.0;
		double h = 0.000030518; // 2^-15

		if (super.getFrequency() > 1) {
			factor = super.getMaxMagnitude() / (super.getFrequency() - 1);
		}
		return ((x + h % this.getFrequency()) * factor - (x - h
				% this.getFrequency())
				* factor)
				/ (2 * h);
	}
}
