/*
 * Copyright (c) 2005-2010 KOM – Multimedia Communications Lab
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

package de.tud.kom.p2psim.impl.analyzer.metric.filter;

import java.util.List;

import de.tudarmstadt.maki.simonstrator.api.Event;
import de.tudarmstadt.maki.simonstrator.api.EventHandler;
import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric;
import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric.MetricValue;

/**
 * Filter with the ability to sample data in specified intervals.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 08.08.2012
 * @param <M>
 */
public abstract class AbstractPeriodicSampleFilter<M extends MetricValue<?>>
		extends AbstractFilter<M> implements EventHandler {

	private final long intervalLength;

	/**
	 * 
	 * @param intervalLength
	 */
	public AbstractPeriodicSampleFilter(long intervalLength) {
		super();
		this.intervalLength = intervalLength;
	}

	@Override
	protected void onInitialize(List<Metric<?>> incomingMetrics) {
		scheduleNextInterval();
	}

	public long getIntervalLength() {
		return intervalLength;
	}

	/**
	 * Called at the end of an interval, use this to update the metric
	 * accordingly.
	 */
	protected abstract void onNewInterval();

	@Override
	public void eventOccurred(Object se, int type) {
		onNewInterval();
		notifyListenersOfUpdate();
		scheduleNextInterval();
	}

	private void scheduleNextInterval() {
		Event.scheduleWithDelay(intervalLength, this, null, 0);
	}

}
