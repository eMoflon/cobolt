package de.tudarmstadt.maki.simonstrator.tc.patternMatching.matching;

import java.io.Serializable;

import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.api.component.topology.ITopologyChangedEvent;
import de.tudarmstadt.maki.simonstrator.api.component.topology.VariableAssignment;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.pattern.TopologyPattern;

public interface TopologyPatternMatcher extends Serializable {

	/**
	 * Sets the graph pattern of this matcher
	 */
	void setPattern(final TopologyPattern pattern);

	/**
	 * Returns the currently stored pattern
	 */
	TopologyPattern getPattern();

	/**
	 * Returns all matches of the stored pattern in the given graph that map the given local node to the origin of the pattern.
	 *
	 * Preconditions: pattern has to be set.
	 *
	 * @param localNode the ID of the node in the graph that will be mapped to the origin of the stored pattern in all matches
	 * @param graph the graph in which the matches shall be identified
	 * @return an iterator over the matches
	 */
	Iterable<TopologyPatternMatch> match(final INodeID localNode, final Graph graph);

	/**
	 * Returns all matches of the stored pattern in the given graph.
	 *
	 * Preconditions: pattern has to be set.
	 *
	 * @param graph the graph in which the matches shall be identified
	 * @return an iterator over the matches
	 */
	Iterable<TopologyPatternMatch> match(final Graph graph);

	/**
	 * Processes the given event and check for new matches.
	 *
	 * Preconditions: pattern has to be set.
	 *
	 * @return an iterator over the matches
	 */
	Iterable<TopologyPatternMatch> handleEvent(ITopologyChangedEvent topologyChangedEvent);

	/**
	 * Finds matches for the stored pattern in the given graph, using the given node as 'origin'.
	 * The given variable assignment may contain bindings for node variables.
	 */
	Iterable<TopologyPatternMatch> match(INodeID localNode, Graph graph, VariableAssignment inputVariableAssignment);

	/**
	 * Returns whether a match of the stored pattern can be found in the given graph using the given input variable assignment
	 */
	boolean hasMatch(INodeID localNode, Graph graph, VariableAssignment variableAssignment);

	/**
	 * Returns whether a match of the stored pattern can be found in the given graph
	 */
	boolean hasMatch(INodeID localNode, Graph graph);

	/**
	 * Returns the number of matches of the stored pattern in the given graph
	 */
	int countMatches(INodeID localNode, Graph graph);

}
