package de.tudarmstadt.maki.simonstrator.tc.patternMatching.matching;

import de.tudarmstadt.maki.simonstrator.api.component.topology.VariableAssignment;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.pattern.TopologyPattern;

/**
 * Represents a concrete match of a particular pattern
 */
public class TopologyPatternMatch {
	private final VariableAssignment variableAssignment;
	private final TopologyPattern pattern;

	public TopologyPatternMatch() {
		this.variableAssignment = new VariableAssignment();
		this.pattern = null;
	}

	public TopologyPatternMatch(final VariableAssignment partialMatch) {
		this.variableAssignment = new VariableAssignment(partialMatch);
		this.pattern = null;
	}

	/**
	 * Creates a match with an empty assignment
	 * 
	 * @deprecated Use the default constructor or the constructor that takes a
	 *             partial match
	 */
	@Deprecated
	public TopologyPatternMatch(final TopologyPattern pattern) {
		this.pattern = pattern;
		this.variableAssignment = new VariableAssignment();
	}

	/**
	 * Creates a match with a given variable assignment. The contents of the
	 * variable assignment are copied to the match.
	 * 
	 * @deprecated Use the default constructor or the constructor that takes a
	 *             partial match
	 */
	@Deprecated
	public TopologyPatternMatch(final TopologyPattern pattern, final VariableAssignment partialMatch) {
		this.pattern = pattern;
		this.variableAssignment = new VariableAssignment(partialMatch);
	}

	public VariableAssignment getVariableAssignment() {
		return variableAssignment;
	}

	/**
	 * @deprecated Will be removed in the future
	 */
	@Deprecated
	public TopologyPattern getPattern() {
		return pattern;
	}

	public static TopologyPatternMatch create(final TopologyPattern pattern,
			final VariableAssignment variableAssignment) {
		return new TopologyPatternMatch(pattern, variableAssignment);
	}

	@Override
	public String toString() {
		return "TopologyPatternMatch " + variableAssignment;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((variableAssignment == null) ? 0 : variableAssignment.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TopologyPatternMatch other = (TopologyPatternMatch) obj;
		if (variableAssignment == null) {
			if (other.variableAssignment != null)
				return false;
		} else if (!variableAssignment.equals(other.variableAssignment))
			return false;
		return true;
	}
}
