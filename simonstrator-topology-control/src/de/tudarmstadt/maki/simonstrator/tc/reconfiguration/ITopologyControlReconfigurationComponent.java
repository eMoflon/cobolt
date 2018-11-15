package de.tudarmstadt.maki.simonstrator.tc.reconfiguration;

import de.tudarmstadt.maki.simonstrator.tc.component.TopologyControlComponent;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmID;

/**
 * Subclasses of this interface represent a MAPE-K component for Topology Control algorithms.
 *
 * @author Roland Kluge - Initial implementation
 */
public interface ITopologyControlReconfigurationComponent
{
   /**
    * Indicates that no reconfiguration shall be performed
    */
   TopologyControlReconfigurationDecision NO_RECONFIGURATION_DECISION = new NullReconfigurationDecision();
   TopologyControlAlgorithmID NO_RECONFIGURATION_OF_TC_ALGORITHM = null;
   double NO_RECONFIGURATION_OF_TC_INTERVAL = -1.0;

   /**
    * Initializes this component
    * @param topologyControlComponent the component from which information may be fetched
    */
   void initialize(final TopologyControlComponent topologyControlComponent);

   /**
    * Derives a {@link TopologyControlReconfigurationDecision} based on the current systemContext
    *
    * If no reconfiguration is proposed, {@link #NO_RECONFIGURATION_DECISION} is returned.
    *
    * @param systemContext the monitored system context
    * @return the reconfiguration decision or {@link #NO_RECONFIGURATION_DECISION} if no reconfiguration is proprosed
    */
   TopologyControlReconfigurationDecision proposeReconfiguration(TopologyControlSystemContext systemContext);

   /**
    * Returns the first point in time (in minutes) at which this component shall be active
    * @return the activation time
    */
   double getActivationTimeInMinutes();

   /**
    * Returns whether decisions of this component shall be (temporarily) ignored
    */
   boolean shallIgnoreDecision();
}
