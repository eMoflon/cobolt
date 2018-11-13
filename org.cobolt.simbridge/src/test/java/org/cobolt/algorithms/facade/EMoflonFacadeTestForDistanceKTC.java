package org.cobolt.algorithms.facade;

import static org.cobolt.algorithms.TopologyControlAlgorithmsTestUtils.assertNoConstraintViolationsAfterContextEventHandling;
import static org.cobolt.algorithms.TopologyControlAlgorithmsTestUtils.assertNoConstraintViolationsAfterTopologyControl;
import static org.cobolt.algorithms.TopologyControlAlgorithmsTestUtils.getPathToDistanceTestGraph;
import static org.cobolt.algorithms.TopologyControlAlgorithmsTestUtils.runFacadeKTC;
import static org.cobolt.model.TopologyModelTestUtils.assertActive;
import static org.cobolt.model.TopologyModelTestUtils.assertAllActiveSymmetricWithExceptions;
import static org.cobolt.model.TopologyModelTestUtils.assertAllActiveWithExceptions;
import static org.cobolt.model.TopologyModelTestUtils.assertInactive;
import static org.cobolt.model.TopologyModelTestUtils.assertIsStatewiseSymmetric;
import static org.cobolt.model.TopologyModelTestUtils.assertNodeAndEdgeCount;
import static org.cobolt.model.TopologyModelTestUtils.assertUnclassified;
import static org.cobolt.model.utils.TopologyUtils.addEdge;
import static org.cobolt.model.utils.TopologyUtils.addNode;
import static org.cobolt.model.utils.TopologyUtils.addUndirectedEdge;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.cobolt.algorithms.PlainKTC;
import org.cobolt.algorithms.TriangleBasedTopologyControlAlgorithm;
import org.cobolt.model.Edge;
import org.cobolt.model.EdgeState;
import org.cobolt.model.Node;
import org.cobolt.model.Topology;
import org.cobolt.model.derivedfeatures.EdgeWeightProviders;
import org.junit.Assert;
import org.junit.Test;

import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmID;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyControlAlgorithms;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyProperties;
import de.tudarmstadt.maki.simonstrator.tc.weighting.DistanceEdgeWeightProvider;

/**
 * Unit tests for {@link EMoflonFacade}, using
 * {@link UnderlayTopologyControlAlgorithms#D_KTC}.
 */
public class EMoflonFacadeTestForDistanceKTC extends AbstractEMoflonFacadeTest {

	private static final double DEFAULT_K = 1.3;

	@Override
	protected TopologyControlAlgorithmID getAlgorithmID() {
		return UnderlayTopologyControlAlgorithms.D_KTC;
	}

	@Test
	public void testUsageExample_GraphModifications() throws Exception {

		final Topology topology = facade.getTopology();
		final Node n1 = addNode(topology, "n1", 10.0);
		final Node n2 = addNode(topology, "n2", 10.0);
		final Edge link1 = addEdge(topology, "e12", "n1", "n2", 150.0, 5.0, EdgeState.UNCLASSIFIED);
		final Edge link2 = addEdge(topology, "e21", "n1", "n2", 150.0, 5.0, EdgeState.UNCLASSIFIED);

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
		final Node n1 = addNode(topology, "n1", 20.0);
		final Node n2 = addNode(topology, "n2", 20.0);
		final Node n3 = addNode(topology, "n3", 20.0);
		addUndirectedEdge(topology, "e12", "e21", n1, n2, 100.0, 5.0);
		addUndirectedEdge(topology, "e13", "e31", n1, n3, 120.0, 5.0);
		addUndirectedEdge(topology, "e23", "e32", n2, n3, 150.0, 5.0);
		EdgeWeightProviders.apply(topology, EdgeWeightProviders.DISTANCE_PROVIDER);

		assertNodeAndEdgeCount(topology, 3, 6);

		assertIsStatewiseSymmetric(topology);

		final double k = 1.41;
		runFacadeKTC(facade, k);

		assertAllActiveSymmetricWithExceptions(topology, "e23");
		assertIsStatewiseSymmetric(topology);
		assertNoConstraintViolationsAfterContextEventHandling(facade);
	}

