package de.tud.kom.p2psim.impl.util.db.dao.metric;

import java.util.HashMap;
import java.util.Map;

import de.tud.kom.p2psim.impl.util.Tuple;
import de.tud.kom.p2psim.impl.util.db.dao.DAO;
import de.tud.kom.p2psim.impl.util.db.metric.Metric;
import de.tud.kom.p2psim.impl.util.db.metric.MetricDescription;

/** Data Access Object to store and retrieve {@link Metric} instances.
 *
 * @author Andreas Hemel
 */
public class MetricDAO extends DAO {

	/** Cache of {@link Metric} objects to avoid database lookups. */
	private static Map<Tuple<MetricDescription, MetricType>, Metric> metricCache =
			new HashMap<Tuple<MetricDescription, MetricType>, Metric>();

	/** Enum to distinguish between single, aggregate and pair metrics.
	 *
	 * @author Christoph Muenker
	 * @version 1.0, 07/05/2011
	 */
	public enum MetricType {
		/**
		 * Identifier for a single metric
		 */
		SINGLE,
		/**
		 * Identifier for an aggregate metric
		 */
		AGGREGATE, PAIR, PAIRLIST
	}

	/** Retrieve a {@link Metric} object for the given MetricDescription and MetricType.
	 *
	 * If there is no matching Metric object, it is created, persisted, and cached
	 * automatically.
	 */
	public static Metric lookupMetric(MetricDescription metricDesc, MetricType type) {
		Tuple<MetricDescription, MetricType> key =
				new Tuple<MetricDescription, MetricType>(metricDesc, type);
		Metric metric = metricCache.get(key);
		if (metric != null) {
			return metric;
		} else {
			metric = new Metric(metricDesc, type.toString(), ExperimentDAO.getExperiment());
			metricCache.put(key, metric);
			persistImmediately(metric);
			return metric;
		}
	}

	/** Retrieve a {@link Metric} object for the given MetricDescription
	 * for single value metrics.
	 *
	 * If there is no matching Metric object, it is created, persisted, and cached
	 * automatically.
	 */
	public static Metric lookupSingleMetric(MetricDescription metricDesc) {
		return lookupMetric(metricDesc, MetricType.SINGLE);
	}

	/** Retrieve a {@link Metric} object for the given MetricDescription
	 * for pair value metrics.
	 *
	 * If there is no matching Metric object, it is created, persisted, and cached
	 * automatically.
	 */
	public static Metric lookupPairMetric(MetricDescription metricDesc) {
		return lookupMetric(metricDesc, MetricType.PAIR);
	}

	/**
	 * Retrieve a {@link Metric} object for the given MetricDescription for pair
	 * list metrics.
	 * 
	 * If there is no matching Metric object, it is created, persisted, and
	 * cached automatically.
	 */
	public static Metric lookupPairListMetric(MetricDescription metricDesc) {
		return lookupMetric(metricDesc, MetricType.PAIRLIST);
	}

	/** Retrieve a {@link Metric} object for the given MetricDescription
	 * for aggregate value metrics.
	 *
	 * If there is no matching Metric object, it is created, persisted, and cached
	 * automatically.
	 */
	public static Metric lookupAggregateMetric(MetricDescription metricDesc){
		return lookupMetric(metricDesc, MetricType.AGGREGATE);
	}
}
