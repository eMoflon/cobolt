package de.tudarmstadt.maki.simonstrator.tc.patternMatching;

/**
 * Exception that marks that the pattern matching failed
 *
 */
public class TopologyPatternMatchingException extends RuntimeException {

	public TopologyPatternMatchingException(final String message) {
		super(message);
	}

	private static final long serialVersionUID = -3056711604705990221L;

}
