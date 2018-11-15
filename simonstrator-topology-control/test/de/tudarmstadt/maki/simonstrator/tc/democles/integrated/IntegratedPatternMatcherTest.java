package de.tudarmstadt.maki.simonstrator.tc.democles.integrated;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Iterables;

import de.tudarmstadt.maki.simonstrator.api.Graphs;
import de.tudarmstadt.maki.simonstrator.api.common.graph.BasicGraph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.DirectedEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.GenericGraphElementProperties;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.api.component.topology.VariableAssignment;
import de.tudarmstadt.maki.simonstrator.tc.GraphUtil;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.constraint.ComparisonOperator;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.constraint.LinkWeightConstraintWithScalarValue;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.constraint.LinkWeightConstraintWithTwoLinks;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.matching.TopologyPatternMatch;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.matching.TopologyPatternMatcher;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.pattern.TopologyPattern;
import de.tudarmstadt.maki.simonstrator.tc.testing.PatternMatcherTestHelper;
import de.tudarmstadt.maki.simonstrator.tc.underlay.KTCConstraint;

/**
 * Unit tests for {@link TopologyPatternMatcher}
 *
 */
public class IntegratedPatternMatcherTest {
	
	
	@Test 
	public void testKTCConstraint_HasMatches() throws Exception {
		//Build Graph
		final INodeID gn1 = INodeID.get("p1");
		final INodeID gn2 = INodeID.get("p2");
		final INodeID gn3 = INodeID.get("p3");
		final INodeID gn4 = INodeID.get("p4");
		final INodeID gn5 = INodeID.get("p5");
		final IEdge ge12 = Graphs.createDirectedWeightedEdge(gn1, gn2, 2.0);
		final IEdge ge13 = Graphs.createDirectedWeightedEdge(gn1, gn3, 1.0);
		final IEdge ge32 = Graphs.createDirectedWeightedEdge(gn3, gn2, 1.5);
		final IEdge ge43 = Graphs.createDirectedWeightedEdge(gn4, gn3, 0.5);
		final IEdge ge42 = Graphs.createDirectedWeightedEdge(gn4, gn2, 2.5);
		final IEdge ge52 = Graphs.createDirectedWeightedEdge(gn5, gn2, 1.5);
		final IEdge ge15 = Graphs.createDirectedWeightedEdge(gn1, gn5, 2.25);
		Graph graph = GraphUtil.createGraph(Arrays.asList(ge12, ge13, ge32, ge43, ge42,ge52,ge15));
		
		// Build pattern
		final INodeID pn1 = INodeID.get("p1");
		final INodeID pn2 = INodeID.get("p2");
		final INodeID pn3 = INodeID.get("p3");
		final IEdge pe12 = Graphs.createDirectedWeightedEdge(pn1, pn2, 0.0);
		final IEdge pe13 = Graphs.createDirectedWeightedEdge(pn1, pn3, 0.0);
		final IEdge pe32 = Graphs.createDirectedWeightedEdge(pn3, pn2, 0.0);
		Graph patternGraph = GraphUtil.createGraph(Arrays.asList(pe12, pe13, pe32));
				
		TopologyPattern pattern = new TopologyPattern(pn1, patternGraph);
		pattern.addConstraint(new KTCConstraint(pe12.getId(), pe13.getId(), pe32.getId(), 1.5, GenericGraphElementProperties.WEIGHT));
		
		TopologyPatternMatcher matcher = new DemoclesTopologyPatternMatcher();
		matcher.setPattern(pattern);
		final Iterable<TopologyPatternMatch> matches = matcher.match(graph);
		Assert.assertThat(Iterables.size(matches), CoreMatchers.is(2));
	}
	

