package de.tudarmstadt.maki.simonstrator.tc.patternMatching;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Iterables;

import de.tudarmstadt.maki.simonstrator.api.Graphs;
import de.tudarmstadt.maki.simonstrator.api.common.graph.BasicGraph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.DirectedEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.EdgeID;
import de.tudarmstadt.maki.simonstrator.api.common.graph.GenericGraphElementProperties;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IElement;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.api.component.topology.VariableAssignment;
import de.tudarmstadt.maki.simonstrator.tc.GraphUtil;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.constraint.ComparisonOperator;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.constraint.GraphElementConstraint;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.constraint.LinkWeightConstraintWithScalarValue;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.matching.TopologyPatternMatch;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.matching.TopologyPatternMatcher;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.matching.TopologyPatternMatcher_Impl;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.pattern.TopologyPattern;
import de.tudarmstadt.maki.simonstrator.tc.testing.GraphTestUtil;
import de.tudarmstadt.maki.simonstrator.tc.testing.PatternMatcherTestHelper;
import de.tudarmstadt.maki.simonstrator.tc.underlay.KTCConstraint;

/**
 * Unit tests for {@link TopologyPatternMatcher}
 *
 */
public class TopologyPatternMatcherTest
{

   @Test
   public void testKTCConstraint_HasMatches() throws Exception
   {
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
      Graph graph = GraphUtil.createGraph(Arrays.asList(ge12, ge13, ge32, ge43, ge42, ge52, ge15));

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

      TopologyPatternMatcher matcher = new TopologyPatternMatcher_Impl();
      matcher.setPattern(pattern);
      final Iterable<TopologyPatternMatch> matches = matcher.match(graph);
      Assert.assertThat(Iterables.size(matches), CoreMatchers.is(2));
   }

   @Test
   public void testKTCConstraint_NoMatch() throws Exception
   {
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

      final TopologyPatternMatcher matcher = new TopologyPatternMatcher_Impl();
      matcher.setPattern(pattern);
      final Iterable<TopologyPatternMatch> matches = matcher.match(graph);
      Assert.assertFalse(matches.iterator().hasNext());

   }

