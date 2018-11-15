package de.tudarmstadt.maki.simonstrator.tc.analyzer.metrics;

import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics.PreinitializedMetric;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics.SimpleNumericMetricValue;

public class DifferenceMetric extends PreinitializedMetric
{

   public DifferenceMetric(final Metric<MetricValue<?>> minuendMetric, final Metric<MetricValue<?>> subtrahendMetric)
   {
      super(String.format("%s - %s", minuendMetric, subtrahendMetric.getName()), minuendMetric.getUnit(),
            new SimpleNumericMetricValue<Number>(calculateDifference(minuendMetric, subtrahendMetric)));
   }

   private static double calculateDifference(final Metric<MetricValue<?>> minuendMetric, final Metric<MetricValue<?>> subtrahendMetric)
   {
      final Number minuendValue = (Number) minuendMetric.getOverallMetric().getValue();
      final Number subtrahendValue = (Number) subtrahendMetric.getOverallMetric().getValue();
      return minuendValue.doubleValue() - subtrahendValue.doubleValue();
   }

}
