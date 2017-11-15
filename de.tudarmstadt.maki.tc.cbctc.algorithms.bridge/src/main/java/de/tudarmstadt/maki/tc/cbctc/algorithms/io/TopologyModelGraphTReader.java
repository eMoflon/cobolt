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
import de.tudarmstadt.maki.tc.cbctc.model.ModelPackage;
import de.tudarmstadt.maki.tc.cbctc.model.Node;
import de.tudarmstadt.maki.tc.cbctc.model.Topology;
import de.tudarmstadt.maki.tc.cbctc.model.TopologyElement;

public class TopologyModelGraphTReader extends GraphTReader
{

   private final Map<String, EStructuralFeature> modelAttributeMapping;

   public TopologyModelGraphTReader()
   {
      super();
      this.modelAttributeMapping = TopologyModelGraphTIO.initializeAttributeIdentifierMapping();
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
      final Object attributeValue = TopologyModelGraphTIO.convertToObject(attributeValueString, attributeIdentifier, topology, topologyElement);
      topologyElement.eSet(eAttribute, attributeValue);
      if (eAttribute == ModelPackage.eINSTANCE.getEdge_ReverseEdge())
      {
         ((Edge) attributeValue).setReverseEdge((Edge) topologyElement);
      }
   }
}
