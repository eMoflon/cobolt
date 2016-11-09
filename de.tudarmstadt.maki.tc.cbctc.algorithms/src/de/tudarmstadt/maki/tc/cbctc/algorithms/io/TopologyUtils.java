package de.tudarmstadt.maki.tc.cbctc.algorithms.io;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.tudarmstadt.maki.tc.cbctc.algorithms.Topology;
import de.tudarmstadt.maki.tc.cbctc.model.Edge;
import de.tudarmstadt.maki.tc.cbctc.model.EdgeState;

public class TopologyUtils {
	public static String formatEdgeStateReport(final Topology graph) {
		final StringBuilder builder = new StringBuilder();
		final Set<Edge> processedEdges = new HashSet<>();
		final List<Edge> edges = new ArrayList<>(graph.getEdges());
		edges.sort(new Comparator<Edge>() {
			@Override
			public int compare(Edge o1, Edge o2) {
				return o1.getId().compareTo(o2.getId());
			}

		});
		final Map<EdgeState, Integer> stateCounts = new HashMap<>();
		stateCounts.put(EdgeState.ACTIVE, 0);
		stateCounts.put(EdgeState.INACTIVE, 0);
		stateCounts.put(EdgeState.UNCLASSIFIED, 0);

		for (final Edge link : edges) {
			if (!processedEdges.contains(link)) {
				final Edge revLink = link.getReverseEdge();
				EdgeState linkState = link.getState();
				builder.append(String.format("%6s", link.getId()) + " : " + linkState.toString().substring(0, 1));
				processedEdges.add(link);
				stateCounts.put(linkState, stateCounts.get(linkState) + 1);

				if (revLink != null) {
					EdgeState revLinkState = revLink.getState();
					builder.append(" || " + String.format("%6s", revLink.getId()) + " : "
							+ revLinkState.toString().substring(0, 1));
					processedEdges.add(revLink);
					stateCounts.put(revLinkState, stateCounts.get(revLinkState) + 1);
				}

				builder.append("\n");

			}
		}

		builder.insert(0,
				String.format("#A : %d || #I : %d || #U : %d || Sum : %d\n", //
						stateCounts.get(EdgeState.ACTIVE), //
						stateCounts.get(EdgeState.INACTIVE), //
						stateCounts.get(EdgeState.UNCLASSIFIED), //
						stateCounts.get(EdgeState.ACTIVE) + stateCounts.get(EdgeState.INACTIVE)
								+ stateCounts.get(EdgeState.UNCLASSIFIED)//
				));

		return builder.toString().trim();

	}

	public static boolean containsUnclassifiedEdges(Topology graph) {
		return graph.getEdges().stream().anyMatch(e -> e.getState() == EdgeState.UNCLASSIFIED);
	}
}
