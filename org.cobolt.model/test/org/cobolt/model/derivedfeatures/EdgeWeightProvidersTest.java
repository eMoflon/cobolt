package org.cobolt.model.derivedfeatures;

import org.cobolt.model.Edge;
import org.cobolt.model.ModelFactory;
import org.cobolt.model.derivedfeatures.EdgeWeightProviders;
import org.cobolt.model.utils.TopologyControlTestHelper;
import org.junit.Test;

/**
 * Unit tests for {@link EdgeWeightProviders}
 * 
 * @author Roland Kluge - Initial implementation
 */
public final class EdgeWeightProvidersTest {
	@Test
	public void testDistanceEdgeWeightProvider() throws Exception {
		Edge edge = ModelFactory.eINSTANCE.createEdge();
		edge.setDistance(3.52);
		TopologyControlTestHelper.assertEquals0(3.52, EdgeWeightProviders.DISTANCE_PROVIDER.getEdgeWeight(edge));
	}

	@Test
	public void testSquaredDistanceEdgeWeightProvider() throws Exception {
		Edge edge = ModelFactory.eINSTANCE.createEdge();
		edge.setDistance(2.5);
		TopologyControlTestHelper.assertEquals0(6.25,
				EdgeWeightProviders.SQUARED_DISTANCE_PROVIDER.getEdgeWeight(edge));
	}
}
