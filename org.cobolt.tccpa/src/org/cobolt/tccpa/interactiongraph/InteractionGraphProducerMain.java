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
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.math3.util.Pair;
import org.graphstream.graph.Graph;

public class InteractionGraphProducerMain
{

   private final boolean isLongVersion = false;

   private final boolean showGraph = false;

   private final List<Predicate<InteractionSequence>> filterPredicates = Arrays.asList(//
         InteractionSequence::containsNoContextEventRecreationInteraction, //
         InteractionSequence::containsNoLocalInteractionsWithProgress
         );

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

         loopConditions.stream() //
               .filter(loopCondition -> containsConditionRule(graph, loopCondition))//
               .forEach(loopCondition -> {
                  final String loopRule = loopCondition.getConditionRuleName();
                  final LoopType loopType = loopCondition.getType();
                  final InteractionType continuationInteractionType = loopType.getContinuationInteractionType();
                  final List<InteractionSequence> interactionSequencesForLoop = interactionSequences.get(loopCondition);
                  InteractionGraphUtil.streamIncomingEdges(loopRule, graph)//
                        .filter(edge -> {
                           final Interaction interaction = InteractionGraphUtil.getEdgeData(edge);
                           return interaction.getType() == continuationInteractionType;
                        }).forEach(continuationInteractionEdge -> {
                           final Interaction continuationInteraction = InteractionGraphUtil.getEdgeData(continuationInteractionEdge);
                           InteractionGraphUtil.streamIncomingEdges(continuationInteraction.getLhsRule(), graph) //
                                 .filter(InteractionGraphUtil::isDependency).forEach(edge -> {
                                    final InteractionSequence interactionSequence = new InteractionSequence(continuationInteraction, loopCondition);
                                    interactionSequence.extend(InteractionGraphUtil.getEdgeData(edge));
                                    interactionSequencesForLoop.add(interactionSequence);
                                 });
                        });
                  Collections.sort(interactionSequencesForLoop);
               });

         final Map<LoopCondition, List<InteractionSequence>> filteredSequences = filterInteractionSequences(interactionSequences);

         printTextualSummary(interactionSequences, filteredSequences);
         System.out.println(LatexUtil.formatInteractionTable(interactionSequences, filteredSequences));

         if (this.showGraph)
            showGraph(graph);
      }

   }

   private void printTextualSummary(final Map<LoopCondition, List<InteractionSequence>> interactionSequences,
         final Map<LoopCondition, List<InteractionSequence>> filteredSequences)
   {
      final StringBuilder sb = new StringBuilder();
      sb.append("Detailed overview:\n");
      for (final LoopCondition loopCondition : filteredSequences.keySet())
      {

         final List<InteractionSequence> allSequences = interactionSequences.get(loopCondition);
         final List<InteractionSequence> sequences = filteredSequences.get(loopCondition);
         final String loopConditionRule = loopCondition.getConditionRuleName();
         final LoopType loopType = loopCondition.getType();

         sb.append(String.format("Loop: %s  %s", loopConditionRule, loopType));
         sb.append(String.format("[%d filtered seqs., %d seqs.]", sequences.size(), allSequences.size()));
         sb.append(":\n");

         sequences.forEach(sequence -> sb.append("\t").append(sequence.format()).append("\n"));
      }

      final int allSequencesCount = countInteractionSequences(interactionSequences);
      final int filteredSequenceCount = countInteractionSequences(filteredSequences);
      System.out.println(sb.toString());
      System.out.printf("Totals: %d filtered seqs., %d all sequences\n", filteredSequenceCount, allSequencesCount);
   }

   private Integer countInteractionSequences(final Map<LoopCondition, List<InteractionSequence>> interactionSequences)
   {
      return interactionSequences.entrySet().stream().map(entry -> entry.getValue().size()).reduce((a, b) -> a + b).orElse(-1);
   }

   private Map<LoopCondition, List<InteractionSequence>> filterInteractionSequences(final Map<LoopCondition, List<InteractionSequence>> interactionSequences)
   {
      final Map<LoopCondition, List<InteractionSequence>> filteredSequences = interactionSequences.entrySet().stream().map(entry -> {

         final LoopCondition loopCondition = entry.getKey();
         final List<InteractionSequence> originalInteractionSequences = entry.getValue();
         Stream<InteractionSequence> filterStream = originalInteractionSequences.stream();
         for (final Predicate<InteractionSequence> filterPredicate : filterPredicates)
         {
            filterStream = filterStream.filter(filterPredicate);
         }

         return new Pair<>(loopCondition, filterStream.collect(Collectors.toList()));
      }).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
      return filteredSequences;
   }

   /**
    * Returns true if the given interaction graph contains a rule node that matches the given {@link LoopCondition}'s rule name
    * @param interactionGraph the interaction graph
    * @param loopCondition the loop condition
    * @return whether some node in the graph has an ID equal to {@link LoopCondition#getConditionRuleName()}
    */
   private boolean containsConditionRule(final Graph interactionGraph, final LoopCondition loopCondition)
   {
      final String nodeId = loopCondition.getConditionRuleName();
      return InteractionGraphUtil.containsNode(interactionGraph, nodeId);
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

   private void showGraph(Graph graph)
   {
      graph.display(false);
   }
}
