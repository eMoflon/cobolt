package org.cobolt.algorithms.facade.preprocessing;

import org.cobolt.algorithms.impl.NodePreprocessorImpl;
import org.cobolt.model.Node;

/**
 * Null implementation of {@link NodePreprocessorImpl}
 * 
 * @author Roland Kluge - Initial implementation
 *
 */
public class NullNodePreprocessor extends NodePreprocessorImpl {
	@Override
	public void preprocess(Node node) {
		// Nop
	}
}
