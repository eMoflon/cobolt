package de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics;

import de.tudarmstadt.maki.simonstrator.tc.reconfiguration.TopologyControlComponentConfig;

public class SeedMetric extends PreinitializedMetric
{
   public SeedMetric(final TopologyControlComponentConfig configuration)
   {
      super("meta-seed", MetricUnit.NONE, new SimpleNumericMetricValue<Long>(configuration.seed));
   }

}
