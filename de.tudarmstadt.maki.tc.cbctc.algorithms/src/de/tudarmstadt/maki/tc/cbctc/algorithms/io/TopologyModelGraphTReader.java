package de.tudarmstadt.maki.tc.cbctc.algorithms.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.eclipse.emf.ecore.EStructuralFeature;

import de.tudarmstadt.maki.simonstrator.tc.io.GraphTReader;
import de.tudarmstadt.maki.tc.cbctc.model.Edge;
import de.tudarmstadt.maki.tc.cbctc.model.EdgeState;
import de.tudarmstadt.maki.tc.cbctc.model.ModelPackage;
import de.tudarmstadt.maki.tc.cbctc.model.Node;
import de.tudarmstadt.maki.tc.cbctc.model.Topology;
import de.tudarmstadt.maki.tc.cbctc.model.TopologyElement;

public class TopologyModelGraphTReader extends GraphTReader
{

   private final HashMap<String, EStructuralFeature> modelAttributeMapping = new HashMap<>();

   public TopologyModelGraphTReader()
   {
      super();
      initializeAttributeIdentifierMapping();
   }

   /**
    * Reads a GraphT file from the given file into the given topology
    *  
    * @param topology the {@link Topology} to fill
    * @param filename the file name to read from
    */
   public void read(final Topology topology, final String filename) throws FileNotFoundException
   {
      this.read(topology, new File(filename));
   }

   /**
    * Reads a GraphT file from the given file into the given topology
    *  
    * @param topology the {@link Topology} to fill
    * @param inputFile the file to read from
    */
   public void read(final Topology topology, final File inputFile) throws FileNotFoundException
   {
      this.read(topology, new FileInputStream(inputFile));
   }

   /**
    * Reads a GraphT file from the given stream into the given topology
    *  
    * @param topology the {@link Topology} to fill
    * @param stream the stream to read from
    */
   public void read(final Topology topology, final InputStream stream)
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
         final Map<String, Node> idToNode = new HashMap<>();
         int readNodeLines = 0;
         while (readNodeLines < n)
         {
            final String line = scanner.nextLine();
            if (isDataLine(line))
            {
               final String[] splitLine = line.split("\\s+");
               final String nodeId = splitLine[0];
               final Node node = topology.addNode(nodeId);
               for (int column = 1; column < splitLine.length; ++column)
               {
                  parseAttributeSpecification(node, splitLine[column], topology);
               }

               idToNode.put(nodeId, node);
               readNodeLines++;
            } else if (isMagicComment(line))
            {
               handleMagicComment(line);
            }
         }
         int readEdgeLines = 0;
         Edge previousEdge = null;
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
               final Node sourceNode = topology.getNodeById(sourceId);
               if (sourceNode == null)
                  throw new IllegalArgumentException(String.format("No node with ID '%s' exists.", sourceId));

               final Node targetNode = topology.getNodeById(targetId);
               if (targetNode == null)
                  throw new IllegalArgumentException(String.format("No node with ID '%s' exists.", targetId));

               final Edge edge = topology.addDirectedEdge(edgeId, sourceNode, targetNode);
               for (int column = 3; column < splitLine.length; ++column)
               {
                  parseAttributeSpecification(edge, splitLine[column], topology);
               }

               readEdgeLines++;

               if (this.isAutoReverseLinkingActive && previousEdge != null)
               {
                  edge.setReverseEdge(previousEdge);
                  previousEdge.setReverseEdge(edge);
                  previousEdge = null;
               }
               else {
                  previousEdge = edge;
               }
            } else if (isMagicComment(line))
            {
               handleMagicComment(line);
            }
         }

      } catch (final Exception e)
      {
         throw new IllegalArgumentException(e);
      } finally
      {
         scanner.close();
         configureDefaults();
      }
   }

   private void parseAttributeSpecification(final TopologyElement topologyElement, final String attributeSpec, Topology topology)
   {
      final String[] attributeEntry = attributeSpec.split(Pattern.quote("="));
      if (attributeEntry.length != 2)
         throw new IllegalArgumentException(String.format("Invalid attribute specification: '%s'", attributeSpec));

      final String attributeIdentifier = attributeEntry[0];
      final EStructuralFeature eAttribute = modelAttributeMapping.get(attributeIdentifier);
      if (eAttribute == null)
         throw new IllegalArgumentException("Invalid attribute identifier: " + attributeIdentifier);

      final String attributeValueString = attributeEntry[1];
      final Object attributeValue = convertToObject(attributeValueString, attributeIdentifier, topology, topologyElement);
      topologyElement.eSet(eAttribute, attributeValue);
      if (eAttribute == ModelPackage.eINSTANCE.getEdge_ReverseEdge())
      {
         ((Edge) attributeValue).setReverseEdge((Edge) topologyElement);
      }
   }

   private void initializeAttributeIdentifierMapping()
   {
      modelAttributeMapping.put("a", ModelPackage.eINSTANCE.getEdge_Angle());
      modelAttributeMapping.put("d", ModelPackage.eINSTANCE.getEdge_Distance());
      modelAttributeMapping.put("E", ModelPackage.eINSTANCE.getNode_EnergyLevel());
      modelAttributeMapping.put("h", ModelPackage.eINSTANCE.getNode_HopCount());
      modelAttributeMapping.put("L", ModelPackage.eINSTANCE.getEdge_ExpectedLifetime());
      modelAttributeMapping.put("P", ModelPackage.eINSTANCE.getEdge_TransmissionPower());
      modelAttributeMapping.put("R", ModelPackage.eINSTANCE.getEdge_ReverseEdge());
      modelAttributeMapping.put("s", ModelPackage.eINSTANCE.getEdge_State());
      modelAttributeMapping.put("w", ModelPackage.eINSTANCE.getEdge_Weight());
      modelAttributeMapping.put("x", ModelPackage.eINSTANCE.getNode_X());
      modelAttributeMapping.put("y", ModelPackage.eINSTANCE.getNode_Y());

   }


   private Object convertToObject(String attributeValue, String attributeIdentifier, Topology topology, TopologyElement topologyElement)
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
         return Double.parseDouble(attributeValue);
      case "h":
         return Integer.parseInt(attributeValue);
      case "s":
         return parseEdgeState(attributeValue);
      case "R":

         if (attributeValue.equals(topologyElement.getId()))
            throw new IllegalArgumentException(
                  String.format("Reverse link ID '%s' must be distinct from the ID of the current topology element", attributeValue));

         final Edge reverseEdge = topology.getEdgeById(attributeValue);
         if (reverseEdge == null)
            throw new IllegalArgumentException(String.format("No edge with ID '%s' is known (yet)", attributeValue));
         return reverseEdge;
      default:
         throw new IllegalArgumentException("Unsupported attribute identifier: " + attributeIdentifier);
      }
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
