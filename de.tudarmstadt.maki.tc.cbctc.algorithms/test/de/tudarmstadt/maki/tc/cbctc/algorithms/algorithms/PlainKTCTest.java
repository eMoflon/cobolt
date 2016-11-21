package de.tudarmstadt.maki.tc.cbctc.algorithms.algorithms;

import static de.tudarmstadt.maki.tc.cbctc.algorithms.TopologyControlAlgorithmsTestUtils.getPathToDistanceTestGraph;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.tudarmstadt.maki.tc.cbctc.algorithms.AlgorithmsFactory;
import de.tudarmstadt.maki.tc.cbctc.algorithms.PlainKTC;
import de.tudarmstadt.maki.tc.cbctc.algorithms.TopologyControlOperationMode;
import de.tudarmstadt.maki.tc.cbctc.algorithms.io.TopologyModelGraphTReader;
import de.tudarmstadt.maki.tc.cbctc.model.Edge;
import de.tudarmstadt.maki.tc.cbctc.model.EdgeState;
import de.tudarmstadt.maki.tc.cbctc.model.ModelFactory;
import de.tudarmstadt.maki.tc.cbctc.model.Node;
import de.tudarmstadt.maki.tc.cbctc.model.Topology;
import de.tudarmstadt.maki.tc.cbctc.model.TopologyTestUtils;
import de.tudarmstadt.maki.tc.cbctc.model.constraints.ConstraintsFactory;
import de.tudarmstadt.maki.tc.cbctc.model.constraints.EdgeStateBasedConnectivityConstraint;
import de.tudarmstadt.maki.tc.cbctc.model.constraints.NoUnclassifiedLinksConstraint;
import de.tudarmstadt.maki.tc.cbctc.model.derivedfeatures.EdgeWeightProviders;
import de.tudarmstadt.maki.tc.cbctc.model.utils.TopologyUtils;

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

		TopologyTestUtils.assertTopologyConstraints(topology, algorithm.getAlgorithmSpecificConstraints());
		TopologyTestUtils.assertTopologyConstraint(topology, noUnclasifiedLinksConstraint);
		TopologyTestUtils.assertTopologyConstraint(topology, strongConnectivityConstraint);
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

		TopologyTestUtils.assertTopologyConstraints(topology, algorithm.getAlgorithmSpecificConstraints());
		TopologyTestUtils.assertTopologyConstraint(topology, noUnclasifiedLinksConstraint);
		TopologyTestUtils.assertTopologyConstraint(topology, strongConnectivityConstraint);

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
