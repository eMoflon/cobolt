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
import de.tudarmstadt.maki.simonstrator.api.common.metric.ActiveMetric;
import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric;
import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric.MetricValue;
import de.tudarmstadt.maki.simonstrator.api.util.XMLConfigurableConstructor;

/**
 * This filter translates a {@link Metric} into an {@link ActiveMetric} by
 * periodically querying it for a value and providing that value over the whole
 * interval as a new ActiveMetric.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 13.08.2012
 */
public class PeriodicSamplingFilter extends
		AbstractPeriodicSampleFilter<MetricValue<Double>> {

	private List<SampledMetricValue> metricValues = new LinkedList<SampledMetricValue>();

	private String prefix;

	@XMLConfigurableConstructor({ "intervalLength" })
	public PeriodicSamplingFilter(long intervalLength) {
		super(intervalLength);
	}

	@Override
	protected void onNewInterval() {
		for (SampledMetricValue mv : metricValues) {
			mv.onNewInterval();
		}
	}

	@Override
	public void onStop() {
		// nothing to do
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
			return "Sampled" + getIntervalLength() / Time.SECOND + "s_"
					+ inputs.get(0).getName();
		} else {
			return prefix + "_" + inputs.get(0).getName();
		}
	}

	@Override
	protected MetricValue<Double> getDerivedMetricValueFor(
			Metric<?> derivedMetric, List<Metric<?>> inputs, Host host) {
		SampledMetricValue mv;
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
		mv = new SampledMetricValue(inputMv);
		metricValues.add(mv);
		return mv;
	}

	/**
	 * 
	 * @author Bjoern Richerzhagen
	 * @version 1.0, 10.08.2012
	 */
	private class SampledMetricValue implements MetricValue<Double> {

		private final MetricValue toSample;

		private double value = 0;

		private boolean valid;

		public SampledMetricValue(MetricValue toSample) {
			this.toSample = toSample;
		}

		@Override
		public Double getValue() {
			return value;
		}

		@Override
		public boolean isValid() {
			return valid;
		}

		public void onNewInterval() {
			Object val = toSample.getValue();
			valid = toSample.isValid();
			if (valid) {
				if (val instanceof Number) {
					value = ((Number) val).doubleValue();
				} else {
					throw new AssertionError();
				}
			}
		}
	}

}
