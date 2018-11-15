package de.tudarmstadt.maki.simonstrator.tc.democles.integrated;

import java.util.ArrayList;
import java.util.List;

import org.gervarro.democles.common.DataFrame;
import org.gervarro.democles.runtime.InterpretedDataFrame;

import de.tudarmstadt.maki.simonstrator.api.common.graph.DirectedEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Node;
import de.tudarmstadt.maki.simonstrator.api.component.topology.VariableAssignment;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.matching.TopologyPatternMatch;

/**
 * Transforms the found Democles-matches to Simonstrator-matches
 * @author lneumann
 */
public class DemoclesMatchesToSimonstratorMatchesTransformation {
	
	private static final DemoclesMatchesToSimonstratorMatchesTransformation instance = new DemoclesMatchesToSimonstratorMatchesTransformation();

	public static DemoclesMatchesToSimonstratorMatchesTransformation getInstance() {
		return instance;
	}

	public Iterable<TopologyPatternMatch> transform(Iterable<InterpretedDataFrame> democlesMatches, SimonstratorPatternToDemoclesIntegratedPatternMapping patternMapping) {
		List<TopologyPatternMatch> matches = new ArrayList<TopologyPatternMatch>();
		for(DataFrame match : democlesMatches){
			VariableAssignment variableAssignment = new VariableAssignment();
			for(int i = 0; i < match.size(); i++){
				Object o = match.getValue(i);
				if(o instanceof DirectedEdge){
					//first parameter is variable in Topology-Pattern, second is parameter in graph
					variableAssignment.bindLinkVariable(patternMapping.getEdgeMapping(i), ((DirectedEdge) o));
				} else if(o instanceof Node){
					variableAssignment.bindNodeVariable(patternMapping.getNodeMapping(i), (Node) o);
				}
			}
			matches.add(new TopologyPatternMatch(variableAssignment));
		}
		return matches;
	}

}
