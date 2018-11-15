package de.tudarmstadt.maki.simonstrator.tc.io;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

import de.tudarmstadt.maki.simonstrator.api.Graphs;
import de.tudarmstadt.maki.simonstrator.api.common.graph.EdgeID;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IElement;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INode;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Node;
import de.tudarmstadt.maki.simonstrator.api.component.sis.type.SiSType;
import de.tudarmstadt.maki.simonstrator.tc.facade.ITopologyControlFacade;
import de.tudarmstadt.maki.simonstrator.tc.underlay.EdgeState;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyProperties;

/**
 * A {@link GraphTReader} that reads into a
 * @author Roland Kluge - Initial implementation
 *
 */
public class FacadeGraphTReader extends GraphTReader
{
   private final HashMap<String, SiSType<?>> simAttributeMapping = new HashMap<>();

   public FacadeGraphTReader()
   {
      super();
      this.initializeSimAttributeIdentifierMapping();
   }

   private void initializeSimAttributeIdentifierMapping()
   {
      simAttributeMapping.put("a", UnderlayTopologyProperties.ANGLE);
      simAttributeMapping.put("d", UnderlayTopologyProperties.DISTANCE);
      simAttributeMapping.put("E", UnderlayTopologyProperties.REMAINING_ENERGY);
      simAttributeMapping.put("h", UnderlayTopologyProperties.HOP_COUNT);
      simAttributeMapping.put("L", UnderlayTopologyProperties.EXPECTED_LIFETIME_PER_EDGE);
      simAttributeMapping.put("P", UnderlayTopologyProperties.REQUIRED_TRANSMISSION_POWER);
      //simAttributeMapping.put("R", ModelPackage.eINSTANCE.getEdge_ReverseEdge());
      simAttributeMapping.put("s", UnderlayTopologyProperties.EDGE_STATE);
      simAttributeMapping.put("w", UnderlayTopologyProperties.WEIGHT);
      simAttributeMapping.put("x", UnderlayTopologyProperties.LONGITUDE);
      simAttributeMapping.put("y", UnderlayTopologyProperties.LATITUDE);
   }

   public void read(final ITopologyControlFacade facade, final FileInputStream stream)
   {
      Scanner scanner = null;

      try
      {
         scanner = new Scanner(stream);
         String nmLine = scanner.nextLine();
         while (!isDataLine(nmLine))
         {
            if (isMagicComment(nmLine))
            {
               handleMagicComment(nmLine);
            }
            nmLine = scanner.nextLine();
         }
         final int n = Integer.parseInt(nmLine.split("\\s+")[0]);
         final int m = Integer.parseInt(nmLine.split("\\s+")[1]);
         final Map<String, INode> idToNode = new HashMap<>();
         int readNodeLines = 0;
         while (readNodeLines < n)
         {
            final String line = scanner.nextLine();
            if (isDataLine(line))
            {
               final String[] splitLine = line.split("\\s+");
               final String nodeId = splitLine[0];
               Node prototype = Graphs.createNode(nodeId);
               for (int column = 1; column < splitLine.length; ++column)
               {
                  parseAttributeSpecification(prototype, splitLine[column], facade);
               }

               final INode node = facade.addNode(prototype);
               idToNode.put(nodeId, node);
               readNodeLines++;
            } else if (isMagicComment(line))
            {
               handleMagicComment(line);
            }
         }
         int readEdgeLines = 0;
         IEdge previousEdge = null;
         while (readEdgeLines < m)
         {
            final String line = scanner.nextLine();
            if (isDataLine(line))
            {
               final String[] splitLine = line.split("\\s+");
               if (splitLine.length < 3)
                  throw new IllegalArgumentException(String.format("Cannot parse edge specification from line '%s'", line));
               final String edgeId = splitLine[0];
               final String sourceId = splitLine[1];
               final String targetId = splitLine[2];
               if (!facade.getGraph().containsNode(INodeID.get(sourceId)))
                  throw new IllegalArgumentException(String.format("No node with ID '%s' exists.", sourceId));

               if (!facade.getGraph().containsNode(INodeID.get(targetId)))
                  throw new IllegalArgumentException(String.format("No node with ID '%s' exists.", targetId));

               final IEdge prototype = Graphs.createDirectedEdge(EdgeID.get(edgeId), INodeID.get(sourceId), INodeID.get(targetId));
               for (int column = 3; column < splitLine.length; ++column)
               {
                  parseAttributeSpecification(prototype, splitLine[column], facade);
               }
               facade.addEdge(prototype);

               readEdgeLines++;

               if (this.isAutoReverseLinkingActive && previousEdge != null)
               {
                  
                  facade.connectOppositeEdges(prototype, previousEdge);
                  previousEdge = null;
               } else
               {
                  previousEdge = prototype;
               }
            } else if (isMagicComment(line))
            {
               handleMagicComment(line);
            }
         }
         while(scanner.hasNextLine())
         {
            final String line = scanner.nextLine();
            if (isDataLine(line))
            {
               throw new IllegalStateException(String.format("Line '%s' appears to be a data line but it is outside the specified range", line));
            }
         }
         

      } catch (final Exception e)
      {
         throw new IllegalArgumentException(e);
      } finally
      {
         IOUtils.closeQuietly(scanner);
         configureDefaults();
      }
   }

   private void parseAttributeSpecification(final IElement topologyElement, final String attributeSpec, ITopologyControlFacade facade)
   {
      final String[] attributeEntry = attributeSpec.split(Pattern.quote("="));
      if (attributeEntry.length != 2)
         throw new IllegalArgumentException(String.format("Invalid attribute specification: '%s'", attributeSpec));

      final String attributeIdentifier = attributeEntry[0];
      final SiSType<?> property = simAttributeMapping.get(attributeIdentifier);
      if (property == null)
         throw new IllegalArgumentException("Invalid attribute identifier: " + attributeIdentifier);

      final String attributeValueString = attributeEntry[1];
      convertToObject(attributeValueString, attributeIdentifier, facade, topologyElement, property);
   }

   @SuppressWarnings("unchecked")
   private void convertToObject(String attributeValue, String attributeIdentifier, ITopologyControlFacade facade, IElement topologyElement, SiSType<?> property)
   {
      switch (attributeIdentifier)
      {
      case "a":
      case "d":
      case "E":
      case "L":
      case "P":
      case "w":
      case "x":
      case "y":
         assignProperty(topologyElement, (SiSType<Double>)property, Double.parseDouble(attributeValue));
         return;
      case "h":
         assignProperty(topologyElement, (SiSType<Integer>)property, Integer.parseInt(attributeValue));
         return;
      case "s":
         assignProperty(topologyElement, (SiSType<EdgeState>)property, parseEdgeState(attributeValue));
         return;
      default:
         throw new IllegalArgumentException("Unsupported attribute identifier: " + attributeIdentifier);
      }
   }

   private <T> void  assignProperty(IElement topologyElement, SiSType<T> property, T value)
   {
      topologyElement.setProperty(property, value);
   }

   private EdgeState parseEdgeState(String stateIdentifier)
   {
      switch (stateIdentifier)
      {
      case "A":
         return EdgeState.ACTIVE;
      case "I":
         return EdgeState.INACTIVE;
      case "U":
         return EdgeState.UNCLASSIFIED;
      default:
         throw new IllegalArgumentException("Unsupported state identifier: " + stateIdentifier);
      }
   }
}
