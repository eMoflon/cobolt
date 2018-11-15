package de.tudarmstadt.maki.simonstrator.tc;

import java.util.HashMap;
import java.util.Map;

import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.util.UtilityClassNotInstantiableException;
import de.tudarmstadt.maki.simonstrator.tc.underlay.EdgeState;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyProperties;

public final class TopologyControlUtils {

	private TopologyControlUtils() {
	    throw new UtilityClassNotInstantiableException();
	}

	public static Map<EdgeState, Integer> calculateStateHistorgram(final Graph graph) {
		final Map<EdgeState, Integer> stateCounts = new HashMap<>();
		stateCounts.put(EdgeState.ACTIVE, 0);
		stateCounts.put(EdgeState.INACTIVE, 0);
		stateCounts.put(EdgeState.UNCLASSIFIED, 0);
		for (final IEdge graphEdge : graph.getEdges()) {
			stateCounts.put(graphEdge.getProperty(UnderlayTopologyProperties.EDGE_STATE),
					stateCounts.get(graphEdge.getProperty(UnderlayTopologyProperties.EDGE_STATE)) + 1);
		}
		return stateCounts;
	}

	public static int getActiveLinkCount(final Graph graph) {
		return getCountByState(graph, EdgeState.ACTIVE);
	}

	public static int getInactiveLinkCount(final Graph graph) {
		return getCountByState(graph, EdgeState.INACTIVE);
	}

	public static int getUnclassifiedLinkCount(final Graph graph) {
		return getCountByState(graph, EdgeState.UNCLASSIFIED);
	}

	private static int getCountByState(final Graph graph, final EdgeState state) {
		int countByState = 0;
		for (final IEdge edge : graph.getEdges()) {
			if (edge.getProperty(UnderlayTopologyProperties.EDGE_STATE) == state)
				++countByState;
		}
		return countByState;
	}

   /**
    * This method returns true if (i) the old value is null or 0.0 or (ii) the
    * absolute difference of old and new value normalized by the old value
    * exceeds the given threshold
    *
    * @param oldValue
    *            the original value
    * @param newValue
    *            the new value
    * @param threshold
    */
   public static <T extends Number> boolean isRelativeDifferenceLargerThanThreshold(final T oldValue,
   		final T newValue,
   		final double threshold) {
   	return oldValue == null //
   			|| Double.isNaN(oldValue.doubleValue()) //
   			|| oldValue.doubleValue() == 0.0 //
   			|| Math.abs(oldValue.doubleValue() - newValue.doubleValue()) / oldValue.doubleValue() > threshold;
   }
}
