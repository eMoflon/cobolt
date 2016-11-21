package de.tudarmstadt.maki.tc.cbctc.model.derivedfeatures;

import org.junit.Assert;
import org.junit.Test;

import de.tudarmstadt.maki.tc.cbctc.model.Edge;
import de.tudarmstadt.maki.tc.cbctc.model.ModelFactory;
import de.tudarmstadt.maki.tc.cbctc.model.TopologyTestUtils;

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
      Assert.assertEquals(3.52, EdgeWeightProviders.DISTANCE_PROVIDER.getEdgeWeight(edge), TopologyTestUtils.EPS_0);
   }

   @Test
   public void testSquaredDistanceEdgeWeightProvider() throws Exception
   {
      Edge edge = ModelFactory.eINSTANCE.createEdge();
      edge.setDistance(2.5);
      Assert.assertEquals(6.25, EdgeWeightProviders.SQUARED_DISTANCE_PROVIDER.getEdgeWeight(edge), TopologyTestUtils.EPS_0);
   }
}
