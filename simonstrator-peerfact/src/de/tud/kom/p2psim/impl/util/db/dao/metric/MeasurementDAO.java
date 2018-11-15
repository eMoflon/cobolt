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

package de.tud.kom.p2psim.impl.util.db.dao.metric;

import java.util.List;

import de.tud.kom.p2psim.impl.util.db.dao.DAO;
import de.tud.kom.p2psim.impl.util.db.metric.CustomMeasurement;
import de.tud.kom.p2psim.impl.util.db.metric.HostMetric;
import de.tud.kom.p2psim.impl.util.db.metric.Measurement;
import de.tud.kom.p2psim.impl.util.db.metric.MeasurementPair;
import de.tud.kom.p2psim.impl.util.db.metric.MeasurementPairList;
import de.tud.kom.p2psim.impl.util.db.metric.MeasurementSingle;
import de.tud.kom.p2psim.impl.util.db.metric.Metric;
import de.tud.kom.p2psim.impl.util.db.metric.MetricDescription;
import de.tud.kom.p2psim.impl.util.stats.StatisticComputation;

/** This class provides methods to persist measurements in a database.
 *
 * Please use only this class to store measurements with the
 * existing database structure. The other DAOs ({@link ExperimentDAO},
 * {@link MetricDAO}, and {@link HostMetricDAO}) will be called by
 * this class for the right mapping.
 *
 * @author Christoph Muenker
 * @author Andreas Hemel
 */
public class MeasurementDAO extends DAO {

	/**
	 * The Host ID that is used for global metrics.
	 */
	private static final long GLOBAL_HOST_ID = -1;
	
	private static boolean inactive = false;
	
	public static void storeCustomMeasurement(MetricDescription metricDesc, long hostId, long time, CustomMeasurement measurement) {
		if (inactive) return;
		
		Metric metric = MetricDAO.lookupSingleMetric(metricDesc);
		HostMetric hostMetric = HostMetricDAO.lookupHostMetric(metric, hostId);

		measurement.setTime(time);
		measurement.setHostMetric(hostMetric);
		
		addToPersistQueue(measurement);
	}
	
	public static void setInactive(boolean inactive) {
		MeasurementDAO.inactive = inactive;
	}

	/**
	 * Store a measurement for table {@link MeasurementSingle}. It contains the
	 * metric description, a hostId, the timestamp of the measurement in
	 * simulation time and the value to the metric.<br>
	 * This method should be used, if the measurement is for a host.
	 *
	 * @param metric
	 *            The {@link MetricDescription} which describes the metric.
	 * @param hostId
	 *            The a unique host identifier for the host.
	 * @param time
	 *            The time of the measurement in simulation time
	 * @param value
	 *            The measurement to this metric.
	 */
	public static void storeSingleMeasurement(MetricDescription metricDesc, long hostId, long time, double value) {
		if (inactive) return;
		
		Metric metric = MetricDAO.lookupSingleMetric(metricDesc);
		HostMetric hostMetric = HostMetricDAO.lookupHostMetric(metric, hostId);

		MeasurementSingle measurement = new MeasurementSingle(time, value, hostMetric);
		addToPersistQueue(measurement);
	}

	/**
	 * Stores for a series of measurements the given values for a host. The
	 * given values are a statistical representation of the series of
	 * measurements.<br>
	 * The series of measurements describes a many of measurements of the same
	 * metric.
	 *
	 * @param metric
	 *            The {@link MetricDescription} which describes the metric.
	 * @param hostId
	 *            The a unique host identifier for the host.
	 * @param time
	 *            A time for the measurement in simulation time
	 * @param sum
	 *            The sum of the measurements
	 * @param sum2
	 *            The square sum of the measurements
	 * @param count
	 *            The number of measurements
	 * @param max
	 *            The maximum value of the measurements
	 * @param min
	 *            The minimum value of the measurements
	 */
	public static void storeMeasurement(MetricDescription metricDesc, long hostId,
			long time, double sum, double sum2, int count, double max, double min) {
		if (inactive) return;
		
		Metric metric = MetricDAO.lookupSingleMetric(metricDesc);
		HostMetric hostMetric = HostMetricDAO.lookupHostMetric(metric, hostId);
		Measurement measurement = new Measurement(time, count, sum, sum2, min, max, hostMetric);
		addToPersistQueue(measurement);
	}
	
	
	/**
	 * Store a list-based measurement with a key (i.e., as a
	 * {@link MeasurementPairList}).
	 * 
	 * @param metricDesc
	 * @param hostId
	 * @param time
	 * @param key
	 * @param sum
	 * @param sum2
	 * @param count
	 * @param max
	 * @param min
	 */
	public static void storePairListMeasurement(MetricDescription metricDesc,
			long hostId, long time, int key, double sum, double sum2,
			int count, double max, double min) {
		Metric metric = MetricDAO.lookupPairListMetric(metricDesc);
		HostMetric hostMetric = HostMetricDAO.lookupHostMetric(metric, hostId);
		MeasurementPairList measurement = new MeasurementPairList(time, key,
				count, sum, sum2, min, max, hostMetric);
		addToPersistQueue(measurement);
	}
	
