package org.cobolt.tccpa.interactiongraph;

import static org.cobolt.tccpa.interactiongraph.RuleNames.R_FIND_U;
import static org.cobolt.tccpa.interactiongraph.RuleNames.R_MINUS_E;
import static org.cobolt.tccpa.interactiongraph.RuleNames.R_MINUS_EH1;
import static org.cobolt.tccpa.interactiongraph.RuleNames.R_MINUS_EH2;
import static org.cobolt.tccpa.interactiongraph.RuleNames.R_MOD_W;
import static org.cobolt.tccpa.interactiongraph.RuleNames.R_MOD_WH1;
import static org.cobolt.tccpa.interactiongraph.RuleNames.R_MOD_WH2;
import static org.cobolt.tccpa.interactiongraph.RuleNames.R_MOD_WH3;
import static org.cobolt.tccpa.interactiongraph.RuleNames.R_MOD_WH4;
import static org.cobolt.tccpa.interactiongraph.RuleNames.R_PLUS_E;
import static org.cobolt.tccpa.interactiongraph.RuleNames.R_PLUS_EH1;
import static org.cobolt.tccpa.interactiongraph.RuleNames.R_PLUS_EH2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

/*
 * TODO@rkluge: Class for interaction sequence
 * TODO@rkluge: Proper latex output (object to text not text to text)
 * TODO@rkluge: Convert interactions before refinement to CSV
 * TODO@rkluge: Automatically highlight non-local sequences in bold
 */
public class InteractionGraphProducerMain
{

   private final boolean isLongVersion = false;

   private final boolean showGraph = false;

   public static void main(String[] args) throws Exception
   {
      new InteractionGraphProducerMain().run();
   }

   private void run() throws IOException
   {
      final Graph graphBefore = calculateInteractionGraphBeforeRefinement();
      final Graph graphAfter = calculateInteractionGraphAfterRefinement();
      for (final Graph graph : Arrays.asList(graphBefore, graphAfter))
      {
         System.out.println("---");
         System.out.println("Graph: " + graph.getId());
         System.out.println("---");
         final List<LoopCondition> loopConditions = getLoopConditions();
         final Map<LoopCondition, List<InteractionSequence>> interactionSequences = new HashMap<>();
         loopConditions.forEach(loopCondition -> interactionSequences.put(loopCondition, new ArrayList<>()));

         loopConditions.stream().filter(loopCondition -> containsConditionRule(graph, loopCondition)).forEach(loopCondition -> {
            final String loopRule = loopCondition.getConditionRuleName();
            final LoopType loopType = loopCondition.getType();
            final InteractionType continuationInteractionType = loopType.getContinuationInteractionType();
            streamIncomingEdges(loopRule, graph).filter(edge -> {
               final Interaction interaction = getEdgeData(edge);
               return interaction.getType() == continuationInteractionType;
            }).forEach(continuationInteractionEdge -> {
               final Interaction continuationInteraction = getEdgeData(continuationInteractionEdge);
               streamIncomingEdges(continuationInteraction.getLhsRule(), graph).filter(InteractionGraphProducerMain::isDependency).forEach(edge -> {
                  final InteractionSequence interactionSequence = new InteractionSequence(continuationInteraction, loopCondition);
                  interactionSequence.extend(getEdgeData(edge));
                  interactionSequences.get(loopCondition).add(interactionSequence);
               });
            });

         });

         int sequenceCount = 0;
         int nonPurelyLocalSequenceCounth1 = 0;
         final StringBuilder sb = new StringBuilder();
         sb.append("Legend:\n");
         sb.append("** : Interaction sequence containing dr or cr.\n");
         sb.append("Detailed overview:\n");
         for (final LoopCondition loopCondition : loopConditions)
         {
            final List<InteractionSequence> sequences = interactionSequences.get(loopCondition);
            sequenceCount += sequences.size();
            Collections.sort(sequences);
            final List<InteractionSequence> nonPurelylocalSequences = sequences.stream().filter(sequence -> !sequence.isPurelyLocal())
                  .collect(Collectors.toList());
            nonPurelyLocalSequenceCounth1 += nonPurelylocalSequences.size();
            sb.append(String.format("Loop: %s  %s [%d seqs., %d seqs. with (d|c)r]:\n", loopCondition.getConditionRuleName(), loopCondition.getType(),
                  sequences.size(), nonPurelylocalSequences.size()));
            for (final InteractionSequence interactionSequence : sequences)
            {
               sb.append("\t");
               if (!interactionSequence.isPurelyLocal())
                  sb.append("**");
               else
                  sb.append("  ");
               sb.append(String.format("%s\n", interactionSequence.format()));
            }
         }
         System.out.println(sb.toString());
         System.out.printf("Totals: %d seqs., %d seqs. with (d|c)r\n", sequenceCount, nonPurelyLocalSequenceCounth1);

         if (this.showGraph)
            showGraph(graph);
      }
   }

