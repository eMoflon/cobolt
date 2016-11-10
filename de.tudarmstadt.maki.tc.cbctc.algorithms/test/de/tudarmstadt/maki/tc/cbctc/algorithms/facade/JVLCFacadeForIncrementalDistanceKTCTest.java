package de.tudarmstadt.maki.tc.cbctc.algorithms.facade;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmID;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlFacadeFactory;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlOperationMode;
import de.tudarmstadt.maki.simonstrator.tc.ktc.UnderlayTopologyControlAlgorithms;
import de.tudarmstadt.maki.simonstrator.tc.ktc.UnderlayTopologyProperties;
import de.tudarmstadt.maki.tc.cbctc.algorithms.JvlcTestHelper;
import de.tudarmstadt.maki.tc.cbctc.algorithms.PlainKTC;
import de.tudarmstadt.maki.tc.cbctc.algorithms.facade.EMoflonFacade;
import de.tudarmstadt.maki.tc.cbctc.algorithms.io.GraphTFileReader;
import de.tudarmstadt.maki.tc.cbctc.model.Edge;
import de.tudarmstadt.maki.tc.cbctc.model.EdgeState;
import de.tudarmstadt.maki.tc.cbctc.model.Node;
import de.tudarmstadt.maki.tc.cbctc.model.Topology;
import de.tudarmstadt.maki.tc.cbctc.model.TopologyModelTestHelper;
import de.tudarmstadt.maki.tc.cbctc.model.utils.TopologyUtils;

/**
 * Unit tests for {@link EMoflonFacade}, using {@link PlainKTC}.
 */
public class JVLCFacadeForIncrementalDistanceKTCTest {
	private EMoflonFacade facade;
	private TopologyControlAlgorithmID algorithmID = UnderlayTopologyControlAlgorithms.D_KTC;
	private GraphTFileReader reader;

	@Before
	public void setup() {
		this.facade = (EMoflonFacade) TopologyControlFacadeFactory
				.create("de.tudarmstadt.maki.tc.cbctc.algorithms.facade.EMoflonFacade");
		this.facade.setOperationMode(TopologyControlOperationMode.INCREMENTAL);
		this.facade.configureAlgorithm(algorithmID);
		this.reader = new GraphTFileReader();
	}

	@Test
	public void testUsageExample_GraphModifications() throws Exception {

		final Topology topology = facade.getTopology();
		final Node n1 = TopologyUtils.addNode(topology, "n1", 10.0);
		final Node n2 = TopologyUtils.addNode(topology, "n2", 10.0);
		final Edge link1 = TopologyUtils.addEdge(topology, "e12", n1, n2, 150.0, 5.0, EdgeState.UNCLASSIFIED);
		final Edge link2 = TopologyUtils.addEdge(topology, "e21", n1, n2, 150.0, 5.0, EdgeState.UNCLASSIFIED);

		n1.setEnergyLevel(2.0);
		link1.setState(EdgeState.ACTIVE);
		link1.setExpectedLifetime(1.1);
		link1.setWeight(150);
		topology.removeEdge(link1);
		topology.removeEdgeById("e21");
		topology.removeEdgeById("e21"); // edge does not exist -> nop :-)
		topology.removeEdge(link2);
		topology.removeNode(n1);
		topology.removeNodeById("n1"); // edge does not exist -> nop :-)
		topology.removeNode(n2);
	}

	/*
	 * ################## Batch test cases ##################
	 */
	@Test
	public void testFacadeWithCodedSampleGraph() throws Exception {
		final Topology topology = facade.getTopology();
		final Node n1 = TopologyUtils.addNode(topology, "n1", 20.0);
		final Node n2 = TopologyUtils.addNode(topology, "n2", 20.0);
		final Node n3 = TopologyUtils.addNode(topology, "n3", 20.0);
		TopologyUtils.addUndirectedEdge(topology, "e12", "e21", n1, n2, 100.0, 5.0);
		TopologyUtils.addUndirectedEdge(topology, "e13", "e31", n1, n3, 120.0, 5.0);
		TopologyUtils.addUndirectedEdge(topology, "e23", "e32", n2, n3, 150.0, 5.0);

		TopologyModelTestHelper.assertIsSymmetricWithRespectToStates(topology);

		final double k = 1.41;
		facade.run(k);

		TopologyModelTestHelper.assertThatAllLinksAreActiveWithExceptionsSymmetric(topology, "e23");
		TopologyModelTestHelper.assertIsSymmetricWithRespectToStates(topology);
		facade.checkConstraintsAfterContextEvent();
		Assert.assertEquals(0, this.facade.getConstraintViolationCount());
	}