	@Test 
	public void testKTCConstraint_NoMatch() throws Exception {
		//Build Graph
		final INodeID gn1 = INodeID.get("p1");
		final INodeID gn2 = INodeID.get("p2");
		final INodeID gn3 = INodeID.get("p3");
		final IEdge ge12 = Graphs.createDirectedWeightedEdge(gn1, gn2, 2.0);
		final IEdge ge13 = Graphs.createDirectedWeightedEdge(gn1, gn3, 1.0);
		final IEdge ge32 = Graphs.createDirectedWeightedEdge(gn3, gn2, 1.5);
		Graph graph = GraphUtil.createGraph(Arrays.asList(ge12, ge13, ge32));
		
		// Build pattern
		final INodeID pn1 = INodeID.get("p1");
		final INodeID pn2 = INodeID.get("p2");
		final INodeID pn3 = INodeID.get("p3");
		final IEdge pe12 = Graphs.createDirectedWeightedEdge(pn1, pn2, 0.0);
		final IEdge pe13 = Graphs.createDirectedWeightedEdge(pn1, pn3, 0.0);
		final IEdge pe32 = Graphs.createDirectedWeightedEdge(pn3, pn2, 0.0);
		Graph patternGraph = GraphUtil.createGraph(Arrays.asList(pe12, pe13, pe32));
				
		TopologyPattern pattern = new TopologyPattern(pn1, patternGraph);
		pattern.addConstraint(new KTCConstraint(pe12.getId(), pe13.getId(), pe32.getId(), 2.5, GenericGraphElementProperties.WEIGHT));
		
		TopologyPatternMatcher matcher = new DemoclesTopologyPatternMatcher();
		matcher.setPattern(pattern);
		final Iterable<TopologyPatternMatch> matches = matcher.match(graph);
		Assert.assertFalse(matches.iterator().hasNext());
	}
	

	@Test 
	public void testKTCConstraint_HasMatch() throws Exception {
		//Build Graph
		final INodeID gn1 = INodeID.get("p1");
		final INodeID gn2 = INodeID.get("p2");
		final INodeID gn3 = INodeID.get("p3");
		final IEdge ge12 = Graphs.createDirectedWeightedEdge(gn1, gn2, 2.0);
		final IEdge ge13 = Graphs.createDirectedWeightedEdge(gn1, gn3, 1.0);
		final IEdge ge32 = Graphs.createDirectedWeightedEdge(gn3, gn2, 1.5);
		Graph graph = GraphUtil.createGraph(Arrays.asList(ge12, ge13, ge32));
		
		// Build pattern
		final INodeID pn1 = INodeID.get("p1");
		final INodeID pn2 = INodeID.get("p2");
		final INodeID pn3 = INodeID.get("p3");
		final IEdge pe12 = Graphs.createDirectedWeightedEdge(pn1, pn2, 0.0);
		final IEdge pe13 = Graphs.createDirectedWeightedEdge(pn1, pn3, 0.0);
		final IEdge pe32 = Graphs.createDirectedWeightedEdge(pn3, pn2, 0.0);
		Graph patternGraph = GraphUtil.createGraph(Arrays.asList(pe12, pe13, pe32));
				
		TopologyPattern pattern = new TopologyPattern(pn1, patternGraph);
		pattern.addConstraint(new KTCConstraint(pe12.getId(), pe13.getId(), pe32.getId(), 1.5, GenericGraphElementProperties.WEIGHT));
		
		final TopologyPatternMatcher matcher = new DemoclesTopologyPatternMatcher();
		matcher.setPattern(pattern);
		final Iterable<TopologyPatternMatch> matches = matcher.match(graph);

		Assert.assertThat(Iterables.size(matches), CoreMatchers.is(1));
		{
			final VariableAssignment expectedMatch = new VariableAssignment();
			expectedMatch.bindLinkVariable(pe12.getId(), ge12);
			expectedMatch.bindLinkVariable(pe32.getId(), ge32);
			expectedMatch.bindLinkVariable(pe13.getId(), ge13);
			expectedMatch.bindNodeVariable(pn1, graph.getNode(gn1));
			expectedMatch.bindNodeVariable(pn2, graph.getNode(gn2));
			expectedMatch.bindNodeVariable(pn3, graph.getNode(gn3));
			PatternMatcherTestHelper.assertHasPatternMatch(matches, expectedMatch);
		}
	}

	
	@Test
	public void testNoMatch() throws Exception {
		final INodeID pn1 = INodeID.get("pn1");
		final INodeID pn2 = INodeID.get("pn2");
		final IEdge pe12 = Graphs.createDirectedWeightedEdge(pn1, pn2, 1.0);
		final Graph patternGraph = BasicGraph.createGraphFromNodeIdsAndEdges(Arrays.asList(pn1, pn2), Arrays.asList(pe12));

		final INodeID gn1 = INodeID.get(1);
		final Graph graph = BasicGraph.createGraphFromNodeIdsAndEdges(Arrays.asList(gn1), new ArrayList<DirectedEdge>());
		
		TopologyPatternMatcher matcher = new DemoclesTopologyPatternMatcher(new TopologyPattern(pn1, patternGraph));
		Iterable<TopologyPatternMatch> matches = matcher.match(gn1, graph);
		final Iterator<TopologyPatternMatch> matchIterator = matches.iterator();

		Assert.assertFalse(matchIterator.hasNext());
	}
	