   private Stream<Edge> streamIncomingEdges(final String nodeId, final Graph graph)
   {
      final Node node = graph.getNode(nodeId);
      return StreamSupport.stream(node.getEachEnteringEdge().spliterator(), false);
   }

   private boolean containsConditionRule(final Graph graph, LoopCondition loopCondition)
   {
      final String nodeId = loopCondition.getConditionRuleName();
      return GraphUtil.containsNode(graph, nodeId);
   }

   private static Interaction getEdgeData(Edge edge)
   {
      return edge.getAttribute(InteractionGraphCsvReader.EDGE_ATTRIBUTE_INTERACTION);
   }

   private static boolean isDependency(Edge edge)
   {
      return getEdgeData(edge).getType() == InteractionType.DEPENDENCY;
   }

   public Graph calculateInteractionGraphBeforeRefinement() throws IOException
   {
      return new InteractionGraphCsvReader(this.isLongVersion).readInteractionGraphFromCsv("InteractionGraphBeforeRefinement",
            "resources/InteractionGraphBeforeRefinement.csv");
   }

   public Graph calculateInteractionGraphAfterRefinement() throws IOException
   {
      return new InteractionGraphCsvReader(this.isLongVersion).readInteractionGraphFromCsv("InteractionGraphAfterRefinement",
            "resources/InteractionGraphAfterRefinement.csv");
   }

   public List<LoopCondition> getLoopConditions()
   {
      // @formatter:off
      final List<LoopCondition> loopConditions = Arrays.asList(
            new LoopCondition(R_FIND_U, LoopType.SUCCESS),
            new LoopCondition(R_MINUS_EH1, LoopType.SUCCESS),
            new LoopCondition(R_MINUS_EH2, LoopType.SUCCESS),
            new LoopCondition(R_MINUS_E, LoopType.FAILURE),
            new LoopCondition(R_PLUS_EH1, LoopType.SUCCESS),
            new LoopCondition(R_PLUS_EH2, LoopType.SUCCESS),
            new LoopCondition(R_PLUS_E, LoopType.FAILURE),
            new LoopCondition(R_MOD_WH1, LoopType.SUCCESS),
            new LoopCondition(R_MOD_WH2, LoopType.SUCCESS),
            new LoopCondition(R_MOD_WH3, LoopType.SUCCESS),
            new LoopCondition(R_MOD_WH4, LoopType.SUCCESS),
            new LoopCondition(R_MOD_W, LoopType.FAILURE));
      // @formatter:on
      final List<LoopCondition> filteredLoopConditions = loopConditions.stream()
            .filter(loopCondition -> !RuleNames.shallOmitRule(loopCondition.getConditionRuleName(), isLongVersion)).collect(Collectors.toList());
      return filteredLoopConditions;
   }

   public static String cleanCommas(Node node)
   {
      String nodeId = node.getId();
      return cleanCommasStr(nodeId);
   }

   private static String cleanCommasStr(String nodeId)
   {
      return nodeId.replaceAll(Pattern.quote(","), "");
   }

   public String getUiLabel(Edge edge)
   {
      return edge.getAttribute("ui.label").toString();
   }

   public void showGraph(Graph graph)
   {
      graph.display(false);
   }
}
