package de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics;

import de.tudarmstadt.maki.simonstrator.tc.analyzer.metrics.spanner.HopSpanner;

/**
 * Stores the maximum pairwise hop-spanner 
 *  
 * @author Roland Kluge - Initial implementation
 */
public class MaxHopSpannerMetric extends PreinitializedMetric
{
   public MaxHopSpannerMetric(final HopSpanner hopSpanner)
   {
      super("MaximumUnderlayHopSpanner", MetricUnit.NONE, calculateMax(hopSpanner));
   }

   private static MetricValue<Double> calculateMax(final HopSpanner hopSpanner)
   {
      return new SimpleNumericMetricValue<Double>(hopSpanner.getMaximumPairwiseSpanner());
   }

}