	@Test
	public void testFacadeWithTestgraphD1() throws Exception {

		readTestCase(1);
		final double k = 1.1;
		runFacadeKTC(facade, k);

		final Topology topology = facade.getTopology();

		assertAllActiveSymmetricWithExceptions(topology, "e13", "e14", "e15");

		assertIsStatewiseSymmetric(topology);
		assertNoConstraintViolationsAfterContextEventHandling(facade);
	}

	@Test
	public void testFacadeWithTestgraph3() throws Exception {

		readTestCase(3);
		final double k = 1.5;
		runFacadeKTC(facade, k);

		final Topology topology = facade.getTopology();

		assertAllActiveSymmetricWithExceptions(topology, "e13", "e31");
		assertIsStatewiseSymmetric(topology);
		assertNoConstraintViolationsAfterContextEventHandling(facade);
	}

	/*
	 * Systematic tests
	 */
	@Test
	public void testEmptyTopology() throws Exception {

		assertEquals(0, topology().getNodeCount());
		assertEquals(0, topology().getEdgeCount());

		runFacadeKTC(facade, DEFAULT_K);
		assertNoConstraintViolationsAfterTopologyControl(facade);
	}

	@Test
	public void testSingleEdge_Active() throws Exception {
		final Node n1 = addNode(topology(), "n1", 20.0);
		final Node n2 = addNode(topology(), "n2", 20.0);
		addUndirectedEdge(topology(), "e12", "e21", n1, n2, 100.0, 5.0);

		runFacadeKTC(facade, DEFAULT_K);
		assertAllActiveSymmetricWithExceptions(topology());
		assertNoConstraintViolationsAfterTopologyControl(facade);
	}

	@Test
	public void testTriangle_InactiveLink1() throws Exception {
		createTriangle(13, 10, 11);
		Assert.assertTrue(algorithm().checkPredicate(topology().getEdgeById("e12"), topology().getEdgeById("e13"),
				topology().getEdgeById("e32")));

		runFacadeKTC(facade, DEFAULT_K);
		assertAllActiveSymmetricWithExceptions(topology(), "e12", "e21");
		assertNoConstraintViolationsAfterTopologyControl(facade);
	}

	@Test
	public void testTriangle_InactiveLink2() throws Exception {
		createTriangle(13, 11, 10);
		Assert.assertTrue(algorithm().checkPredicate(topology().getEdgeById("e12"), topology().getEdgeById("e13"),
				topology().getEdgeById("e32")));

		runFacadeKTC(facade, DEFAULT_K);
		assertInactive(topology(), "e12", "e21");
		assertAllActiveSymmetricWithExceptions(topology(), "e12", "e21");
		assertNoConstraintViolationsAfterTopologyControl(facade);
	}

	@Test
	public void testTriangle_TieBreaking() throws Exception {
		createTriangle(13, 13, 10);
		Assert.assertFalse(algorithm().checkPredicate(topology().getEdgeById("e12"), topology().getEdgeById("e13"),
				topology().getEdgeById("e32")));

		runFacadeKTC(facade, DEFAULT_K);
		assertAllActiveSymmetricWithExceptions(topology());
		assertNoConstraintViolationsAfterTopologyControl(facade);
	}

	@Test
	public void testTriangle_ActiveLink() throws Exception {
		createTriangle(12.9, 10, 11);

		runFacadeKTC(facade, DEFAULT_K);
		assertAllActiveSymmetricWithExceptions(topology());
		assertNoConstraintViolationsAfterTopologyControl(facade);
	}

	@Test
	public void testTriangle_ActiveLink_ThenWeightIncreaseOnActiveLink() throws Exception {
		createTriangle(12.9, 10, 11);

		runFacadeKTC(facade, DEFAULT_K);
		assertNoConstraintViolationsAfterTopologyControl(facade);

		facade.updateModelLinkAttribute(topology().getEdgeById("e12"), UnderlayTopologyProperties.WEIGHT, 13.0);
		assertNoConstraintViolationsAfterContextEventHandling(facade);
		assertUnclassified(topology(), "e12");
		assertActive(topology(), "e21");

		runFacadeKTC(facade, DEFAULT_K);
		assertNoConstraintViolationsAfterTopologyControl(facade);
	}

