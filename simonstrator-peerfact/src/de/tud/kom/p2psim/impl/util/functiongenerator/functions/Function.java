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

import java.util.LinkedList;
import java.util.List;

import de.tud.kom.p2psim.impl.util.functiongenerator.frequencyadjuster.FrequencyAdjuster;

/**
 * The Function class can be used to implement different functions for the
 * function generator.
 * 
 * @author Fabio Zöllner
 * 
 */
public abstract class Function {

	private List<FrequencyAdjuster> frequencyAdjusters = new LinkedList<FrequencyAdjuster>();

	private double maxMagnitude = 0;

	private double frequency = 0;

	public Function() {
		/* Required for instantiation by a configurator */
	}

	/**
	 * Instantiates a new function
	 * 
	 * @param maxMagnitude
	 *            Maximum magnitude of the function
	 * @param frequency
	 *            Frequency of the function
	 */
	public Function(double maxMagnitude, double frequency) {
		this.maxMagnitude = maxMagnitude;
		this.frequency = frequency;
	}

	public abstract double execute(long x);

	/**
	 * Compute the derivative of function at the given param x.
	 * 
	 * @param x
	 *            a time
	 * @return The derivative at time x from this function
	 */
	public double getDerivativeAt(long x) {
		// derive f'(x)=(f(x+h)-f(x))/h, with h=1;
		return execute(x + 1) - execute(x);
	}

	/**
	 * Returns the frequency of the function
	 * 
	 * @return Frequency of the function
	 */
	public double getFrequency() {
		return frequency;
	}

	/**
	 * Sets the frequency of the function as a long value to allow the
	 * DefaultConfigurator to set values of 1s, 2m or 3h and so on.
	 * 
	 * The DefaultConfigurator does not support method overloading. To be able
	 * to set double values use setFrequencyDouble()
	 * 
	 * @param frequency
	 *            Frequency of the function
	 */
	public void setFrequency(long frequency) {
		this.frequency = frequency;
	}

	/**
	 * Returns the maximum magnitude of the function
	 * 
	 * @return Maximum magnitude of the function
	 */
	public double getMaxMagnitude() {
		return maxMagnitude;
	}

	/**
	 * Sets the maximum magnitude of the function
	 * 
	 * @param maxMagnitude
	 *            Maximum magnitude of the function
	 */
	public void setMaxMagnitude(long maxMagnitude) {
		this.maxMagnitude = maxMagnitude;
	}

	/**
	 * Adds ands schedules a new frequency adjuster for this function
	 * 
	 * @param adjuster
	 *            Frequency adjuster for this function
	 */
	public final void setFrequencyAdjuster(FrequencyAdjuster adjuster) {
		adjuster.setFunction(this);

		this.frequencyAdjusters.add(adjuster);

		adjuster.schedule();
	}

	/**
	 * Returns a list of all loaded frequency adjusters of the function
	 * 
	 * @return List of frequency adjusters
	 */
	public List<FrequencyAdjuster> getFrequencyAdjusters() {
		return this.frequencyAdjusters;
	}

	@Override
	public String toString() {
		StringBuilder strBuilder = new StringBuilder(this.getClass()
				.getSimpleName()).append(" [").append("frequency=")
				.append(this.frequency).append(", maxMagnitude=")
				.append(this.maxMagnitude).append("]");

		return strBuilder.toString();
	}

	/**
	 * Used within the frequency adjusters to set accurate frequencies
	 * 
	 * @param frequency
	 *            Frequency of the function
	 */
	public void setFrequencyDouble(double frequency) {
		this.frequency = frequency;
	}
}
