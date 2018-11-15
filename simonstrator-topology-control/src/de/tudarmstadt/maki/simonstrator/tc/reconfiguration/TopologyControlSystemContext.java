package de.tudarmstadt.maki.simonstrator.tc.reconfiguration;

import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric;
import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric.MetricValue;
import de.tudarmstadt.maki.simonstrator.tc.component.TopologyControlComponent;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.TopologyControlInformationStoreComponent;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics.TimestampMetric;

public class TopologyControlSystemContext
{
   private final TopologyControlComponent topologyControlComponent;

   public TopologyControlSystemContext(TopologyControlComponent topologyControlComponent)
   {
      this.topologyControlComponent = topologyControlComponent;
   }

   public double getTimestampInMinutes()
   {
      return 1.0 * getRawTimestamp() / Time.MINUTE;
   }

   public long getRawTimestamp()
   {
      final Long timestamp = this.getMonitoringDataLong(TimestampMetric.class);
      if (timestamp == null)
         throw new IllegalStateException("Timestamp has not been set.");
      return timestamp;
   }

   @Override
   public String toString()
   {
      return String.format("System context: (time=%s)", Time.getFormattedTime(this.getRawTimestamp()));
   }

   public TopologyControlInformationStoreComponent getInformationStore()
   {
      return this.topologyControlComponent.getInformationStore();
   }

   public TopologyControlComponentConfig getConfiguration()
   {
      return this.topologyControlComponent.getConfiguration();
   }

   public TopologyControlComponent getTopologyControlComponent() {
      return this.topologyControlComponent;
   }

   public Integer getMonitoringDataInt(final Class<? extends Metric<?>> metricType)
   {
      return (Integer) getMonitoringData(metricType);
   }

   public Long getMonitoringDataLong(final Class<? extends Metric<?>> metricType)
   {
      return (Long) getMonitoringData(metricType);
   }

   public Double getMonitoringDataDouble(final Class<? extends Metric<?>> metricType)
   {
      return (Double) getMonitoringData(metricType);
   }

   private Object getMonitoringData(final Class<? extends Metric<?>> metricType)
   {
      final TopologyControlInformationStoreComponent informationStore = this.getInformationStore();
      if (informationStore == null)
         throw new IllegalStateException("No information record set");

      final Metric<MetricValue<?>> data = informationStore.getLatestByType(metricType);
      if (data == null)
         throw new IllegalArgumentException("No metric with name " + metricType + " in " + informationStore);

      final MetricValue<?> metricValue = data.getOverallMetric();
      final Object value = metricValue.getValue();
      return value;
   }

}