	@Test
	public void testLinkWeightConstraintWithTwoLinks() throws Exception {
		// Build pattern
		final INodeID pn1 = INodeID.get("p1");
		final INodeID pn2 = INodeID.get("p2");
		final INodeID pn3 = INodeID.get("p3");
		final IEdge pe12 = Graphs.createDirectedWeightedEdge(pn1, pn2, 0.0);
		final IEdge pe23 = Graphs.createDirectedWeightedEdge(pn2, pn3, 0.0);
		final Graph patternGraph = GraphUtil.createGraph(Arrays.asList(pe12, pe23));
		final TopologyPattern pattern = new TopologyPattern(pn1, patternGraph);
		pattern.addConstraint(new LinkWeightConstraintWithTwoLinks(pe12.getId(), pe23.getId(), ComparisonOperator.EQUAL));

		// Build triangle graph
		final INodeID gn1 = INodeID.get(1);
		final INodeID gn2 = INodeID.get(2);
		final INodeID gn3 = INodeID.get(3);
		final IEdge ge12 = Graphs.createDirectedWeightedEdge(gn1, gn2, 1.0);
		final IEdge ge23 = Graphs.createDirectedWeightedEdge(gn2, gn3, 1.0);
		final IEdge ge31 = Graphs.createDirectedWeightedEdge(gn3, gn1, 1.0);
		final Graph graph = GraphUtil.createGraph(Arrays.asList(ge12, ge23, ge31));

		// Configure pattern matcher
		TopologyPatternMatcher matcher = new DemoclesTopologyPatternMatcher(pattern);
		Iterable<TopologyPatternMatch> matches = matcher.match(graph);
		Assert.assertThat(Iterables.size(matches), CoreMatchers.is(3));
		{
			final VariableAssignment expectedMatch = new VariableAssignment();
			expectedMatch.bindLinkVariable(pe12.getId(), ge12);
			expectedMatch.bindLinkVariable(pe23.getId(), ge23);
			expectedMatch.bindNodeVariable(pn1, graph.getNode(gn1));
			expectedMatch.bindNodeVariable(pn2, graph.getNode(gn2));
			expectedMatch.bindNodeVariable(pn3, graph.getNode(gn3));
			PatternMatcherTestHelper.assertHasPatternMatch(matches, expectedMatch);
		}
		{
			final VariableAssignment expectedMatch = new VariableAssignment();
			expectedMatch.bindLinkVariable(pe12.getId(), ge23);
			expectedMatch.bindLinkVariable(pe23.getId(), ge31);
			expectedMatch.bindNodeVariable(pn1, graph.getNode(gn2));
			expectedMatch.bindNodeVariable(pn2, graph.getNode(gn3));
			expectedMatch.bindNodeVariable(pn3, graph.getNode(gn1));
			PatternMatcherTestHelper.assertHasPatternMatch(matches, expectedMatch);
		}
		{
			final VariableAssignment expectedMatch = new VariableAssignment();
			expectedMatch.bindLinkVariable(pe12.getId(), ge31);
			expectedMatch.bindLinkVariable(pe23.getId(), ge12);
			expectedMatch.bindNodeVariable(pn1, graph.getNode(gn3));
			expectedMatch.bindNodeVariable(pn2, graph.getNode(gn1));
			expectedMatch.bindNodeVariable(pn3, graph.getNode(gn2));
			PatternMatcherTestHelper.assertHasPatternMatch(matches, expectedMatch);
		}
	}

	
	@Test
	public void testMatchWithoutOriginNode() throws Exception {
		// Build pattern
		final INodeID pn1 = INodeID.get("p1");
		final INodeID pn2 = INodeID.get("p2");
		final IEdge pe12 = Graphs.createDirectedWeightedEdge(pn1, pn2, 0.0);
		final Graph patternGraph = GraphUtil.createGraph(Arrays.asList(pe12));
		final TopologyPattern pattern = new TopologyPattern(pn1, patternGraph);
		pattern.addConstraint(new LinkWeightConstraintWithScalarValue(pe12.getId(), 1.0, ComparisonOperator.EQUAL));

		// Build triangle graph
		final INodeID gn1 = INodeID.get(1);
		final INodeID gn2 = INodeID.get(2);
		final INodeID gn3 = INodeID.get(3);
		final IEdge ge12 = Graphs.createDirectedWeightedEdge(gn1, gn2, 1.0);
		final IEdge ge21 = Graphs.createDirectedWeightedEdge(gn2, gn1, 0.0);
		final IEdge ge23 = Graphs.createDirectedWeightedEdge(gn2, gn3, 1.0);
		final IEdge ge32 = Graphs.createDirectedWeightedEdge(gn3, gn2, 0.0);
		final IEdge ge13 = Graphs.createDirectedWeightedEdge(gn1, gn3, 1.0);
		final IEdge ge31 = Graphs.createDirectedWeightedEdge(gn3, gn1, 0.0);
		final Graph graph = GraphUtil.createGraph(Arrays.asList(ge12, ge21, ge23, ge32, ge13, ge31));

		// Configure pattern matcher
		TopologyPatternMatcher matcher = new DemoclesTopologyPatternMatcher(pattern);
		Iterable<TopologyPatternMatch> matches = matcher.match(graph);
		Assert.assertThat(Iterables.size(matches), CoreMatchers.is(3));
		{
			final VariableAssignment expectedMatch = new VariableAssignment();
			expectedMatch.bindLinkVariable(pe12.getId(), ge12);
			expectedMatch.bindNodeVariable(pn1, graph.getNode(gn1));
			expectedMatch.bindNodeVariable(pn2, graph.getNode(gn2));
			PatternMatcherTestHelper.assertHasPatternMatch(matches, expectedMatch);
		}
		{
			final VariableAssignment expectedMatch = new VariableAssignment();
			expectedMatch.bindLinkVariable(pe12.getId(), ge23);
			expectedMatch.bindNodeVariable(pn1, graph.getNode(gn2));
			expectedMatch.bindNodeVariable(pn2, graph.getNode(gn3));
			PatternMatcherTestHelper.assertHasPatternMatch(matches, expectedMatch);
		}
		{
			final VariableAssignment expectedMatch = new VariableAssignment();
			expectedMatch.bindLinkVariable(pe12.getId(), ge13);
			expectedMatch.bindNodeVariable(pn1, graph.getNode(gn1));
			expectedMatch.bindNodeVariable(pn2, graph.getNode(gn3));
			PatternMatcherTestHelper.assertHasPatternMatch(matches, expectedMatch);
		}

	}

