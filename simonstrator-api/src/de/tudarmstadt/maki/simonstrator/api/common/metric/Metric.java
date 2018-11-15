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

package de.tudarmstadt.maki.simonstrator.api.common.metric;

import java.util.List;

import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric.MetricValue;

/**
 * This allows metrics to be defined without a specific analyzer or output-type
 * in mind (DB, CSV, Live...). Use in conjunction with the
 * {@link MetricAnalyzer}. This is intended for overlay or app-specific metrics
 * that are very hard to measure using the normal Analyzer-Interfaces for
 * messages or operations.
 *
 * Please note: these Metrics are used for analyzing (simulations etc.), they
 * are <strong>not</strong> part of any monitoring overlay within the MAPE
 * component!
 *
 * @author Bjoern Richerzhagen
 * @version 1.0, 07.08.2012
 */
public interface Metric<M extends MetricValue<?>> {

	/**
	 * Units for your metrics.
	 *
	 * @author Bjoern Richerzhagen
	 * @version 1.0, 08.08.2012
	 */
	public enum MetricUnit {
		/**
		 * No unit (e.g., count)
		 */
		NONE(""),
		/**
		 * Traffic in byte per second
		 */
		TRAFFIC("byte/s"),
		/**
		 * Time in microseconds
		 */
		TIME("us"),
		/**
		 * Data size in byte
		 */
		DATA("byte"),
		/**
		 * Length in meter
		 */
		LENGTH("m"),
		/**
		 * Unknown metric unit
		 */
		UNKNOWN("unknown"),
		/**
		 * Energy in microjoule
		 */
		ENERGY("uJ"),
		/**
		 * Fraction in percent
		 */
		PERCENT("%");

		private final String representation;

		private MetricUnit(String representation) {
			this.representation = representation;
		}

		@Override
		public String toString() {
			return representation;
		}
	}

	/**
	 * Called as soon as the Monitor starts.
	 *
	 * @param host
	 */
	public void initialize(List<Host> hosts);

	/**
	 * Values for all hosts by hostId. If this is provided, getOverallMetric
	 * should return null.
	 *
	 * @return map or null, if the metric is not defined as a per-host metric
	 *         (for example "numberOfOnlineHosts")
	 */
	public M getPerHostMetric(INodeID nodeId);

	/**
	 * List of all Per-Host metrics
	 *
	 * @return
	 */
	public List<M> getAllPerHostMetrics();

	/**
	 * Value of the Metric aggregated for all hosts. If this is provided,
	 * getPerHostMetric should return null.
	 *
	 * @return value or null, if the metric can not be aggregated in a
	 *         meaningful way.
	 */
	public M getOverallMetric();

	/**
	 * Has to return true, if the metric is an overall metric (not per host)
	 *
	 * @return
	 */
	public boolean isOverallMetric();

	/**
	 * Name of the Metric - should be an unique identifier
	 *
	 * @return
	 */
	public String getName();

	/**
	 * A more descriptive name or text of the metric
	 *
	 * @return
	 */
	public String getDescription();

	/**
	 * Unit the metric is measured in
	 *
	 * @return
	 */
	public MetricUnit getUnit();

	/**
	 * Value of a metric. The {@link Metric}-concept implies that the
	 * aggregation of values is to be handled by the analyzer that calls
	 * getValue().
	 *
	 * For OverallMetrics (i.e. metrics that are already available as an
	 * aggregate), you should also implement toString in a meaningful way, as
	 * this string will be used by the live monitor.
	 *
	 * @author Bjoern Richerzhagen
	 * @version 1.0, 07.08.2012
	 * @param <T>
	 */
	public interface MetricValue<T> {

		/**
		 * Value of the metric, call this before checking isValid!
		 *
		 * @return
		 */
		public T getValue();

		/**
		 * True, if the value is valid and should be used, false otherwise. This
		 * might be used to exclude values from offline hosts. You have to call
		 * getValue before calling this method!
		 *
		 * @return
		 */
		public boolean isValid();

	}

}
