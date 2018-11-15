package de.tudarmstadt.maki.simonstrator.tc.filtering;

import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;

/**
 * Implementing classes of this interface represent a predicate to filter the
 * edges in a topology
 */
public interface EdgeFilter {
	/**
	 * Returns true if the given edge should be retained, i.e., not be filtered
	 */
	boolean ignoreEdge(final IEdge edge);
}