	@Test
	public void testPatternWithNAC() throws Exception {

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
		TopologyPatternMatcher matcher = new DemoclesTopologyPatternMatcher(pattern);
		Iterable<TopologyPatternMatch> matches = matcher.match(graph);;
		
		final Iterator<TopologyPatternMatch> matchIterator = matches.iterator();

		final TopologyPatternMatch match = matchIterator.next();
		final VariableAssignment variableAssignment = match.getVariableAssignment();
		final INodeID matchedNode1 = variableAssignment.getNodeVariableBinding(pn1);
		final INodeID matchedNode2 = variableAssignment.getNodeVariableBinding(pn2);
		final IEdge matchedEdge1= variableAssignment.getBindingLink(pe12.getId());
		Assert.assertEquals(gn2, matchedNode1);
		Assert.assertEquals(gn3, matchedNode2);
		Assert.assertEquals(ge23, matchedEdge1);
	}
	
	@Test
	public void testPatternWithTwoNACs() throws Exception {

		// Build pattern
		final INodeID pn1 = INodeID.get("pn1");
		final INodeID pn2 = INodeID.get("pn2");
		final IEdge pe12 = Graphs.createDirectedWeightedEdge(pn1, pn2, 1.0);
		final Graph patternGraph = GraphUtil.createGraph(Arrays.asList(pe12));
		final TopologyPattern pattern = new TopologyPattern(pn1, patternGraph);
		
		// Build NAC1
		final INodeID pn3 = INodeID.get("pn3");
		final IEdge pe13 = Graphs.createDirectedWeightedEdge(pn1, pn3, 1.0);
		final Graph nacGraph1 = GraphUtil.createGraph(Arrays.asList(pe12, pe13));
		final TopologyPattern nac1 = new TopologyPattern(pn1, nacGraph1);
		pattern.getNegativeApplicationConstraints().add(nac1);
		
		// Build NAC2
		final Graph nacGraph2 = GraphUtil.createGraph(Arrays.asList(pe12));
		final TopologyPattern nac2 = new TopologyPattern(pn1, nacGraph2);
		nac2.addConstraint(new LinkWeightConstraintWithScalarValue(pe12.getId(), 5.0, ComparisonOperator.GREATER));
		pattern.getNegativeApplicationConstraints().add(nac2);

		// Build graph
		final INodeID gn1 = INodeID.get(1);
		final INodeID gn2 = INodeID.get(2);
		final INodeID gn3 = INodeID.get(3);
		final IEdge ge12 = Graphs.createDirectedWeightedEdge(gn1, gn2, 1.0);
		final IEdge ge13 = Graphs.createDirectedWeightedEdge(gn1, gn3, 1.0);
		final IEdge ge23 = Graphs.createDirectedWeightedEdge(gn2, gn3, 5.0);
		final Graph graph = GraphUtil.createGraph(Arrays.asList(ge12, ge13, ge23));

		// Configure pattern matcher
		TopologyPatternMatcher matcher = new DemoclesTopologyPatternMatcher(pattern);
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
	}
	

