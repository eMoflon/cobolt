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
 * LinearFunction provides a linear function for the function generator. (x /
 * frequency)
 * 
 * @author Fabio Zöllner
 * 
 */
public class LinearFunction extends Function {

	public LinearFunction() {
		/* Required for instantiation by a configurator */
	}

	/**
	 * Instantiates a new linear function
	 * 
	 * @param maxMagnitude
	 *            Maximum magnitude of the function
	 * @param frequency
	 *            Frequency of the function
	 */
	public LinearFunction(double maxMagnitude, double frequency) {
		super(maxMagnitude, frequency);
	}

	@Override
	public double execute(long x) {
		return x / super.getFrequency();
	}

	@Override
	public double getDerivativeAt(long x) {
		return 1.0 / super.getFrequency();
	}
}
