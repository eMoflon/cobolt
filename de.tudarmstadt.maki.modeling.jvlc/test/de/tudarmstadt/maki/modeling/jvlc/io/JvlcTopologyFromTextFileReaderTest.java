package de.tudarmstadt.maki.modeling.jvlc.io;

import static de.tudarmstadt.maki.modeling.jvlc.JvlcTestHelper.getPathToDistanceTestGraph;
import static de.tudarmstadt.maki.modeling.jvlc.JvlcTestHelper.getPathToEnergyTestGraph;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.tudarmstadt.maki.modeling.jvlc.JvlcFactory;
import de.tudarmstadt.maki.modeling.jvlc.KTCLink;
import de.tudarmstadt.maki.modeling.jvlc.KTCNode;
import de.tudarmstadt.maki.modeling.jvlc.Topology;

/**
 * Unit tests for {@link JvlcTopologyFromTextFileReader}
 */
public class JvlcTopologyFromTextFileReaderTest {

	private static final double EPS_6 = 1e-6;
	private Topology topology;

	@Before
	public void setup() {
		this.topology = JvlcFactory.eINSTANCE.createTopology();
	}

	@Test
	public void testWithTestgraph1() throws Exception {
		JvlcTopologyFromTextFileReader.readTopology(this.topology, getPathToDistanceTestGraph(1));

		Assert.assertEquals(5, topology.getNodeCount());
		Assert.assertEquals(2 * 7, topology.getEdgeCount());

		final KTCLink link34 = (KTCLink) topology.getEdgeById("e34");
		Assert.assertEquals(22.0, link34.getDistance(), 0.0);

		final KTCLink revLink34 = ((KTCLink) link34.getReverseEdge());
		Assert.assertEquals(22.0, revLink34.getDistance(), 0.0);
		Assert.assertEquals("e43", revLink34.getId());

		Assert.assertSame(link34, revLink34.getReverseEdge());
	}

	@Test
	public void testWithTestgraph3() throws Exception {
		JvlcTopologyFromTextFileReader.readTopology(this.topology, getPathToDistanceTestGraph(3));

		Assert.assertEquals(3, topology.getNodeCount());
		Assert.assertEquals(2 * 3, topology.getEdgeCount());

		final KTCLink link13 = (KTCLink) topology.getEdgeById("e13");
		Assert.assertEquals(20.0, link13.getDistance(), 0.0);
	}

	@Test
	public void testWithTestgraph4() throws Exception {
		JvlcTopologyFromTextFileReader.readTopology(this.topology, getPathToEnergyTestGraph(1));

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

	}
}