	@Test
	public void testMatchOneEdgeWithWeight_NoMatch() throws Exception {

		// Build pattern
		final INodeID pn1 = INodeID.get("pn1");
		final INodeID pn2 = INodeID.get("pn2");
		final IEdge pe12 = Graphs.createDirectedWeightedEdge(pn1, pn2, 1.0);
		final Graph patternGraph = GraphUtil.createGraph(Arrays.asList(pe12));
		final TopologyPattern pattern = new TopologyPattern(pn1, patternGraph);
		pattern.addConstraint(new LinkWeightConstraintWithScalarValue(pe12.getId(), 1.0, ComparisonOperator.GREATER));

		// Build graph
		final INodeID gn1 = INodeID.get(1);
		final INodeID gn2 = INodeID.get(2);
		final IEdge ge12 = Graphs.createDirectedWeightedEdge(gn1, gn2, 1.0);
		final Graph graph = GraphUtil.createGraph(Arrays.asList(ge12));

		// Configure pattern matcher
		TopologyPatternMatcher matcher = new DemoclesTopologyPatternMatcher(pattern);
		Iterable<TopologyPatternMatch> matches = matcher.match(gn1, graph);
		Assert.assertFalse(matches.iterator().hasNext());
	}
	

	@Test
	public void testMatchOneEdge() throws Exception {

		// Build pattern
		final INodeID pn1 = INodeID.get("pn1");
		final INodeID pn2 = INodeID.get("pn2");
		final IEdge pe12 = Graphs.createDirectedWeightedEdge(pn1, pn2, 1.0);
		final Graph patternGraph = GraphUtil.createGraph(Arrays.asList(pe12));
		TopologyPattern pattern = new TopologyPattern(pn1, patternGraph);

		// Build graph
		final INodeID gn1 = INodeID.get(1);
		final INodeID gn2 = INodeID.get(2);
		final IEdge ge12 = Graphs.createDirectedWeightedEdge(gn1, gn2, 1.0);
		final Graph graph = GraphUtil.createGraph(Arrays.asList(ge12));

		// Configure pattern matcher
		TopologyPatternMatcher matcher = new DemoclesTopologyPatternMatcher(pattern);
		Iterable<TopologyPatternMatch> matches = matcher.match(gn1, graph);
		final Iterator<TopologyPatternMatch> matchIterator = matches.iterator();

		final TopologyPatternMatch match = matchIterator.next();
		final VariableAssignment variableAssignment = match.getVariableAssignment();
		final INodeID matchedNode1 = variableAssignment.getNodeVariableBinding(pn1);
		final INodeID matchedNode2 = variableAssignment.getNodeVariableBinding(pn2);
		Assert.assertEquals(gn1, matchedNode1);
		Assert.assertEquals(gn2, matchedNode2);
	}


