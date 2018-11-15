package de.tudarmstadt.maki.simonstrator.tc.patternMatching;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import de.tudarmstadt.maki.simonstrator.api.Graphs;
import de.tudarmstadt.maki.simonstrator.api.common.graph.EdgeID;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.GraphBuilder;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INode;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.api.component.topology.VariableAssignment;
import de.tudarmstadt.maki.simonstrator.tc.GraphUtil;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.constraint.ArithmeticOperator;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.constraint.ComparisonOperator;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.matching.TopologyPatternMatch;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.matching.TopologyPatternMatcher;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.matching.TopologyPatternMatcher_Impl;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.pattern.PatternBuilder;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.pattern.TopologyPattern;

/**
 * Tests for {@link PatternBuilder} and {@link TopologyPatternMatcher}.
 *
 */
public class PatternBuilderAndMatcherIntegrationTest {

	@Test
	public void testMatchWithOneEdge() throws Exception {
		final TopologyPattern pattern = PatternBuilder.create().setLocalNode("self").addDirectedEdge("self", "e1", "n2")
				.addLinkWeightConstraint("e1", 1.0, ComparisonOperator.EQUAL).done();
		final INode pn1 = pattern.getNodeVariableById("self");
		final INode pn2 = pattern.getNodeVariableById("n2");
		final IEdge pe1 = pattern.getLinkVariableById("e1");

		// Build graph
		final INodeID localNode = INodeID.get(1);
		final INodeID n2 = INodeID.get(2);
		final IEdge e1 = Graphs.createDirectedWeightedEdge(localNode, n2, 1.0);
		final Graph hostGraph = GraphUtil.createGraph(Arrays.asList(e1));

		// Configure pattern matcher
		final TopologyPatternMatcher matcher = new TopologyPatternMatcher_Impl(pattern);
		final Iterable<TopologyPatternMatch> matches = matcher.match(localNode, hostGraph);
		final TopologyPatternMatch match = matches.iterator().next();
		final VariableAssignment variableAssignment = match.getVariableAssignment();

		final VariableAssignment expected = new VariableAssignment();
		expected.bindNodeVariable(pn1.getId(), hostGraph.getNode(localNode));
		expected.bindNodeVariable(pn2.getId(), hostGraph.getNode(n2));
		expected.bindLinkVariable(pe1.getId(), e1);
		Assert.assertEquals(expected, variableAssignment);
	}

	@Test
	public void testMatchWithTernaryConstraint() throws Exception {

		// Build pattern
		final TopologyPattern pattern = PatternBuilder.create().setLocalNode("self")//
				.addDirectedEdge("self", "e1", "n2")//
				.addDirectedEdge("n2", "e2", "n3")//
				.addLinkWeightConstraint("e1", ComparisonOperator.GREATER, "e2", ArithmeticOperator.MULTIPLY, 1.5)//
				.done();

		// Build graph
		final INodeID localNode = INodeID.get(1);
		final INodeID n2 = INodeID.get(2);
		final INodeID n3 = INodeID.get(3);
		final IEdge e1 = Graphs.createDirectedWeightedEdge(localNode, n2, 1.6);
		final IEdge e2 = Graphs.createDirectedWeightedEdge(n2, n3, 1.0);
		final Graph graph = GraphUtil.createGraph(Arrays.asList(e1, e2));

		// Perform matching
		final TopologyPatternMatcher matcher = new TopologyPatternMatcher_Impl(pattern);
		final Iterable<TopologyPatternMatch> matches = matcher.match(localNode, graph);
		final TopologyPatternMatch match = matches.iterator().next();

		// Assert
		final VariableAssignment variableAssignment = match.getVariableAssignment();
		Assert.assertEquals(localNode, variableAssignment.getNodeVariableBinding("self"));
		Assert.assertEquals(e1, variableAssignment.getBindingLink(EdgeID.get("e1")));
		Assert.assertEquals(n2, variableAssignment.getNodeVariableBinding("n2"));
		Assert.assertEquals(e2, variableAssignment.getBindingLink(EdgeID.get("e2")));
		Assert.assertEquals(n3, variableAssignment.getNodeVariableBinding("n3"));

	}

	@Test
	public void testMatchOfKtcPattern() throws Exception {
		final TopologyPattern pattern = createKtcInactivationPattern(1.4);

		// Build triangle graph
		final INodeID n1 = INodeID.get(1);
		final INodeID n2 = INodeID.get(2);
		final INodeID n3 = INodeID.get(3);
		final IEdge e1 = Graphs.createDirectedWeightedEdge(n1, n2, 1.5);
		final IEdge e2 = Graphs.createDirectedWeightedEdge(n1, n3, 1.3);
		final IEdge e3 = Graphs.createDirectedWeightedEdge(n2, n3, 1.0);
		final Graph hostGraph = GraphUtil.createGraph(Arrays.asList(e1, e2, e3));

		// Configure pattern matcher
		final TopologyPatternMatcher matcher = new TopologyPatternMatcher_Impl(pattern);
		final Iterable<TopologyPatternMatch> matches = matcher.match(n1, hostGraph);
		final TopologyPatternMatch match = matches.iterator().next();
		final VariableAssignment variableAssignment = match.getVariableAssignment();

		Assert.assertEquals(n1, variableAssignment.getNodeVariableBinding("self"));
		Assert.assertEquals(e1, variableAssignment.getBindingLink(EdgeID.get("e_max")));
		Assert.assertEquals(n2, variableAssignment.getNodeVariableBinding("n2"));
		Assert.assertEquals(e2, variableAssignment.getBindingLink(EdgeID.get("e_med")));
		Assert.assertEquals(n3, variableAssignment.getNodeVariableBinding("n3"));
		Assert.assertEquals(e3, variableAssignment.getBindingLink(EdgeID.get("e_min")));
	}

