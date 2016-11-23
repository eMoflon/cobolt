package de.tudarmstadt.maki.tc.cbctc.algorithms.io;

import static de.tudarmstadt.maki.tc.cbctc.algorithms.TopologyControlAlgorithmsTestUtils.getPathToAngleTestGraph;
import static de.tudarmstadt.maki.tc.cbctc.algorithms.TopologyControlAlgorithmsTestUtils.getPathToDistanceTestGraph;
import static de.tudarmstadt.maki.tc.cbctc.algorithms.TopologyControlAlgorithmsTestUtils.getPathToEnergyTestGraph;
import static de.tudarmstadt.maki.tc.cbctc.algorithms.TopologyControlAlgorithmsTestUtils.getPathToHopCountTestGraph;
import static de.tudarmstadt.maki.tc.cbctc.model.TopologyModelTestUtils.assertEdgeDistance;
import static de.tudarmstadt.maki.tc.cbctc.model.TopologyModelTestUtils.assertEdgeWeight;
import static de.tudarmstadt.maki.tc.cbctc.model.TopologyModelTestUtils.assertIsStatewiseSymmetric;
import static de.tudarmstadt.maki.tc.cbctc.model.TopologyModelTestUtils.assertNodeAndEdgeCount;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.tudarmstadt.maki.simonstrator.tc.io.GraphTReader;
import de.tudarmstadt.maki.simonstrator.tc.testing.TopologyControlTestHelper;
import de.tudarmstadt.maki.tc.cbctc.model.Edge;
import de.tudarmstadt.maki.tc.cbctc.model.EdgeState;
import de.tudarmstadt.maki.tc.cbctc.model.ModelFactory;
import de.tudarmstadt.maki.tc.cbctc.model.Topology;
import de.tudarmstadt.maki.tc.cbctc.model.TopologyModelTestUtils;
import de.tudarmstadt.maki.tc.cbctc.model.derivedfeatures.EdgeWeightProviders;

/**
 * Unit tests for {@link GraphTReader}
 */
public class GraphTReaderTest {

	private Topology topology;
   private TopologyModelGraphTReader reader;

	@Before
	public void setup() {
		this.topology = ModelFactory.eINSTANCE.createTopology();
      this.reader = new TopologyModelGraphTReader();
	}

	@Test
	public void testWithTestgraphD1() throws Exception {
		this.reader.read(this.topology, getPathToDistanceTestGraph(1));
		EdgeWeightProviders.apply(this.topology, EdgeWeightProviders.DISTANCE_PROVIDER);

		Assert.assertEquals(5, topology.getNodeCount());
		Assert.assertEquals(2 * 7, topology.getEdgeCount());

		final Edge link34 = topology.getEdgeById("e34");
		TopologyControlTestHelper.assertEquals0(22.0, link34.getWeight());

		final Edge revLink34 = link34.getReverseEdge();
		TopologyControlTestHelper.assertEquals0(22.0, revLink34.getWeight());
		Assert.assertEquals("e43", revLink34.getId());

		Assert.assertSame(link34, revLink34.getReverseEdge());
		assertIsStatewiseSymmetric(topology);
	}
	
	@Test
   public void testWithTestgraphD2() throws Exception {
	   this.reader.read(this.topology, getPathToDistanceTestGraph(2));
	}

	@Test
	public void testWithTestgraphD3() throws Exception {
	   this.reader.read(this.topology, getPathToDistanceTestGraph(3));
		EdgeWeightProviders.apply(this.topology, EdgeWeightProviders.DISTANCE_PROVIDER);

		Assert.assertEquals(3, topology.getNodeCount());
		Assert.assertEquals(2 * 3, topology.getEdgeCount());

		final Edge link13 = topology.getEdgeById("e13");
		Assert.assertEquals(20.0, link13.getWeight(), 0.0);
		assertIsStatewiseSymmetric(topology);
	}

	@Test
	public void testWithTestgrahpD4() throws Exception {
	   this.reader.read(this.topology, getPathToDistanceTestGraph(4));
		EdgeWeightProviders.apply(this.topology, EdgeWeightProviders.DISTANCE_PROVIDER);

		assertEdgeDistance(topology, "e1-2", 10.0);
		assertEdgeDistance(topology, "e1-3", 20.0);
		assertEdgeDistance(topology, "e1-7", 10.0);
		assertEdgeDistance(topology, "e1-7", 10.0);

		assertIsStatewiseSymmetric(topology);

	}

	@Test
	public void testWithTestgrahpD5() throws Exception {
	   this.reader.read(this.topology, getPathToDistanceTestGraph(5));
		EdgeWeightProviders.apply(this.topology, EdgeWeightProviders.DISTANCE_PROVIDER);

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
	   this.reader.read(this.topology, getPathToDistanceTestGraph(6));
	   
	   assertNodeAndEdgeCount(topology, 3, 3);
	   
	   assertEdgeWeight(topology, "e12", 5);
	   assertEdgeWeight(topology, "e13", 3);
	   assertEdgeWeight(topology, "e32", 4);
	}
	
