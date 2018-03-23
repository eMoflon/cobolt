package org.cobolt.algorithms.facade;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.cobolt.model.Edge;
import org.cobolt.model.EdgeState;
import org.cobolt.model.Node;
import org.cobolt.model.Topology;
import org.cobolt.model.TopologyModelTestUtils;
import org.cobolt.model.derivedfeatures.EdgeWeightProviders;
import org.cobolt.model.utils.TopologyUtils;
import org.junit.Assert;
import org.junit.Test;

import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmID;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyControlAlgorithms;
import de.tudarmstadt.maki.simonstrator.tc.weighting.DistanceEdgeWeightProvider;
import org.cobolt.algorithms.TopologyControlAlgorithmsTestUtils;
import org.cobolt.algorithms.facade.EMoflonFacade;

/**
 * Unit tests for {@link EMoflonFacade}, using
 * {@link UnderlayTopologyControlAlgorithms#D_KTC}.
 */
public class EMoflonFacadeTestForDistanceKTC extends AbstractEMoflonFacadeTest {

	@Override
	protected TopologyControlAlgorithmID getAlgorithmID() {
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

		TopologyModelTestUtils.assertNodeAndEdgeCount(topology, 3, 6);

		TopologyModelTestUtils.assertIsStatewiseSymmetric(topology);

		final double k = 1.41;
		TopologyControlAlgorithmsTestUtils.runFacadeKTC(facade, k);

		TopologyModelTestUtils.assertAllActiveSymmetricWithExceptions(topology, "e23");
		TopologyModelTestUtils.assertIsStatewiseSymmetric(topology);
		facade.checkConstraintsAfterContextEvent();
		Assert.assertEquals(0, this.facade.getConstraintViolationCount());
	}

	@Test
	public void testFacadeWithTestgraphD1() throws Exception {

		readTestCase(1);
		double k = 1.1;
		final double k1 = k;
		TopologyControlAlgorithmsTestUtils.runFacadeKTC(facade, k1);

		final Topology topology = facade.getTopology();

		TopologyModelTestUtils.assertAllActiveSymmetricWithExceptions(topology, "e13", "e14", "e15");

		TopologyModelTestUtils.assertIsStatewiseSymmetric(topology);
		facade.checkConstraintsAfterContextEvent();
		Assert.assertEquals(0, this.facade.getConstraintViolationCount());
	}

	@Test
	public void testFacadeWithTestgraph3() throws Exception {

		readTestCase(3);
		final double k = 1.5;
		TopologyControlAlgorithmsTestUtils.runFacadeKTC(facade, k);

		final Topology topology = facade.getTopology();

		TopologyModelTestUtils.assertAllActiveSymmetricWithExceptions(topology, "e13", "e31");
		TopologyModelTestUtils.assertIsStatewiseSymmetric(topology);
		facade.checkConstraintsAfterContextEvent();
		Assert.assertEquals(0, this.facade.getConstraintViolationCount());
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
		TopologyControlAlgorithmsTestUtils.runFacadeKTC(facade, k);

		TopologyModelTestUtils.assertAllActiveSymmetricWithExceptions(facade.getTopology());
	}

	private void readTestCase(int id) throws FileNotFoundException {
		reader.read(facade,
				new FileInputStream(new File(TopologyControlAlgorithmsTestUtils.getPathToDistanceTestGraph(id))));
		de.tudarmstadt.maki.simonstrator.tc.weighting.EdgeWeightProviders.apply(facade,
				DistanceEdgeWeightProvider.getInstance());
	}

}