	@Test
	public void testMatchOfSimpleNACPattern() throws Exception {
		final TopologyPattern nac = PatternBuilder.create().addDirectedEdge("self", "e1", "n2").doneWithoutLocalNode();
		final TopologyPattern pattern = PatternBuilder.create().setLocalNode("self").addNAC(nac).done();

		// Build graph
		final INodeID n1 = INodeID.get(1);
		final INodeID n2 = INodeID.get(2);
		final IEdge e1 = Graphs.createDirectedWeightedEdge(n1, n2, 1.5);
		final Graph hostGraph = GraphUtil.createGraph(Arrays.asList(e1));

		// Configure pattern matcher
		final TopologyPatternMatcher matcher = new TopologyPatternMatcher_Impl(pattern);
		Assert.assertFalse(matcher.hasMatch(n1, hostGraph));
	}

	@Test
	public void testEdgeActivationCondition() throws Exception {
		final TopologyPattern pattern = createKtcActivationPattern(1.4);

		// Build triangle graph
		final INodeID localNode = INodeID.get(1);
		final INodeID n2 = INodeID.get(2);
		final INodeID n3 = INodeID.get(3);
		final IEdge e1 = Graphs.createDirectedWeightedEdge(localNode, n2, 1.5);
		final IEdge e2 = Graphs.createDirectedWeightedEdge(localNode, n3, 1.3);
		final IEdge e3 = Graphs.createDirectedWeightedEdge(n2, n3, 1.0);
		final Graph graph = GraphUtil.createGraph(Arrays.asList(e1, e2, e3));

		// Configure pattern matcher
		final TopologyPatternMatcher matcher = new TopologyPatternMatcher_Impl(pattern);
		Assert.assertEquals(1, matcher.countMatches(localNode, graph));
		final TopologyPatternMatch match = matcher.match(localNode, graph).iterator().next();
		final VariableAssignment variableAssignment = match.getVariableAssignment();

		Assert.assertEquals(localNode, variableAssignment.getNodeVariableBinding("self"));
		Assert.assertEquals(e2, variableAssignment.getBindingLink(EdgeID.get("e_max")));
		Assert.assertEquals(n3, variableAssignment.getNodeVariableBinding("n2"));

	}

	/**
	 * This test produces a star with "origin" at the center.
	 * The "one-edge" pattern self --selfToNode-> pn2 should match "number of stripes"  times (if the constraint is not used) and only once
	 * @throws Exception
	 */
	@Test
	public void testMatchingPerformanceWithStarTopology() throws Exception {
		final TopologyPattern pattern = PatternBuilder.create().setLocalNode("self").addDirectedEdge("self", "self->pn2", "pn2").done();
		final TopologyPattern pattern2 = PatternBuilder.create().setLocalNode("self").addDirectedEdge("self", "self->pn2", "pn2")
				.addLinkWeightConstraint("self->pn2", 6, ComparisonOperator.GREATER_OR_EQUAL).done();
		final GraphBuilder graphBuilder = GraphBuilder.create();
		final int numberOfStripes = 100;
		for (int i = 1; i < numberOfStripes; ++i) {
			graphBuilder.e("origin", "n" + i, "e1_" + i, 5);
		}
		graphBuilder.e("origin", "n" + numberOfStripes, "e1_" + numberOfStripes, 10.0);
		final Graph graph = graphBuilder.done();

		final TopologyPatternMatcher matcher = new TopologyPatternMatcher_Impl(pattern);
		final INodeID origin = INodeID.get("origin");
      Assert.assertEquals(numberOfStripes, matcher.countMatches(origin, graph));
		matcher.setPattern(pattern2);
		Assert.assertEquals(1, matcher.countMatches(origin, graph));
		// final TopologyPatternMatch match = matcher.match(INodeID.get("n1"), graph).iterator().next();
		// final VariableAssignment variableAssignment = match.getVariableAssignment();

	}

	private TopologyPattern createKtcInactivationPattern(final double k) {
		final TopologyPattern ktcPattern = PatternBuilder.create().setLocalNode("self")//
				.addDirectedEdge("self", "e_max", "n2")//
				.addDirectedEdge("self", "e_med", "n3")//
				.addDirectedEdge("n2", "e_min", "n3")//
				.addLinkWeightConstraint("e_max", ComparisonOperator.GREATER_OR_EQUAL, "e_med")//
				.addLinkWeightConstraint("e_med", ComparisonOperator.GREATER_OR_EQUAL, "e_min")//
				.addLinkWeightConstraint("e_max", ComparisonOperator.GREATER_OR_EQUAL, "e_min", ArithmeticOperator.MULTIPLY, k)//
				.done();
		return ktcPattern;
	}

	private TopologyPattern createKtcActivationPattern(final double k) {
		final TopologyPattern nac = PatternBuilder.create()//
				.addDirectedEdge("self", "e_max", "n2")//
				.addDirectedEdge("self", "e_med", "n3")//
				.addDirectedEdge("n2", "e_min", "n3")//
				.addLinkWeightConstraint("e_max", ComparisonOperator.GREATER_OR_EQUAL, "e_med")//
				.addLinkWeightConstraint("e_med", ComparisonOperator.GREATER_OR_EQUAL, "e_min")//
				.addLinkWeightConstraint("e_max", ComparisonOperator.GREATER_OR_EQUAL, "e_min", ArithmeticOperator.MULTIPLY, k)//
				.doneWithoutLocalNode();
		final TopologyPattern ktcPattern = PatternBuilder.create().setLocalNode("self")//
				.addDirectedEdge("self", "e_max", "n2")//
				.addNAC(nac)//
				.done();
		return ktcPattern;
	}

}
