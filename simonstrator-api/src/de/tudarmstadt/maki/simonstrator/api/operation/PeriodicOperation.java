/*
 * Copyright (c) 2005-2010 KOM – Multimedia Communications Lab
 *
 * This file is part of Simonstrator.KOM.
 * 
 * Simonstrator.KOM is free software: you can redistribute it and/or modify
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

package de.tudarmstadt.maki.simonstrator.api.operation;

import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.component.HostComponent;
import de.tudarmstadt.maki.simonstrator.api.util.Distribution;

/**
 * 
 * @author Bjoern Richerzhagen
 * 
 */
public abstract class PeriodicOperation<T extends HostComponent, R> extends
		AbstractOperation<T, R> {

	private boolean stopped = true;

	private Distribution intervalDist;

	private long intervalDistScaling = Time.MICROSECOND;

	private int counter = 0;

	private long interval;

	private long currentInterval = 0;

	/**
	 * A more flexible approach to periodic operations with an interval-length
	 * based on a distribution. You can provide a callback and a scaling value.
	 * If your Distribution returns values that should be interpreted in ms for
	 * example, you would pass Simulator.MILLISECOND_UNIT as last parameter.
	 * 
	 * @param component
	 * @param callback
	 * @param intervalDistribution
	 * @param simTimeScaling
	 */
	protected PeriodicOperation(T component,
			OperationCallback<R> callback,
			Distribution intervalDistribution, long simTimeScaling) {
		super(component, callback);
		this.intervalDist = intervalDistribution;
		this.intervalDistScaling = simTimeScaling;
		assert simTimeScaling == Time.HOUR || simTimeScaling == Time.MINUTE
				|| simTimeScaling == Time.SECOND
				|| simTimeScaling == Time.MILLISECOND
				|| simTimeScaling == Time.MICROSECOND;
	}

	/**
	 * Create a periodic operation. Once started, it will be executed in every
	 * interval, where Interval has to be specified in Simultion-Time-Units
	 * 
	 * @param component
	 *            the parent component
	 * @param callback
	 *            a callback that is called every time the operation is executed
	 *            and finishes, either with or without success
	 * @param interval
	 *            a time in Simulation units
	 */
	protected PeriodicOperation(T component,
			OperationCallback<R> callback,
			long interval) {
		super(component, callback);
		this.interval = interval;
	}

	@Override
	protected final void execute() {
		if (counter > 0) {
			// stop prevous iteration, notify callback
			if (!isFinished()) {
				operationTimeoutOccured();
			}
		}
		if (!stopped) {
			// reset internal state of last iteration
			resetInternalState();
			counter++;
			this.executeOnce();
		}
		// it is possible, that the operation will be stopped in executeOnce.
		if (!stopped) {
			long nextInterval = calculateNextInterval();
			if (nextInterval < 0) {
				nextInterval = 0;
			}
			currentInterval = nextInterval;
			this.scheduleWithDelay(nextInterval);
		}
	}

	/**
	 * This method should <b>ONLY</b> be called by the {@link PeriodicOperation}
	 * itself, as it manipulates the distribution. To retrieve the last chosen
	 * interval, use getInterval.
	 * 
	 * @return
	 */
	protected long calculateNextInterval() {
		if (intervalDist != null) {
			return Math.round(intervalDist.returnValue() * intervalDistScaling);
		} else {
			return interval;
		}
	}

	/**
	 * This method can be used to access the current interval between two
	 * executions of this operation.
	 * 
	 * @return
	 */
	public long getInterval() {
		return currentInterval;
	}

	/**
	 * Number of times this Periodic Operation was executed
	 * 
	 * @return
	 */
	public int getIterationCount() {
		return counter;
	}

	/**
	 * Stops the periodic execution. Any subsequent calls of executeOnce() will
	 * not be made.
	 */
	public void stop() {
		this.stopped = true;
	}

	/**
	 * Same functionality like scheduleImmediately
	 */
	public void start() {
		if (!isStopped()) {
			throw new AssertionError(
					"Periodic Operation already running - ignoring start()!");
		}
		this.scheduleImmediately();
	}

	/**
	 * Same functionality like scheduleWithDelay
	 * 
	 * @param delay
	 *            The delay for the first execution
	 */
	public void startWithDelay(long delay) {
		if (!isStopped()) {
			throw new AssertionError(
					"Periodic Operation already running - ignoring start()!");
		}
		this.scheduleWithDelay(delay);
	}

	@Override
	public void scheduleWithDelay(long delay) {
		this.stopped = false;
		super.scheduleWithDelay(delay);
	}

	/**
	 * True, if the operation is currently stopped or was not yet scheduled to
	 * start.
	 * 
	 * @return
	 */
	public boolean isStopped() {
		return stopped;
	}

	protected abstract void executeOnce();

}
