package org.cobolt.algorithms.facade.preprocessing;

import org.cobolt.algorithms.EnergyAwareKTC;
import org.cobolt.algorithms.impl.NodePreprocessorImpl;
import org.cobolt.model.Edge;
import org.cobolt.model.Node;

/**
 * This preprocessor uses a standard, algorithm-specific sorting order for
 * edges.
 * 
 * For all algorithms (apart from {@link EnergyAwareKTC}), the preprocessor
 * sorts all outgoing edges of a node by increasing weight. In case of
 * {@link EnergyAwareKTC}, this preprocessor sorts the outgoing edges of a node
 * by decreasing expected lifetime.
 * 
 * The sort order can be inverted by setting {@link #shallReverseOrder} to
 * false.
 * 
 * @author Roland Kluge - Initial implementation
 *
 */
public class DefaultEdgeOrderNodePreprocessor extends NodePreprocessorImpl {
	@Override
	public void preprocess(Node node) {
		final long ticMillis = System.currentTimeMillis();
		org.eclipse.emf.common.util.ECollections.sort(node.getOutgoingEdges(), new java.util.Comparator<Edge>() {
			@Override
			public int compare(final Edge o1, final Edge o2) {
				final int signum = isShallReverseEdgeOrder() ? -1 : 1;
				if (getAlgorithm() instanceof EnergyAwareKTC) {
					return signum * -Double.compare(o1.getExpectedLifetime(), o2.getExpectedLifetime());
				} else {
					return signum * Double.compare(o1.getWeight(), o2.getWeight());
				}
			}

		});
		final long tocMillis = System.currentTimeMillis();
		final long durationInMillis = tocMillis - ticMillis;
		de.tudarmstadt.maki.simonstrator.api.Monitor.log(getClass(),
				de.tudarmstadt.maki.simonstrator.api.Monitor.Level.DEBUG,
				"Sorting during preprocessing of unclassified link identification took %dms", durationInMillis);
	}
}
