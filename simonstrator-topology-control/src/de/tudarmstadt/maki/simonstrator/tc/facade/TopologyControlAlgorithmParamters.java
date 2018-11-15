package de.tudarmstadt.maki.simonstrator.tc.facade;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class TopologyControlAlgorithmParamters implements Iterable<TopologyControlAlgorithmParamter>
{

   private final Map<TopologyControlAlgorithmParameterId<?>, TopologyControlAlgorithmParamter> data = new HashMap<>();

   /**
    * Factory method for creating a parameter value set from a list of String-Object pairs.
    * 
    * In the given list, values at even indices must be Strings
    */
   public static TopologyControlAlgorithmParamters create(final Object... parameterList)
   {
      final TopologyControlAlgorithmParamters parameters = new TopologyControlAlgorithmParamters();
      if (!(parameterList.length % 2 == 0))
         throw new IllegalArgumentException("Parameter list must have even length");
      for (int i = 0; i < parameterList.length; i += 2)
      {
         final Object keyEntry = parameterList[i];
         final Object valueEntry = parameterList[i + 1];
         if (!(keyEntry instanceof TopologyControlAlgorithmParameterId<?>))
            throw new IllegalArgumentException(String.format("Parameter %s at even position %d must be a string.", keyEntry, i));

         final TopologyControlAlgorithmParameterId<?> key = (TopologyControlAlgorithmParameterId<?>) keyEntry;
         parameters.put(key, valueEntry);
      }
      return parameters;
   }

   /**
    * Stores the given {@link TopologyControlAlgorithmParamter} 
    * 
    * @param parameter the parameter
    */
   public void setTopologyControlAlgorithmParamter(final TopologyControlAlgorithmParamter parameter)
   {
      this.put(parameter.key, parameter.value);
   }

   /**
    * Stores the given parameter value assignment
    * @param key parameter name
    * @param value parameter value
    */
   public void put(final TopologyControlAlgorithmParameterId<?> key, final Object value)
   {
      this.put(new TopologyControlAlgorithmParamter(key, value));
   }

   public void put(final TopologyControlAlgorithmParamter parameter)
   {
      this.data.put(parameter.key, parameter);
   }

   public boolean hasParameter(TopologyControlAlgorithmParameterId<?> key)
   {
      return this.data.containsKey(key);
   }

   /**
    * Returns the parameter value for the given key
    * @param key the parameter name
    * @return the parameter value (if exists), else null.
    */
   public Object getValue(TopologyControlAlgorithmParameterId<?> key)
   {
      final TopologyControlAlgorithmParamter algorithmParamter = this.data.get(key);
      if (algorithmParamter == null)
         return null;
      else
         return algorithmParamter.value;
   }

   /**
    * Corresponds to #get(String) with and additional cast to {@link Integer}
    * @param key the parameter name
    */
   public Integer getInt(final TopologyControlAlgorithmParameterId<Integer> key)
   {
      return (Integer) getValue(key);
   }

   /**
    * Corresponds to #get(String) with and additional cast to {@link Double}
    * @param key the parameter name
    */
   public Double getDouble(final TopologyControlAlgorithmParameterId<Double> key)
   {
      return (Double) getValue(key);
   }

   /**
    * Returns a map-based view of this parameter set 
    */
   public Map<TopologyControlAlgorithmParameterId<?>, Object> getAll()
   {
      return Collections.unmodifiableMap(this.data);
   }

   /**
    * Removes all data from this container
    */
   public void clear()
   {
      this.data.clear();
   }

   public Stream<TopologyControlAlgorithmParamter> stream()
   {
      final Iterable<TopologyControlAlgorithmParamter> iterable = () -> iterator();
      final Stream<TopologyControlAlgorithmParamter> targetStream = StreamSupport.stream(iterable.spliterator(), false);
      return targetStream;
   }

   @Override
   public String toString()
   {
      return "Parameters: " + this.data;
   }

   @Override
   public Iterator<TopologyControlAlgorithmParamter> iterator()
   {
      return this.data.values().iterator();
   }

}
