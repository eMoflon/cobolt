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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math.stat.StatUtils;

import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric;
import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric.MetricValue;
import de.tudarmstadt.maki.simonstrator.api.util.XMLConfigurableConstructor;

/**
 * This filter computes statistics as overall-metrics of all per-host metrics.
 * (For example, sum, std, avg...). The input metric has to be either numeric or
 * boolean (where true will be one, false will be zero). You can extend this
 * basic filter for more complex statistical computations on the provided
 * dataset. Common functions are included as inner classes in this abstract
 * filter.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 08.08.2012
 */
public abstract class StatisticsFilter extends
		AbstractFilter<MetricValue<Double>> {

	private final String description;

	public StatisticsFilter(String name, String description) {
		super(name);
		this.description = description;
	}

	public StatisticsFilter(String description) {
		super();
		this.description = description;
	}

	public StatisticsFilter() {
		super();
		this.description = getClass().getSimpleName();
	}

	@Override
	protected void onInitialize(List<Metric<?>> incomingMetrics) {
		for (Metric metric : incomingMetrics) {
			if (metric.isOverallMetric()) {
				continue;
			}
			createDerivedMetric(metric, true, metric.getUnit(), description
					+ " of " + metric.getName(), false);
		}
	}

	@Override
	public void onStop() {
		// nothing to do
	}

	@Override
	protected MetricValue<Double> getDerivedMetricValueFor(
			Metric<?> derivedMetric, List<Metric<?>> inputs, Host host) {
		assert inputs.size() == 1;
		assert host == null;
		Metric<?> input = inputs.get(0);
		if (input.isOverallMetric()) {
			throw new AssertionError(
					"Only available for per-host input metrics.");
		}
		LinkedList<MetricValue> mvs = new LinkedList<MetricValue>(
				input.getAllPerHostMetrics());
		return new StatisticsMetricValue(mvs);
	}

	/**
	 * Computes statistics such as svg, sum, std...
	 * 
	 * @author Bjoern Richerzhagen
	 * @version 1.0, 08.08.2012
	 */
	private class StatisticsMetricValue implements MetricValue<Double> {

		private final List<MetricValue> inputs;

		private Double result = Double.NaN;

		public StatisticsMetricValue(List<MetricValue> inputs) {
			this.inputs = inputs;
		}

		@Override
		public Double getValue() {

			double min = Double.MAX_VALUE;
			double max = Double.MIN_VALUE;
			double sum = 0;
			double sum2 = 0;

			int count = 0;

			double[] dValues = new double[inputs.size()];

			for (MetricValue m : inputs) {
				Object mv = m.getValue();
				if (!m.isValid()) {
					continue;
				}
				double v = Double.NaN;

				if (mv instanceof Number) {
					v = ((Number) mv).doubleValue();
				} else if (mv instanceof Boolean) {
					boolean b = ((Boolean) mv).booleanValue();
					v = (b ? 1 : 0); // interpret boolean as 0 or 1
				} else {
					throw new AssertionError();
				}

				if (v > max) {
					max = v;
				}
				if (v < min) {
					min = v;
				}
				sum += v;
				sum2 += (v * v);

				dValues[count] = v;
				count++;
			}
			/*
			 * This will truncate the array to the length given by the number of
			 * valid items
			 */
			if (count == 0) {
				result = Double.NaN;
				return result;
			}
			double[] valArray = Arrays.copyOf(dValues, count);
			result = getResult(valArray, count, sum, sum2, min, max);
			return result;
		}

		@Override
		public boolean isValid() {
			return !result.isNaN();
		}

	}

	/**
	 * Function that has to compute sth. on the double-list. For convenience,
	 * some basic statistical values are already included.
	 * 
	 * @param incoming
	 * @param count
	 *            number of values
	 * @param sum
	 * @param sum2
	 *            sum of the squares
	 * @param min
	 * @param max
	 * @return
	 */
	protected abstract Double getResult(double[] incoming,
			double count, double sum, double sum2, double min, double max);

	/**
	 * Standard deviation
	 * 
	 * @author Bjoern Richerzhagen
	 * @version 1.0, 08.08.2012
	 */
	public static class Std extends StatisticsFilter {

		@Override
		protected Double getResult(double[] incoming, double count,
				double sum, double sum2, double min, double max) {
			return Math
					.sqrt(1 / (count - 1) * (sum2 - (1 / count) * sum * sum));
		}

	}

	/**
	 * Average
	 * 
	 * @author Bjoern Richerzhagen
	 * @version 1.0, 08.08.2012
	 */
	public static class Avg extends StatisticsFilter {

		@Override
		protected Double getResult(double[] incoming, double count,
				double sum, double sum2, double min, double max) {
			if (count == 0) {
				return Double.NaN;
			}
			return sum / count;
		}

        @Override
        public String getName() {
            return "Avg";
        }
    }

	/**
	 * Minimum
	 * 
	 * @author Bjoern Richerzhagen
	 * @version 1.0, 08.08.2012
	 */
	public static class Min extends StatisticsFilter {
		@Override
		protected Double getResult(double[] incoming, double count,
				double sum, double sum2, double min, double max) {
			return min;
		}

        @Override
        public String getName() {
            return "Min";
        }
    }

	/**
	 * Maximum
	 * 
	 * @author Bjoern Richerzhagen
	 * @version 1.0, 08.08.2012
	 */
	public static class Max extends StatisticsFilter {
		@Override
		protected Double getResult(double[] incoming, double count,
				double sum, double sum2, double min, double max) {
			return max;
		}

        @Override
        public String getName() {
            return "Max";
        }
	}

	/**
	 * The Percentile, pass a value between 0 (exclusive) and 100 (inclusive).
	 * The resulting metric will be called "P20_incomingmetric" in the case of
	 * the 20th percentile.
	 * 
	 * @author Bjoern Richerzhagen
	 * @version 1.0, 08.08.2012
	 */
	public static class Percentile extends StatisticsFilter {

		private final double percentile;

		@XMLConfigurableConstructor({ "percentile" })
		public Percentile(double percentile) {
			super("P" + (int) percentile, "the " + (int) percentile
					+ "th percentile");
			this.percentile = percentile;
		}

		@Override
		protected Double getResult(double[] incoming, double count,
				double sum, double sum2, double min, double max) {
			return StatUtils.percentile(incoming, percentile);
		}

	}

}
