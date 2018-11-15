package de.tudarmstadt.maki.simonstrator.tc.monitoring;

import java.util.HashMap;
import java.util.Map;

import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric;
import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric.MetricValue;

/**
 * An information record aggregates {@link Metric}s at a given point in time
 * 
 * @author Roland Kluge - Initial implementation
 */
public class InformationRecord
{
   /**
    * Maps from {@link Metric} name to {@link Metric}
    */
   private final Map<String, Metric<MetricValue<?>>> data = new HashMap<>();

   /**
    * The container of this record
    */
   private TopologyControlInformationStoreComponent informationStore;

   /**
    * Marks the time at which the this record has been created
    */
   private long timestamp;

   /**
    * Creates an {@link InformationRecord} for the given {@link TopologyControlInformationStoreComponent} at the given timestamp
    * 
    * This constructor is not part of the public API - create records via {@link TopologyControlInformationStoreComponent} instead.
    * 
    * @param informationStore the information store
    * @param timestamp the timestamp
    */
   InformationRecord(final TopologyControlInformationStoreComponent informationStore, final long timestamp)
   {
      this.informationStore = informationStore;
      this.timestamp = timestamp;
   }

   /**
    * Stores a datum in this {@link InformationRecord}.
    * 
    * If a metric of the same type already exists, it is overridden
    * 
    * This method also invokes {@link TopologyControlInformationStoreComponent#notifyMetricAdded(Metric, long)}
    *  
    * @param metric the datum to store
    */
   public void put(final Metric<MetricValue<?>> metric)
   {
      this.data.put(metric.getName(), metric);
      
      if (this.informationStore != null) {
         this.informationStore.notifyMetricAdded(metric, this.timestamp);
      }
   }

   /**
    * The metric for with the given name or <code>null</code> if no such datum exists
    * 
    * @param metricName the name of the desired metric
    * @return the metric or <code>null</code>
    */
   public Metric<MetricValue<?>> get(final String metricName)
   {
      return data.get(metricName);
   }

   @Override
   public String toString()
   {
      return "InformationRecord [data=" + data + "]";
   }
}
