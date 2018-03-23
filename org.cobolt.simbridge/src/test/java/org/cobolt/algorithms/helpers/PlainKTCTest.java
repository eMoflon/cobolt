package org.cobolt.algorithms.helpers;

import static org.cobolt.algorithms.TopologyControlAlgorithmsTestUtils.getPathToDistanceTestGraph;

import org.cobolt.algorithms.AlgorithmsFactory;
import org.cobolt.algorithms.PlainKTC;
import org.cobolt.algorithms.TopologyControlOperationMode;
import org.cobolt.model.Edge;
import org.cobolt.model.EdgeState;
import org.cobolt.model.ModelFactory;
import org.cobolt.model.Node;
import org.cobolt.model.Topology;
import org.cobolt.model.TopologyModelTestUtils;
import org.cobolt.model.constraints.ConstraintsFactory;
import org.cobolt.model.constraints.EdgeStateBasedConnectivityConstraint;
import org.cobolt.model.constraints.NoUnclassifiedLinksConstraint;
import org.cobolt.model.derivedfeatures.EdgeWeightProviders;
import org.cobolt.model.utils.TopologyUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.cobolt.algorithms.io.TopologyModelGraphTReader;

public class PlainKTCTest {

	private Topology topology;

	private PlainKTC algorithm;

	private NoUnclassifiedLinksConstraint noUnclasifiedLinksConstraint;

	private EdgeStateBasedConnectivityConstraint strongConnectivityConstraint;

	private TopologyModelGraphTReader reader;

	@Before
	public void setUp() {
		this.topology = ModelFactory.eINSTANCE.createTopology();
		this.reader = new TopologyModelGraphTReader();
		this.algorithm = AlgorithmsFactory.eINSTANCE.createPlainKTC();
		this.algorithm.setOperationMode(TopologyControlOperationMode.INCREMENTAL);
		this.noUnclasifiedLinksConstraint = ConstraintsFactory.eINSTANCE.createNoUnclassifiedLinksConstraint();
		this.strongConnectivityConstraint = ConstraintsFactory.eINSTANCE.createEdgeStateBasedConnectivityConstraint();
		this.strongConnectivityConstraint.getStates().add(EdgeState.ACTIVE);
	}

	@Test
	public void testAlgorithmWithTestgraph1_RunOnTopology() throws Exception {
		this.reader.read(topology, getPathToDistanceTestGraph(5));
		algorithm.setK(1.1);

		algorithm.runOnTopology(topology);

		TopologyModelTestUtils.assertTopologyConstraints(topology, algorithm.getAlgorithmSpecificConstraints());
		TopologyModelTestUtils.assertTopologyConstraint(topology, noUnclasifiedLinksConstraint);
		TopologyModelTestUtils.assertTopologyConstraint(topology, strongConnectivityConstraint);
	}

	@Test
	public void testAlgorithmWithTestgraph1_RunOnNode() throws Exception {
		this.reader.read(topology, getPathToDistanceTestGraph(5));
		algorithm.setK(1.1);
		algorithm.initializeConstraints();

		while (TopologyUtils.containsUnclassifiedEdges(topology)) {
			for (final Node node : topology.getNodes()) {
				algorithm.runOnNode(node);
			}
		}

		TopologyModelTestUtils.assertTopologyConstraints(topology, algorithm.getAlgorithmSpecificConstraints());
		TopologyModelTestUtils.assertTopologyConstraint(topology, noUnclasifiedLinksConstraint);
		TopologyModelTestUtils.assertTopologyConstraint(topology, strongConnectivityConstraint);

	}

	@Test
	public void testPredicateWithTestgraph1() throws Exception {
		this.reader.read(topology, getPathToDistanceTestGraph(5));
		EdgeWeightProviders.apply(topology, EdgeWeightProviders.DISTANCE_PROVIDER);

		final Edge e13 = getEdgeById(topology, "e1-3");
		final Edge e12 = getEdgeById(topology, "e1-2");
		final Edge e23 = getEdgeById(topology, "e2-3");

		algorithm.setK(1.3);
		Assert.assertTrue(algorithm.checkPredicate(e13, e12, e23));
		Assert.assertTrue(algorithm.checkPredicate(e13, e23, e12));
		Assert.assertFalse(algorithm.checkPredicate(e23, e13, e12));

		algorithm.setK(1.5);
		Assert.assertFalse(algorithm.checkPredicate(e13, e12, e23));
		Assert.assertFalse(algorithm.checkPredicate(e13, e23, e12));
	}

	private Edge getEdgeById(final Topology testGraph, final String edgeId) {
		return testGraph.getEdgeById(edgeId);
	}

}
