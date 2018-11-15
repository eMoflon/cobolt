package de.tudarmstadt.maki.simonstrator.tc.patternMatching;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.junit.Assert;
import org.junit.Test;

import de.tudarmstadt.maki.simonstrator.api.Graphs;
import de.tudarmstadt.maki.simonstrator.api.common.graph.DirectedEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.EdgeID;
import de.tudarmstadt.maki.simonstrator.api.common.graph.GenericGraphElementProperties;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IElement;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.api.component.topology.VariableAssignment;
import de.tudarmstadt.maki.simonstrator.tc.GraphUtil;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.constraint.ArithmeticOperator;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.constraint.ComparisonOperator;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.constraint.GraphElementConstraint;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.matching.TopologyPatternMatch;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.matching.TopologyPatternMatcher;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.matching.TopologyPatternMatcher_Impl;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.pattern.PatternBuilder;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.pattern.TopologyPattern;

/**
 * Unit tests for {@link PatternBuilder}.
 */
public class PatternBuilderTest {

	@Test(expected = TopologyPatternMatchingException.class)
	public void testMissingLocalNode() throws Exception {
		PatternBuilder.create().done();
	}

	@Test(expected = TopologyPatternMatchingException.class)
	public void testConstraintWithAdditionalLinkVariable() throws Exception {
		PatternBuilder.create().addLinkWeightConstraint("e12", 1.0, ComparisonOperator.GREATER).done();
	}

	@Test
	public void testSingleNodePattern() throws Exception {
		PatternBuilder.create().setLocalNode("self");
	}

	@Test
	public void testOneEdgePattern() throws Exception {
		PatternBuilder.create().addDirectedEdge("n1", "e12", "n2").setLocalNode("n1").done();
		PatternBuilder.create().setLocalNode("n1").addDirectedEdge("n1", "e12", "n2").done();
	}

	@Test
	public void testKTCPattern() throws Exception {
		PatternBuilder.create().setLocalNode("self")
				//
				.addDirectedEdge("self", "e1", "n2").addDirectedEdge("self", "e2", "n3").addDirectedEdge("n2", "e3", "n3")
				//
				.addLinkWeightConstraint("e1", ComparisonOperator.GREATER_OR_EQUAL, "e2")
				.addLinkWeightConstraint("e2", ComparisonOperator.GREATER_OR_EQUAL, "e3")
				.addLinkWeightConstraint("e1", ComparisonOperator.GREATER, "e3", ArithmeticOperator.MULTIPLY, 1.41).done();
	}

	@Test
	public void testConstraintBeforeLinkDefinitionPattern() throws Exception {
		PatternBuilder.create().setLocalNode("self") //
				.addLinkWeightConstraint("e1", ComparisonOperator.GREATER_OR_EQUAL, "e2") //
				.addLinkWeightConstraint("e2", ComparisonOperator.GREATER_OR_EQUAL, "e3")//
				.addLinkWeightConstraint("e1", ComparisonOperator.GREATER, "e3", ArithmeticOperator.MULTIPLY, 1.41)//
				.addDirectedEdge("self", "e1", "n2").addDirectedEdge("self", "e2", "n3")//
				.addDirectedEdge("n2", "e3", "n3").done();
	}

	@Test(expected = TopologyPatternMatchingException.class)
	public void testUsageOfLinkInConstraintWithoutLinkDefinition() throws Exception {
		PatternBuilder.create().setLocalNode("self") //
				.addLinkWeightConstraint("e1", ComparisonOperator.GREATER_OR_EQUAL, "e2") //
				.addLinkWeightConstraint("e2", ComparisonOperator.GREATER_OR_EQUAL, "e3")//
				.addLinkWeightConstraint("e1", ComparisonOperator.GREATER, "e3", ArithmeticOperator.MULTIPLY, 1.41)//
				.addDirectedEdge("self", "e2", "n3")//
				// Definition of e1 is missing
				.addDirectedEdge("n2", "e3", "n3").done();
	}

