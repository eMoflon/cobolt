package de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics;

/**
 * This enumeration describes different operators for deriving an aggregated metric from a per-host metric.
 *
 * @author Roland Kluge - Initial implementation
 *
 */
public enum MetricAggregationOperator {
	MEAN, MEDIAN, MAX, MIN, SUM;
}
