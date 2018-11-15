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
import de.tudarmstadt.maki.simonstrator.api.Event;
import de.tudarmstadt.maki.simonstrator.api.EventHandler;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.Time;

/**
 * The abstract FrequencyAdjuster provides a class to implement
 * simulation events that modify the frequency of a specific function.
 * 
 * @author Fabio Zöllner
 * 
 */
public abstract class FrequencyAdjuster implements EventHandler {
    private long start = 0;
	private Function function = null;
	private long interval = 0;
    private long executionCounter = 1;
    private long maxExecutions = 0;
    private long stop = -1; // -1 to allow a stop at 0 ms sim time

    public FrequencyAdjuster() {
        /* Required for instantiation by a configurator */
    }

    /**
     * Instantiates a new frequency adjuster
     *
     * @param simTime Simulation time at which the frequency adjuster shall be executed
     * @param function Function to be changed
     * @param interval Interval in which the frequency shall be adjusted (0 = non-repeating)
     */
	public FrequencyAdjuster(long simTime, Function function, long interval) {
        this.start = simTime;
        this.function = function;
        this.interval = interval;
	}

    /**
     * Schedules the frequency adjuster with the current start time
     */
	public void schedule() {
		scheduleAdjustment(this.start);
	}

    /**
     * Schedules the frequency adjuster with the given start time
     *
     * @param simTime Simulation time at which the frequency shall be adjusted
     */
	private void scheduleAdjustment(long simTime) {
		Event.scheduleWithDelay(simTime, this, null, 0);
	}

	@Override
	public void eventOccurred(Object se, int type) {
		if (interval != 0
                && !(maxExecutions > 0 && executionCounter >= maxExecutions)
				&& !(stop >= 0 && Time.getCurrentTime() >= stop)) {
			scheduleAdjustment(Time.getCurrentTime() + interval);
		}

        if (this.function != null) {
		    adjustFrequency();
            this.executionCounter++;
        } else {
			Monitor.log(FrequencyAdjuster.class, Level.WARN,
					"Missing function, couldn't adjust frequency at "
							+ this.start + " with "
							+ this.getClass().getSimpleName());
        }
	}

	/**
	 * This method is called when the event occurred
	 * and the frequency needs to be changed.
	 */
	protected abstract void adjustFrequency();

    public void setStart(long simTime) {
        this.start = simTime;
    }

    public long getStart() {
        return this.start;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public long getInterval() {
        return this.interval;
    }

    public void setFunction(Function function) {
        this.function = function;
    }

    public Function getFunction() {
        return this.function;
    }

    @Override
    public String toString() {
        StringBuilder strBuilder = new StringBuilder(this.getClass().getSimpleName())
                .append(" [")
                .append("start=").append(this.start)
                .append(", interval=").append(this.interval)
                .append(", function=")
                .append(this.function != null ? this.function.getClass().getSimpleName() : "null")
                .append("]");

        return strBuilder.toString();
    }

    public long getMaxExecutions() {
        return maxExecutions;
    }

    public void setMaxExecutions(long maxExecutions) {
        this.maxExecutions = maxExecutions;
    }

    public long getStop() {
        return stop;
    }

    public void setStop(long stop) {
        this.stop = stop;
    }

    protected double getFrequency() {
        return this.function.getFrequency();
    }

    protected void setFrequency(double frequency) {
        this.function.setFrequencyDouble(frequency);
    }
}
