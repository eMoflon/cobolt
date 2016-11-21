package de.tudarmstadt.maki.tc.cbctc.model.derivedfeatures;

import de.tudarmstadt.maki.tc.cbctc.model.Edge;
import de.tudarmstadt.maki.tc.cbctc.model.ModelPackage;
import de.tudarmstadt.maki.tc.cbctc.model.Topology;

public final class EdgeWeightProviders
{
   public static final EdgeWeightProvider DISTANCE_PROVIDER = new EAttributeBasedEdgeWeightProvider(ModelPackage.eINSTANCE.getEdge_Distance());

   public static final EdgeWeightProvider SQUARED_DISTANCE_PROVIDER = new EAttributeBasedEdgeWeightProvider(ModelPackage.eINSTANCE.getEdge_Distance(),
         x -> x * x);

   public static final EdgeWeightProvider EXPECTED_REMAINING_LIFETIME_PROVIDER = new EAttributeBasedEdgeWeightProvider(
         ModelPackage.eINSTANCE.getEdge_ExpectedLifetime());

   public static void apply(final Edge edge, EdgeWeightProvider weightProvider)
   {
      edge.setWeight(weightProvider.getEdgeWeight(edge));
   }

   public static void apply(final Topology topology, final EdgeWeightProvider weightProvider)
   {
      topology.getEdges().forEach(edge -> apply(edge, weightProvider));
   }
}
