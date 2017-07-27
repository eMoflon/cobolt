package de.tudarmstadt.maki.tc.cbctc.algorithms.io;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.tudarmstadt.maki.simonstrator.tc.io.GraphTConstants;
import de.tudarmstadt.maki.tc.cbctc.model.Edge;
import de.tudarmstadt.maki.tc.cbctc.model.Node;
import de.tudarmstadt.maki.tc.cbctc.model.Topology;

public class TopologyModelGraphTWriter
{
   private static final int DEFAULT_PRECISION_FOR_FLOATS = 3;

   private int precisionForFloats;

   public TopologyModelGraphTWriter()
   {
      this.precisionForFloats = DEFAULT_PRECISION_FOR_FLOATS;
   }

   public String writeToString(final Topology topology)
   {
      final StringBuilder sb = new StringBuilder();
      sb.append(topology.getNodeCount()).append(" ").append(topology.getEdgeCount()).append(GraphTConstants.NL);
      final List<Node> nodes = new ArrayList<>(topology.getNodes());
      final NodeComparator nodeComparator = new NodeComparator();
      Collections.sort(nodes, nodeComparator);
      nodes.forEach(node -> {
         sb.append(formatNode(node)).append(GraphTConstants.NL);
      });
      final List<Edge> edges = new ArrayList<>(topology.getEdges());
      Collections.sort(edges, new EdgeComparator(nodeComparator));
      edges.forEach(edge -> {
         sb.append(formatEdge(edge)).append(GraphTConstants.NL);
      });
      return sb.toString();
   }

   private String formatNode(Node node)
   {
      final StringBuilder sb = new StringBuilder();
      sb.append(node.getId()).append(" ");
      sb.append("E").append("=").append(formatDouble(node.getEnergyLevel())).append(" ");
      sb.append("h").append("=").append(node.getHopCount()).append(" ");
      sb.append("x").append("=").append(formatDouble(node.getX())).append(" ");
      sb.append("y").append("=").append(formatDouble(node.getY())).append(" ");
      return sb.toString();
   }

   private String formatEdge(Edge edge)
   {
      final StringBuilder sb = new StringBuilder();
      sb.append(edge.getId()).append(" ");
      sb.append(edge.getSource().getId()).append(" ");
      sb.append(edge.getTarget().getId()).append(" ");
      sb.append("s").append("=").append(getStateAsChar(edge)).append(" ");
      sb.append("w").append("=").append(formatDouble(edge.getWeight())).append(" ");
      sb.append("a").append("=").append(formatDouble(edge.getAngle())).append(" ");
      sb.append("d").append("=").append(formatDouble(edge.getDistance())).append(" ");
      sb.append("R").append("=");
      final Edge reverseEdge = edge.getReverseEdge();
      if (reverseEdge != null)
      {
         sb.append(reverseEdge.getId());
      } else
      {
         sb.append("null");
      }
      sb.append(" ");
      return sb.toString();
   }

   private String formatDouble(final double value)
   {
      return String.format("%." + this.precisionForFloats + "f", value);
   }

   private char getStateAsChar(Edge edge)
   {
      return edge.getState().toString().charAt(0);
   }

   private final class EdgeComparator implements Comparator<Edge>
   {
      private final NodeComparator nodeComparator;

      private EdgeComparator(NodeComparator nodeComparator)
      {
         this.nodeComparator = nodeComparator;
      }

      @Override
      public int compare(Edge o1, Edge o2)
      {
         final int sourceNodeComparison = nodeComparator.compare(o1.getSource(), o2.getSource());
         if (sourceNodeComparison == 0)
         {
            return nodeComparator.compare(o1.getTarget(), o2.getTarget());
         } else
         {
            return sourceNodeComparison;
         }
      }
   }

   private final class NodeComparator implements Comparator<Node>
   {
      @Override
      public int compare(Node o1, Node o2)
      {
         return o1.getId().compareTo(o2.getId());
      }
   }
}
