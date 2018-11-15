package de.tudarmstadt.maki.simonstrator.tc.patternMatching;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

import de.tudarmstadt.maki.simonstrator.api.common.graph.DirectedEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.EdgeID;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.api.component.topology.VariableAssignment;
import de.tudarmstadt.maki.simonstrator.tc.GraphUtil;
import de.tudarmstadt.maki.simonstrator.tc.lomba.util.GraphOperations;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.matching.TopologyPatternMatch;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.matching.TopologyPatternMatcher;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.matching.TopologyPatternMatcher_Impl;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.pattern.PatternBuilder;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.pattern.TopologyPattern;

public class PatternMatcherTossaTest {

	@Test
	public void incommingEdgeTest() {
		final Graph graph = GraphOperations.graphFromString("0->1,1->2,2->3");
		final Graph graphPattern = GraphOperations.graphFromString("1->0");
		graphPattern.createAndAddNode(INodeID.get(0));
		final INodeID n0 = INodeID.get(0);
		final TopologyPattern pattern = new TopologyPattern(n0, graphPattern);
		final TopologyPatternMatcher matcher = new TopologyPatternMatcher_Impl();
		matcher.setPattern(pattern);
		final Iterable<TopologyPatternMatch> matches = matcher.match(graph);

		final Set<VariableAssignment> matchVariableAssignments = new HashSet<>();
		for (final TopologyPatternMatch match : matches) {
			matchVariableAssignments.add(match.getVariableAssignment());
		}
		
		final Set<VariableAssignment> expectedBindings = new HashSet<>();
		for(int i = 0; i < 3; i++){
			VariableAssignment va = new VariableAssignment();
			va.bindNodeVariable(INodeID.get(1), graph.getNode(INodeID.get(i)));
			va.bindNodeVariable(INodeID.get(0), graph.getNode(INodeID.get(i+1)));;
			va.bindLinkVariable(EdgeID.get("1->0"), graph.getEdge(EdgeID.get(i + "->" + (i+1))));
			expectedBindings.add(va);
		}
		assertEquals("More or less matches than expected!", expectedBindings.size(), matchVariableAssignments.size());

		
		assertEquals(expectedBindings, matchVariableAssignments);
	}
	
	@Test
	public void patternBuilderTest(){
		PatternBuilder builder = PatternBuilder.create();
		builder.addDirectedEdge("n1", "e1", "self").setLocalNode("self").done();
	}
	
	
	@Ignore
	@Test 
	public void patternMatcherTest(){
		PatternBuilder builder1 = PatternBuilder.create();
		TopologyPattern top1Pattern = builder1.addDirectedEdge("n1", "e1", "self").setLocalNode("self").done();
		
		PatternBuilder builder2 = PatternBuilder.create();
		TopologyPattern top2Pattern = builder2.addDirectedEdge("n2", "e2", "n1").setLocalNode("self").done();
		
		Set<IEdge> edgesTop1 = new HashSet<>();
		edgesTop1.add(new DirectedEdge(INodeID.get(1), INodeID.get(0)));
		edgesTop1.add(new DirectedEdge(INodeID.get(2), INodeID.get(0)));
		
		Set<IEdge> edgesTop2 = new HashSet<>();
		edgesTop2.add(new DirectedEdge(INodeID.get(1), INodeID.get(0)));
		edgesTop2.add(new DirectedEdge(INodeID.get(3), INodeID.get(1)));
		edgesTop2.add(new DirectedEdge(INodeID.get(4), INodeID.get(1)));
		Graph graphTop1 = GraphUtil.createGraph(edgesTop1);
		Graph graphTop2 = GraphUtil.createGraph(edgesTop2);
		
		INodeID localNode = INodeID.get(0);
		
		TopologyPatternMatcher matcherTop1 = new TopologyPatternMatcher_Impl();
		matcherTop1.setPattern(top1Pattern);
		final Iterable<TopologyPatternMatch> matchesTop1 = matcherTop1.match(localNode, graphTop1);
		
		final Set<VariableAssignment> matchVariableAssignmentsTop1 = new HashSet<>();
		for (final TopologyPatternMatch match : matchesTop1) {
			matchVariableAssignmentsTop1.add(match.getVariableAssignment());
		}
		
		assertEquals(2, matchVariableAssignmentsTop1.size());
		
		TopologyPatternMatcher matcherTop2 = new TopologyPatternMatcher_Impl();
		matcherTop2.setPattern(top2Pattern);
		final Iterable<TopologyPatternMatch> matchesTop2 = matcherTop2.match(localNode, graphTop2);
		
		final Set<VariableAssignment> matchVariableAssignmentsTop2 = new HashSet<>();
		for (final TopologyPatternMatch match : matchesTop2) {
			matchVariableAssignmentsTop2.add(match.getVariableAssignment());
		}
		
		assertEquals(3, matchVariableAssignmentsTop2.size());


	}

}
