package de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics;

import de.tudarmstadt.maki.simonstrator.tc.reconfiguration.TopologyControlComponentConfig;

public class WorldSizeMetric extends PreinitializedMetric
{
   public WorldSizeMetric(final TopologyControlComponentConfig configuration)
   {
      super("WorldSize", MetricUnit.LENGTH, new SimpleNumericMetricValue<Integer>(configuration.worldSize));
   }
}
