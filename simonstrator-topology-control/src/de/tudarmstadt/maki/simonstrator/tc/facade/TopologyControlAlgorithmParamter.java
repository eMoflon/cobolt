package de.tudarmstadt.maki.simonstrator.tc.facade;

import de.tudarmstadt.maki.simonstrator.api.util.XMLConfigurableConstructor;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyControlAlgorithms;

public class TopologyControlAlgorithmParamter
{
   public final TopologyControlAlgorithmParameterId<?> key;

   public final Object value;

   @XMLConfigurableConstructor({ "key", "value" })
   public TopologyControlAlgorithmParamter(final String name, final Integer value)
   {
      this(UnderlayTopologyControlAlgorithms.parseTopologyControlAlgorithmParameterId(name), value);
   }
   
   @XMLConfigurableConstructor({"key", "value"})
   public TopologyControlAlgorithmParamter(final String name, final Double value)
   {
      this(UnderlayTopologyControlAlgorithms.parseTopologyControlAlgorithmParameterId(name), value);
   }

   public TopologyControlAlgorithmParamter(final TopologyControlAlgorithmParameterId<?> key, final Object value)
   {
      this.key = key;
      this.value = value;
   }
   
   @Override
   public String toString()
   {
      return String.format("TCA Parameter [key=%s, value=%s]", this.key, this.value);
   }
}
