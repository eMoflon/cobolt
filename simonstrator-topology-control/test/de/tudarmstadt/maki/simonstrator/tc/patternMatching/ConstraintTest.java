package de.tudarmstadt.maki.simonstrator.tc.patternMatching;

import java.util.LinkedList;

import org.junit.Test;

import de.tudarmstadt.maki.simonstrator.api.common.graph.BasicGraph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.GraphElementProperty;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.api.component.topology.VariableAssignment;
import de.tudarmstadt.maki.simonstrator.tc.lomba.util.GraphOperations;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.constraint.ComparisonOperator;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.constraint.GraphElementAttributeConstraint;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.constraint.GraphElementConstraint;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.matching.TopologyPatternMatch;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.matching.TopologyPatternMatcher;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.matching.TopologyPatternMatcher_Impl;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.pattern.TopologyPattern;
import de.tudarmstadt.maki.simonstrator.tc.testing.PatternMatcherTestHelper;

public class ConstraintTest {
	@Test
	public void simplePropertyTest() {
		// Define a Node-Property
		GraphElementProperty<Double> prop = new GraphElementProperty<>("Double", Double.class);

		// Create a graph and set property for first Node
		INodeID n0 = INodeID.get(0);
		Graph graph = GraphOperations.graphFromString("0-1,1-2,0-3,1-3,3-4");
		graph.getNode(n0).setProperty(prop, 1.0);

		// Define Pattern to match one Node
		Graph patternGraph = new BasicGraph();
		patternGraph.createAndAddNode(n0);

		// Define Constraint to only match Nodes with a Value == 1.0
		GraphElementAttributeConstraint constraint = new GraphElementAttributeConstraint(n0, prop, 1.0,
				ComparisonOperator.EQUAL);
		LinkedList<GraphElementConstraint> constraints = new LinkedList<>();
		constraints.add(constraint); // Comment out to make it work without
										// constraints.

		// Assemble Pattern
		TopologyPattern pattern = new TopologyPattern(n0, patternGraph, constraints);
		TopologyPatternMatcher matcher = new TopologyPatternMatcher_Impl();
		matcher.setPattern(pattern);

		// Run PatternMatcher
		// Expected result: 1 match -> n0
		Iterable<TopologyPatternMatch> matches = matcher.match(graph);
		VariableAssignment expectedMatch = new VariableAssignment();
		expectedMatch.bindNodeVariable(n0, graph.getNode(n0));
		PatternMatcherTestHelper.assertHasPatternMatch(matches, expectedMatch);
	}
}
