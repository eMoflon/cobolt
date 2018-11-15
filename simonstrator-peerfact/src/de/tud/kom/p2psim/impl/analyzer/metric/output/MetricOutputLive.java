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

package de.tud.kom.p2psim.impl.analyzer.metric.output;

import java.util.List;

import de.tud.kom.p2psim.impl.analyzer.metric.MetricAnalyzer;
import de.tud.kom.p2psim.impl.util.LiveMonitoring;
import de.tud.kom.p2psim.impl.util.LiveMonitoring.ProgressValue;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric;
import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric.MetricValue;

/**
 * Live Monitoring of all configured {@link Metric}s
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 07.08.2012
 */
public class MetricOutputLive extends AbstractOutput {

	@Override
	public void onInitialize(List<Metric> metrics) {
		for (Metric metric : metrics) {
			ProgressValueAdapter pa = new ProgressValueAdapter(metric);
			if (pa.valid) {
				LiveMonitoring.addProgressValue(pa);
			}
		}
	}

	@Override
	public void onStop() {
		// not interested
	}

	/**
	 * Just matching the {@link ProgressValue}-interface to the
	 * {@link TransitMetric} interface.
	 * 
	 * @author Bjoern Richerzhagen
	 * @version 1.0, 07.08.2012
	 */
	private static class ProgressValueAdapter implements ProgressValue {

		protected final Metric<?> metric;

		private final String name;

		private double factor;

		private String unit;

		private final boolean isDerived;

		public final boolean valid;

		private MetricValue<Number> std = null;

		private MetricValue<Number> avg = null;

		private MetricValue<Number> min = null;

		private MetricValue<Number> max = null;

		protected ProgressValueAdapter(Metric metric) {

			this.metric = metric;
			this.name = metric.getDescription() + " [" + metric.getName() + "]";
			switch (metric.getUnit()) {
			case TRAFFIC:
				factor = 8d / 1000d;
				unit = " kbit/s";
				break;

			case TIME:
				factor = 1 / (double) Time.MILLISECOND;
				unit = " ms";
				break;

			default:
				factor = 1;
				unit = " " + metric.getUnit().toString();
				break;
			}

			if (!metric.isOverallMetric()) {
				// Try to find Avg, Std, Min, Max
				Metric avgM = MetricAnalyzer.getMetric("Avg_"
						+ metric.getName());
				Metric stdM = MetricAnalyzer.getMetric("Std_"
						+ metric.getName());
				if (avgM != null && avgM.isOverallMetric()) {
					avg = avgM.getOverallMetric();
				}
				if (avg != null && stdM != null && stdM.isOverallMetric()) {
					std = stdM.getOverallMetric();
				}

				Metric minM = MetricAnalyzer.getMetric("Min_"
						+ metric.getName());
				Metric maxM = MetricAnalyzer.getMetric("Max_"
						+ metric.getName());
				if (minM != null && minM.isOverallMetric()) {
					min = minM.getOverallMetric();
				}
				if (maxM != null && maxM.isOverallMetric()) {
					max = maxM.getOverallMetric();
				}
				this.isDerived = true;
				this.valid = avg != null || std != null || min != null
						|| max != null;
			} else {
				this.isDerived = false;
				this.valid = true;
			}
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getValue() {
			if (isDerived) {
				StringBuilder sb = new StringBuilder();
				if (avg != null) {
					sb.append("avg: ");
					sb.append(printNumericalValue(avg.getValue()));
					sb.append(" ");
				}
				if (min != null) {
					sb.append("min: ");
					sb.append(printNumericalValue(min.getValue()));
					sb.append(" ");
				}
				if (max != null) {
					sb.append("max: ");
					sb.append(printNumericalValue(max.getValue()));
					sb.append(" ");
				}
				if (std != null) {
					sb.append("std: ");
					sb.append(printNumericalValue(std.getValue()));
					sb.append(" ");
				}
				return sb.toString();
			} else {
				Object val = metric.getOverallMetric().getValue();
				if (val instanceof Number) {
					return printNumericalValue((Number) val);
				}
				return val.toString();
			}
		}

		private String printNumericalValue(Number val) {
			return Math.round((val).doubleValue() * factor * 10000) / 10000d
					+ unit;
		}

	}

}