	@Test
	public void testTriangle_ActiveLink_ThenWeightDecreaseOnOtherLink_ExpectedUnclassification1() throws Exception {
		createTriangle(12.9, 10, 11);

		runFacadeKTC(facade, DEFAULT_K);
		assertAllActiveSymmetricWithExceptions(topology());
		assertNoConstraintViolationsAfterTopologyControl(facade);

		facade.updateModelLinkAttribute(topology().getEdgeById("e13"), UnderlayTopologyProperties.WEIGHT, 6.0);
		assertNoConstraintViolationsAfterContextEventHandling(facade);
		assertUnclassified(topology(), "e12");
		assertActive(topology(), "e21");

		runFacadeKTC(facade, DEFAULT_K);
		assertInactive(topology(), "e12");
		assertNoConstraintViolationsAfterTopologyControl(facade);
	}

	@Test
	public void testTriangle_ActiveLink_ThenWeightDecreaseOnOtherLink_ExpectedUnclassification2() throws Exception {
		createTriangle(12.9, 10, 11);

		runFacadeKTC(facade, DEFAULT_K);
		assertAllActiveSymmetricWithExceptions(topology());
		assertNoConstraintViolationsAfterTopologyControl(facade);

		facade.updateModelLinkAttribute(topology().getEdgeById("e32"), UnderlayTopologyProperties.WEIGHT, 6.0);
		assertNoConstraintViolationsAfterContextEventHandling(facade);
		assertUnclassified(topology(), "e12");
		assertActive(topology(), "e21");

		runFacadeKTC(facade, DEFAULT_K);
		assertInactive(topology(), "e12");
		assertNoConstraintViolationsAfterTopologyControl(facade);
	}

	@Test
	public void testTriangle_ActiveLink_ThenWeightIncreaseOnOtherLink_ExpectedNoChange1() throws Exception {
		createTriangle(12.9, 12, 11);

		runFacadeKTC(facade, DEFAULT_K);
		assertAllActiveSymmetricWithExceptions(topology());
		assertNoConstraintViolationsAfterTopologyControl(facade);

		facade.updateModelLinkAttribute(topology().getEdgeById("e13"), UnderlayTopologyProperties.WEIGHT, 10.0);
		assertNoConstraintViolationsAfterContextEventHandling(facade);
		assertAllActiveSymmetricWithExceptions(topology());
	}

	@Test
	public void testTriangle_ActiveLink_ThenWeightDecreaseOnOtherLink_ExpectedNoChange2() throws Exception {
		createTriangle(12.9, 11, 12);

		runFacadeKTC(facade, DEFAULT_K);
		assertAllActiveSymmetricWithExceptions(topology());
		assertNoConstraintViolationsAfterTopologyControl(facade);

		facade.updateModelLinkAttribute(topology().getEdgeById("e13"), UnderlayTopologyProperties.WEIGHT, 10.0);
		assertNoConstraintViolationsAfterContextEventHandling(facade);
		assertAllActiveSymmetricWithExceptions(topology());
	}

	private TriangleBasedTopologyControlAlgorithm algorithm() {
		Assert.assertTrue(facade.getAlgorithm() instanceof PlainKTC);
		return (PlainKTC) facade.getAlgorithm();
	}

	@Test
	public void testTriangle_InactiveLink_ThenWeightDecreaseOnInactiveLink() throws Exception {
		createTriangle(13, 10, 11);

		runFacadeKTC(facade, DEFAULT_K);
		assertNoConstraintViolationsAfterTopologyControl(facade);

		facade.updateModelLinkAttribute(topology().getEdgeById("e12"), UnderlayTopologyProperties.WEIGHT, 12.9);
		assertNoConstraintViolationsAfterContextEventHandling(facade);
		assertUnclassified(topology(), "e12");
		assertInactive(topology(), "e21");

		runFacadeKTC(facade, DEFAULT_K);
		assertAllActiveWithExceptions(topology(), "e21");
		assertNoConstraintViolationsAfterTopologyControl(facade);
	}

