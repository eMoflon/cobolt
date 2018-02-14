package org.cobolt.model.utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cobolt.model.Edge;
import org.cobolt.model.EdgeState;
import org.cobolt.model.Node;
import org.cobolt.model.Topology;
import org.moflon.core.utilities.UtilityClassNotInstantiableException;

/**
 * Utility class for manipulating {@link Topology}'s
 *
 * @author Roland Kluge - Initial implementation
 *
 */
public class TopologyUtils {
	private TopologyUtils() {
		throw new UtilityClassNotInstantiableException();
	}

	public static Node addNode(final Topology topology, String id, double remainingEnergy)
	{
	   Node node = topology.addNode(id);
	   node.setEnergyLevel(remainingEnergy);
	   return node;
	}

	public static Edge addEdge(final Topology topology, String id, Node source, Node target, double distance, double requiredTransmissionPower, EdgeState state)
	{
	   Edge edge = topology.addDirectedEdge(id, source, target);
	   edge.setWeight(distance);
	   edge.setExpectedLifetime(edge.getSource().getEnergyLevel() / requiredTransmissionPower);
	   edge.setState(state);
	   return edge;
	}

	public static Edge addEdge(final Topology topology, String id, Node source, Node target, double distance, double requiredTransmissionPower)
	{
	   return addEdge(topology, id, source, target, distance, requiredTransmissionPower, EdgeState.UNCLASSIFIED);
	}

	public static Edge addUndirectedEdge(final Topology topology, String idFwd, String idBwd, Node node1, Node node2, double distance, double requiredTransmissionPower)
	{
	   final Edge fwdEdge = topology.addUndirectedEdge(idFwd, idBwd, node1, node2);
	   fwdEdge.setExpectedLifetime(fwdEdge.getSource().getEnergyLevel() / requiredTransmissionPower);
	   fwdEdge.setDistance(distance);

	   final Edge bwdEdge = fwdEdge.getReverseEdge();
      bwdEdge.setExpectedLifetime(bwdEdge.getSource().getEnergyLevel() / requiredTransmissionPower);
	   bwdEdge.setDistance(distance);

	   return fwdEdge;
	}

	/**
	 * Creates a summary of the edge states of the given {@link Topology}.
	 *
	 * For instance, this method can be used during debugging as follows:
	 *
	 * de.tudarmstadt.maki.tc.cbctc.model.utils.TopologyUtils.formatEdgeStateReport(unclassifiedEdge.getTopology())
	 *
	 * de.tudarmstadt.maki.tc.cbctc.model.utils.TopologyUtils.formatEdgeStateReport(topology)
	 *
	 * @param topology
	 * @return
	 */
	public static String formatEdgeStateReport(final Topology topology) {
      final StringBuilder builder = new StringBuilder();
      final Set<Edge> processedEdges = new HashSet<>();
      final List<Edge> edges = new ArrayList<>(topology.getEdges());
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

	/**
	 * Returns true if the topology contains at least one unclassified edge
	 *
	 * @param topology the topology to check
	 * @return whether the topology contains at least one unclassified edge
	 */
   public static boolean containsUnclassifiedEdges(Topology topology) {
      return topology.getEdges().stream().anyMatch(e -> e.getState() == EdgeState.UNCLASSIFIED);
   }
}
