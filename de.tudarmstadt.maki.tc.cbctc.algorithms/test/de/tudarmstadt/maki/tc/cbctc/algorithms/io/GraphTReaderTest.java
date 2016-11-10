package de.tudarmstadt.maki.tc.cbctc.algorithms.io;

import static de.tudarmstadt.maki.tc.cbctc.algorithms.TopologyControlAlgorithmsTestUtils.getPathToDistanceTestGraph;
import static de.tudarmstadt.maki.tc.cbctc.algorithms.TopologyControlAlgorithmsTestUtils.getPathToEnergyTestGraph;
import static de.tudarmstadt.maki.tc.cbctc.model.TopologyTestUtils.EPS_0;
import static de.tudarmstadt.maki.tc.cbctc.model.TopologyTestUtils.EPS_6;
import static de.tudarmstadt.maki.tc.cbctc.model.TopologyTestUtils.assertEdgeDistance;
import static de.tudarmstadt.maki.tc.cbctc.model.TopologyTestUtils.assertEdgeWeight;
import static de.tudarmstadt.maki.tc.cbctc.model.TopologyTestUtils.assertEquals0;
import static de.tudarmstadt.maki.tc.cbctc.model.TopologyTestUtils.assertEquals6;
import static de.tudarmstadt.maki.tc.cbctc.model.TopologyTestUtils.assertIsStatewiseSymmetric;
import static de.tudarmstadt.maki.tc.cbctc.model.TopologyTestUtils.assertNodeAndEdgeCount;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.tudarmstadt.maki.tc.cbctc.model.Edge;
import de.tudarmstadt.maki.tc.cbctc.model.EdgeState;
import de.tudarmstadt.maki.tc.cbctc.model.ModelFactory;
import de.tudarmstadt.maki.tc.cbctc.model.Topology;
import de.tudarmstadt.maki.tc.cbctc.model.derivedfeatures.EdgeWeightProviders;

/**
 * Unit tests for {@link GraphTReader}
 */
public class GraphTReaderTest {

	private Topology topology;

	@Before
	public void setup() {
		this.topology = ModelFactory.eINSTANCE.createTopology();
	}

	@Test
	public void testWithTestgraphD1() throws Exception {
		GraphTReader.readTopology(this.topology, getPathToDistanceTestGraph(1));
		EdgeWeightProviders.applyEdgeWeightProvider(this.topology, EdgeWeightProviders.DISTANCE_PROVIDER);

		Assert.assertEquals(5, topology.getNodeCount());
		Assert.assertEquals(2 * 7, topology.getEdgeCount());

		final Edge link34 = topology.getEdgeById("e34");
		Assert.assertEquals(22.0, link34.getWeight(), EPS_0);

		final Edge revLink34 = link34.getReverseEdge();
		Assert.assertEquals(22.0, revLink34.getWeight(), EPS_0);
		Assert.assertEquals("e43", revLink34.getId());

		Assert.assertSame(link34, revLink34.getReverseEdge());
		assertIsStatewiseSymmetric(topology);
	}
	
	@Test
   public void testWithTestgraphD2() throws Exception {
	   GraphTReader.readTopology(this.topology, getPathToDistanceTestGraph(2));
	}

	@Test
	public void testWithTestgraphD3() throws Exception {
		GraphTReader.readTopology(this.topology, getPathToDistanceTestGraph(3));
		EdgeWeightProviders.applyEdgeWeightProvider(this.topology, EdgeWeightProviders.DISTANCE_PROVIDER);

		Assert.assertEquals(3, topology.getNodeCount());
		Assert.assertEquals(2 * 3, topology.getEdgeCount());

		final Edge link13 = topology.getEdgeById("e13");
		Assert.assertEquals(20.0, link13.getWeight(), 0.0);
		assertIsStatewiseSymmetric(topology);
	}

