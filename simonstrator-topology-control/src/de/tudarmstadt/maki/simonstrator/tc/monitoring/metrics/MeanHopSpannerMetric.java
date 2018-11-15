package de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics;

import de.tudarmstadt.maki.simonstrator.tc.analyzer.metrics.spanner.HopSpanner;

/**
 * Stores the mean pairwise hop-spanner 
 *  
 * @author Roland Kluge - Initial implementation
 */
public class MeanHopSpannerMetric extends PreinitializedMetric
{
   public MeanHopSpannerMetric(final HopSpanner hopSpanner)
   {
      super("MeanUnderlayHopSpanner", MetricUnit.NONE, calculateMean(hopSpanner));
   }

   private static MetricValue<Double> calculateMean(HopSpanner hopSpanner)
   {
      return new SimpleNumericMetricValue<Double>(hopSpanner.getAveragePairwiseSpanner());
   }

}
