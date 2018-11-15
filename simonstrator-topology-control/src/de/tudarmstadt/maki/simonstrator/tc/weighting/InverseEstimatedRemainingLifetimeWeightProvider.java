package de.tudarmstadt.maki.simonstrator.tc.weighting;

import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.GraphElementProperties;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INode;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyProperties;

public class InverseEstimatedRemainingLifetimeWeightProvider implements EdgeWeightProvider
{

   public static final InverseEstimatedRemainingLifetimeWeightProvider INSTANCE = new InverseEstimatedRemainingLifetimeWeightProvider();

   private InverseEstimatedRemainingLifetimeWeightProvider()
   {
      // Singleton constructor
   }
   
   @Override
   public double calculateWeight(final IEdge edge, final Graph graph)
   {

      final INode source = graph.getNode(edge.fromId());

      GraphElementProperties.validateThatPropertyIsPresent(source, UnderlayTopologyProperties.REMAINING_ENERGY);
      final Double remainingEnergy = source.getProperty(UnderlayTopologyProperties.REMAINING_ENERGY);

      if (remainingEnergy == 0.0)
         return Double.MAX_VALUE;

      GraphElementProperties.validateThatPropertyIsPresent(edge, UnderlayTopologyProperties.REQUIRED_TRANSMISSION_POWER);
      final Double requiredTransmissionPower = edge.getProperty(UnderlayTopologyProperties.REQUIRED_TRANSMISSION_POWER);

      return requiredTransmissionPower / remainingEnergy;
   }

}
