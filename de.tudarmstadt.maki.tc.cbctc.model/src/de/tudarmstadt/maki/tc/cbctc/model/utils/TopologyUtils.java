package de.tudarmstadt.maki.tc.cbctc.model.utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.tudarmstadt.maki.tc.cbctc.model.Edge;
import de.tudarmstadt.maki.tc.cbctc.model.EdgeState;
import de.tudarmstadt.maki.tc.cbctc.model.Topology;
import de.tudarmstadt.maki.tc.cbctc.model.Node;

public class TopologyUtils {
	private TopologyUtils() {
		throw new UnsupportedOperationException();
	}

	public static Node addNode(final Topology topology, String id, double remainingEnergy)
	{
	   Node node = topology.addNode(id);
	   node.setEnergyLevel(remainingEnergy);
	   return node;
	}
	
	public static Edge addEdge(final Topology topology, String id, Node source, Node target, double distance, double expectedRemainingLifetime, EdgeState state)
	{
	   Edge edge = topology.addDirectedEdge(id, source, target);
	   edge.setWeight(distance);
	   edge.setExpectedLifetime(expectedRemainingLifetime);
	   edge.setState(state);
	   return edge;
	}
	
	public static Edge addEdge(final Topology topology, String id, Node source, Node target, double distance, double expectedRemainingLifetime)
	{
	   return addEdge(topology, id, source, target, distance, expectedRemainingLifetime, EdgeState.UNCLASSIFIED);
	}
	
	public static Edge addUndirectedEdge(final Topology topology, String idFwd, String idBwd, Node node1, Node node2, double distance, double expectedRemainingLifetime)
	{
	   final Edge fwdEdge = addEdge(topology, idFwd, node1, node2, distance, expectedRemainingLifetime);
	   final Edge bwdEdge = addEdge(topology, idFwd, node1, node2, distance, expectedRemainingLifetime);
	   fwdEdge.setReverseEdge(bwdEdge);
	   return fwdEdge;
	}
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
