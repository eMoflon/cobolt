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
 * ZigZagFunction provides a square function for the function generator. floor(x
 * / frequency) % 2 == 0 ? 0 : magnitude
 * 
 * @author Fabio Zöllner
 * 
 */
public class ZigZagFunction extends Function {

	public ZigZagFunction() {
		/* Required for instantiation by a configurator */
	}

	/**
	 * Instantiates a new zigzag function
	 * 
	 * @param maxMagnitude
	 *            Maximum magnitude of the function
	 * @param frequency
	 *            Frequency of the function
	 */
	public ZigZagFunction(double maxMagnitude, double frequency) {
		super(maxMagnitude, frequency);
	}

	/*
	 * Reference Code: de.tud.kom.p2psim.impl.skynet.metrics.ReferenceMetrics
	 * 
	 * long modPeriodeT = Simulator.getCurrentTime() % period;
	 * 
	 * if (modPeriodeT < (period / 2)) return 0; return 1;
	 */

	@Override
	public double execute(long x) {
		return Math.floor(x / super.getFrequency()) % 2 == 0 ? 0 : super
				.getMaxMagnitude();
	}

	@Override
	public double getDerivativeAt(long x) {
		return 0;
	}
}
