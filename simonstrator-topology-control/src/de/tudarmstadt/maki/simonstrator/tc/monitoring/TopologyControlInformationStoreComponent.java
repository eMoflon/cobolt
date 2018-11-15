package de.tudarmstadt.maki.simonstrator.tc.monitoring;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric;
import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric.MetricValue;
import de.tudarmstadt.maki.simonstrator.api.component.ComponentNotAvailableException;
import de.tudarmstadt.maki.simonstrator.api.component.HostComponent;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics.MetricUtils;

public class TopologyControlInformationStoreComponent implements HostComponent
{
   private final Host host;

   /**
    * Maps a simulation timestamp to the corresponding information record
    */
   private final Map<Long, InformationRecord> monitoringData;

   /**
    * Maintains an internal index of {@link Metric}s by metric name.
    * The {@link List} in the value part of the map should be ordered by time
    */
   private final Map<String, List<Metric<MetricValue<?>>>> metricIndex;

   /**
    * Initializes this information store for the given host
    *
    * The host may be <code>null</code> if the component is initialized in stand-alone mode.
    * @param host the host of this component
    */
   public TopologyControlInformationStoreComponent(final Host host)
   {
      this.host = host;
      this.monitoringData = new TreeMap<>();
      this.metricIndex = new HashMap<>();
   }

   /**
    * Locates the {@link TopologyControlInformationStoreComponent} on the given {@link Host}
    *
    * @throws RuntimeException if the component cannot be found
    */
   public static TopologyControlInformationStoreComponent find(final Host host)
   {
      final Class<TopologyControlInformationStoreComponent> componentClass = TopologyControlInformationStoreComponent.class;
      try
      {
         return host.getComponent(componentClass);
      } catch (final ComponentNotAvailableException e)
      {
         throw new RuntimeException(String.format("Missing component %s on host %s", componentClass.getSimpleName(), host), e);
      }
   }

   /**
    * Nothing to do
    */
   @Override
   public void initialize()
   {
      // nop
   }

   /**
    * Nothing to do
    */
   @Override
   public void shutdown()
   {
      // nop
   }

   @Override
   public Host getHost()
   {
      return this.host;
   }

   /**
    * Stores measured data for the given timestamp (in simulation time units)
    * @param timestamp the timestamp
    * @param data the data to store
    */
   public void put(final long timestamp, final Metric<MetricValue<?>> data)
   {
      this.getOrCreateRecord(timestamp).put(data);
   }

   /**
    * Stores measured data for the {@link InformationRecord} with the most recent time stamp
    * @param data the data to store
    * @throws IllegalStateException if this information store has no records
    */
   public void put(final Metric<MetricValue<?>> data) {
      final Set<Long> timestamps = this.monitoringData.keySet();
      if (timestamps.isEmpty())
         throw new IllegalStateException("Information store is empty");

      final long latestTimestamp = Collections.max(timestamps);
      this.put(latestTimestamp, data);
   }

   /**
    * Returns the {@link Metric} with the given name at the given point in time or <code>null</code> if no such datum exists
    * @param timestamp the timestamp in simulation time units
    * @param metricName the name of the metric
    * @return the metric or <code>null</code>
    */
   public Metric<MetricValue<?>> get(final long timestamp, final String metricName)
   {
      return this.getOrCreateRecord(timestamp).get(metricName);
   }

   /**
    * Convenience method for {@link #getOldest(String)} using {@link MetricUtils#getDefaultName(Class)}
    * @param metricType the type of the metric to return
    * @return metric or <code>null</code> if there is no value for the metric
    */
   public Metric<MetricValue<?>> getOldestByType(final Class<? extends Metric<?>> metricType)
   {
      return getOldest(MetricUtils.getDefaultName(metricType));
   }

   /**
    * Convenience method for returning the metric with the minimal time stamp
    * @param metricName the name of the metric
    * @return the metric or <code>null</code> if there is no value for the metric
    */
   public Metric<MetricValue<?>> getOldest(final String metricName)
   {
      final List<Metric<MetricValue<?>>> metricsByName = getMetricsByName(metricName);
      return metricsByName.isEmpty() ? null : metricsByName.get(0);
   }

   /**
    * Convenience method for {@link #getLatest(String)} using {@link MetricUtils#getDefaultName(Class)}
    * @param metricType the type of the metric to return
    * @return metric or <code>null</code> if there is no value for the metric
    */
   public Metric<MetricValue<?>> getLatestByType(final Class<? extends Metric<?>> metricType)
   {
      return getLatest(MetricUtils.getDefaultName(metricType));
   }

   /**
    * Convenience method for returning the metric with the newest time stamp
    * @param metricName the name of the metric
    * @return the metric or <code>null</code> if there is no value for the metric
    */
   public Metric<MetricValue<?>> getLatest(final String metricName)
   {
      final List<Metric<MetricValue<?>>> metricsByName = getMetricsByName(metricName);
      return metricsByName.isEmpty() ? null : metricsByName.get(metricsByName.size() - 1);
   }

   /**
    * Creates an empty {@link InformationRecord} for the given timestamp.
    *
    * If this component already contains a record for the timestamp, the existing record is returned
    *
    * @param timestamp the timestamp
    *
    * @return the information record
    */
   public InformationRecord getOrCreateRecord(final long timestamp)
   {
      if (!this.monitoringData.containsKey(timestamp))
      {
         this.monitoringData.put(timestamp, new InformationRecord(this, timestamp));
      }

      return this.monitoringData.get(timestamp);
   }

   /**
    * Returns all metrics having the given name
    *
    * @param metricName the name of the desired metrics
    * @return the list of all metrics with the given name
    */
   public List<Metric<MetricValue<?>>> getMetricsByName(final String metricName)
   {
      return this.metricIndex.containsKey(metricName) ? this.metricIndex.get(metricName) : Collections.emptyList();
   }

   /**
    * Returns all metrics having the given type, using the convention that the simple class name is equal to the metric name
    *
    * @param metricType the type of the desired metrics
    * @return
    * @return the list of all metrics with the given type
    * @see MetricUtils#getDefaultName(Class)
    */
   public List<Metric<MetricValue<?>>> getMetricsByType(final Class<? extends Metric<?>> metricType)
   {
      return this.getMetricsByName(MetricUtils.getDefaultName(metricType));
   }

   /**
    * Notifies this store about a new {@link Metric} being added to one of its child {@link InformationRecord}s
    * @param metric the new metric
    * @param timestamp
    */
   void notifyMetricAdded(final Metric<MetricValue<?>> metric, final long timestamp)
   {
      final String name = metric.getName();
      if (!this.metricIndex.containsKey(name))
      {
         this.metricIndex.put(name, new ArrayList<Metric<MetricValue<?>>>());
      }

      this.metricIndex.get(name).add(metric);
   }

}
