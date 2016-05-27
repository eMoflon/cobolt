package de.tudarmstadt.maki.modeling.jvlc.io;

import static de.tudarmstadt.maki.modeling.jvlc.JvlcTestHelper.assertHasDistance;
import static de.tudarmstadt.maki.modeling.jvlc.JvlcTestHelper.getPathToDistanceTestGraph;
import static de.tudarmstadt.maki.modeling.jvlc.JvlcTestHelper.getPathToEnergyTestGraph;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.tudarmstadt.maki.modeling.graphmodel.GraphModelTestHelper;
import de.tudarmstadt.maki.modeling.jvlc.JvlcFactory;
import de.tudarmstadt.maki.modeling.jvlc.KTCLink;
import de.tudarmstadt.maki.modeling.jvlc.KTCNode;
import de.tudarmstadt.maki.modeling.jvlc.Topology;

/**
 * Unit tests for {@link GraphTFileReader}
 */
public class GraphTFileReaderTest {

	private static final double EPS_6 = 1e-6;
	private Topology topology;

	@Before
	public void setup() {
		this.topology = JvlcFactory.eINSTANCE.createTopology();
	}

	@Test
	public void testWithTestgraphD1() throws Exception {
		GraphTFileReader.readTopology(this.topology, getPathToDistanceTestGraph(1));

		Assert.assertEquals(5, topology.getNodeCount());
		Assert.assertEquals(2 * 7, topology.getEdgeCount());

		final KTCLink link34 = (KTCLink) topology.getEdgeById("e34");
		Assert.assertEquals(22.0, link34.getDistance(), 0.0);

		final KTCLink revLink34 = ((KTCLink) link34.getReverseEdge());
		Assert.assertEquals(22.0, revLink34.getDistance(), 0.0);
		Assert.assertEquals("e43", revLink34.getId());

		Assert.assertSame(link34, revLink34.getReverseEdge());
		GraphModelTestHelper.assertIsSymmetricWithRespectToStates(topology);
	}

	@Test
	public void testWithTestgraphD3() throws Exception {
		GraphTFileReader.readTopology(this.topology, getPathToDistanceTestGraph(3));

		Assert.assertEquals(3, topology.getNodeCount());
		Assert.assertEquals(2 * 3, topology.getEdgeCount());

		final KTCLink link13 = (KTCLink) topology.getEdgeById("e13");
		Assert.assertEquals(20.0, link13.getDistance(), 0.0);
		GraphModelTestHelper.assertIsSymmetricWithRespectToStates(topology);
	}

	@Test
	public void testWithTestgraphE1() throws Exception {
		GraphTFileReader.readTopology(this.topology, getPathToEnergyTestGraph(1));

		Assert.assertEquals(6, topology.getEdgeCount());
		Assert.assertEquals(3, topology.getNodeCount());

		Assert.assertEquals(10, ((KTCNode) topology.getNodeById("n1")).getRemainingEnergy(), EPS_6);
		Assert.assertEquals(5, ((KTCLink) topology.getEdgeById("e12")).getRequiredTransmissionPower(), EPS_6);

		Assert.assertEquals(2, ((KTCLink) topology.getEdgeById("e12")).calculateEstimatedRemainingLifetime(), EPS_6);
		Assert.assertEquals(6, ((KTCLink) topology.getEdgeById("e21")).calculateEstimatedRemainingLifetime(), EPS_6);
		Assert.assertEquals(1, ((KTCLink) topology.getEdgeById("e13")).calculateEstimatedRemainingLifetime(), EPS_6);
		Assert.assertEquals(6, ((KTCLink) topology.getEdgeById("e31")).calculateEstimatedRemainingLifetime(), EPS_6);
		Assert.assertEquals(2, ((KTCLink) topology.getEdgeById("e23")).calculateEstimatedRemainingLifetime(), EPS_6);
		Assert.assertEquals(4, ((KTCLink) topology.getEdgeById("e32")).calculateEstimatedRemainingLifetime(), EPS_6);

		GraphModelTestHelper.assertIsSymmetricWithRespectToStates(topology);

	}

	@Test
	public void testWithTestgrahpD4() throws Exception {
		GraphTFileReader.readTopology(this.topology, getPathToDistanceTestGraph(4));

		assertHasDistance(topology, "e1-2", 10.0);
		assertHasDistance(topology, "e1-3", 20.0);
		assertHasDistance(topology, "e1-7", 10.0);
		assertHasDistance(topology, "e1-7", 10.0);

		GraphModelTestHelper.assertIsSymmetricWithRespectToStates(topology);

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

		GraphModelTestHelper.assertIsSymmetricWithRespectToStates(topology);

	}

}
