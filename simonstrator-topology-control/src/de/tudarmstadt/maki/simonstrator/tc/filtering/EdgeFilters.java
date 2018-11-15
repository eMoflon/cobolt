package de.tudarmstadt.maki.simonstrator.tc.filtering;

import java.util.Collection;

import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;

public class EdgeFilters {

	/**
	 * Returns the logical conjunction (AND) of the decisions of the given edge
	 * filters for the given edge.
	 * 
	 * This means that the result is true if and only if all of the edge filters
	 * return true for {@link EdgeFilter#ignoreEdge(IEdge)}
	 */
	public static boolean ignoreEdge(IEdge edge, Collection<EdgeFilter> filters) {
		if (filters.isEmpty())
			return false;

		for (final EdgeFilter filter : filters) {
			if (!filter.ignoreEdge(edge))
				return false;
		}
		return true;
	}

}
