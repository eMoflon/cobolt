package org.cobolt.tccpa.interactiongraph;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.util.Pair;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

public class InteractionGraphCsvReader
{
   private static final int NUM_EXPECTED_COLUMNS = 4;

   private boolean isLongVersion;

   /**
    *
    * @param isLongVersion true if all rules shall be read. False if only the short-version rules shall be read
    */
   public InteractionGraphCsvReader(boolean isLongVersion)
   {
      this.isLongVersion = isLongVersion;
   }

   public Graph readInteractionGraphFromCsv(String graphId, String filename) throws FileNotFoundException, IOException
   {
      final String inputFileContent = FileUtils.readFileToString(new File(filename));
      final String cleanedContent = inputFileContent.replaceAll("[#].*(\n|$)", "");
      Graph graph = initializeDefaultInteractionGraph(graphId);
      final CSVParser parser = new CSVParserBuilder().withSeparator(';').withIgnoreQuotations(true).build();
      final CSVReader reader = new CSVReaderBuilder(new StringReader(cleanedContent)).withCSVParser(parser).withSkipLines(1).build();
      final List<String[]> csvData = reader.readAll();
      int lineCounter = 0;
      for (final String[] csvLine : csvData)
      {
         ++lineCounter;
         try
         {
            if (csvLine.length < NUM_EXPECTED_COLUMNS)
               throw new IllegalArgumentException("CSV row '" + Arrays.toString(csvLine) + "' should have " + NUM_EXPECTED_COLUMNS + " columns.");

            final String lhsRule = csvLine[0];
            final String rhsRule = csvLine[1];
            final String interactionStr = csvLine[2];
            final String reason = csvLine[3];

            if (RuleNames.shallOmitRule(lhsRule, this.isLongVersion) || RuleNames.shallOmitRule(rhsRule, this.isLongVersion))
               continue;

            for (final String ruleName : Arrays.asList(lhsRule, rhsRule))
            {
               addNodeIfMissing(ruleName, graph);
            }

            final Interaction interaction = Interaction.create(lhsRule, rhsRule, interactionStr, reason);
            addInteraction(interaction, graph);
         } catch (final Exception ex)
         {
            throw new IllegalArgumentException(String.format("Problem while converting line %d: '%s'.", lineCounter, Arrays.toString(csvLine)), ex);
         }
      }
      return graph;
   }

   private void addNodeIfMissing(final String ruleName, Graph graph)
   {
      if (!InteractionGraphUtil.containsNode(graph, ruleName))
      {
         final Pair<Integer, Integer> ruleNodePosition = InteractionGraphLayout.getPosition(ruleName);
         addRuleNode(ruleName, ruleNodePosition.getFirst(), ruleNodePosition.getSecond(), graph);
      }
   }

   private Graph initializeDefaultInteractionGraph(String graphId)
   {
      Graph graph = new MultiGraph(graphId);
      graph.setNullAttributesAreErrors(true);
      InteractionGraphLayout.configureLayout(graph);

      return graph;
   }

   private void addInteraction(final Interaction interaction, final Graph graph)
   {
      int i = 1;
      final String basicEdgeId = interaction.getLhsRule() + "-" + interaction.formatTypeAndLocality() + "->" + interaction.getRhsRule();
      final String edgeId = InteractionGraphUtil.createUniqueEdgeId(graph, i, basicEdgeId);
      final Edge edge = graph.addEdge(edgeId, interaction.getLhsRule(), interaction.getRhsRule(), true);
      edge.setAttribute(InteractionGraphLayout.ELEMENT_ATTRIBUTE_UILABEL, edgeId);
      edge.setAttribute(InteractionGraphUtil.EDGE_ATTRIBUTE_INTERACTION, interaction);
   }

   private static void addRuleNode(final String id, final int x, final int y, Graph graph)
   {
      final Node node = graph.addNode(id);
      node.setAttribute(InteractionGraphLayout.ELEMENT_ATTRIBUTE_UILABEL, id);
      node.setAttribute(InteractionGraphLayout.NODE_ATTRIBUTE_XYZ, x, y, 0);
   }

}
