package de.tudarmstadt.maki.simonstrator.tc.reconfiguration;

import java.util.HashMap;

import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmID;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmParameterId;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmParamter;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmParamters;

public class TopologyControlReconfigurationDecision
{
   private TopologyControlAlgorithmID algorithmId;

   private boolean algorithmIdHasChanged;

   private TopologyControlAlgorithmParamters parameters;

   private HashMap<TopologyControlAlgorithmParameterId<?>, Boolean> parameterModificationState;

   private double topologyControlIntervalInMinutes;

   /**
    * Creates am empty reconfiguration decision
    */
   public TopologyControlReconfigurationDecision()
   {
      topologyControlIntervalInMinutes = ITopologyControlReconfigurationComponent.NO_RECONFIGURATION_OF_TC_INTERVAL;
      algorithmId = ITopologyControlReconfigurationComponent.NO_RECONFIGURATION_OF_TC_ALGORITHM;
      algorithmIdHasChanged = false;
      parameters = new TopologyControlAlgorithmParamters();
      parameterModificationState = new HashMap<>();
   }

   /**
    * Pre-populates this instance with configuration information from the given system context
    * @param systemContext the previous system context. May be null to indicate no knowledge about the context
    */
   public TopologyControlReconfigurationDecision(final TopologyControlSystemContext systemContext)
   {
      this();
      if (systemContext != null)
      {
         initializeFromSystemContext(systemContext);
      }
   }

   public void setAlgorithmId(final TopologyControlAlgorithmID algorithmId)
   {
      this.algorithmIdHasChanged = areUnequalWithNullCheck(algorithmId, this.algorithmId);
      this.algorithmId = algorithmId;
   }

   public TopologyControlAlgorithmID getAlgorithmId()
   {
      return algorithmId;
   }

   public boolean hasAlgorithmIdChanged()
   {
      return algorithmIdHasChanged;
   }

   public void setAlgorithmParameters(final TopologyControlAlgorithmParamters parameters)
   {
      this.parameters.stream().forEach(parameter -> this.setAlgorithmParameter(parameter));
   }

   public void setAlgorithmParameter(final TopologyControlAlgorithmParamter parameter)
   {
      final boolean hasChanged = this.parameters.hasParameter(parameter.key)
            && (areUnequalWithNullCheck(this.parameters.getValue(parameter.key), parameter.value));
      this.parameterModificationState.put(parameter.key, hasChanged);
      this.parameters.put(parameter.key, parameter.value);
   }

   public boolean hasAlgorithmParameterChanged(final TopologyControlAlgorithmParameterId<?> parameter)
   {
      return !this.parameters.hasParameter(parameter) || this.parameterModificationState.get(parameter);
   }

   public TopologyControlAlgorithmParamters getAlgorithmParameters()
   {
      return this.parameters;
   }

   public double getTopologyControlIntervalInMinutes()
   {
      return topologyControlIntervalInMinutes;
   }

   public void setTopologyControlIntervalInMinutes(final double topologyControlIntervalInMinutes)
   {
      this.topologyControlIntervalInMinutes = topologyControlIntervalInMinutes;
   }

   @Override
   public String toString()
   {
      return "TopologyControlReconfigurationDecision [algorithmId=" + algorithmId + ", algorithmIdHasChanged=" + algorithmIdHasChanged
            + ", algorithmParameterMapping=" + parameters + ", algorithmParameterHasChanged=" + parameterModificationState
            + ", topologyControlIntervalInMinutes=" + topologyControlIntervalInMinutes + "]";
   }

   /**
    * Uses the given {@link TopologyControlSystemContext} to initialize this decision
    * @param systemContext the system context. Must not be <code>null</code>
    */
   private void initializeFromSystemContext(final TopologyControlSystemContext systemContext)
   {
      this.setAlgorithmId(systemContext.getConfiguration().topologyControlAlgorithmID);
      this.setTopologyControlIntervalInMinutes(systemContext.getConfiguration().topologyControlIntervalInMinutes);
      for (final TopologyControlAlgorithmParamter parameter : systemContext.getConfiguration().topologyControlAlgorithmParamters)
      {
         this.setAlgorithmParameter(parameter);
      }
   }

   /**
    * Returns true if exactly one of the given parameters is null
    */
   private static boolean exactlyOneIsNull(Object o1, Object o2)
   {
      return (o1 == null) ^ (o2 == null);
   }

   static <T> boolean areUnequalWithNullCheck(final T o1, final T o2)
   {
      return exactlyOneIsNull(o1, o2) || (o1 != null && !o1.equals(o2));
   }

}
