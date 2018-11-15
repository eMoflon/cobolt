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
 * This frequency adjuster divides the current
 * frequency of a function by a given amount.
 * 
 * @author Fabio Zöllner
 *
 */
public class DivisionAdjuster extends FrequencyAdjuster {
	private double divisor = 1.0;

    public DivisionAdjuster() {
        /* Required for instantiation by a configurator */
    }

    /**
     * Instantiates a new division adjuster
     *
     * @param simTime Simulation time at which the frequency adjuster shall be executed
     * @param function Function to be changed
     * @param divisor Divisor by which the frequency is divided
     * @param interval Interval in which the frequency shall be adjusted (0 = non-repeating)
     */
	public DivisionAdjuster(long simTime, Function function, double divisor, long interval) {
		super(simTime, function, interval);
		
		this.divisor = divisor;
	}

    /**
     * Instantiates a new non-repeating division adjuster
     *
     * @param simTime Simulation time at which the frequency adjuster shall be executed
     * @param function Function to be changed
     * @param divisor Divisor by which the frequency is divided
     */
	public DivisionAdjuster(long simTime, Function function, double divisor) {
		super(simTime, function, 0);
		
		this.divisor = divisor;
	}

	@Override
	protected void adjustFrequency() {
		setFrequency(getFrequency() / this.divisor);
	}

    /**
     * Sets the divisor by which the frequency is divided
     *
     * @param divisor Divisor by which the frequency is divided
     */
    public void setDivisor(double divisor) {
        this.divisor = divisor;
    }

    /**
     * Sets the divisor by which the frequency is divided
     *
     * @return Divisor by which the frequency is divided
     */
    public double getDivisor() {
        return this.divisor;
    }

    @Override
    public String toString() {
        StringBuilder strBuilder = new StringBuilder(this.getClass().getSimpleName())
                .append(" [")
                .append("start=").append(super.getStart())
                .append(", interval=").append(super.getInterval())
                .append(", divisor=").append(this.divisor)
                .append(", function=")
                .append(super.getFunction() != null ? super.getFunction().getClass().getSimpleName() : "null")
                .append("]");

        return strBuilder.toString();
    }
}
