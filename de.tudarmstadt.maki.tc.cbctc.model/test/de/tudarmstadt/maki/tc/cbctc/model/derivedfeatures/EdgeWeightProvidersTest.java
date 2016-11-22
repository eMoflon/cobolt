package de.tudarmstadt.maki.tc.cbctc.model.derivedfeatures;

import org.junit.Test;

import de.tudarmstadt.maki.simonstrator.tc.testing.TopologyControlTestHelper;
import de.tudarmstadt.maki.tc.cbctc.model.Edge;
import de.tudarmstadt.maki.tc.cbctc.model.ModelFactory;

/**
 * Unit tests for {@link EdgeWeightProviders}
 * 
 * @author Roland Kluge - Initial implementation
 */
public final class EdgeWeightProvidersTest
{
   @Test
   public void testDistanceEdgeWeightProvider() throws Exception
   {
      Edge edge = ModelFactory.eINSTANCE.createEdge();
      edge.setDistance(3.52);
      TopologyControlTestHelper.assertEquals0(3.52, EdgeWeightProviders.DISTANCE_PROVIDER.getEdgeWeight(edge));
   }

   @Test
   public void testSquaredDistanceEdgeWeightProvider() throws Exception
   {
      Edge edge = ModelFactory.eINSTANCE.createEdge();
      edge.setDistance(2.5);
      TopologyControlTestHelper.assertEquals0(6.25, EdgeWeightProviders.SQUARED_DISTANCE_PROVIDER.getEdgeWeight(edge));
   }
}
