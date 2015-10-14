package de.tudarmstadt.maki.modeling.jvlc.facade;

import static de.tudarmstadt.maki.modeling.jvlc.JvlcTestHelper.assertAllActiveWithExceptionsSymmetric;
import static de.tudarmstadt.maki.modeling.jvlc.JvlcTestHelper.assertIsActiveSymmetric;
import static de.tudarmstadt.maki.modeling.jvlc.JvlcTestHelper.assertIsSymmetric;
import static de.tudarmstadt.maki.modeling.jvlc.JvlcTestHelper.assertIsUnclassified;
import static de.tudarmstadt.maki.modeling.jvlc.JvlcTestHelper.assertIsUnclassifiedSymmetric;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.tudarmstadt.maki.modeling.jvlc.IncrementalDistanceKTC;
import de.tudarmstadt.maki.modeling.jvlc.JvlcTestHelper;
import de.tudarmstadt.maki.modeling.jvlc.KTCLink;
import de.tudarmstadt.maki.modeling.jvlc.KTCNode;
import de.tudarmstadt.maki.modeling.jvlc.LinkState;
import de.tudarmstadt.maki.modeling.jvlc.Topology;
import de.tudarmstadt.maki.modeling.jvlc.constraints.AssertConstraintViolationEnumerator;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmID;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlFacadeFactory;

/**
 * Unit tests for {@link JVLCFacade}, using {@link IncrementalDistanceKTC}.
 */
public class JVLCFacadeForIncrementalDistanceKTCTest {

	private JVLCFacade facade;
	private static TopologyControlAlgorithmID ALGO_ID = TopologyControlAlgorithmID.ID_KTC;

	@Before
	public void setup() {
		this.facade = (JVLCFacade) TopologyControlFacadeFactory.create("de.tudarmstadt.maki.modeling.jvlc.facade.JVLCFacade");
		this.facade.configureAlgorithm(ALGO_ID);
	}

	@Test
	public void testUsageExample_GraphModifications() throws Exception {

		final Topology topology = facade.getTopology();
		final KTCNode n1 = topology.addKTCNode("n1", 10.0);
		final KTCNode n2 = topology.addKTCNode("n2", 10.0);
		final KTCLink link1 = topology.addKTCLink("e12", n1, n2, 150.0, 5.0, LinkState.UNCLASSIFIED);
		final KTCLink link2 = topology.addKTCLink("e21", n1, n2, 150.0, 5.0, LinkState.UNCLASSIFIED);

		n1.setRemainingEnergy(2.0);
		link1.setState(LinkState.ACTIVE);
		link1.setRequiredTransmissionPower(1.1);
		link1.setDistance(150);
		topology.removeEdge(link1);
		topology.removeEdgeById("e21");
		topology.removeEdgeById("e21"); // edge does not exist -> nop :-)
		topology.removeEdge(link2);
		topology.removeNode(n1);
		topology.removeNodeById("n1"); // edge does not exist -> nop :-)
		topology.removeNode(n2);
	}

	/*
	 * ##################
	 * Batch test cases
	 * ##################
	 */
	@Test
	public void testFacadeWithCodedSampleGraph() throws Exception {
		final Topology graph = facade.getTopology();
		final KTCNode n1 = graph.addKTCNode("n1", 20.0);
		final KTCNode n2 = graph.addKTCNode("n2", 20.0);
		final KTCNode n3 = graph.addKTCNode("n3", 20.0);
		graph.addUndirectedKTCLink("e12", "e21", n1, n2, 100.0, 5.0);
		graph.addUndirectedKTCLink("e13", "e31", n1, n3, 120.0, 5.0);
		graph.addUndirectedKTCLink("e23", "e32", n2, n3, 150.0, 5.0);

		assertIsSymmetric(graph);

		facade.run(1.41);

		assertAllActiveWithExceptionsSymmetric(graph, "e23");
		assertIsSymmetric(graph);
		AssertConstraintViolationEnumerator.getInstance().checkPredicate(this.facade.getTopology(), JVLCFacade.getAlgorithmForID(ALGO_ID));
	}

	@Test
	public void testFacadeWithTestgraphD1() throws Exception {
		facade.loadAndSetTopologyFromFile(JvlcTestHelper.getPathToDistanceTestGraph(1));
		facade.run(1.1);

		final Topology topology = facade.getTopology();

		assertAllActiveWithExceptionsSymmetric(topology, "e13", "e14", "e15");

		assertIsSymmetric(topology);
		AssertConstraintViolationEnumerator.getInstance().checkPredicate(this.facade.getTopology(), JVLCFacade.getAlgorithmForID(ALGO_ID));
	}

	@Test
	public void testFacadeWithTestgraph3() throws Exception

	{
		facade.loadAndSetTopologyFromFile(JvlcTestHelper.getPathToDistanceTestGraph(3));
		facade.run(1.5);

		final Topology topology = facade.getTopology();

		assertAllActiveWithExceptionsSymmetric(topology, "e13", "e31");
		assertIsSymmetric(topology);
		AssertConstraintViolationEnumerator.getInstance().checkPredicate(this.facade.getTopology(), JVLCFacade.getAlgorithmForID(ALGO_ID));
	}

	/*
	 * ##################
	 * Incremental test cases
	 * ##################
	 */
	@Test
	public void testFacadeWithTestgraphD4() throws Exception {
		final int k = 2;

		facade.loadAndSetTopologyFromFile(JvlcTestHelper.getPathToDistanceTestGraph(4));

		// TC(i)
		facade.run(k);

		final Topology topology = facade.getTopology();
		assertAllActiveWithExceptionsSymmetric(topology, "e1-3", "e2-4", "e2-5", "e2-6", "e3-9", "e3-11", "e9-11");

		// CE(i) - Add link e7-9
		facade.addSymmetricKTCLink("e7-9", "e9-7", topology.getKTCNodeById("7"), topology.getKTCNodeById("9"), 10.0, 100.0);

		assertIsUnclassified(topology, "e7-9");

		// CE(i) - Remove node 10
		facade.removeKTCNode(topology.getKTCNodeById("10"));
		Assert.assertEquals(10, topology.getNodeCount());
		Assert.assertEquals(34, topology.getEdgeCount());

		assertIsUnclassifiedSymmetric(topology, "e3-9", "e3-11", "e9-11");
		assertIsActiveSymmetric(topology, "e7-8", "e8-9", "e3-7");

		// TC(ii)
		facade.run(k);
		assertAllActiveWithExceptionsSymmetric(topology, "e1-3", "e2-4", "e2-5", "e2-6", "e3-9", "e7-8");
	}
	// TODO@rkluge: We need tests for incremental scenarios
	/*
	 *  Use cases:
	 *  * no constraint violation (e.g. increase distance of already inactive link)
	 *  * constraint violation (decrease distance of inactive link)
	 *  * edge addition, node addition -> no problem
	 *  * removing node -> setting incident edges of neighbors to unclassified
	 *  * no handling necessary
	 */

}