	/**
	 * Shortcut for {@link MeasurementPairList} metrics based on a single list.
	 * 
	 * @param metric
	 * @param hostId
	 * @param time
	 * @param values
	 */
	public static void storePairListMeasurement(MetricDescription metric,
			long hostId, long time, int key, List<Double> values) {
		if (inactive)
			return;

		if (values == null)
			throw new AssertionError("The list with values should be not null");

		storePairListMeasurement(metric, hostId, time, key,
				StatisticComputation.sum(values),
				StatisticComputation.sum2(values), values.size(),
				StatisticComputation.max(values),
				StatisticComputation.min(values));
	}
	
	/**
	 * Store a measurement for table {@link MeasurementPair}. It contains the
	 * metric description, a hostId, the timestamp of the measurement in
	 * simulation time and the two values for the metric.<br>
	 * This method should be used, if the measurement is for a host.
	 *
	 * @param metric
	 *            The {@link MetricDescription} which describes the metric.
	 * @param hostId
	 *            The a unique host identifier for the host.
	 * @param time
	 *            The time of the measurement in simulation time
	 * @param key
	 *            The integer value
	 * @param value
	 *            The double value
	 */
	public static void storePairMeasurement(MetricDescription metricDesc,
			long hostId, long time, int key, double value) {
		Metric metric = MetricDAO.lookupPairMetric(metricDesc);
		HostMetric hostMetric = HostMetricDAO.lookupHostMetric(metric, hostId);
		MeasurementPair measurement = new MeasurementPair(time, key, value, hostMetric);
		addToPersistQueue(measurement);
	}

	/**
	 * Stores a series of measurements as a statistical representation, like the
	 * method storeMeasurement for a host.<br>
	 * The series of measurements describes a many of measurements of the same
	 * metric.
	 *
	 * @param metric
	 *            The {@link MetricDescription} which describes the metric.
	 * @param hostId
	 *            The a unique host identifier for the host.
	 * @param time
	 *            A time for the measurement in simulation time
	 * @param values
	 *            A series of measurements, which should be not null!
	 */
	public static void storeListMeasurement(MetricDescription metric, long hostId, long time, List<Double> values) {
		if (inactive) return;
		
		if (values == null)
			throw new AssertionError("The list with values should be not null");
		
		storeMeasurement(metric, hostId, time,
				StatisticComputation.sum(values),
				StatisticComputation.sum2(values), values.size(),
				StatisticComputation.max(values),
				StatisticComputation.min(values));
	}

	/**
	 * Stores for a series of measurements the given values. The given values
	 * are a statistical representation of the series of measurements.<br>
	 * The series of measurements describes a many of measurements of the same
	 * metric for the global simulation.
	 *
	 * @param metric
	 *            The {@link MetricDescription} which describes the metric.
	 * @param time
	 *            A time for the measurement in simulation time
	 * @param sum
	 *            The sum of the measurements
	 * @param sum2
	 *            The square sum of the measurements
	 * @param count
	 *            The number of measurements
	 * @param max
	 *            The maximum value of the measurements
	 * @param min
	 *            The minimum value of the measurements
	 */
	public static void storeGlobalMeasurement(MetricDescription metric, long time,
			double sum, double sum2, int count, double max, double min) {
		if (inactive) return;
		
		storeMeasurement(metric, GLOBAL_HOST_ID, time, sum, sum2, count, max, min);
	}

	/**
	 * Store a measurement for table {@link MeasurementSingle}. It contains the
	 * metric description, the timestamp of the measurement in simulation time
	 * and the value to the metric.<br>
	 * This method should be used, if the measurement is for the global
	 * simulation.
	 *
	 * @param metric
	 *            The {@link MetricDescription} which describes the metric.
	 * @param hostId
	 *            The a unique host identifier for the host.
	 * @param time
	 *            The time of the measurement in simulation time
	 * @param value
	 *            The measurement to this metric.
	 */
	public static void storeGlobalSingleMeasurement(MetricDescription metric, long time, double value) {
		if (inactive) return;
		
		storeSingleMeasurement(metric, GLOBAL_HOST_ID, time, value);
	}
	
	
	/**
	 * Store a measurement for table {@link MeasurementPair}. It contains the
	 * metric description, a hostId, the timestamp of the measurement in
	 * simulation time and the two values for the metric.<br>
	 * This method should be used, if the measurement is for the global
	 * simulation.
	 *
	 * @param metric
	 *            The {@link MetricDescription} which describes the metric.
	 * @param time
	 *            The time of the measurement in simulation time
	 * @param key
	 *            The integer value
	 * @param value
	 *            The double value
	 */
	public static void storeGlobalPairMeasurement(MetricDescription metric, long time, int key, double value) {
		storePairMeasurement(metric, GLOBAL_HOST_ID, time, key, value);
	}

	/**
	 * Stores a series of measurements as a statistical representation, like the
	 * method storeMeasurement.<br>
	 * The series of measurements describes a many of measurements of the same
	 * metric for the global simulation.
	 *
	 * @param metric
	 *            The {@link MetricDescription} which describes the metric.
	 * @param hostId
	 *            The a unique host identifier for the host.
	 * @param time
	 *            A time for the measurement in simulation time
	 * @param values
	 *            A series of measurements, which should be not null!
	 */
	public static void storeGlobalListMeasurement(MetricDescription metric, long time, List<Double> values) {
		if (inactive) return;
		
		storeListMeasurement(metric, GLOBAL_HOST_ID, time, values);
	}
}
