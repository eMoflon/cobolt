package org.cobolt.tccpa.interactiongraph;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

public final class InteractionGraphUtil
{
   static final String EDGE_ATTRIBUTE_INTERACTION = "interaction";

   private InteractionGraphUtil() {
      throw new UnsupportedOperationException("Utility class");
   }

   static boolean containsNode(final Graph graph, final String nodeId)
   {
      return graph.getNode(nodeId) != null;
   }

   static boolean containsEdge(final Graph graph, final String edgeId)
   {
      return graph.getEdge(edgeId) != null;
   }

   public static String createUniqueEdgeId(final Graph graph, int i, final String basicEdgeId)
   {
      String edgeId = basicEdgeId;
      while (containsEdge(graph, edgeId))
      {
         edgeId = basicEdgeId + "(" + i + ")";
         ++i;
      }
      return edgeId;
   }

   /**
    * Returns the interaction stored as data of the given {@link Edge}
    * @param edge the edge
    * @return the stored {@link Interaction}
    * @see InteractionGraphUtil#EDGE_ATTRIBUTE_INTERACTION
    */
   static Interaction getEdgeData(final Edge edge)
   {
      return edge.getAttribute(InteractionGraphUtil.EDGE_ATTRIBUTE_INTERACTION);
   }

   /**
    * Returns true if the stored interaction is a dependency
    * @param edge the edge to check
    */
   static boolean isDependency(final Edge edge)
   {
      return getEdgeData(edge).getType() == InteractionType.DEPENDENCY;
   }

   /**
    * Returns a {@link Stream} of the incoming edges of the {@link Node} with the given ID in the given {@link Graph}
    */
   public static Stream<Edge> streamIncomingEdges(final String nodeId, final Graph graph)
   {
      final Node node = graph.getNode(nodeId);
      return StreamSupport.stream(node.getEachEnteringEdge().spliterator(), false);
   }


}