   @Test
   public void testKTCConstraint_HasMatch() throws Exception
   {
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

      final TopologyPatternMatcher matcher = new TopologyPatternMatcher_Impl();
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
   public void testNoMatch() throws Exception
   {
      final INodeID pn1 = INodeID.get("pn1");
      final INodeID pn2 = INodeID.get("pn2");
      final IEdge pe12 = Graphs.createDirectedWeightedEdge(pn1, pn2, 1.0);
      final Graph patternGraph = BasicGraph.createGraphFromNodeIdsAndEdges(Arrays.asList(pn1, pn2), Arrays.asList(pe12));

      final INodeID gn1 = INodeID.get(1);
      final Graph graph = BasicGraph.createGraphFromNodeIdsAndEdges(Arrays.asList(gn1), new ArrayList<DirectedEdge>());

      final TopologyPatternMatcher matcher = new TopologyPatternMatcher_Impl();
      matcher.setPattern(new TopologyPattern(pn1, patternGraph));
      final Iterable<TopologyPatternMatch> matches = matcher.match(gn1, graph);
      final Iterator<TopologyPatternMatch> matchIterator = matches.iterator();

      Assert.assertFalse(matchIterator.hasNext());
   }

   @Test
   public void testMatchOneEdge() throws Exception
   {

      // Build pattern
      final INodeID pn1 = INodeID.get("pn1");
      final INodeID pn2 = INodeID.get("pn2");
      final IEdge pe12 = Graphs.createDirectedWeightedEdge(pn1, pn2, 1.0);
      final Graph patternGraph = GraphUtil.createGraph(Arrays.asList(pe12));

      // Build graph
      final INodeID gn1 = INodeID.get(1);
      final INodeID gn2 = INodeID.get(2);
      final IEdge ge12 = Graphs.createDirectedWeightedEdge(gn1, gn2, 1.0);
      final Graph graph = GraphUtil.createGraph(Arrays.asList(ge12));

      // Configure pattern matcher
      final TopologyPatternMatcher matcher = new TopologyPatternMatcher_Impl();
      matcher.setPattern(new TopologyPattern(pn1, patternGraph));
      final Iterable<TopologyPatternMatch> matches = matcher.match(gn1, graph);
      final Iterator<TopologyPatternMatch> matchIterator = matches.iterator();

      final TopologyPatternMatch match = matchIterator.next();
      final VariableAssignment variableAssignment = match.getVariableAssignment();
      final INodeID matchedNode1 = variableAssignment.getNodeVariableBinding(pn1);
      final INodeID matchedNode2 = variableAssignment.getNodeVariableBinding(pn2);
      Assert.assertEquals(gn1, matchedNode1);
      Assert.assertEquals(gn2, matchedNode2);
   }

   @Test
   public void testMatchOneEdgeWithWeight_NoMatch() throws Exception
   {

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
      final TopologyPatternMatcher matcher = new TopologyPatternMatcher_Impl();
      matcher.setPattern(pattern);
      final Iterable<TopologyPatternMatch> matches = matcher.match(gn1, graph);
      Assert.assertFalse(matches.iterator().hasNext());
   }

   @Test
   public void testMatchOneEdgeWithWeight_HasMatch() throws Exception
   {

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
      final TopologyPatternMatcher matcher = new TopologyPatternMatcher_Impl();
      matcher.setPattern(pattern);
      final Iterable<TopologyPatternMatch> matches = matcher.match(gn1, graph);
      Assert.assertTrue(matches.iterator().hasNext());
   }

   @Test
   public void testTriangleGraphWithChainPattern_HasMatch() throws Exception
   {
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
      final TopologyPatternMatcher matcher = new TopologyPatternMatcher_Impl();
      matcher.setPattern(pattern);
      final Iterable<TopologyPatternMatch> matches = matcher.match(gn1, graph);
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

   @Test
   public void testMatchWithoutOriginNode() throws Exception
   {
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
      final TopologyPatternMatcher matcher = new TopologyPatternMatcher_Impl();
      matcher.setPattern(pattern);
      final Iterable<TopologyPatternMatch> matches = matcher.match(graph);
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
   public void testMatchInMultigraph_Simple() throws Exception
   {
      // Build pattern (that matches a single edge)
      final INodeID pn1 = INodeID.get("p1");
      final INodeID pn2 = INodeID.get("p2");
      final IEdge pe12 = Graphs.createDirectedEdge(EdgeID.get("pe12"), pn1, pn2);
      final Graph patternGraph = GraphUtil.createGraph(Arrays.asList(pe12));
      final TopologyPattern pattern = new TopologyPattern(pn1, patternGraph);
      
      final INodeID gn1 = INodeID.get(1);
      final INodeID gn2 = INodeID.get(2);
      final IEdge ge12_1 = Graphs.createDirectedWeightedEdge(gn1, gn2, EdgeID.get("e12_1"), 1.0);
      final IEdge ge12_2 = Graphs.createDirectedWeightedEdge(gn1, gn2, EdgeID.get("e12_2"), 2.0);
      final Graph graph = GraphUtil.createGraph(Arrays.asList(ge12_1, ge12_2));
      
      GraphTestUtil.assertEdgeCount(2, graph);
      
      final TopologyPatternMatcher pm = new TopologyPatternMatcher_Impl(pattern);
      final Iterable<TopologyPatternMatch> matches = pm.match(graph);
      Assert.assertThat(Iterables.size(matches), CoreMatchers.is(2));
      {
         final VariableAssignment expectedMatch = new VariableAssignment();
         expectedMatch.bindLinkVariable(pe12.getId(), ge12_1);
         expectedMatch.bindNodeVariable(pn1, graph.getNode(gn1));
         expectedMatch.bindNodeVariable(pn2, graph.getNode(gn2));
         PatternMatcherTestHelper.assertHasPatternMatch(matches, expectedMatch);
      }
      {
         final VariableAssignment expectedMatch = new VariableAssignment();
         expectedMatch.bindLinkVariable(pe12.getId(), ge12_2);
         expectedMatch.bindNodeVariable(pn1, graph.getNode(gn1));
         expectedMatch.bindNodeVariable(pn2, graph.getNode(gn2));
         PatternMatcherTestHelper.assertHasPatternMatch(matches, expectedMatch);
      }
   }

   /**
    * This is a slightly more complex multi-graph pattern matching test
    * 
    * The pattern is a 2-chain: p1 -pe12-> p2 -pe23-> p3
    * The graph consists of three nodes (1,2,3) and the pairs (1,2) and (2,3) are connected with two parallel edges each.
    * 
    * The result should be the set of all four possible combinations.
    */
   @Test
   public void testMatchInMultigraph_Medium() throws Exception
   {
      // Build pattern (that matches a single edge)
      final INodeID pn1 = INodeID.get("p1");
      final INodeID pn2 = INodeID.get("p2");
      final INodeID pn3 = INodeID.get("p3");
      final IEdge pe12 = Graphs.createDirectedEdge(EdgeID.get("pe12"), pn1, pn2);
      final IEdge pe23 = Graphs.createDirectedEdge(EdgeID.get("pe23"), pn2, pn3);
      final Graph patternGraph = GraphUtil.createGraph(Arrays.asList(pe12, pe23));
      final TopologyPattern pattern = new TopologyPattern(pn1, patternGraph);

      final INodeID gn1 = INodeID.get(1);
      final INodeID gn2 = INodeID.get(2);
      final INodeID gn3 = INodeID.get(3);
      final IEdge ge12_1 = Graphs.createDirectedWeightedEdge(gn1, gn2, EdgeID.get("e12_1"), 1.0);
      final IEdge ge12_2 = Graphs.createDirectedWeightedEdge(gn1, gn2, EdgeID.get("e12_2"), 2.0);
      final IEdge ge23_1 = Graphs.createDirectedWeightedEdge(gn2, gn3, EdgeID.get("e23_1"), 1.0);
      final IEdge ge23_2 = Graphs.createDirectedWeightedEdge(gn2, gn3, EdgeID.get("e23_2"), 2.0);
      final Graph graph = GraphUtil.createGraph(Arrays.asList(ge12_1, ge12_2, ge23_1, ge23_2));

      GraphTestUtil.assertEdgeCount(4, graph);

      final TopologyPatternMatcher pm = new TopologyPatternMatcher_Impl(pattern);
      final Iterable<TopologyPatternMatch> matches = pm.match(graph);
      Assert.assertThat(Iterables.size(matches), CoreMatchers.is(4));
      final VariableAssignment basicExpectedMatch = new VariableAssignment();
      basicExpectedMatch.bindNodeVariable(pn1, graph.getNode(gn1));
      basicExpectedMatch.bindNodeVariable(pn2, graph.getNode(gn2));
      basicExpectedMatch.bindNodeVariable(pn3, graph.getNode(gn3));
      {
         final VariableAssignment expectedMatch = new VariableAssignment(basicExpectedMatch);
         expectedMatch.bindLinkVariable(pe12.getId(), ge12_1);
         expectedMatch.bindLinkVariable(pe23.getId(), ge23_1);
         PatternMatcherTestHelper.assertHasPatternMatch(matches, expectedMatch);
      }
      {
         final VariableAssignment expectedMatch = new VariableAssignment(basicExpectedMatch);
         expectedMatch.bindLinkVariable(pe12.getId(), ge12_2);
         expectedMatch.bindLinkVariable(pe23.getId(), ge23_1);
         PatternMatcherTestHelper.assertHasPatternMatch(matches, expectedMatch);
      }
      {
         final VariableAssignment expectedMatch = new VariableAssignment(basicExpectedMatch);
         expectedMatch.bindLinkVariable(pe12.getId(), ge12_1);
         expectedMatch.bindLinkVariable(pe23.getId(), ge23_2);
         PatternMatcherTestHelper.assertHasPatternMatch(matches, expectedMatch);
      }
      {
         final VariableAssignment expectedMatch = new VariableAssignment(basicExpectedMatch);
         expectedMatch.bindLinkVariable(pe12.getId(), ge12_2);
         expectedMatch.bindLinkVariable(pe23.getId(), ge23_2);
         PatternMatcherTestHelper.assertHasPatternMatch(matches, expectedMatch);
      }
   }

   /**
    * This is an adapted version of #testMatchInMultigraph_Medium
    * 
    * The only difference is that two of the four combinations (those containing e12_1) are filtered out due to the violated link weight constraint pe12.weight > 1.0
    */
   @Test
   public void testMatchInMultigraph_WithWeiht() throws Exception
   {
      // Build pattern (that matches a single edge)
      final INodeID pn1 = INodeID.get("p1");
      final INodeID pn2 = INodeID.get("p2");
      final INodeID pn3 = INodeID.get("p3");
      final IEdge pe12 = Graphs.createDirectedEdge(EdgeID.get("pe12"), pn1, pn2);
      final IEdge pe23 = Graphs.createDirectedEdge(EdgeID.get("pe23"), pn2, pn3);
      final Graph patternGraph = GraphUtil.createGraph(Arrays.asList(pe12, pe23));
      final TopologyPattern pattern = new TopologyPattern(pn1, patternGraph);
      pattern.getConstraints().add(new GraphElementConstraint(Arrays.asList(EdgeID.get("pe12"))) {
         @Override
         protected boolean checkCandidates(final Collection<? extends IElement> bindingCandidates)
         {
            final IElement candidate = bindingCandidates.iterator().next();
            return candidate.getProperty(GenericGraphElementProperties.WEIGHT) > 1.0;
         }
         
         private static final long serialVersionUID = 5281762649254084594L;
      });

      final INodeID gn1 = INodeID.get(1);
      final INodeID gn2 = INodeID.get(2);
      final INodeID gn3 = INodeID.get(3);
      final IEdge ge12_1 = Graphs.createDirectedWeightedEdge(gn1, gn2, EdgeID.get("e12_1"), 1.0);
      final IEdge ge12_2 = Graphs.createDirectedWeightedEdge(gn1, gn2, EdgeID.get("e12_2"), 2.0);
      final IEdge ge23_1 = Graphs.createDirectedWeightedEdge(gn2, gn3, EdgeID.get("e23_1"), 1.0);
      final IEdge ge23_2 = Graphs.createDirectedWeightedEdge(gn2, gn3, EdgeID.get("e23_2"), 2.0);
      final Graph graph = GraphUtil.createGraph(Arrays.asList(ge12_1, ge12_2, ge23_1, ge23_2));

      GraphTestUtil.assertEdgeCount(4, graph);

      final TopologyPatternMatcher pm = new TopologyPatternMatcher_Impl(pattern);
      final Iterable<TopologyPatternMatch> matches = pm.match(graph);
      Assert.assertThat(Iterables.size(matches), CoreMatchers.is(2));
      final VariableAssignment basicExpectedMatch = new VariableAssignment();
      basicExpectedMatch.bindNodeVariable(pn1, graph.getNode(gn1));
      basicExpectedMatch.bindNodeVariable(pn2, graph.getNode(gn2));
      basicExpectedMatch.bindNodeVariable(pn3, graph.getNode(gn3));
      basicExpectedMatch.bindLinkVariable(pe12.getId(), ge12_2);
      {
         final VariableAssignment expectedMatch = new VariableAssignment(basicExpectedMatch);
         expectedMatch.bindLinkVariable(pe23.getId(), ge23_1);
         PatternMatcherTestHelper.assertHasPatternMatch(matches, expectedMatch);
      }
      {
         final VariableAssignment expectedMatch = new VariableAssignment(basicExpectedMatch);
         expectedMatch.bindLinkVariable(pe23.getId(), ge23_2);
         PatternMatcherTestHelper.assertHasPatternMatch(matches, expectedMatch);
      }
   }

}
