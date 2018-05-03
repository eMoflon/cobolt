package org.cobolt.tccpa.stabilizationanalysis;

import static org.cobolt.tccpa.stabilizationanalysis.RuleNames.R_FIND_U;
import static org.cobolt.tccpa.stabilizationanalysis.RuleNames.R_MINUS_E;
import static org.cobolt.tccpa.stabilizationanalysis.RuleNames.R_MINUS_EH1;
import static org.cobolt.tccpa.stabilizationanalysis.RuleNames.R_MINUS_EH2;
import static org.cobolt.tccpa.stabilizationanalysis.RuleNames.R_MOD_W;
import static org.cobolt.tccpa.stabilizationanalysis.RuleNames.R_MOD_WH1;
import static org.cobolt.tccpa.stabilizationanalysis.RuleNames.R_MOD_WH2;
import static org.cobolt.tccpa.stabilizationanalysis.RuleNames.R_MOD_WH3;
import static org.cobolt.tccpa.stabilizationanalysis.RuleNames.R_MOD_WH4;
import static org.cobolt.tccpa.stabilizationanalysis.RuleNames.R_PLUS_E;
import static org.cobolt.tccpa.stabilizationanalysis.RuleNames.R_PLUS_EH1;
import static org.cobolt.tccpa.stabilizationanalysis.RuleNames.R_PLUS_EH2;

import java.io.FileNotFoundException;
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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.math3.util.Pair;
import org.graphstream.graph.Graph;

/**
 * Executor class for running the automated part of the stabilization analysis
 *
 * @author Roland Kluge - Initial implementation
 *
 */
public class StabilizationAnalysisMain
{

   private final boolean isLongVersion = false;

   private final boolean shallPrintLatexTable = false;

   private final boolean showGraph = false;

   private final List<Predicate<InteractionSequence>> filterPredicates = Arrays.asList(//
         InteractionSequence::containsNoContextEventRecreationInteraction, //
         InteractionSequence::containsNoLocalInteractionsWithProgress);

   /**
    * Executes the automated part of the stabilization analysis for the given interaction lists
    *
    * @param args the list of CSV files containing interaction lists
    */
   public static void main(final String[] args) throws Exception
   {
      final List<String> filenames = Arrays.asList(args);
      new StabilizationAnalysisMain().runForAllFiles(filenames);
   }

   private void runForAllFiles(List<String> filenames) throws IOException
   {
      for (final String filename : filenames)
      {
         runForFile(filename);
      }
   }

   private void runForFile(final String filename) throws IOException
   {
      final Graph graph = readInteractionGraph(filename);
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
         if (this.shallPrintLatexTable)
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

         sb.append(String.format("Loop: %s  %s ", loopConditionRule, loopType));
         sb.append(String.format("[%d filtered seqs., %d seqs.]", sequences.size(), allSequences.size()));
         sb.append(":\n");

         sequences.forEach(sequence -> sb.append("  ").append(sequence.format()).append("\n"));
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

   private Graph readInteractionGraph(final String filename) throws FileNotFoundException, IOException
   {
      final String graphName = FilenameUtils.removeExtension(FilenameUtils.getBaseName(filename));
      return new InteractionGraphCsvReader(this.isLongVersion).readInteractionGraphFromCsv(graphName, filename);
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