	@Test
	public void testWithTestgrahpD7() throws Exception {
	   this.reader.read(this.topology, getPathToDistanceTestGraph(7));
	   
	   assertNodeAndEdgeCount(topology, 3, 4);
	   
	   TopologyControlTestHelper.assertEquals6(3, topology.getNodeById("1").getX());
	   TopologyControlTestHelper.assertEquals6(4, topology.getNodeById("1").getY());
	   TopologyControlTestHelper.assertEquals6(123.2, topology.getNodeById("2").getEnergyLevel());
	   TopologyControlTestHelper.assertEquals0(7, topology.getNodeById("3").getHopCount());
	   
	   TopologyControlTestHelper.assertEquals6(5, topology.getEdgeById("e12").getWeight());
	   TopologyControlTestHelper.assertEquals6(3, topology.getEdgeById("e12").getDistance());
	   TopologyControlTestHelper.assertEquals6(180, topology.getEdgeById("e13").getAngle());
	   Assert.assertEquals(EdgeState.INACTIVE, topology.getEdgeById("e13").getState());
	   Assert.assertEquals(EdgeState.ACTIVE, topology.getEdgeById("e32").getState());
	   Assert.assertEquals(EdgeState.UNCLASSIFIED, topology.getEdgeById("e23").getState());
	   Assert.assertEquals(topology.getEdgeById("e32"), topology.getEdgeById("e23").getReverseEdge());
	}

   @Test
   public void testWithTestgraphE1() throws Exception {
      this.reader.read(this.topology, getPathToEnergyTestGraph(1));
   	EdgeWeightProviders.apply(this.topology, EdgeWeightProviders.EXPECTED_REMAINING_LIFETIME_PROVIDER);

      TopologyModelTestUtils.assertNodeAndEdgeCount(this.topology, 3, 6);
   
      TopologyControlTestHelper.assertEquals6(10, topology.getNodeById("n1").getEnergyLevel());
      TopologyControlTestHelper.assertEquals6(2, topology.getEdgeById("e12").getExpectedLifetime());
      TopologyControlTestHelper.assertEquals6(1, topology.getEdgeById("e13").getExpectedLifetime());
      TopologyControlTestHelper.assertEquals6(2, topology.getEdgeById("e23").getExpectedLifetime());
   
   	assertIsStatewiseSymmetric(topology);
   
   }

   @Test
   public void testWithTestgraphH1() throws Exception
   {
      this.reader.read(this.topology, getPathToHopCountTestGraph(1));
      
      TopologyModelTestUtils.assertNodeAndEdgeCount(this.topology, 3, 3);
      
      Assert.assertEquals(1, this.topology.getNodeById("1").getHopCount());
      Assert.assertEquals(1, this.topology.getNodeById("2").getHopCount());
      Assert.assertEquals(1, this.topology.getNodeById("3").getHopCount());
      
      TopologyControlTestHelper.assertEquals0(5, this.topology.getEdgeById("e12").getWeight());
      TopologyControlTestHelper.assertEquals0(1, this.topology.getEdgeById("e13").getWeight());
      TopologyControlTestHelper.assertEquals0(3, this.topology.getEdgeById("e32").getWeight());
   }
   
   @Test
   public void testWithTestgraphH2() throws Exception
   {
      this.reader.read(this.topology, getPathToHopCountTestGraph(2));
      
      TopologyModelTestUtils.assertNodeAndEdgeCount(this.topology, 3, 3);
      
      Assert.assertEquals(10, this.topology.getNodeById("1").getHopCount());
      Assert.assertEquals(1, this.topology.getNodeById("2").getHopCount());
      Assert.assertEquals(11, this.topology.getNodeById("3").getHopCount());
      
      TopologyControlTestHelper.assertEquals0(5, this.topology.getEdgeById("e12").getWeight());
      TopologyControlTestHelper.assertEquals0(1, this.topology.getEdgeById("e13").getWeight());
      TopologyControlTestHelper.assertEquals0(3, this.topology.getEdgeById("e32").getWeight());
   }
   
   @Test
   public void testWithTestgraphH3() throws Exception
   {
      this.reader.read(this.topology, getPathToHopCountTestGraph(3));
      
      TopologyModelTestUtils.assertNodeAndEdgeCount(this.topology, 3, 3);
      
      Assert.assertEquals(10, this.topology.getNodeById("1").getHopCount());
      Assert.assertEquals(-1, this.topology.getNodeById("2").getHopCount());
      Assert.assertEquals(1, this.topology.getNodeById("3").getHopCount());
      
      TopologyControlTestHelper.assertEquals0(5, this.topology.getEdgeById("e12").getWeight());
      TopologyControlTestHelper.assertEquals0(1, this.topology.getEdgeById("e13").getWeight());
      TopologyControlTestHelper.assertEquals0(3, this.topology.getEdgeById("e32").getWeight());
   }
   
   @Test
   public void testWithTestgraphA1() throws Exception
   {
      this.reader.read(this.topology, getPathToAngleTestGraph(1));
      
      TopologyModelTestUtils.assertNodeAndEdgeCount(this.topology, 5, 12);
      
      TopologyControlTestHelper.assertEquals0(0, this.topology.getEdgeById("e12").getAngle());
      TopologyControlTestHelper.assertEquals0(10, this.topology.getEdgeById("e12").getWeight());
      TopologyControlTestHelper.assertEquals0(90, this.topology.getEdgeById("e13").getAngle());
      TopologyControlTestHelper.assertEquals0(20, this.topology.getEdgeById("e13").getWeight());
      TopologyControlTestHelper.assertEquals0(180, this.topology.getEdgeById("e14").getAngle());
      TopologyControlTestHelper.assertEquals0(30, this.topology.getEdgeById("e14").getWeight());
      TopologyControlTestHelper.assertEquals0(270, this.topology.getEdgeById("e15").getAngle());
      TopologyControlTestHelper.assertEquals0(40, this.topology.getEdgeById("e15").getWeight());
   }
}