	@Test
	public void testTriangle_InactiveLink_ThenWeightIncreaseOnActiveLink1() throws Exception {
		createTriangle(13, 10, 11);

		runFacadeKTC(facade, DEFAULT_K);
		assertAllActiveWithExceptions(topology(), "e21", "e12");
		assertNoConstraintViolationsAfterTopologyControl(facade);

		facade.updateModelLinkAttribute(topology().getEdgeById("e13"), UnderlayTopologyProperties.WEIGHT, 11.0);
		assertNoConstraintViolationsAfterContextEventHandling(facade);
		assertUnclassified(topology(), "e12");
		assertInactive(topology(), "e21");

		runFacadeKTC(facade, DEFAULT_K);
		assertAllActiveWithExceptions(topology(), "e21");
		assertNoConstraintViolationsAfterTopologyControl(facade);
	}

	@Test
	public void testTriangle_InactiveLink_ThenWeightIncreaseOnActiveLink2() throws Exception {
		createTriangle(13, 11, 10);

		runFacadeKTC(facade, DEFAULT_K);
		assertAllActiveWithExceptions(topology(), "e21", "e12");
		assertNoConstraintViolationsAfterTopologyControl(facade);

		facade.updateModelLinkAttribute(topology().getEdgeById("e32"), UnderlayTopologyProperties.WEIGHT, 11.0);
		assertNoConstraintViolationsAfterContextEventHandling(facade);
		assertUnclassified(topology(), "e12");
		assertInactive(topology(), "e21");

		runFacadeKTC(facade, DEFAULT_K);
		assertAllActiveWithExceptions(topology(), "e21");
		assertNoConstraintViolationsAfterTopologyControl(facade);
	}

	@Test
	public void testTriangle_InactiveLink_ThenRemovalOfActiveLink1() throws Exception {
		createTriangle(13, 10, 11);

		runFacadeKTC(facade, DEFAULT_K);
		assertAllActiveWithExceptions(topology(), "e21", "e12");
		assertNoConstraintViolationsAfterTopologyControl(facade);

		facade.removeEdge(topology().getEdgeById("e13"));
		assertNoConstraintViolationsAfterContextEventHandling(facade);
		assertUnclassified(topology(), "e12");

		runFacadeKTC(facade, DEFAULT_K);
		assertAllActiveWithExceptions(topology(), "e21");
		assertNoConstraintViolationsAfterTopologyControl(facade);
	}

	@Test
	public void testTriangle_InactiveLink_ThenRemovalOfActiveLink2() throws Exception {
		createTriangle(13, 10, 11);

		runFacadeKTC(facade, DEFAULT_K);
		assertAllActiveWithExceptions(topology(), "e21", "e12");
		assertNoConstraintViolationsAfterTopologyControl(facade);

		facade.removeEdge(topology().getEdgeById("e32"));
		assertNoConstraintViolationsAfterContextEventHandling(facade);
		assertUnclassified(topology(), "e12");

		runFacadeKTC(facade, DEFAULT_K);
		assertAllActiveWithExceptions(topology(), "e21");
		assertNoConstraintViolationsAfterTopologyControl(facade);
	}

	@Test
	public void test_OpenTriangleRight_ThenClose_ExpectedActive() throws Exception {
		createOpenTriangleRight(13, 10);

		runFacadeKTC(facade, DEFAULT_K);
		assertAllActiveWithExceptions(topology());
		assertNoConstraintViolationsAfterTopologyControl(facade);

		final Edge newEdge = addEdge(topology(), "e32", "n3", "n2", 11.0);
		facade.getAlgorithm().handleLinkAddition(newEdge);

		assertNoConstraintViolationsAfterContextEventHandling(facade);
		assertUnclassified(topology(), "e12", "e32");
		assertAllActiveWithExceptions(topology(), "e12", "e32");

		runFacadeKTC(facade, DEFAULT_K);
		assertInactive(topology(), "e12");
		assertAllActiveWithExceptions(topology(), "e12");
		assertNoConstraintViolationsAfterTopologyControl(facade);
	}

