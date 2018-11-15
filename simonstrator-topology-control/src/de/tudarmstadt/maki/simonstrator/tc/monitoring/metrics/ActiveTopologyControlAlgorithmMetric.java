package de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics;

import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmID;
import de.tudarmstadt.maki.simonstrator.tc.reconfiguration.TopologyControlComponentConfig;

/**
 * Captures the ID of the currently active topology control algorithm
 *
 * @author Roland Kluge - Initial implementation
 *
 * @see TopologyControlAlgorithmID#getUniqueId()
 */
public class ActiveTopologyControlAlgorithmMetric extends PreinitializedMetric
{
   public static final int NOT_AVAILABLE = -1;

   public ActiveTopologyControlAlgorithmMetric(final TopologyControlComponentConfig configuration)
   {
      super("Active TC Algorithm", new SimpleNumericMetricValue<Double>((double) configuration.topologyControlAlgorithmID.getUniqueId()));
   }
}
