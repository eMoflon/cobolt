package de.tudarmstadt.maki.modeling.jvlc.facade;

import static de.tudarmstadt.maki.modeling.jvlc.JvlcTestHelper.assertAllActiveWithExceptionsSymmetric;
import static de.tudarmstadt.maki.modeling.jvlc.JvlcTestHelper.assertIsActiveSymmetric;
import static de.tudarmstadt.maki.modeling.jvlc.JvlcTestHelper.assertIsSymmetric;
import static de.tudarmstadt.maki.modeling.jvlc.JvlcTestHelper.assertIsUnclassified;
import static de.tudarmstadt.maki.modeling.jvlc.JvlcTestHelper.assertIsUnclassifiedSymmetric;

import java.io.File;
import java.io.FileInputStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.tudarmstadt.maki.modeling.jvlc.IncrementalDistanceKTC;
import de.tudarmstadt.maki.modeling.jvlc.JvlcTestHelper;
import de.tudarmstadt.maki.modeling.jvlc.KTCLink;
import de.tudarmstadt.maki.modeling.jvlc.KTCNode;
import de.tudarmstadt.maki.modeling.jvlc.LinkState;
import de.tudarmstadt.maki.modeling.jvlc.Topology;
import de.tudarmstadt.maki.modeling.jvlc.algorithm.AlgorithmHelper;
import de.tudarmstadt.maki.modeling.jvlc.constraints.AssertConstraintViolationEnumerator;
import de.tudarmstadt.maki.modeling.jvlc.io.GraphTFileReader;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmID;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlFacadeFactory;
import de.tudarmstadt.maki.simonstrator.tc.ktc.KTCConstants;

/**
 * Unit tests for {@link JVLCFacade}, using {@link IncrementalDistanceKTC}.
 */
public class JVLCFacadeForIncrementalDistanceKTCTest {
	/*
	 *  Use cases:
	 *  * no constraint violation (e.g. increase distance of already inactive link)
	 *  * constraint violation (decrease distance of inactive link)
	 *  * edge addition, node addition -> no problem
	 *  * removing node -> setting incident edges of neighbors to unclassified
	 *  * no handling necessary
	 */
	private JVLCFacade facade;
	private static TopologyControlAlgorithmID ALGO_ID = KTCConstants.ID_KTC;
	private GraphTFileReader reader;

	@Before
	public void setup() {
		this.facade = (JVLCFacade) TopologyControlFacadeFactory.create("de.tudarmstadt.maki.modeling.jvlc.facade.JVLCFacade");
		this.facade.configureAlgorithm(ALGO_ID);
		this.reader = new GraphTFileReader();
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
		AssertConstraintViolationEnumerator.getInstance().checkPredicate(this.facade.getTopology(), AlgorithmHelper.createAlgorithmForID(ALGO_ID));
	}

	@Test
	public void testFacadeWithTestgraphD1() throws Exception {
		
		reader.read(facade, new FileInputStream(new File(JvlcTestHelper.getPathToDistanceTestGraph(1))));
		facade.run(1.1);

		final Topology topology = facade.getTopology();

		assertAllActiveWithExceptionsSymmetric(topology, "e13", "e14", "e15");

		assertIsSymmetric(topology);
		AssertConstraintViolationEnumerator.getInstance().checkPredicate(this.facade.getTopology(), AlgorithmHelper.createAlgorithmForID(ALGO_ID));
	}

	@Test
	public void testFacadeWithTestgraph3() throws Exception
	{
		
		reader.read(facade, new FileInputStream(new File(JvlcTestHelper.getPathToDistanceTestGraph(3))));
		facade.run(1.5);

		final Topology topology = facade.getTopology();

		assertAllActiveWithExceptionsSymmetric(topology, "e13", "e31");
		assertIsSymmetric(topology);
		AssertConstraintViolationEnumerator.getInstance().checkPredicate(this.facade.getTopology(), AlgorithmHelper.createAlgorithmForID(ALGO_ID));
	}

	/*
	 * ##################
	 * Incremental test cases
	 * ##################
	 */
	@Test
	public void testFacadeWithTestgraphD4() throws Exception {
		final int k = 2;
		
		reader.read(facade, new FileInputStream(new File(JvlcTestHelper.getPathToDistanceTestGraph(4))));

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

		// CE(ii)
		facade.updateLinkAttributeSymmetric(topology.getKTCLinkById("e2-6"), KTCConstants.DISTANCE, 15.0);
		assertIsUnclassifiedSymmetric(topology, "e2-6");
		assertIsActiveSymmetric(topology, "e5-6");

		facade.updateLinkAttributeSymmetric(topology.getKTCLinkById("e2-5"), KTCConstants.DISTANCE, 15.0);
		assertIsUnclassifiedSymmetric(topology, "e2-5");
		assertIsActiveSymmetric(topology, "e4-5");
		assertIsUnclassifiedSymmetric(topology, "e2-4");

		// TC(iii)
		facade.run(k);
		assertAllActiveWithExceptionsSymmetric(topology, "e1-3", "e2-4", "e3-9", "e4-5", "e5-6", "e7-8");

	}

	/**
	 * This test illustrates that in a triangle that contains two equally long 'longest' links (in terms of distance), only the link with the larger ID ('e23'  in this case) is inactivated.
	 */
	@Test
	public void testTriangleWithEquisecles() throws Exception {
		reader.read(facade, new FileInputStream(new File(JvlcTestHelper.getPathToDistanceTestGraph(2))));
		facade.run(1.1);

		Assert.assertTrue(facade.getTopology().getKTCLinkById("e21").hasSameDistanceAndSmallerID(facade.getTopology().getKTCLinkById("e23")));

		assertAllActiveWithExceptionsSymmetric(facade.getTopology(), "e23", "e32");
	}

}