	@Test
	public void test_OpenTriangleRight_ThenClose_ExpectedInActive() throws Exception {
		createOpenTriangleRight(13, 11);

		runFacadeKTC(facade, DEFAULT_K);
		assertAllActiveWithExceptions(topology());
		assertNoConstraintViolationsAfterTopologyControl(facade);

		final Edge newEdge = addEdge(topology(), "e32", "n3", "n2", 11.0);
		facade.getAlgorithm().handleLinkAddition(newEdge);

		assertNoConstraintViolationsAfterContextEventHandling(facade);
		assertUnclassified(topology(), "e32");
		assertAllActiveWithExceptions(topology(), "e32");
	}

	@Test
	public void test_OpenTriangleLeft_ThenClose_ExpectedActive() throws Exception {
		createOpenTriangleLeft(13, 10);

		runFacadeKTC(facade, DEFAULT_K);
		assertAllActiveWithExceptions(topology());
		assertNoConstraintViolationsAfterTopologyControl(facade);

		final Edge newEdge = addEdge(topology(), "e13", "n1", "n3", 11.0);
		facade.getAlgorithm().handleLinkAddition(newEdge);

		assertNoConstraintViolationsAfterContextEventHandling(facade);
		assertUnclassified(topology(), "e12", "e13");
		assertAllActiveWithExceptions(topology(), "e12", "e13");

		runFacadeKTC(facade, DEFAULT_K);
		assertInactive(topology(), "e12");
		assertAllActiveWithExceptions(topology(), "e12");
		assertNoConstraintViolationsAfterTopologyControl(facade);
	}

	@Test
	public void test_OpenTriangleLeft_ThenClose_ExpectedInActive() throws Exception {
		createOpenTriangleLeft(13, 11);

		runFacadeKTC(facade, DEFAULT_K);
		assertAllActiveWithExceptions(topology());
		assertNoConstraintViolationsAfterTopologyControl(facade);

		final Edge newEdge = addEdge(topology(), "e13", "n1", "n3", 11.0);
		facade.getAlgorithm().handleLinkAddition(newEdge);

		assertNoConstraintViolationsAfterContextEventHandling(facade);
		assertUnclassified(topology(), "e13");
		assertAllActiveWithExceptions(topology(), "e13");
	}

	/**
	 * Convenience access to topology of the {@link EMoflonFacade} of this test
	 *
	 * @return
	 */
	private Topology topology() {
		return this.facade.getTopology();
	}

	private void createTriangle(final double w12, final double w13, final double w32) {
		final Node n1 = addNode(topology(), "n1");
		final Node n2 = addNode(topology(), "n2");
		final Node n3 = addNode(topology(), "n3");
		addUndirectedEdge(topology(), "e12", "e21", n1, n2, w12);
		addUndirectedEdge(topology(), "e13", "e31", n1, n3, w13);
		addUndirectedEdge(topology(), "e23", "e32", n2, n3, w32);
		EdgeWeightProviders.apply(topology(), EdgeWeightProviders.DISTANCE_PROVIDER);
	}

	private void createOpenTriangleRight(final double w12, final double w13) {
		final Node n1 = addNode(topology(), "n1");
		final Node n2 = addNode(topology(), "n2");
		final Node n3 = addNode(topology(), "n3");
		addUndirectedEdge(topology(), "e12", "e21", n1, n2, w12);
		addUndirectedEdge(topology(), "e13", "e31", n1, n3, w13);
		EdgeWeightProviders.apply(topology(), EdgeWeightProviders.DISTANCE_PROVIDER);
	}

	private void createOpenTriangleLeft(final double w12, final double w32) {
		final Node n1 = addNode(topology(), "n1");
		final Node n2 = addNode(topology(), "n2");
		final Node n3 = addNode(topology(), "n3");
		addUndirectedEdge(topology(), "e12", "e21", n1, n2, w12);
		addUndirectedEdge(topology(), "e23", "e32", n2, n3, w32);
		EdgeWeightProviders.apply(topology(), EdgeWeightProviders.DISTANCE_PROVIDER);
	}

	private void readTestCase(final int id) throws FileNotFoundException {
		reader.read(facade, new FileInputStream(new File(getPathToDistanceTestGraph(id))));
		de.tudarmstadt.maki.simonstrator.tc.weighting.EdgeWeightProviders.apply(facade,
				DistanceEdgeWeightProvider.getInstance());
	}

}
