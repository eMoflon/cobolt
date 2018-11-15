package de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics;

import java.util.List;
import java.util.Map;

import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.common.metric.AbstractMetric;
import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric.MetricValue;

/**
 * Parent class for pre-initialized metrics that do not require any invocatino of {@link #initialize(List)}
 * 
 * @author Roland Kluge - Initial implementation
 *
 */
public abstract class PreinitializedMetric extends AbstractMetric<MetricValue<?>>
{
   public PreinitializedMetric(final String description)
   {
      this(description, MetricUnit.NONE);
   }

   public PreinitializedMetric(final String description,  Map<Host, MetricValue<?>> perHostMetric)
   {
      this(description, MetricUnit.NONE, perHostMetric);
   }

   public PreinitializedMetric(final String description, final MetricUnit unit, Map<Host, MetricValue<?>> perHostMetric)
   {
      this(description, unit);
      perHostMetric.forEach((host, metricValue) -> addHost(host, metricValue));
   }

   public PreinitializedMetric(final String description, final MetricValue<?> overallMetric)
   {
      this(description, MetricUnit.NONE, overallMetric);
   }

   public PreinitializedMetric(final String description, final MetricUnit unit, MetricValue<?> overallMetric)
   {
      this(description, unit);
      setOverallMetric(overallMetric);
   }

   public PreinitializedMetric(final String description, final MetricUnit unit)
   {
      super(description, unit);
   }

   @Override
   public final void initialize(final List<Host> hosts)
   {
      // nop
   }
}
