package de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric;
import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric.MetricValue;
import de.tudarmstadt.maki.simonstrator.api.util.UtilityClassNotInstantiableException;

/**
 * Utilities for working with metrics
 *
 * @author Roland Kluge - Initial implementation
 */
public final class MetricUtils
{
   private static final Object DEFAULT_MARKER_FOR_UNAVAILABLE = new Object();

   // Disabled utility class constructor
   private MetricUtils()
   {
      throw new UtilityClassNotInstantiableException();
   }

   /**
    * Determines the name of the metric based on its class name
    * @param clazz the class of the metric
    * @return the standard name
    */
   public static String getDefaultName(final Class<? extends Metric<?>> clazz)
   {
      return clazz.getSimpleName();
   }

   /**
    * Utility method to extract the value of a double-valued overall metric
    */
   public static double getOverallDoubleMetric(final Metric<MetricValue<?>> metric)
   {
      checkNotNull(metric);

      checkNumericOverallMetric(metric);

      return ((Number) metric.getOverallMetric().getValue()).doubleValue();
   }

   /**
    * Utility method to extract the value of a integer-valued overall metric
    */
   public static int getOverallIntegerMetric(Metric<MetricValue<?>> metric)
   {
      checkNotNull(metric);

      checkNumericOverallMetric(metric);

      return ((Number) metric.getOverallMetric().getValue()).intValue();
   }

   /**
    * Utility method to calculate a {@link DescriptiveStatistics} of all the hosts of the given per-host metric
    */
   public static DescriptiveStatistics getStatisticsOverHosts(final Metric<MetricValue<?>> metric)
   {
      return getStatisticsOverHostsIgnoringUnavailableValues(metric, DEFAULT_MARKER_FOR_UNAVAILABLE);
   }

   /**
    * Utility method to calculate a {@link DescriptiveStatistics} of all the hosts of the given per-host metric, ignoring metrics whose {@link MetricValue}'s value is equal to the given marker {@link Object}
    *
    * @param metric the metric to analyzer
    */
   public static DescriptiveStatistics getStatisticsOverHostsIgnoringUnavailableValues(final Metric<MetricValue<?>> metric,
         final Object markerForUnavailableDatum)
   {
      checkNotNull(metric);

      checkNumericPerHostMetric(metric);

      final DescriptiveStatistics statistics = new DescriptiveStatistics();
      metric.getAllPerHostMetrics().stream()//
            .filter(value -> !matches(value, markerForUnavailableDatum))//
            .forEach(value -> statistics.addValue(((Number) value.getValue()).doubleValue()));
      return statistics;
   }

   /**
    * Utility method to calculate a {@link DescriptiveStatistics} of all the given numeric overall metrics
    */
   public static DescriptiveStatistics getStatisticsOfOverallMetrics(final Collection<Metric<MetricValue<?>>> metrics)
   {
      metrics.forEach(metric -> checkNumericOverallMetric(metric));

      final DescriptiveStatistics statistics = new DescriptiveStatistics();
      metrics.forEach(metric -> statistics.addValue(((Number) metric.getOverallMetric().getValue()).doubleValue()));
      return statistics;
   }

   public static void checkNotNull(final Metric<MetricValue<?>> metric)
   {
      if (metric == null)
         throw new IllegalArgumentException("Metric must not be null");
   }

   private static void checkNumericOverallMetric(final Metric<MetricValue<?>> metric)
   {
      if (!isNumericOverallMetric(metric))
         throw new IllegalArgumentException(String.format("'%s' is not an numeric overall metric", metric));
   }

   private static void checkNumericPerHostMetric(final Metric<MetricValue<?>> metric)
   {
      if (!isNumericPerHostMetric(metric))
         throw new IllegalArgumentException(String.format("'%s' is not a numeric metric", metric));
   }

   private static boolean isNumericOverallMetric(final Metric<MetricValue<?>> metric)
   {
      return metric.isOverallMetric() && isNumericMetricValue(metric.getOverallMetric());
   }

   private static boolean isNumericPerHostMetric(final Metric<MetricValue<?>> metric)
   {
      return isPerHostMetric(metric) && isNumericMetricValue(metric.getAllPerHostMetrics().get(0));
   }

   /**
    * Returns true if the given {@link Metric} is a per-host metric, i.e., not an overall metric
    * @param metric the {@link Metric} to check
    */
   private static boolean isPerHostMetric(final Metric<MetricValue<?>> metric)
   {
      return !metric.isOverallMetric();
   }

   /**
    * Returns true if the given {@link MetricValue}'s value is {@link Number}
    * @param metricValue the {@link MetricValue} to check
    * @return true if the value is numeric
    */
   private static boolean isNumericMetricValue(MetricValue<?> metricValue)
   {
      return metricValue.getValue() instanceof Number;
   }

   /**
    * Fairness according to Jain as explained here:
    * https://en.wikipedia.org/wiki/Fairness_measure
    *
    * @param distribution
    * @return
    */
   public static final double getJainFairness(final DescriptiveStatistics distribution)
   {
      final double nominator = Math.pow(Arrays.stream(distribution.getValues()).reduce(0.0, (a, b) -> a + b), 2.0);
      final double denominator = distribution.getN() * Arrays.stream(distribution.getValues()).map(x -> x * x).reduce(0.0, (a, b) -> a + b);
      return nominator / denominator;
   }

   /**
    * Checks whether the value of the given matches the given marker for unavailable data
    *
    * @param metricValue the {@link MetricValue} to check
    * @param markerForUnavailable the marker object
    * @return true iff the values match
    */
   private static boolean matches(final MetricValue<?> metricValue, final Object markerForUnavailable)
   {
      return markerForUnavailable == null ? metricValue.getValue() == null : markerForUnavailable.equals(metricValue.getValue());

   }

   public static double aggregateAccordingToMetricTypeAndOperator(final Metric<MetricValue<?>> metric,
   		final Object markerForUnavailableDatum, final MetricAggregationOperator operator) {
   	final DescriptiveStatistics statisticsOverAllHosts;
   	if (metric.isOverallMetric()) {
   		final Object overallValue = metric.getOverallMetric().getValue();
   		if (overallValue instanceof Number) {
   			final Number value = (Number) overallValue;
   			statisticsOverAllHosts = new DescriptiveStatistics();
   			statisticsOverAllHosts.addValue(value.doubleValue());
   		} else {
   			throw new IllegalArgumentException(
   					String.format("Value of metric '%s' cannot be cast to Number", metric));
   		}
   	} else {
   		statisticsOverAllHosts = getStatisticsOverHostsIgnoringUnavailableValues(metric,
   				markerForUnavailableDatum);
   	}
   	final double aggregated;
   	switch (operator) {
   	case MEAN:
   		aggregated = statisticsOverAllHosts.getMean();
   		break;
   	case MEDIAN:
   		aggregated = statisticsOverAllHosts.getPercentile(50);
   		break;
   	case MAX:
   		aggregated = statisticsOverAllHosts.getMax();
   		break;
   	case MIN:
   		aggregated = statisticsOverAllHosts.getMin();
   		break;
   	case SUM:
   		aggregated = statisticsOverAllHosts.getSum();
   		break;
   	default:
   		throw new IllegalArgumentException("Unsupported aggregation operation " + operator);
   	}
   	return aggregated;
   }
}
