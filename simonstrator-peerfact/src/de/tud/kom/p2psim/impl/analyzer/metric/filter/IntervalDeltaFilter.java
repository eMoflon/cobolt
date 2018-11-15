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
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric;
import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric.MetricValue;
import de.tudarmstadt.maki.simonstrator.api.util.XMLConfigurableConstructor;

/**
 * A filter that provides a metric recoding the change of another metric in a
 * given interval.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 10.08.2012
 */
public class IntervalDeltaFilter extends
		AbstractPeriodicSampleFilter<MetricValue<Double>> {

	private List<DeltaMetricValue> metricValues = new LinkedList<DeltaMetricValue>();

	private String prefix;

	@XMLConfigurableConstructor({ "intervalLength" })
	public IntervalDeltaFilter(long intervalLength) {
		super(intervalLength);
	}

	@Override
	protected void onNewInterval() {
		for (DeltaMetricValue mv : metricValues) {
			mv.onNewInterval();
		}
	}

	@Override
	public void onStop() {
		// here, we do nothing
	}

	@Override
	protected void onInitialize(List<Metric<?>> incomingMetrics) {
		for (Metric metric : incomingMetrics) {
			createDerivedMetric(metric, metric.isOverallMetric(),
					metric.getUnit(), "delta t=" + getIntervalLength()
							/ Time.SECOND + "s of " + metric.getDescription(),
					true);
		}
		super.onInitialize(incomingMetrics);
	}

	/**
	 * Prefix to use instead of the Filtername
	 * 
	 * @param prefix
	 * @return
	 */
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	@Override
	protected String getNameForDerivedMetric(List<Metric<?>> inputs) {
		assert inputs.size() == 1;
		if (prefix == null) {
			return "Delta" + getIntervalLength() / Time.SECOND + "s_"
					+ inputs.get(0).getName();
		} else {
			return prefix + "_" + inputs.get(0).getName();
		}
	}

	@Override
	protected MetricValue<Double> getDerivedMetricValueFor(
			Metric<?> derivedMetric, List<Metric<?>> inputs, Host host) {
		DeltaMetricValue mv;
		if (inputs.size() != 1) {
			throw new AssertionError();
		}
		Metric input = inputs.get(0);
		MetricValue inputMv = null;
		if (input.isOverallMetric()) {
			inputMv = input.getOverallMetric();
		} else {
			inputMv = input.getPerHostMetric(host.getId());
		}
		if (inputMv == null) {
			return null;
		}
		mv = new DeltaMetricValue(inputMv);
		metricValues.add(mv);
		return mv;
	}

	/**
	 * 
	 * @author Bjoern Richerzhagen
	 * @version 1.0, 10.08.2012
	 */
	private class DeltaMetricValue implements MetricValue<Double> {

		private final MetricValue toAggregate;

		private double lastValue = 0;

		private double delta = 0;

		private boolean valid;

		public DeltaMetricValue(MetricValue toAggregate) {
			this.toAggregate = toAggregate;
		}

		@Override
		public Double getValue() {
			return delta;
		}

		@Override
		public boolean isValid() {
			return valid;
		}

		public void onNewInterval() {
			Object val = toAggregate.getValue();
			valid = toAggregate.isValid();
			if (valid) {
				if (val instanceof Number) {
					delta = ((Number) val).doubleValue() - lastValue;
					lastValue = ((Number) val).doubleValue();
				} else {
					throw new AssertionError();
				}
			}
		}
	}

}
