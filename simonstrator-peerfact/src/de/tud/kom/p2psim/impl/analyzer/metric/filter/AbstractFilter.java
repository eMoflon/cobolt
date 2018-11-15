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

import java.util.LinkedList;
import java.util.List;

import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.common.metric.AbstractMetric;
import de.tudarmstadt.maki.simonstrator.api.common.metric.ActiveMetric;
import de.tudarmstadt.maki.simonstrator.api.common.metric.ActiveMetric.ActiveMetricListener;
import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric;
import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric.MetricUnit;
import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric.MetricValue;
import de.tudarmstadt.maki.simonstrator.api.common.metric.MetricFilter;

/**
 * Base class of a {@link MetricFilter}
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 08.08.2012
 */
public abstract class AbstractFilter<M extends MetricValue<?>> implements
		MetricFilter {

	private final String name;

	private final LinkedList<Metric<?>> incomingMetrics = new LinkedList<Metric<?>>();

	private final LinkedList<Metric<?>> outgoingMetrics = new LinkedList<Metric<?>>();

	private final LinkedList<DerivedActiveMetric> outgoingActiveMetrics = new LinkedList<DerivedActiveMetric>();

	private String[] blacklist = null;

	private String[] whitelist = null;

	public AbstractFilter(String name) {
		this.name = name;
	}

	public AbstractFilter() {
		this.name = getClass().getSimpleName();
	}

	protected boolean inFilter(Metric metric, String[] filter) {
		for (int i = 0; i < filter.length; i++) {
			String string = filter[i];
			if (metric.getName().equals(string)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public final void initialize(List<Metric<?>> metrics, List<Host> hosts) {
		for (Metric metric : metrics) {
			if (blacklist != null && inFilter(metric, blacklist)) {
				continue;
			}
			if (whitelist != null && !inFilter(metric, whitelist)) {
				continue;
			}
			incomingMetrics.add(metric);
		}
		if (incomingMetrics.isEmpty()) {
			throw new AssertionError("No incoming metrics configured!");
		}

		onInitialize(incomingMetrics);

		for (Metric<?> metric : outgoingMetrics) {
			metric.initialize(hosts);
		}
	}

	/**
	 * Called with the filtered list of metrics. Call createDerivedMetric once
	 * or multiple times in this method!
	 * 
	 * @param incomingMetrics
	 */
	protected abstract void onInitialize(List<Metric<?>> incomingMetrics);

	/**
	 * Call this to create a derived metric out of the specified metrics
	 * 
	 * @param incomingMetrics
	 *            all incoming metrics that are used by the resulting metric
	 * @param isOverallMetric
	 *            false, if this is a per-host metric
	 * @param unit
	 *            Metric unit
	 * @param description
	 * @param isActiveMetric
	 *            is this metric active, i.e. does the filter actively update
	 *            the metric in defined intervals or on other defined actions
	 */
	protected void createDerivedMetric(List<Metric<?>> incomingMetrics,
			boolean isOverallMetric, MetricUnit unit, String description,
			boolean isActiveMetric) {
		DerivedMetric m = null;
		if (isActiveMetric) {
			DerivedActiveMetric am = new DerivedActiveMetric(this, description,
					unit, isOverallMetric, incomingMetrics);
			outgoingActiveMetrics.add(am);
			m = am;
		} else {
			m = new DerivedMetric(this, description, unit, isOverallMetric,
					incomingMetrics);
		}
		outgoingMetrics.add(m);
	}

	/**
	 * Create a derived metric out of a single incoming metric.
	 * 
	 * @param incomingMetric
	 * @param isOverallMetric
	 * @param unit
	 * @param description
	 * @param isActiveMetric
	 *            is this metric active, i.e. does the filter actively update
	 *            the metric in defined intervals or on other defined actions
	 */
	protected void createDerivedMetric(Metric<?> incomingMetric,
			boolean isOverallMetric, MetricUnit unit, String description,
			boolean isActiveMetric) {
		List<Metric<?>> metrics = new LinkedList<Metric<?>>();
		metrics.add(incomingMetric);
		createDerivedMetric(metrics, isOverallMetric, unit, description,
				isActiveMetric);
	}

	@Override
	public final List<Metric<?>> getOutputMetrics() {
		return outgoingMetrics;
	}

	@Override
	public final List<Metric<?>> getInputMetrics() {
		return incomingMetrics;
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * This is invoked once for overall metrics and multiple times for per-host
	 * metrics.
	 * 
	 * @param derivedMetric
	 *            the derived metric
	 * @param inputs
	 * @param host
	 *            or null if it is an overall metric
	 * @return
	 */
	protected abstract M getDerivedMetricValueFor(Metric<?> derivedMetric,
			List<Metric<?>> inputs, Host host);

	/**
	 * Class for a derived metric.
	 * 
	 * @author Bjoern Richerzhagen
	 * @version 1.0, 08.08.2012
	 * @param <M>
	 */
	private class DerivedMetric extends AbstractMetric<M> {

		private final List<Metric<?>> incoming = new LinkedList<Metric<?>>();

		private final boolean isOverallMetric;

		public DerivedMetric(AbstractFilter filter, String description,
				MetricUnit unit, boolean isOverallMetric,
				List<Metric<?>> incomingMetrics) {
			super(getNameForDerivedMetric(incomingMetrics), description, unit);
			this.isOverallMetric = isOverallMetric;
			incoming.addAll(incomingMetrics);
		}

		@Override
		public void initialize(List<Host> hosts) {
			if (isOverallMetric) {
				M overall = getDerivedMetricValueFor(DerivedMetric.this,
						incoming, null);
				setOverallMetric(overall);
			} else {
				for (Host host : hosts) {
					M perHost = getDerivedMetricValueFor(DerivedMetric.this,
							incoming, host);
					if (perHost != null) {
						addHost(host, perHost);
					}
				}
			}
		}

		@Override
		public boolean isOverallMetric() {
			return isOverallMetric;
		}

	}

	/**
	 * Class for a derived metric that implements the active metric interface
	 * 
	 * @author Bjoern Richerzhagen
	 * @version 1.0, 13.08.2012
	 */
	private class DerivedActiveMetric extends DerivedMetric implements
			ActiveMetric<M> {

		private final List<ActiveMetricListener> listeners;

		public DerivedActiveMetric(AbstractFilter filter, String description,
				MetricUnit unit, boolean isOverallMetric,
				List<Metric<?>> incomingMetrics) {
			super(filter, description, unit, isOverallMetric, incomingMetrics);
			listeners = new LinkedList<ActiveMetricListener>();
		}

		@Override
		public void addActiveMetricListener(ActiveMetricListener listener) {
			listeners.add(listener);
		}

		protected void notifyListenersOfUpdate() {
			for (ActiveMetricListener listener : listeners) {
				listener.onMetricUpdate(DerivedActiveMetric.this);
			}
		}
	}

	/**
	 * Call this to notify all {@link ActiveMetricListener}s of an update of one
	 * or many {@link ActiveMetric}s
	 */
	protected void notifyListenersOfUpdate() {
		for (DerivedActiveMetric am : outgoingActiveMetrics) {
			am.notifyListenersOfUpdate();
		}
	}

	/**
	 * Use this to name your derived metric.
	 * 
	 * @param inputs
	 * @return
	 */
	protected String getNameForDerivedMetric(List<Metric<?>> inputs) {
		String str = getName();
		for (Metric metric : inputs) {
			str += "_" + metric.getName();
		}
		return str;
	}

	/**
	 * List of Metrics
	 * 
	 * @param blacklist
	 */
	public void setBlacklist(String blacklist) {
		this.blacklist = blacklist.split(";");
	}

	public void setWhitelist(String whitelist) {
		this.whitelist = whitelist.split(";");
	}

}
