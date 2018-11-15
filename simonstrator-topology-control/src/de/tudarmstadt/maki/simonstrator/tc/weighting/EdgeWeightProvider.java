package de.tudarmstadt.maki.simonstrator.tc.weighting;

import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;

/**
 * This is a generic interface to calculate the weight of {@link IEdge}s.
 */
public interface EdgeWeightProvider {

	/**
	 * Returns the (application-)specific weight of the given edge
	 */
	double calculateWeight(final IEdge edge, final Graph graph);
}