	@Test
	public void testMatchOneEdgeWithWeight_HasMatch() throws Exception {

		// Build pattern
		final INodeID pn1 = INodeID.get("pn1");
		final INodeID pn2 = INodeID.get("pn2");
		final IEdge pe12 = Graphs.createDirectedWeightedEdge(pn1, pn2, 1.0);
		final Graph patternGraph = GraphUtil.createGraph(Arrays.asList(pe12));
		final TopologyPattern pattern = new TopologyPattern(pn1, patternGraph);
		pattern.addConstraint(new LinkWeightConstraintWithScalarValue(pe12.getId(), 1.0, ComparisonOperator.EQUAL));

		// Build graph
		final INodeID gn1 = INodeID.get(1);
		final INodeID gn2 = INodeID.get(2);
		final IEdge ge12 = Graphs.createDirectedWeightedEdge(gn1, gn2, 1.0);
		final Graph graph = GraphUtil.createGraph(Arrays.asList(ge12));

		// Configure pattern matcher
		TopologyPatternMatcher matcher = new DemoclesTopologyPatternMatcher(pattern);
		Iterable<TopologyPatternMatch> matches = matcher.match(gn1, graph);
		
		Assert.assertTrue(matches.iterator().hasNext());
	}


	@Test
	public void testTriangleGraphWithChainPattern_HasMatch() throws Exception {
		// Build pattern
		final INodeID pn1 = INodeID.get("p1");
		final INodeID pn2 = INodeID.get("p2");
		final INodeID pn3 = INodeID.get("p3");
		final IEdge pe12 = Graphs.createDirectedWeightedEdge(pn1, pn2, 0.0);
		final IEdge pe23 = Graphs.createDirectedWeightedEdge(pn2, pn3, 0.0);
		final Graph patternGraph = GraphUtil.createGraph(Arrays.asList(pe12, pe23));
		final TopologyPattern pattern = new TopologyPattern(pn1, patternGraph);

		// Build triangle graph
		final INodeID gn1 = INodeID.get(1);
		final INodeID gn2 = INodeID.get(2);
		final INodeID gn3 = INodeID.get(3);
		final IEdge ge12 = Graphs.createDirectedWeightedEdge(gn1, gn2, 0.0);
		final IEdge ge21 = Graphs.createDirectedWeightedEdge(gn2, gn1, 0.0);
		final IEdge ge23 = Graphs.createDirectedWeightedEdge(gn2, gn3, 0.0);
		final IEdge ge32 = Graphs.createDirectedWeightedEdge(gn3, gn2, 0.0);
		final IEdge ge13 = Graphs.createDirectedWeightedEdge(gn1, gn3, 0.0);
		final IEdge ge31 = Graphs.createDirectedWeightedEdge(gn3, gn1, 0.0);
		final Graph graph = GraphUtil.createGraph(Arrays.asList(ge12, ge21, ge23, ge32, ge13, ge31));

		// Configure pattern matcher
		TopologyPatternMatcher matcher = new DemoclesTopologyPatternMatcher(pattern);
		Iterable<TopologyPatternMatch> matches = matcher.match(gn1, graph);
		Assert.assertThat(Iterables.size(matches), CoreMatchers.is(2));
		{
			final VariableAssignment expectedMatch = new VariableAssignment();
			expectedMatch.bindLinkVariable(pe12.getId(), ge12);
			expectedMatch.bindLinkVariable(pe23.getId(), ge23);
			expectedMatch.bindNodeVariable(pn1, graph.getNode(gn1));
			expectedMatch.bindNodeVariable(pn2, graph.getNode(gn2));
			expectedMatch.bindNodeVariable(pn3, graph.getNode(gn3));
			PatternMatcherTestHelper.assertHasPatternMatch(matches, expectedMatch);
		}
		{
			final VariableAssignment expectedMatch = new VariableAssignment();
			expectedMatch.bindLinkVariable(pe12.getId(), ge13);
			expectedMatch.bindLinkVariable(pe23.getId(), ge32);
			expectedMatch.bindNodeVariable(pn1, graph.getNode(gn1));
			expectedMatch.bindNodeVariable(pn2, graph.getNode(gn3));
			expectedMatch.bindNodeVariable(pn3, graph.getNode(gn2));
			PatternMatcherTestHelper.assertHasPatternMatch(matches, expectedMatch);
		}
	}

}
