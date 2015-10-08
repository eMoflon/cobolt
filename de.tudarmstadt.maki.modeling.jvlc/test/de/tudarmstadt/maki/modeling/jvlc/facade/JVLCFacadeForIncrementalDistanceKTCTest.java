package de.tudarmstadt.maki.modeling.jvlc.facade;

import static de.tudarmstadt.maki.modeling.jvlc.JvlcTestHelper.assertIsActive;
import static de.tudarmstadt.maki.modeling.jvlc.JvlcTestHelper.assertIsInactive;
import static de.tudarmstadt.maki.modeling.jvlc.JvlcTestHelper.assertIsSymmetric;

import org.junit.Before;
import org.junit.Test;

import de.tudarmstadt.maki.modeling.jvlc.IncrementalDistanceKTC;
import de.tudarmstadt.maki.modeling.jvlc.JvlcTestHelper;
import de.tudarmstadt.maki.modeling.jvlc.KTCLink;
import de.tudarmstadt.maki.modeling.jvlc.KTCNode;
import de.tudarmstadt.maki.modeling.jvlc.LinkState;
import de.tudarmstadt.maki.modeling.jvlc.Topology;

/**
 * Unit tests for {@link JVLCFacade}, using {@link IncrementalDistanceKTC}.
 */
public class JVLCFacadeForIncrementalDistanceKTCTest {

	private JVLCFacade facade;

	@Before
	public void setup() {

		this.facade = JVLCFacade.createFacadeForIncrementalDistanceKTC();
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
		final KTCLink link12 = graph.addUndirectedKTCLink("e12", "e21", n1, n2, 100.0, 5.0);
		final KTCLink link13 = graph.addUndirectedKTCLink("e13", "e31", n1, n3, 120.0, 5.0);
		final KTCLink link23 = graph.addUndirectedKTCLink("e23", "e32", n2, n3, 150.0, 5.0);

		assertIsSymmetric(graph);

		facade.run(1.41);

		assertIsActive(link12);
		assertIsActive(link12.getReverseEdge());
		assertIsActive(link13);
		assertIsActive(link13.getReverseEdge());
		assertIsInactive(link23);
		assertIsInactive(link23.getReverseEdge());
		assertIsSymmetric(graph);
	}

	@Test
	public void testFacadeWithTestgraph1() throws Exception {
		facade.loadAndSetTopologyFromFile(JvlcTestHelper.getPathToDistanceTestGraph(1));
		facade.run(1.1);

		final Topology topology = facade.getTopology();

		assertIsActive(topology.getEdgeById("e12"));
		assertIsActive(topology.getEdgeById("e23"));
		assertIsInactive(topology.getEdgeById("e13"));
		assertIsActive(topology.getEdgeById("e34"));
		assertIsInactive(topology.getEdgeById("e14"));
		assertIsActive(topology.getEdgeById("e45"));
		assertIsInactive(topology.getEdgeById("e15"));

		assertIsSymmetric(topology);
	}

	@Test
	public void testFacadeWithTestgraph3() throws Exception

	{
		facade.loadAndSetTopologyFromFile(JvlcTestHelper.getPathToDistanceTestGraph(3));
		facade.run(1.5);

		final Topology topology = facade.getTopology();

		assertIsActive(topology.getEdgeById("e12"));
		assertIsActive(topology.getEdgeById("e21"));
		assertIsActive(topology.getEdgeById("e23"));
		assertIsActive(topology.getEdgeById("e32"));
		assertIsInactive(topology.getEdgeById("e13"));
		assertIsInactive(topology.getEdgeById("e31"));
		assertIsSymmetric(topology);
	}

	/*
	 * ##################
	 * Incremental test cases
	 * ##################
	 */
	// TODO@rkluge: We need tests for incremental scenarios
	/*
	 *  Use cases:
	 *  * no constraint violation (e.g. increase distance of already inactive link)
	 *  * constraint violation (decrease distance of inactive link)
	 *  * edge addition, node addition -> no problem
	 *  * removing node -> setting incident edges of neighbors to unclassified
	 */

}
