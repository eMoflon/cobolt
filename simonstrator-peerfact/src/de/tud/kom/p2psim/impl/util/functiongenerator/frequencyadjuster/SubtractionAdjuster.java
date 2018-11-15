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

package de.tud.kom.p2psim.impl.util.functiongenerator.frequencyadjuster;

import de.tud.kom.p2psim.impl.util.functiongenerator.functions.Function;

/**
 * This frequency adjuster decreases the current frequency of a function by a
 * given amount.
 * 
 * @author Fabio Zöllner
 * 
 */
public class SubtractionAdjuster extends FrequencyAdjuster {
	private double value = 0;

	public SubtractionAdjuster() {
		/* Required for instantiation by a configurator */
	}

	/**
	 * Instantiates a new subtraction adjuster
	 * 
	 * @param simTime
	 *            Simulation time at which the frequency adjuster shall be
	 *            executed
	 * @param function
	 *            Function to be changed
	 * @param value
	 *            Value by which the frequency is decreased
	 * @param interval
	 *            Interval in which the frequency shall be adjusted (0 =
	 *            non-repeating)
	 */
	public SubtractionAdjuster(long simTime, Function function, double value,
			long interval) {
		super(simTime, function, interval);

		this.value = value;
	}

	/**
	 * Instantiates a new non-repeating subtraction adjuster
	 * 
	 * @param simTime
	 *            Simulation time at which the frequency adjuster shall be
	 *            executed
	 * @param function
	 *            Function to be changed
	 * @param value
	 *            Value by which the frequency is decreased
	 */
	public SubtractionAdjuster(long simTime, Function function, double value) {
		super(simTime, function, 0);

		this.value = value;
	}

	@Override
	protected void adjustFrequency() {
		setFrequency(getFrequency() - this.value);
	}

	/**
	 * Sets the value by which the frequency is decreased
	 * 
	 * @param value
	 *            Value by which the frequency is decreased
	 */
	public void setValue(double value) {
		this.value = value;
	}

	/**
	 * Returns the value by which the frequency is decreased
	 * 
	 * @return Value by which the frequency is decreased
	 */
	public double getValue() {
		return this.value;
	}

	@Override
	public String toString() {
		StringBuilder strBuilder = new StringBuilder(this.getClass()
				.getSimpleName())
				.append(" [")
				.append("start=")
				.append(super.getStart())
				.append(", interval=")
				.append(super.getInterval())
				.append(", value=")
				.append(this.value)
				.append(", function=")
				.append(super.getFunction() != null ? super.getFunction()
						.getClass().getSimpleName() : "null").append("]");

		return strBuilder.toString();
	}
}