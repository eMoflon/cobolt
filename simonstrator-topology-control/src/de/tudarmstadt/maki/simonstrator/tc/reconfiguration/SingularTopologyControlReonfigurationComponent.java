package de.tudarmstadt.maki.simonstrator.tc.reconfiguration;

import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmID;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmParamters;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyControlAlgorithms;

/**
 * Reconfiguration component that reconfigures the Topology Control component once, at a specific point in time.
 * 
 * The activation time of this component ({@link #getActivationTimeInMinutes()} is also the lower bound for its reconfiguration time
 *  
 * @author Roland Kluge - Initial implementation
 */
public class SingularTopologyControlReonfigurationComponent extends AbstractTopologyControlReconfigurationComponent
{
   private TopologyControlAlgorithmID algorithmId;

   private double timestampOfPreviousReconfigurationRequest;

   private TopologyControlAlgorithmParamters parameters;

   // Used in configuration files
   public void setAlgorithmId(final String algorithmIdString)
   {
      this.algorithmId = UnderlayTopologyControlAlgorithms.mapToTopologyControlID(algorithmIdString);
   }

   @Override
   public TopologyControlReconfigurationDecision proposeReconfiguration(final TopologyControlSystemContext systemContext)
   {
      final TopologyControlReconfigurationDecision decision;
      if (this.timestampOfPreviousReconfigurationRequest < this.getActivationTimeInMinutes() && systemContext.getTimestampInMinutes() >= this.getActivationTimeInMinutes())
      {
         decision = new TopologyControlReconfigurationDecision();
         decision.setAlgorithmId(algorithmId);
         decision.setAlgorithmParameters(this.parameters);
         decision.setTopologyControlIntervalInMinutes(ITopologyControlReconfigurationComponent.NO_RECONFIGURATION_OF_TC_INTERVAL);
      } else {
         decision = ITopologyControlReconfigurationComponent.NO_RECONFIGURATION_DECISION;
      }

      this.timestampOfPreviousReconfigurationRequest = systemContext.getTimestampInMinutes();
      
      return decision;
   }
   
   public void setTopologyControlAlgorithmParamters(final TopologyControlAlgorithmParamters parameters) {
      this.parameters = parameters;
   }

   @Override
   public String toString()
   {
      return String.format("%s [algoId=%s, reconfiguration time=%.2fm]", getClass().getSimpleName(), this.algorithmId, this.getActivationTimeInMinutes());
   }
}
