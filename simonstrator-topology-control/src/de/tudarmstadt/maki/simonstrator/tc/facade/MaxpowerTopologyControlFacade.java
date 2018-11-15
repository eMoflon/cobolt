package de.tudarmstadt.maki.simonstrator.tc.facade;

import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.tc.underlay.EdgeState;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyProperties;

/**
 * This facade implements the maxpower topology control algorithm that simply activates all links in its input topology.
 * 
 * @author Roland Kluge - Initial implementation
 *
 */
public class MaxpowerTopologyControlFacade extends TopologyControlFacade_ImplBase
{
   @Override
   public void run(final TopologyControlAlgorithmParamters parameters)
   {
      this.simonstratorGraph.getEdges().forEach(e -> activateLink(e));
   }

   private void activateLink(IEdge edge)
   {
      final EdgeState originalState = edge.getProperty(UnderlayTopologyProperties.EDGE_STATE);
      if (originalState != EdgeState.ACTIVE)
      {
         edge.setProperty(UnderlayTopologyProperties.EDGE_STATE, EdgeState.ACTIVE);
         fireLinkStateChanged(edge);
      }
   }

}
