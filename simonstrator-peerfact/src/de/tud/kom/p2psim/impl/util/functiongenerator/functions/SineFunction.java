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
 * SineFunction provides a sine function for the function generator. sin( ((PI *
 * 2) / frequency) * x ) * magnitude;
 * 
 * @author Fabio Zöllner
 * 
 */
public class SineFunction extends Function {

	public SineFunction() {
		/* Required for instantiation by a configurator */
	}

	/**
	 * Instantiates a new sine function
	 * 
	 * @param maxMagnitude
	 *            Maximum magnitude of the function
	 * @param frequency
	 *            Frequency of the function
	 */
	public SineFunction(double maxMagnitude, double frequency) {
		super(maxMagnitude, frequency);
	}

	/*
	 * Reference Code: de.tud.kom.p2psim.impl.skynet.metrics.ReferenceMetrics
	 * 
	 * public static double sin(long period) { double value =
	 * Math.sin((Simulator.getCurrentTime() / (double) period) 2 * Math.PI);
	 * 
	 * return value; }
	 */

	@Override
	public double execute(long x) {
		return Math.sin((x / super.getFrequency()) * 2 * Math.PI)
				* super.getMaxMagnitude();
	}

	@Override
	public double getDerivativeAt(long x) {
		return Math.cos((x / super.getFrequency()) * 2 * Math.PI)
				* super.getMaxMagnitude() * (1 / super.getFrequency()) * 2
				* Math.PI;
	}
}
