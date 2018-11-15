package de.tudarmstadt.maki.simonstrator.tc.testing;

import org.junit.Assert;

import de.tudarmstadt.maki.simonstrator.api.component.topology.VariableAssignment;
import de.tudarmstadt.maki.simonstrator.api.util.UtilityClassNotInstantiableException;
import de.tudarmstadt.maki.simonstrator.tc.patternMatching.matching.TopologyPatternMatch;

/**
 * This utility class contains helper methods for testing pattern matching logic
 * @author Roland Kluge - Initial implementation
 *
 */
public final class PatternMatcherTestHelper {

   /**
    * Disabled constructor due to this class being a utility class
    */
	private PatternMatcherTestHelper() {
	    throw new UtilityClassNotInstantiableException();
	}

	/**
	 * Asserts that the given expected match is contained in the provided list of matches
	 * 
	 * Matches are compared by their {@link VariableAssignment}
	 * 
	 * @param matches the list of matches to check
	 * @param expectedMatch the match that should be contained in 'matches'
	 */
	public static void assertHasPatternMatch(final Iterable<TopologyPatternMatch> matches, final VariableAssignment expectedMatch) {
		for (final TopologyPatternMatch match : matches) {
			if (match.getVariableAssignment().equals(expectedMatch)) {
				return;
			}
		}
		Assert.fail("Match " + expectedMatch + " not found in " + matches);
	}
}