	@Test
	public void testFacadeWithTestgraphD1() throws Exception {

		readTestCase(1);
		double k = 1.1;
		facade.run(k);

		final Topology topology = facade.getTopology();

		TopologyModelTestHelper.assertThatAllLinksAreActiveWithExceptionsSymmetric(topology, "e13", "e14", "e15");

		TopologyModelTestHelper.assertIsSymmetricWithRespectToStates(topology);
		facade.checkConstraintsAfterContextEvent();
		Assert.assertEquals(0, this.facade.getConstraintViolationCount());
	}

	@Test
	public void testFacadeWithTestgraph3() throws Exception {

		readTestCase(3);
		final double k = 1.5;
		facade.run(k);

		final Topology topology = facade.getTopology();

		TopologyModelTestHelper.assertThatAllLinksAreActiveWithExceptionsSymmetric(topology, "e13", "e31");
		TopologyModelTestHelper.assertIsSymmetricWithRespectToStates(topology);
		facade.checkConstraintsAfterContextEvent();
		Assert.assertEquals(0, this.facade.getConstraintViolationCount());
	}

	/*
	 * ################## Incremental test cases ##################
	 */
	@Ignore // TODO@rkluge: Check back later
	@Test
	public void testFacadeWithTestgraphD4() throws Exception {
		final int k = 2;

		readTestCase(4);

		// TC(i)
		facade.run(k);

		final Topology topology = facade.getTopology();
		TopologyModelTestHelper.assertThatAllLinksAreActiveWithExceptionsSymmetric(topology, "e1-3", "e2-4", "e2-5",
				"e2-6", "e3-9", "e3-11", "e9-11");

		// CE(i) - Add link e7-9
		facade.addSymmetricEdge("e7-9", "e9-7", topology.getNodeById("7"), topology.getNodeById("9"), 10.0,
				100.0);

		TopologyModelTestHelper.assertIsUnclassified(topology, "e7-9");

		// CE(i) - Remove node 10
		facade.removeNode(topology.getNodeById("10"));
		Assert.assertEquals(10, topology.getNodeCount());
		Assert.assertEquals(34, topology.getEdgeCount());

		TopologyModelTestHelper.assertIsUnclassifiedSymmetric(topology, "e3-9", "e3-11", "e9-11");
		TopologyModelTestHelper.assertIsActiveSymmetric(topology, "e7-8", "e8-9", "e3-7");

		// TC(ii)
		facade.run(k);
		TopologyModelTestHelper.assertThatAllLinksAreActiveWithExceptionsSymmetric(topology, "e1-3", "e2-4", "e2-5",
				"e2-6", "e3-9", "e7-8");

		// CE(ii)
		facade.updateModelLinkAttributeSymmetric(topology.getEdgeById("e2-6"), UnderlayTopologyProperties.WEIGHT, 15.0);
		TopologyModelTestHelper.assertIsUnclassifiedSymmetric(topology, "e2-6");
		TopologyModelTestHelper.assertIsActiveSymmetric(topology, "e5-6");

		facade.updateModelLinkAttributeSymmetric(topology.getEdgeById("e2-5"), UnderlayTopologyProperties.WEIGHT, 15.0);
		TopologyModelTestHelper.assertIsUnclassifiedSymmetric(topology, "e2-5");
		TopologyModelTestHelper.assertIsActiveSymmetric(topology, "e4-5");
		TopologyModelTestHelper.assertIsUnclassifiedSymmetric(topology, "e2-4");

		// TC(iii)
		facade.run(k);
		TopologyModelTestHelper.assertThatAllLinksAreActiveWithExceptionsSymmetric(topology, "e1-3", "e2-4", "e3-9",
				"e4-5", "e5-6", "e7-8");

	}

	/**
	 * This test illustrates that in a triangle that contains two equally long
	 * 'longest' links (in terms of distance), only the link with the larger ID
	 * ('e23' in this case) is inactivated.
	 */
	@Test
	public void testTriangleWithEquisecles() throws Exception {
		readTestCase(2);
		final double k = 1.1;
		facade.run(k);

		TopologyModelTestHelper.assertThatAllLinksAreActiveWithExceptionsSymmetric(facade.getTopology());
	}

	private void readTestCase(int id) throws FileNotFoundException {
		reader.read(facade, new FileInputStream(new File(JvlcTestHelper.getPathToDistanceTestGraph(id))));
	}

}