	@Test
	public void testWithTestgrahpD4() throws Exception {
		GraphTReader.readTopology(this.topology, getPathToDistanceTestGraph(4));
		EdgeWeightProviders.applyEdgeWeightProvider(this.topology, EdgeWeightProviders.DISTANCE_PROVIDER);

		assertEdgeDistance(topology, "e1-2", 10.0);
		assertEdgeDistance(topology, "e1-3", 20.0);
		assertEdgeDistance(topology, "e1-7", 10.0);
		assertEdgeDistance(topology, "e1-7", 10.0);

		assertIsStatewiseSymmetric(topology);

	}

	@Test
	public void testWithTestgrahpD5() throws Exception {
		GraphTReader.readTopology(this.topology, getPathToDistanceTestGraph(5));
		EdgeWeightProviders.applyEdgeWeightProvider(this.topology, EdgeWeightProviders.DISTANCE_PROVIDER);

		assertEdgeDistance(topology, "e1-2", 15.0);
		assertEdgeDistance(topology, "e1-3", 20.0);
		assertEdgeDistance(topology, "e1-4", 25.0);
		assertEdgeDistance(topology, "e1-5", 30.0);
		assertEdgeDistance(topology, "e2-3", 17.0);
		assertEdgeDistance(topology, "e3-4", 22.0);
		assertEdgeDistance(topology, "e4-5", 27.0);

		assertIsStatewiseSymmetric(topology);

	}
	
	@Test
	public void testWithTestgrahpD6() throws Exception {
	   GraphTReader.readTopology(this.topology, getPathToDistanceTestGraph(6));
	   
	   assertNodeAndEdgeCount(topology, 3, 3);
	   
	   assertEdgeWeight(topology, "e12", 5);
	   assertEdgeWeight(topology, "e13", 3);
	   assertEdgeWeight(topology, "e32", 4);
	}
	
	@Test
	public void testWithTestgrahpD7() throws Exception {
	   GraphTReader.readTopology(this.topology, getPathToDistanceTestGraph(7));
	   
	   assertNodeAndEdgeCount(topology, 3, 4);
	   
	   assertEquals6(3, topology.getNodeById("1").getX());
	   assertEquals6(4, topology.getNodeById("1").getY());
	   assertEquals6(123.2, topology.getNodeById("2").getEnergyLevel());
	   assertEquals0(7, topology.getNodeById("3").getHopCount());
	   
	   assertEquals6(5, topology.getEdgeById("e12").getWeight());
	   assertEquals6(3, topology.getEdgeById("e12").getDistance());
	   assertEquals6(180, topology.getEdgeById("e13").getAngle());
	   Assert.assertEquals(EdgeState.INACTIVE, topology.getEdgeById("e13").getState());
	   Assert.assertEquals(EdgeState.ACTIVE, topology.getEdgeById("e32").getState());
	   Assert.assertEquals(EdgeState.UNCLASSIFIED, topology.getEdgeById("e23").getState());
	   Assert.assertEquals(topology.getEdgeById("e32"), topology.getEdgeById("e23").getReverseEdge());
	}

   @Test
   public void testWithTestgraphE1() throws Exception {
   	GraphTReader.readTopology(this.topology, getPathToEnergyTestGraph(1));
   	EdgeWeightProviders.applyEdgeWeightProvider(this.topology, EdgeWeightProviders.EXPECTED_REMAINING_LIFETIME_PROVIDER);
   
   	Assert.assertEquals(6, topology.getEdgeCount());
   	Assert.assertEquals(3, topology.getNodeCount());
   
   	Assert.assertEquals(10, topology.getNodeById("n1").getEnergyLevel(), EPS_6);
   	Assert.assertEquals(2, topology.getEdgeById("e12").getExpectedLifetime(), EPS_6);
   	Assert.assertEquals(1, topology.getEdgeById("e13").getExpectedLifetime(), EPS_6);
   	Assert.assertEquals(2, topology.getEdgeById("e23").getExpectedLifetime(), EPS_6);
   
   	assertIsStatewiseSymmetric(topology);
   
   }

}