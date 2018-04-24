package org.cobolt.tccpa.interactiongraph;

import org.graphstream.graph.Graph;

public final class GraphUtil
{
   private GraphUtil() {
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


}