	@Test
	public void testNACPatterns() throws Exception {
		final TopologyPattern nac = PatternBuilder.create().addDirectedEdge("self", "e1", "n2").doneWithoutLocalNode();
		final TopologyPattern pattern = PatternBuilder.create().setLocalNode("self").addNAC(nac).done();

		final TopologyPattern nacPattern = pattern.getNegativeApplicationConstraints().iterator().next();

		Assert.assertEquals(new HashSet<>(Arrays.asList(Graphs.createNode("self"), Graphs.createNode("n2"))),
				new HashSet<>(nacPattern.getGraph().getNodes()));
		Assert.assertEquals(new HashSet<>(Arrays.asList(new DirectedEdge(Graphs.createNode("self"), Graphs.createNode("n2"), "e1"))),
				new HashSet<>(nacPattern.getGraph().getEdges()));
	}
	
	@Test
   public void testPatternFindNodesWithSingleOutgoingEdge() throws Exception {

      // Build pattern
      final INodeID pn1 = INodeID.get("pn1");
      final INodeID pn2 = INodeID.get("pn2");
      final IEdge pe12 = Graphs.createDirectedWeightedEdge(pn1, pn2, 1.0);
      final Graph patternGraph = GraphUtil.createGraph(Arrays.asList(pe12));
      final TopologyPattern pattern = new TopologyPattern(pn1, patternGraph);
      
      // Build NAC
      final INodeID pn3 = INodeID.get("pn3");
      final IEdge pe13 = Graphs.createDirectedWeightedEdge(pn1, pn3, 1.0);
      final Graph nacGraph = GraphUtil.createGraph(Arrays.asList(pe12, pe13));
      final TopologyPattern nac = new TopologyPattern(pn1, nacGraph);
      pattern.getNegativeApplicationConstraints().add(nac);

      // Build graph
      final INodeID gn1 = INodeID.get(1);
      final INodeID gn2 = INodeID.get(2);
      final INodeID gn3 = INodeID.get(3);
      final IEdge ge12 = Graphs.createDirectedWeightedEdge(gn1, gn2, 1.0);
      final IEdge ge13 = Graphs.createDirectedWeightedEdge(gn1, gn3, 1.0);
      final IEdge ge23 = Graphs.createDirectedWeightedEdge(gn2, gn3, 1.0);
      final Graph graph = GraphUtil.createGraph(Arrays.asList(ge12, ge13, ge23));

      // Configure pattern matcher
      TopologyPatternMatcher matcher = new TopologyPatternMatcher_Impl(pattern);
      Iterable<TopologyPatternMatch> matches = matcher.match(graph);
      
      final Iterator<TopologyPatternMatch> matchIterator = matches.iterator();

      final TopologyPatternMatch match = matchIterator.next();
      final VariableAssignment variableAssignment = match.getVariableAssignment();
      final INodeID matchedNode1 = variableAssignment.getNodeVariableBinding(pn1);
      final INodeID matchedNode2 = variableAssignment.getNodeVariableBinding(pn2);
      final IEdge matchedEdge1= variableAssignment.getBindingLink(pe12.getId());
      Assert.assertEquals(gn2, matchedNode1);
      Assert.assertEquals(gn3, matchedNode2);
      Assert.assertEquals(ge23, matchedEdge1);
      
      Assert.assertFalse(matchIterator.hasNext());
   }

	@Test
	public void testCreatePropertyConstraint() throws Exception {
		PatternBuilder.create().setLocalNode("self") //
				.addDirectedEdge("self", "e1", "n2").addDirectedEdge("self", "e2", "n3")
				.addPropertyComparisonConstraint("e1", ComparisonOperator.GREATER_OR_EQUAL, GenericGraphElementProperties.WEIGHT, "e2").done();
	}

	@Test
	public void testCreateConstraintOnTheFly() throws Exception {
		PatternBuilder.create().setLocalNode("self") //
				.addDirectedEdge("self", "e1", "n2").addDirectedEdge("self", "e2", "n3")
				.addBinaryConstraint(new GraphElementConstraint(Arrays.asList(EdgeID.get("e1"), EdgeID.get("e2"))) {
					private static final long serialVersionUID = 488790888931972416L;

					@Override
					protected boolean checkCandidates(final Collection<? extends IElement> bindingCandidates) {
						return false;
					}
				}).done();
	}
}
