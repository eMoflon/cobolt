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
 * This frequency adjuster multiplies the current
 * frequency of a function by a given amount.
 * 
 * @author Fabio Zöllner
 *
 */
public class MultiplicationAdjuster extends FrequencyAdjuster {
	private double factor = 1.0;

    public MultiplicationAdjuster() {
        /* Required for instantiation by a configurator */
    }

    /**
     * Instantiates a new multiplication adjuster
     *
     * @param simTime Simulation time at which the multiplication adjuster shall be executed
     * @param function Function to be changed
     * @param factor Factor by which the frequency is multiplied
     * @param interval Interval in which the frequency shall be adjusted (0 = non-repeating)
     */
	public MultiplicationAdjuster(long simTime, Function function, double factor, long interval) {
		super(simTime, function, interval);
		
		this.factor = factor;
	}

    /**
     * Instantiates a new non-repeating multiplication adjuster
     *
     * @param simTime Simulation time at which the multiplication adjuster shall be executed
     * @param function Function to be changed
     * @param factor Factor by which the frequency is multiplied
     */
	public MultiplicationAdjuster(long simTime, Function function, double factor) {
		super(simTime, function, 0);
		
		this.factor = factor;
	}

	@Override
	protected void adjustFrequency() {
		setFrequency(getFrequency() * this.factor);
	}

    /**
     * Sets the factor by which the frequency is multiplied
     *
     * @param factor Factor by which the frequency is multiplied
     */
    public void setFactor(double factor) {
        this.factor = factor;
    }

    /**
     * Returns the factor by which the frequency is multiplied
     *
     * @return Factor by which the frequency is multiplied
     */
    public double getFactor() {
        return this.factor;
    }

    @Override
    public String toString() {
        StringBuilder strBuilder = new StringBuilder(this.getClass().getSimpleName())
                .append(" [")
                .append("start=").append(super.getStart())
                .append(", interval=").append(super.getInterval())
                .append(", factor=").append(this.factor)
                .append(", function=")
                .append(super.getFunction() != null ? super.getFunction().getClass().getSimpleName() : "null")
                .append("]");

        return strBuilder.toString();
    }
}
