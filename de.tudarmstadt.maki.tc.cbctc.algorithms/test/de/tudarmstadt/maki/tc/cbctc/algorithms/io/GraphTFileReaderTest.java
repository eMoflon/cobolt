package de.tudarmstadt.maki.tc.cbctc.algorithms.io;

import static de.tudarmstadt.maki.tc.cbctc.algorithms.JvlcTestHelper.assertHasDistance;
import static de.tudarmstadt.maki.tc.cbctc.algorithms.JvlcTestHelper.getPathToDistanceTestGraph;
import static de.tudarmstadt.maki.tc.cbctc.algorithms.JvlcTestHelper.getPathToEnergyTestGraph;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.tudarmstadt.maki.tc.cbctc.algorithms.io.GraphTFileReader;
import de.tudarmstadt.maki.tc.cbctc.model.Edge;
import de.tudarmstadt.maki.tc.cbctc.model.ModelFactory;
import de.tudarmstadt.maki.tc.cbctc.model.Topology;
import de.tudarmstadt.maki.tc.cbctc.model.TopologyModelTestHelper;

/**
 * Unit tests for {@link GraphTFileReader}
 */
public class GraphTFileReaderTest {

	private static final double EPS_6 = 1e-6;
	private Topology topology;

	@Before
	public void setup() {
		this.topology = ModelFactory.eINSTANCE.createTopology();
	}

	@Test
	public void testWithTestgraphD1() throws Exception {
		GraphTFileReader.readTopology(this.topology, getPathToDistanceTestGraph(1));

		Assert.assertEquals(5, topology.getNodeCount());
		Assert.assertEquals(2 * 7, topology.getEdgeCount());

		final Edge link34 = topology.getEdgeById("e34");
		Assert.assertEquals(22.0, link34.getWeight(), 0.0);

		final Edge revLink34 = link34.getReverseEdge();
		Assert.assertEquals(22.0, revLink34.getWeight(), 0.0);
		Assert.assertEquals("e43", revLink34.getId());

		Assert.assertSame(link34, revLink34.getReverseEdge());
		TopologyModelTestHelper.assertIsSymmetricWithRespectToStates(topology);
	}

	@Test
	public void testWithTestgraphD3() throws Exception {
		GraphTFileReader.readTopology(this.topology, getPathToDistanceTestGraph(3));

		Assert.assertEquals(3, topology.getNodeCount());
		Assert.assertEquals(2 * 3, topology.getEdgeCount());

		final Edge link13 = topology.getEdgeById("e13");
		Assert.assertEquals(20.0, link13.getWeight(), 0.0);
		TopologyModelTestHelper.assertIsSymmetricWithRespectToStates(topology);
	}

	@Test
	public void testWithTestgraphE1() throws Exception {
		GraphTFileReader.readTopology(this.topology, getPathToEnergyTestGraph(1));

		Assert.assertEquals(6, topology.getEdgeCount());
		Assert.assertEquals(3, topology.getNodeCount());

		Assert.assertEquals(10, topology.getNodeById("n1").getEnergyLevel(), EPS_6);
		Assert.assertEquals(5, topology.getEdgeById("e12").getExpectedLifetime(), EPS_6);

		TopologyModelTestHelper.assertIsSymmetricWithRespectToStates(topology);

	}

	@Test
	public void testWithTestgrahpD4() throws Exception {
		GraphTFileReader.readTopology(this.topology, getPathToDistanceTestGraph(4));

		assertHasDistance(topology, "e1-2", 10.0);
		assertHasDistance(topology, "e1-3", 20.0);
		assertHasDistance(topology, "e1-7", 10.0);
		assertHasDistance(topology, "e1-7", 10.0);

		TopologyModelTestHelper.assertIsSymmetricWithRespectToStates(topology);

	}

	@Test
	public void testWithTestgrahpD5() throws Exception {
		GraphTFileReader.readTopology(this.topology, getPathToDistanceTestGraph(5));

		assertHasDistance(topology, "e1-2", 15.0);
		assertHasDistance(topology, "e1-3", 20.0);
		assertHasDistance(topology, "e1-4", 25.0);
		assertHasDistance(topology, "e1-5", 30.0);
		assertHasDistance(topology, "e2-3", 17.0);
		assertHasDistance(topology, "e3-4", 22.0);
		assertHasDistance(topology, "e4-5", 27.0);

		TopologyModelTestHelper.assertIsSymmetricWithRespectToStates(topology);

	}

}
