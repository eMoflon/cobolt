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

import de.tud.kom.p2psim.api.scenario.ConfigurationException;
import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.common.metric.ActiveMetric;
import de.tudarmstadt.maki.simonstrator.api.common.metric.ActiveMetric.ActiveMetricListener;
import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric;
import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric.MetricUnit;
import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric.MetricValue;

/**
 * This filter computes a ratio between two metrics
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 09.08.2012
 */
public abstract class RatioFilter extends AbstractFilter<MetricValue<Double>>
		implements ActiveMetricListener {

	private String nominatorFilter = null;

	private String denominatorFilter = null;

	private Metric<?> nominator = null;

	private Metric<?> denominator = null;

	private String description;

	private String metricName = null;

	private long lastActiveMetricUpdate = 0;

	private MetricUnit metricUnit = MetricUnit.UNKNOWN;

	public RatioFilter() {
		//
	}

	/**
	 * 
	 * @param description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	protected void onInitialize(List<Metric<?>> incomingMetrics) {
		if (nominatorFilter == null || denominatorFilter == null) {
			throw new ConfigurationException(
					"Nominator and Denominator must be set to a single metric.");
		}
		for (Metric metric : incomingMetrics) {
			if (metric.getName().equals(nominatorFilter)) {
				nominator = metric;
			} else if (metric.getName().equals(denominatorFilter)) {
				denominator = metric;
			}
		}
		if (nominator == null || denominator == null) {
			throw new ConfigurationException(
					"No Nominators or Denominators found.");
		}
		
		List<Metric<?>> quotient = new LinkedList<Metric<?>>();
		quotient.add(nominator);
		quotient.add(denominator);
		
		boolean isOverallMetric = nominator.isOverallMetric() && denominator.isOverallMetric();
		
		boolean active = false;
		if (nominator instanceof ActiveMetric) {
			active = true;
			((ActiveMetric) nominator).addActiveMetricListener(this);
		}
		if (denominator instanceof ActiveMetric) {
			active = true;
			((ActiveMetric) denominator).addActiveMetricListener(this);
		}

		createDerivedMetric(quotient, isOverallMetric, metricUnit, description,
				active);
	}

	@Override
	public void onMetricUpdate(ActiveMetric metric) {
		// nominator or denominator changed, we notify parents!
		if (lastActiveMetricUpdate != Time.getCurrentTime()) {
			lastActiveMetricUpdate = Time.getCurrentTime();
			notifyListenersOfUpdate();
		}
	}

	@Override
	public void onStop() {
		// nothing to do
	}

	@Override
	protected MetricValue<Double> getDerivedMetricValueFor(
			Metric<?> derivedMetric, List<Metric<?>> inputs, Host host) {
		MetricValue nominatorValue;
		MetricValue denominatorValue;
		if (nominator.isOverallMetric()) {
			nominatorValue = nominator.getOverallMetric();
		} else {
			nominatorValue = nominator.getPerHostMetric(host.getId());
		}
		if (denominator.isOverallMetric()) {
			denominatorValue = denominator.getOverallMetric();
		} else {
			denominatorValue = denominator.getPerHostMetric(host.getId());
		}
		if (nominatorValue == null || denominatorValue == null) {
			return null;
		}
		return new RatioMetricValue(nominatorValue, denominatorValue);
	}

	/**
	 * Implement the ratio computation here.
	 * 
	 * @param nominatorValue
	 * @param denominatorValue
	 * @return
	 */
	protected abstract Double computeRatio(MetricValue nominatorValue,
			MetricValue denominatorValue);

	public void setNominator(String nominator) {
		this.nominatorFilter = nominator;
	}

	public void setDenominator(String denominator) {
		this.denominatorFilter = denominator;
	}

	public void setMetricName(String metricName) {
		this.metricName = metricName;
	}

	public void setUnit(String metricUnit) {
		this.metricUnit = MetricUnit.valueOf(metricUnit);
	}

	@Override
	protected String getNameForDerivedMetric(List<Metric<?>> inputs) {
		if (metricName == null) {
			return super.getNameForDerivedMetric(inputs);
		}
		return metricName;
	}

	/**
	 * 
	 * @author Bjoern Richerzhagen
	 * @version 1.0, 09.08.2012
	 */
	private class RatioMetricValue implements MetricValue<Double> {

		private final MetricValue nominatorValue;

		private final MetricValue denominatorValue;

		private double ratio = 0;

		public RatioMetricValue(MetricValue nominatorValue,
				MetricValue denominatorValue) {
			this.nominatorValue = nominatorValue;
			this.denominatorValue = denominatorValue;
		}

		@Override
		public Double getValue() {
			ratio = computeRatio(nominatorValue, denominatorValue);
			return ratio;
		}

		@Override
		public boolean isValid() {
			return !Double.isNaN(ratio);
		}

	}

	/**
	 * Both metrics are interpreted as numbers and divided.
	 * 
	 * @author Bjoern Richerzhagen
	 * @version 1.0, 09.08.2012
	 */
	public static class SimpleRatio extends RatioFilter {

		@Override
		protected Double computeRatio(MetricValue nominatorValue,
				MetricValue denominatorValue) {
			Object nom = nominatorValue.getValue();
			Object den = denominatorValue.getValue();
			if (!nominatorValue.isValid() || !denominatorValue.isValid()) {
				return Double.NaN;
			}
			if (nom instanceof Number && den instanceof Number) {
				double d = ((Number) den).doubleValue();
				if (d == 0) {
					return Double.NaN;
				}
				return ((Number) nom).doubleValue()
						/ ((Number) den).doubleValue();
			}
			throw new AssertionError();
		}
	}

}
