package de.tudarmstadt.maki.simonstrator.tc.patternMatching;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Iterables;

import de.tudarmstadt.maki.simonstrator.api.common.graph.DirectedEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.api.component.topology.VariableAssignment;
import de.tudarmstadt.maki.simonstrator.tc.lomba.util.GraphOperations;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.constraint.GraphElementConstraint;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.matching.TopologyPatternMatch;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.matching.TopologyPatternMatcher;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.matching.TopologyPatternMatcher_Impl;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.pattern.TopologyPattern;
import de.tudarmstadt.maki.simonstrator.tc.testing.PatternMatcherTestHelper;

public class PatternMatcherTest {
	@Test
	public void motifTest() {
		final Graph graph = GraphOperations.graphFromString("0-1,1-2,0-3,1-3,3-4");
		final Graph nac = GraphOperations.graphFromString("0-2");
		nac.createAndAddNode(INodeID.get(1));
		final Graph motif = GraphOperations.graphFromString("0-1,1-2");
		final INodeID n0 = INodeID.get(0);
		final TopologyPattern nacPattern = new TopologyPattern(n0, nac);
		final TopologyPattern pattern = new TopologyPattern(n0, motif, new ArrayList<GraphElementConstraint>(),
				Arrays.asList(nacPattern));
		final TopologyPatternMatcher matcher = new TopologyPatternMatcher_Impl();
		matcher.setPattern(pattern);
		final Iterable<TopologyPatternMatch> matches = matcher.match(graph);

		Assert.assertEquals(8, Iterables.size(matches));
		
		// Check first expected match
		VariableAssignment expectedMatch1 = new VariableAssignment();
		expectedMatch1.bindNodeVariable(INodeID.get(1), graph.getNode(INodeID.get(1)));
		expectedMatch1.bindNodeVariable(INodeID.get(2), graph.getNode(INodeID.get(3)));
		expectedMatch1.bindNodeVariable(INodeID.get(0), graph.getNode(INodeID.get(2)));
		expectedMatch1.bindLinkVariable(new DirectedEdge(INodeID.get(0), INodeID.get(1)).getId(),
				new DirectedEdge(INodeID.get(2), INodeID.get(1)));
		expectedMatch1.bindLinkVariable(new DirectedEdge(INodeID.get(1), INodeID.get(2)).getId(),
				new DirectedEdge(INodeID.get(1), INodeID.get(3)));
		expectedMatch1.bindLinkVariable(new DirectedEdge(INodeID.get(2), INodeID.get(1)).getId(),
				new DirectedEdge(INodeID.get(3), INodeID.get(1)));
		expectedMatch1.bindLinkVariable(new DirectedEdge(INodeID.get(1), INodeID.get(0)).getId(),
				new DirectedEdge(INodeID.get(1), INodeID.get(2)));
		PatternMatcherTestHelper.assertHasPatternMatch(matches, expectedMatch1);
		// Should be:
		// TopologyPatternMatch VariableAssignment [nodeBinding:{1=1, 2=3, 0=2},
		// linkBinding: {0->1=2->1, 1->2=1->3, 2->1=3->1, 1->0=1->2}]
		// TopologyPatternMatch VariableAssignment [nodeBinding:{1=1, 2=0, 0=2},
		// linkBinding: {0->1=2->1, 1->2=1->0, 2->1=0->1, 1->0=1->2}]
		// TopologyPatternMatch VariableAssignment [nodeBinding:{1=1, 2=2, 0=3},
		// linkBinding: {0->1=3->1, 1->2=1->2, 2->1=2->1, 1->0=1->3}]
		// TopologyPatternMatch VariableAssignment [nodeBinding:{1=3, 2=1, 0=4},
		// linkBinding: {0->1=4->3, 1->2=3->1, 2->1=1->3, 1->0=3->4}]
		// TopologyPatternMatch VariableAssignment [nodeBinding:{1=3, 2=0, 0=4},
		// linkBinding: {0->1=4->3, 1->2=3->0, 2->1=0->3, 1->0=3->4}]
		// TopologyPatternMatch VariableAssignment [nodeBinding:{1=1, 2=2, 0=0},
		// linkBinding: {0->1=0->1, 1->2=1->2, 2->1=2->1, 1->0=1->0}]
		// TopologyPatternMatch VariableAssignment [nodeBinding:{1=3, 2=4, 0=0},
		// linkBinding: {0->1=0->3, 1->2=3->4, 2->1=4->3, 1->0=3->0}]
		// TopologyPatternMatch VariableAssignment [nodeBinding:{1=3, 2=4, 0=1},
		// linkBinding: {0->1=1->3, 1->2=3->4, 2->1=4->3, 1->0=3->1}]
	}
}
