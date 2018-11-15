package de.tudarmstadt.maki.simonstrator.tc.weighting;

import de.tudarmstadt.maki.simonstrator.tc.facade.ITopologyControlFacade;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyProperties;

public class EdgeWeightProviders
{
   public static void apply(final ITopologyControlFacade facade, final EdgeWeightProvider provider)
   {
      facade.getGraph().getEdges().forEach(edge -> {
         edge.setProperty(UnderlayTopologyProperties.WEIGHT, provider.calculateWeight(edge, facade.getGraph()));
         facade.updateEdgeAttribute(edge, UnderlayTopologyProperties.WEIGHT);
      });
   }

}
