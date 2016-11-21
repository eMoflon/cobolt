package de.tudarmstadt.maki.tc.cbctc.algorithms.facade;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmID;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyControlAlgorithms;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyProperties;
import de.tudarmstadt.maki.simonstrator.tc.weighting.DistanceEdgeWeightProvider;
import de.tudarmstadt.maki.tc.cbctc.algorithms.PlainKTC;
import de.tudarmstadt.maki.tc.cbctc.algorithms.TopologyControlAlgorithmsTestUtils;
import de.tudarmstadt.maki.tc.cbctc.model.Edge;
import de.tudarmstadt.maki.tc.cbctc.model.EdgeState;
import de.tudarmstadt.maki.tc.cbctc.model.Node;
import de.tudarmstadt.maki.tc.cbctc.model.Topology;
import de.tudarmstadt.maki.tc.cbctc.model.TopologyTestUtils;
import de.tudarmstadt.maki.tc.cbctc.model.derivedfeatures.EdgeWeightProviders;
import de.tudarmstadt.maki.tc.cbctc.model.utils.TopologyUtils;

/**
 * Unit tests for {@link EMoflonFacade}, using {@link PlainKTC}.
 */
public class EMoflonFacadeTestForDistanceKTC extends AbstractEMoflonFacadeTest {

   @Override
   protected TopologyControlAlgorithmID getAlgorithmID()
   {
      return UnderlayTopologyControlAlgorithms.D_KTC;
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
	 * ### Test cases with single TC runs
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
		EdgeWeightProviders.apply(topology, EdgeWeightProviders.DISTANCE_PROVIDER);
		
		TopologyTestUtils.assertNodeAndEdgeCount(topology, 3, 6);

		TopologyTestUtils.assertIsStatewiseSymmetric(topology);

		final double k = 1.41;
		facade.run(k);

		TopologyTestUtils.assertAllActiveSymmetricWithExceptions(topology, "e23");
		TopologyTestUtils.assertIsStatewiseSymmetric(topology);
		facade.checkConstraintsAfterContextEvent();
		Assert.assertEquals(0, this.facade.getConstraintViolationCount());
	}

	@Test
	public void testFacadeWithTestgraphD1() throws Exception {

		readTestCase(1);
		double k = 1.1;
		facade.run(k);

		final Topology topology = facade.getTopology();

		TopologyTestUtils.assertAllActiveSymmetricWithExceptions(topology, "e13", "e14", "e15");

		TopologyTestUtils.assertIsStatewiseSymmetric(topology);
		facade.checkConstraintsAfterContextEvent();
		Assert.assertEquals(0, this.facade.getConstraintViolationCount());
	}

	@Test
	public void testFacadeWithTestgraph3() throws Exception {

		readTestCase(3);
		final double k = 1.5;
		facade.run(k);

		final Topology topology = facade.getTopology();

		TopologyTestUtils.assertAllActiveSymmetricWithExceptions(topology, "e13", "e31");
		TopologyTestUtils.assertIsStatewiseSymmetric(topology);
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
		TopologyTestUtils.assertAllActiveSymmetricWithExceptions(topology, "e1-3", "e2-4", "e2-5",
				"e2-6", "e3-9", "e3-11", "e9-11");

		// CE(i) - Add link e7-9
		facade.addSymmetricEdge("e7-9", "e9-7", topology.getNodeById("7"), topology.getNodeById("9"), 10.0,
				100.0);

		TopologyTestUtils.assertUnclassified(topology, "e7-9");

		// CE(i) - Remove node 10
		facade.removeNode(topology.getNodeById("10"));
		Assert.assertEquals(10, topology.getNodeCount());
		Assert.assertEquals(34, topology.getEdgeCount());

		TopologyTestUtils.assertUnclassifiedSymmetric(topology, "e3-9", "e3-11", "e9-11");
		TopologyTestUtils.assertActiveSymmetric(topology, "e7-8", "e8-9", "e3-7");

		// TC(ii)
		facade.run(k);
		TopologyTestUtils.assertAllActiveSymmetricWithExceptions(topology, "e1-3", "e2-4", "e2-5",
				"e2-6", "e3-9", "e7-8");

		// CE(ii)
		facade.updateModelLinkAttributeSymmetric(topology.getEdgeById("e2-6"), UnderlayTopologyProperties.WEIGHT, 15.0);
		TopologyTestUtils.assertUnclassifiedSymmetric(topology, "e2-6");
		TopologyTestUtils.assertActiveSymmetric(topology, "e5-6");

		facade.updateModelLinkAttributeSymmetric(topology.getEdgeById("e2-5"), UnderlayTopologyProperties.WEIGHT, 15.0);
		TopologyTestUtils.assertUnclassifiedSymmetric(topology, "e2-5");
		TopologyTestUtils.assertActiveSymmetric(topology, "e4-5");
		TopologyTestUtils.assertUnclassifiedSymmetric(topology, "e2-4");

		// TC(iii)
		facade.run(k);
		TopologyTestUtils.assertAllActiveSymmetricWithExceptions(topology, "e1-3", "e2-4", "e3-9",
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

		TopologyTestUtils.assertAllActiveSymmetricWithExceptions(facade.getTopology());
	}

	private void readTestCase(int id) throws FileNotFoundException {
		reader.read(facade, new FileInputStream(new File(TopologyControlAlgorithmsTestUtils.getPathToDistanceTestGraph(id))));
		de.tudarmstadt.maki.simonstrator.tc.weighting.EdgeWeightProviders.apply(facade, DistanceEdgeWeightProvider.getInstance());
	}

}
