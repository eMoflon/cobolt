package org.cobolt.tccpa.interactiongraph;

import static org.cobolt.tccpa.interactiongraph.RuleNames.R_A;
import static org.cobolt.tccpa.interactiongraph.RuleNames.R_FIND_U;
import static org.cobolt.tccpa.interactiongraph.RuleNames.R_I;
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
import static org.cobolt.tccpa.interactiongraph.RuleNames.R_UNLOCK;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Spliterator;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import org.apache.commons.math3.stat.Frequency;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import de.normalisiert.utils.graphs.ElementaryCyclesSearch;

/*
 * TODO@rkluge: Class for interaction sequence
 * TODO@rkluge: Proper latex output (object to text not text to text)
 * TODO@rkluge: Convert interactions before refinement to CSV
 * TODO@rkluge: Automatically highlight non-local sequences in bold
 */
public class InteractionGraphProducerMain
{


   private final boolean usePaperRulesOnly = true;

   private final boolean ignoreLoops = true;

   private static final String LATEX_BACKSLASH = "\\\\\\";

   private static final String LATEX_LINEEND = LATEX_BACKSLASH + "\\\\";
   private static final String LATEX_GTR_PREFIX = LATEX_BACKSLASH + "gtr";
   private static final Map<String, String> TO_LATEX = new HashMap<>();

   static {
      TO_LATEX.put(R_I, "I");
      TO_LATEX.put(R_A, "A");
      TO_LATEX.put(R_UNLOCK, "Unlock");
      TO_LATEX.put(R_MOD_WH4, "HandleLinkWeightModificationSelfI");
      TO_LATEX.put(R_MOD_WH3, "HandleLinkWeightModificationRemoteI");
      TO_LATEX.put(R_MOD_WH2, "HandleLinkWeightModificationSelfA");
      TO_LATEX.put(R_MOD_WH1, "HandleLinkWeightModificationRemoteA");
      TO_LATEX.put(R_MOD_W, "LinkWeightModification");
      TO_LATEX.put(R_PLUS_EH2, "HandleLinkAdditionRemote");
      TO_LATEX.put(R_PLUS_EH1, "HandleLinkAdditionSelf");
      TO_LATEX.put(R_PLUS_E, "LinkAddtion");
      TO_LATEX.put(R_MINUS_EH2, "HandleLinkRemovalRemote");
      TO_LATEX.put(R_MINUS_EH1, "HandleLinkRemovalSelf");
      TO_LATEX.put(R_MINUS_E, "LinkRemoval");
      TO_LATEX.put(R_FIND_U, "FindU");
      TO_LATEX.keySet().forEach(key -> TO_LATEX.put(key, LATEX_GTR_PREFIX + TO_LATEX.get(key)));
   }

   public static void main(String[] args)
   {
      new InteractionGraphProducerMain().run();
   }

   private void run()
   {
      final Graph graphBefore = calculateInteractionGraphBeforeRefinement();
      final Graph graphAfter = calculateInteractionGraphAfterRefinement();
      for (final Graph graph : Arrays.asList(graphBefore, graphAfter))
      {
         System.out.println("---");
         System.out.println("--- Graph: " + graph.getId());
         System.out.println("---");
         final List<LoopCondition> loopConditions = getLoopConditions();

         StringBuilder sb = new StringBuilder();
         sb.append("Rule;Type;RemoteInteractions;InteractionTriple").append("\n");
         for (final LoopCondition loopCondition : loopConditions)
         {
            final String loopRule = loopCondition.getConditionRuleName();
            final LoopType loopType = loopCondition.getType();
            InteractionType interactionType = LoopType.SUCCESS == loopType ? InteractionType.DEPENDENCY : InteractionType.CONFLICT;
            final Node loopRuleNode = graph.getNode(loopRule);
            if (loopRuleNode != null)
            {
               final List<Edge> continuationInteractions = StreamSupport.stream(loopRuleNode.getEachEnteringEdge().spliterator(), false).filter(edge -> {
                  final String edgeLabel = getUiLabel(edge);
                  return edgeLabel.contains(interactionType.format());
               }).collect(Collectors.toList());
               final Set<String> interactionTriples = new HashSet<>();
               for (final Edge continuationInteraction : continuationInteractions)
               {
                  // Find each incoming dependency
                  StreamSupport.stream(streamOutgoingEdges(graph, continuationInteraction.getSourceNode()), false)
                        .filter(edge -> getUiLabel(edge).contains(InteractionType.DEPENDENCY.format()))
                        .forEach(edge -> interactionTriples.add("(" + cleanCommas(edge.getSourceNode()) + ")-" + getUiLabel(edge) + "->("
                              + cleanCommas(continuationInteraction.getSourceNode()) + ")-" + getUiLabel(continuationInteraction) + "->(" + cleanCommas(continuationInteraction.getTargetNode()) + ")" ));
               }
               sb.append(String.format("%s;%s;%s\n", cleanCommas(loopRuleNode), loopType, interactionTriples));
            }
         }
         System.out.println(sb.toString());
         System.out.println(formatAsLatex(sb.toString()));

         // determineCycles(graph, loopConditions);

         // showGraph(graph);
      }
   }

   private String formatAsLatex(final String csvCode)
   {
      String latexCode = csvCode;
      latexCode = latexCode.replaceAll("[;]", "&");
      latexCode = latexCode.replaceAll("[()]", "");
      List<String> sortedKeys = new ArrayList<>(TO_LATEX.keySet());
      Collections.sort(sortedKeys, (s1, s2) -> -Integer.compare(s1.length(), s2.length()));
      for (final String ruleName : sortedKeys) {
         latexCode = latexCode.replaceAll(Pattern.quote(cleanCommasStr(ruleName)), TO_LATEX.get(ruleName));
      }
      latexCode = latexCode.replaceAll("[*]\\d+", "");
      latexCode = latexCode.replaceAll(Pattern.quote("[S]"), LATEX_BACKSLASH + "guardSuccess");
      latexCode = latexCode.replaceAll(Pattern.quote("[F]"), LATEX_BACKSLASH + "guardFailure");
      latexCode = latexCode.replaceAll(Pattern.quote("-dl->"), LATEX_BACKSLASH + "cdaDependencyLocalSymbol");
      latexCode = latexCode.replaceAll(Pattern.quote("-dr->"), LATEX_BACKSLASH + "cdaDependencyRemoteSymbol");
      latexCode = latexCode.replaceAll(Pattern.quote("-cl->"), LATEX_BACKSLASH + "cdaConflictLocalSymbol");
      latexCode = latexCode.replaceAll(Pattern.quote("-cr->"), LATEX_BACKSLASH + "cdaConflictRemoteSymbol");
      latexCode = latexCode.replaceAll("\\[\\]", "\\$" + LATEX_BACKSLASH + "emptyset\\$");
      latexCode = latexCode.replaceAll("[\\[\\]]", "");
      latexCode = latexCode.replaceAll("\n", "\n" + LATEX_LINEEND + "\n");
      latexCode = latexCode.replaceAll(Pattern.quote(", "), ",\n");
      latexCode = latexCode.replaceAll("[&]", "&\n");
      latexCode = latexCode.replaceAll("(\\\\gtr.*Local.*Local.*[,])", "%$1");
      return latexCode;
   }

   public Graph calculateInteractionGraphBeforeRefinement()
   {
      int factor = 300;
      int tcRuleColumn = 0;
      int minusEColumn = 1 * factor;
      int plusEColumn = 2 * factor;
      int modwColumn = 3 * factor;
      int row1 = 0;
      int row2 = -1 * factor;
      int row3 = -2 * factor;
      int row4 = -3 * factor;
      int row5 = -4 * factor;
      Graph graph = new MultiGraph("InteractionGraph");
      graph.setNullAttributesAreErrors(true);
      configureLayout(graph);

      // What rules exist in the specification?
      addRuleNode(R_A, tcRuleColumn, row1, graph);
      addRuleNode(R_I, tcRuleColumn, row2, graph);
      addRuleNode(R_FIND_U, tcRuleColumn, row3, graph);
      addRuleNode(R_MINUS_E, minusEColumn, row1, graph);
      addRuleNode(R_MINUS_EH1, minusEColumn, row2, graph);
      addRuleNode(R_MINUS_EH2, minusEColumn, row3, graph);
      if (!usePaperRulesOnly)
      {
         addRuleNode(R_PLUS_E, plusEColumn, row1, graph);
         addRuleNode(R_PLUS_EH1, plusEColumn, row4, graph);
         addRuleNode(R_PLUS_EH2, plusEColumn, row5, graph);
         addRuleNode(R_MOD_W, modwColumn, row1, graph);
         addRuleNode(R_MOD_WH1, modwColumn, row2, graph);
         addRuleNode(R_MOD_WH2, modwColumn, row3, graph);
         addRuleNode(R_MOD_WH3, modwColumn, row4, graph);
         addRuleNode(R_MOD_WH4, modwColumn, row5, graph);
      }

      // Which critical pairs exist in the specification?
      {
         final String lhs = R_A;
         addSameRuleInteraction(lhs, graph, Interaction.CM);
         addInteraction(lhs, R_I, graph, Interaction.NONE); // Reason: Complementary due to PAC. No interaction.
         addInteraction(lhs, R_FIND_U, graph, Interaction.CS); // Reason: Link no longer unmarked
         addInteraction(lhs, R_MINUS_E, graph, Interaction.NONE); // Reason: R-e only sensitive to inactive links
         addInteraction(lhs, R_MINUS_EH1, graph, Interaction.NONE); // Reason: handlers only sensitive to inactive
         // links
         addInteraction(lhs, R_MINUS_EH2, graph, Interaction.NONE); // Reason: handlers only sensitive to inactive
         // links
         addInteraction(lhs, R_PLUS_E, graph, Interaction.CS, Interaction.CR);
         addInteraction(lhs, R_PLUS_EH1, graph, Interaction.DS);
         addInteraction(lhs, R_PLUS_EH2, graph, Interaction.DR);
         addInteraction(lhs, R_MOD_W, graph, Interaction.CS, Interaction.CR, Interaction.CR); // Reason: New partial
         // match of LHS
         addInteraction(lhs, R_MOD_WH1, graph, Interaction.NONE); // Reason: No new match of LHS
         addInteraction(lhs, R_MOD_WH2, graph, Interaction.NONE); // Reason: No new match of LHS
         addInteraction(lhs, R_MOD_WH3, graph, Interaction.DS); // Reason: New match of LHS
         addInteraction(lhs, R_MOD_WH4, graph, Interaction.DR); // Reason: New match of LHS
      }

      {
         final String lhs = R_I;
         addSameRuleInteraction(lhs, graph, Interaction.CM);
         addInteraction(lhs, R_A, graph, Interaction.NONE); // Reasons: Complementary due to PAC.
         addInteraction(lhs, R_FIND_U, graph, Interaction.CS); // Reason: Link no longer unmarked
         addInteraction(lhs, R_MINUS_E, graph, Interaction.CS, Interaction.CR); // Reasons: (i)/(ii) new partial
         // match of PACs
         addInteraction(lhs, R_MINUS_EH1, graph, Interaction.DS); // Reason: new match of LHS
         addInteraction(lhs, R_MINUS_EH2, graph, Interaction.DR); // Reason: new match of LHS
         addInteraction(lhs, R_MOD_W, graph, Interaction.CS, Interaction.CR); // Reasons: (i)/(ii) new partial match
         // of PACs
         addInteraction(lhs, R_MOD_WH1, graph, Interaction.DS); // Reason: new match of LHS
         addInteraction(lhs, R_MOD_WH2, graph, Interaction.DR); // Reason: new match of LHS
         addInteraction(lhs, R_MOD_WH3, graph, Interaction.NONE); // Reason: Not possible to produce a new match of
         // LHS
         addInteraction(lhs, R_MOD_WH4, graph, Interaction.NONE); // Reason: Not possible to produce a new match of
         // LHS
      }

      {
         final String lhs = R_MINUS_E;
         addSameRuleInteraction(lhs, graph, Interaction.CM);
         addInteraction(lhs, R_A, graph, Interaction.CS); // Reason: the link that would have been activated
         addInteraction(lhs, R_I, graph, Interaction.CS, Interaction.CS, Interaction.CR); // Reasons: (i) inactivated
         // link, (ii)/(iii)
         // triangle links
         addInteraction(lhs, R_FIND_U, graph, Interaction.CS); // Reason: The link to be unclassified
         addInteraction(lhs, R_MINUS_EH1, graph, Interaction.CS); // Reasons: (i) Remove to-be-unmarked link e12
         // (other links are uncritical due to NAC-PAC
         // pairs)
         addInteraction(lhs, R_MINUS_EH2, graph, Interaction.CR); // Reasons: (i) Remove to-be-unmarked link e32
         // (other links are uncritical due to NAC-PAC
         // pairs)
         addInteraction(lhs, R_PLUS_E, graph, Interaction.DS); // Reason: NAC of R+e that forbids parallel links
         addInteraction(lhs, R_PLUS_EH1, graph, null); // Reason:
         addInteraction(lhs, R_PLUS_EH2, graph, null); // Reason:
         addInteraction(lhs, R_MOD_W, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH1, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH2, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH3, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH4, graph, null); // Reason:
      }

      {
         final String lhs = R_MINUS_EH1;
         addSameRuleInteraction(lhs, graph, Interaction.CM);
         addInteraction(lhs, R_A, graph, Interaction.NONE); // Reason: The unmarked link is not eligible for
         // activation
         addInteraction(lhs, R_I, graph, Interaction.DS); // Reasons: This rule is only applicable if e12 is inactive
         addInteraction(lhs, R_FIND_U, graph, Interaction.DS); // Reason: Newly unmarked link
         addInteraction(lhs, R_MINUS_E, graph, Interaction.DS, Interaction.DR); // Reasons: Prepares self or remote
         // link removal
         addInteraction(lhs, R_MINUS_EH2, graph, Interaction.CR); // Reasons: Inactivates a link that would have been
         // inactivated on a remote node
         addInteraction(lhs, R_PLUS_E, graph, Interaction.NONE); // Reason: Does not create new node or match of
         // no-parallel-links NAC
         addInteraction(lhs, R_PLUS_EH1, graph, null); // Reason:
         addInteraction(lhs, R_PLUS_EH2, graph, null); // Reason:
         addInteraction(lhs, R_MOD_W, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH1, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH2, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH3, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH4, graph, null); // Reason:
      }

      {
         final String lhs = R_MINUS_EH2;
         addInteraction(lhs, R_A, graph, Interaction.NONE); // Reason: The unmarked link is not eligible for
         // activation
         addInteraction(lhs, R_I, graph, Interaction.DR); // Reasons:
         addInteraction(lhs, R_FIND_U, graph, Interaction.DR); // Reason: Newly unmarked link
         addInteraction(lhs, R_MINUS_E, graph, Interaction.DS, Interaction.DR); // Reasons: Prepares self or remote
         // link removal
         addInteraction(lhs, R_MINUS_EH1, graph, Interaction.CR); // Reasons: Inactivates a link that would have been
         // inactivated on a remote node
         addInteraction(lhs, R_MINUS_EH2, graph, Interaction.CM, Interaction.CS, Interaction.CR); // Reason: Unmarks
         // a link that
         // would have
         // been unmarked
         // from remote
         // or due to
         // another
         // pending link
         // removal
         addInteraction(lhs, R_PLUS_E, graph, Interaction.NONE); // Reason: Does not create new node or match of
         // no-parallel-links NAC
         addInteraction(lhs, R_PLUS_EH1, graph, null); // Reason:
         addInteraction(lhs, R_PLUS_EH2, graph, null); // Reason:
         addInteraction(lhs, R_MOD_W, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH1, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH2, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH3, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH4, graph, null); // Reason:
      }
      {
         final String lhs = R_PLUS_E;
         addSameRuleInteraction(lhs, graph, Interaction.CM);
         addInteraction(lhs, R_A, graph, null); // Reason:
         addInteraction(lhs, R_I, graph, null); // Reasons:
         addInteraction(lhs, R_FIND_U, graph, Interaction.DS); // Reason: Newly unmarked link
         addInteraction(lhs, R_MINUS_E, graph, null); // Reasons:
         addInteraction(lhs, R_MINUS_EH1, graph, null); // Reasons:
         addInteraction(lhs, R_MINUS_EH2, graph, null); // Reasons:
         addInteraction(lhs, R_PLUS_EH1, graph, null); // Reason:
         addInteraction(lhs, R_PLUS_EH2, graph, null); // Reason:
         addInteraction(lhs, R_MOD_W, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH1, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH2, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH3, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH4, graph, null); // Reason:
      }

      {
         final String lhs = R_PLUS_EH1;
         addSameRuleInteraction(lhs, graph, Interaction.CM);
         addInteraction(lhs, R_A, graph, null); // Reason:
         addInteraction(lhs, R_I, graph, null); // Reasons:
         addInteraction(lhs, R_FIND_U, graph, Interaction.DS); // Reason: Newly unmarked link
         addInteraction(lhs, R_MINUS_E, graph, null); // Reasons:
         addInteraction(lhs, R_MINUS_EH1, graph, null); // Reasons:
         addInteraction(lhs, R_MINUS_EH2, graph, null); // Reasons:
         addInteraction(lhs, R_PLUS_E, graph, null); // Reason:
         addInteraction(lhs, R_PLUS_EH2, graph, null); // Reason:
         addInteraction(lhs, R_MOD_W, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH1, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH2, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH3, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH4, graph, null); // Reason:
      }

      {
         final String lhs = R_PLUS_EH2;
         addSameRuleInteraction(lhs, graph, Interaction.CM);
         addInteraction(lhs, R_A, graph, null); // Reason:
         addInteraction(lhs, R_I, graph, null); // Reasons:
         addInteraction(lhs, R_FIND_U, graph, Interaction.DS); // Reason: Newly unmarked link
         addInteraction(lhs, R_MINUS_E, graph, null); // Reasons:
         addInteraction(lhs, R_MINUS_EH1, graph, null); // Reasons:
         addInteraction(lhs, R_PLUS_E, graph, null); // Reason:
         addInteraction(lhs, R_PLUS_EH1, graph, null); // Reason:
         addInteraction(lhs, R_PLUS_EH2, graph, null); // Reason:
         addInteraction(lhs, R_MOD_W, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH1, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH2, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH3, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH4, graph, null); // Reason:
      }

      {
         final String lhs = R_MOD_W;
         addSameRuleInteraction(lhs, graph, Interaction.CM);
         addInteraction(lhs, R_A, graph, null); // Reason:
         addInteraction(lhs, R_I, graph, null); // Reasons:
         addInteraction(lhs, R_FIND_U, graph, Interaction.DS); // Reason: Newly unmarked link
         addInteraction(lhs, R_MINUS_E, graph, null); // Reasons:
         addInteraction(lhs, R_MINUS_EH1, graph, null); // Reasons:
         addInteraction(lhs, R_MINUS_EH2, graph, null); // Reasons:
         addInteraction(lhs, R_PLUS_E, graph, null); // Reason:
         addInteraction(lhs, R_PLUS_EH1, graph, null); // Reason:
         addInteraction(lhs, R_PLUS_EH2, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH1, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH2, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH3, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH4, graph, null); // Reason:
      }

      {
         final String lhs = R_MOD_WH1;
         addSameRuleInteraction(lhs, graph, Interaction.CM);
         addInteraction(lhs, R_A, graph, null); // Reason:
         addInteraction(lhs, R_I, graph, null); // Reasons:
         addInteraction(lhs, R_FIND_U, graph, Interaction.DS); // Reason: Newly unmarked link
         addInteraction(lhs, R_MINUS_E, graph, null); // Reasons:
         addInteraction(lhs, R_MINUS_EH1, graph, null); // Reasons:
         addInteraction(lhs, R_MINUS_EH2, graph, null); // Reasons:
         addInteraction(lhs, R_PLUS_E, graph, null); // Reason:
         addInteraction(lhs, R_PLUS_EH1, graph, null); // Reason:
         addInteraction(lhs, R_PLUS_EH2, graph, null); // Reason:
         addInteraction(lhs, R_MOD_W, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH1, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH2, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH3, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH4, graph, null); // Reason:
      }

      {
         final String lhs = R_MOD_WH2;
         addSameRuleInteraction(lhs, graph, Interaction.CM);
         addInteraction(lhs, R_A, graph, null); // Reason:
         addInteraction(lhs, R_I, graph, null); // Reasons:
         addInteraction(lhs, R_FIND_U, graph, Interaction.DS); // Reason: Newly unmarked link
         addInteraction(lhs, R_MINUS_E, graph, null); // Reasons:
         addInteraction(lhs, R_MINUS_EH1, graph, null); // Reasons:
         addInteraction(lhs, R_MINUS_EH2, graph, null); // Reasons:
         addInteraction(lhs, R_PLUS_E, graph, null); // Reason:
         addInteraction(lhs, R_PLUS_EH1, graph, null); // Reason:
         addInteraction(lhs, R_PLUS_EH2, graph, null); // Reason:
         addInteraction(lhs, R_MOD_W, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH1, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH2, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH3, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH4, graph, null); // Reason:
      }

      {
         final String lhs = R_MOD_WH3;
         addSameRuleInteraction(lhs, graph, Interaction.CM);
         addInteraction(lhs, R_A, graph, null); // Reason:
         addInteraction(lhs, R_I, graph, null); // Reasons:
         addInteraction(lhs, R_FIND_U, graph, Interaction.DS); // Reason: Newly unmarked link
         addInteraction(lhs, R_MINUS_E, graph, null); // Reasons:
         addInteraction(lhs, R_MINUS_EH1, graph, null); // Reasons:
         addInteraction(lhs, R_MINUS_EH2, graph, null); // Reasons:
         addInteraction(lhs, R_PLUS_E, graph, null); // Reason:
         addInteraction(lhs, R_PLUS_EH1, graph, null); // Reason:
         addInteraction(lhs, R_PLUS_EH2, graph, null); // Reason:
         addInteraction(lhs, R_MOD_W, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH1, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH2, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH3, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH4, graph, null); // Reason:
      }

      {
         final String lhs = R_MOD_WH4;
         addSameRuleInteraction(lhs, graph, Interaction.CM);
         addInteraction(lhs, R_A, graph, null); // Reason:
         addInteraction(lhs, R_I, graph, null); // Reasons:
         addInteraction(lhs, R_FIND_U, graph, Interaction.DS); // Reason: Newly unmarked link
         addInteraction(lhs, R_MINUS_E, graph, null); // Reasons:
         addInteraction(lhs, R_MINUS_EH1, graph, null); // Reasons:
         addInteraction(lhs, R_MINUS_EH2, graph, null); // Reasons:
         addInteraction(lhs, R_PLUS_E, graph, null); // Reason:
         addInteraction(lhs, R_PLUS_EH1, graph, null); // Reason:
         addInteraction(lhs, R_PLUS_EH2, graph, null); // Reason:
         addInteraction(lhs, R_MOD_W, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH1, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH2, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH3, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH4, graph, null); // Reason:
      }
      return graph;
   }

   public Graph calculateInteractionGraphAfterRefinement()
   {
      int factor = 300;
      int tcRuleColumn = 0;
      int minusEColumn = 1 * factor;
      int plusEColumn = 2 * factor;
      int modwColumn = 3 * factor;
      int unlockColumn = 4 * factor;
      int row1 = 0;
      int row2 = -1 * factor;
      int row3 = -2 * factor;
      int row4 = -3 * factor;
      int row5 = -4 * factor;
      Graph graph = new MultiGraph("InteractionGraphAfterRefinement");
      graph.setNullAttributesAreErrors(true);
      configureLayout(graph);

      // What rules exist in the specification?
      addRuleNode(R_A, tcRuleColumn, row1, graph);
      addRuleNode(R_I, tcRuleColumn, row2, graph);
      addRuleNode(R_FIND_U, tcRuleColumn, row3, graph);
      addRuleNode(R_MINUS_E, minusEColumn, row1, graph);
      addRuleNode(R_MINUS_EH1, minusEColumn, row2, graph);
      addRuleNode(R_MINUS_EH2, minusEColumn, row3, graph);
      if (!usePaperRulesOnly)
      {
         addRuleNode(R_PLUS_E, plusEColumn, row1, graph);
         addRuleNode(R_PLUS_EH1, plusEColumn, row4, graph);
         addRuleNode(R_PLUS_EH2, plusEColumn, row5, graph);
         addRuleNode(R_MOD_W, modwColumn, row1, graph);
         addRuleNode(R_MOD_WH1, modwColumn, row2, graph);
         addRuleNode(R_MOD_WH2, modwColumn, row3, graph);
         addRuleNode(R_MOD_WH3, modwColumn, row4, graph);
         addRuleNode(R_MOD_WH4, modwColumn, row5, graph);
      }
      addRuleNode(R_UNLOCK, unlockColumn, row1, graph);

      try
      {
         final CSVParser parser = new CSVParserBuilder().withSeparator(';').withIgnoreQuotations(true).build();
         final CSVReader reader = new CSVReaderBuilder(new FileReader("resources/InteractionGraphAfterRefinement.csv")).withCSVParser(parser).withSkipLines(1)
               .build();
         final List<String> nodeIds = getNodeIds(graph);
         final List<String[]> csvData = reader.readAll();
         for (final String[] csvLines : csvData)
         {
            if (csvLines.length != 3)
               throw new IllegalArgumentException("CSV row '" + csvLines + "' should have three columns.");

            final String lhsRule = csvLines[0];
            final String rhsRule = csvLines[1];
            final String interactions = csvLines[2];

            if (!nodeIds.contains(lhsRule))
               throw new IllegalArgumentException("LHS rule '" + lhsRule + "' in " + Arrays.toString(csvLines) + " is not a valid rule name");

            if (!getNodeIds(graph).contains(rhsRule))
               throw new IllegalArgumentException("RHS rule '" + rhsRule + "' in " + Arrays.toString(csvLines) + " is not a valid rule name");

            final String[] splitInteractions = interactions.split(Pattern.quote(","));
            final List<Interaction> parsedInteractions = new ArrayList<>();
            for (final String interactionWithReasons : splitInteractions)
            {
               final String interactionStr = interactionWithReasons.replaceAll("[(].*[)]", "");
               try
               {
                  parsedInteractions.add(Interaction.fromString(interactionStr));
               } catch (final Exception ex)
               {
                  throw new IllegalArgumentException("A problem occurred while converting interaction '" + interactionWithReasons + "'.", ex);
               }
            }
            addInteraction(lhsRule, rhsRule, graph, parsedInteractions.toArray(new Interaction[parsedInteractions.size()]));

         }
      } catch (IOException e)
      {
         e.printStackTrace();
      }

      // Which critical pairs exist in the specification?
      {
         final String lhs = R_A;
         addSameRuleInteraction(lhs, graph, Interaction.CM);
         addInteraction(lhs, R_I, graph, null); // Reason:
         addInteraction(lhs, R_FIND_U, graph, null); // Reason:
         addInteraction(lhs, R_MINUS_E, graph, null); // Reason:
         addInteraction(lhs, R_MINUS_EH1, graph, null); // Reason:
         addInteraction(lhs, R_MINUS_EH2, graph, null); // Reason:
         addInteraction(lhs, R_PLUS_E, graph, null);
         addInteraction(lhs, R_PLUS_EH1, graph, null);
         addInteraction(lhs, R_PLUS_EH2, graph, null);
         addInteraction(lhs, R_MOD_W, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH1, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH2, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH3, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH4, graph, null); // Reason:
      }

      {
         final String lhs = R_I;
         addSameRuleInteraction(lhs, graph, null);
         addInteraction(lhs, R_A, graph, null); // Reason:
         addInteraction(lhs, R_FIND_U, graph, null); // Reason:
         addInteraction(lhs, R_MINUS_E, graph, null); // Reason:
         addInteraction(lhs, R_MINUS_EH1, graph, null); // Reason:
         addInteraction(lhs, R_MINUS_EH2, graph, null); // Reason:
         addInteraction(lhs, R_MOD_W, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH1, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH2, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH3, graph, null); // Reason:
         // LHS
         addInteraction(lhs, R_MOD_WH4, graph, null); // Reason:
         // LHS
      }

      {
         final String lhs = R_MINUS_E;
         addSameRuleInteraction(lhs, graph, null);
         addInteraction(lhs, R_A, graph, null); // Reason:
         addInteraction(lhs, R_I, graph, null); // Reason:
         addInteraction(lhs, R_FIND_U, graph, null); // Reason:
         addInteraction(lhs, R_MINUS_EH1, graph, null); // Reason:
         addInteraction(lhs, R_MINUS_EH2, graph, null); // Reason:
         addInteraction(lhs, R_PLUS_E, graph, null); // Reason:
         addInteraction(lhs, R_PLUS_EH1, graph, null); // Reason:
         addInteraction(lhs, R_PLUS_EH2, graph, null); // Reason:
         addInteraction(lhs, R_MOD_W, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH1, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH2, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH3, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH4, graph, null); // Reason:
      }

      {
         final String lhs = R_MINUS_EH1;
         addSameRuleInteraction(lhs, graph, null);
         addInteraction(lhs, R_A, graph, null); // Reason:
         addInteraction(lhs, R_I, graph, null); // Reason:
         addInteraction(lhs, R_FIND_U, graph, null); // Reason:
         addInteraction(lhs, R_MINUS_E, graph, null); // Reason:
         addInteraction(lhs, R_MINUS_EH2, graph, null); // Reason:
         addInteraction(lhs, R_PLUS_E, graph, null); // Reason:
         addInteraction(lhs, R_PLUS_EH1, graph, null); // Reason:
         addInteraction(lhs, R_PLUS_EH2, graph, null); // Reason:
         addInteraction(lhs, R_MOD_W, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH1, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH2, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH3, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH4, graph, null); // Reason:
      }

      {
         final String lhs = R_MINUS_EH2;
         addInteraction(lhs, R_A, graph, null); // Reason:
         // activation
         addInteraction(lhs, R_I, graph, null); // Reason:
         addInteraction(lhs, R_FIND_U, graph, null); // Reason:
         addInteraction(lhs, R_MINUS_E, graph, null); // Reason:
         addInteraction(lhs, R_MINUS_EH1, graph, null); // Reason:
         addInteraction(lhs, R_MINUS_EH2, graph, null); // Reason:
         addInteraction(lhs, R_PLUS_E, graph, null); // Reason:
         addInteraction(lhs, R_PLUS_EH1, graph, null); // Reason:
         addInteraction(lhs, R_PLUS_EH2, graph, null); // Reason:
         addInteraction(lhs, R_MOD_W, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH1, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH2, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH3, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH4, graph, null); // Reason:
      }
      {
         final String lhs = R_PLUS_E;
         addSameRuleInteraction(lhs, graph, null);
         addInteraction(lhs, R_A, graph, null); // Reason:
         addInteraction(lhs, R_I, graph, null); // Reason:
         addInteraction(lhs, R_FIND_U, graph, null); // Reason:
         addInteraction(lhs, R_MINUS_E, graph, null); // Reason:
         addInteraction(lhs, R_MINUS_EH1, graph, null); // Reason:
         addInteraction(lhs, R_MINUS_EH2, graph, null); // Reason:
         addInteraction(lhs, R_PLUS_EH1, graph, null); // Reason:
         addInteraction(lhs, R_PLUS_EH2, graph, null); // Reason:
         addInteraction(lhs, R_MOD_W, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH1, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH2, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH3, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH4, graph, null); // Reason:
      }

      {
         final String lhs = R_PLUS_EH1;
         addSameRuleInteraction(lhs, graph, null);
         addInteraction(lhs, R_A, graph, null); // Reason:
         addInteraction(lhs, R_I, graph, null); // Reason:
         addInteraction(lhs, R_FIND_U, graph, null); // Reason:
         addInteraction(lhs, R_MINUS_E, graph, null); // Reason:
         addInteraction(lhs, R_MINUS_EH1, graph, null); // Reason:
         addInteraction(lhs, R_MINUS_EH2, graph, null); // Reason:
         addInteraction(lhs, R_PLUS_E, graph, null); // Reason:
         addInteraction(lhs, R_PLUS_EH2, graph, null); // Reason:
         addInteraction(lhs, R_MOD_W, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH1, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH2, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH3, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH4, graph, null); // Reason:
      }

      {
         final String lhs = R_PLUS_EH2;
         addSameRuleInteraction(lhs, graph, null);
         addInteraction(lhs, R_A, graph, null); // Reason:
         addInteraction(lhs, R_I, graph, null); // Reason:
         addInteraction(lhs, R_FIND_U, graph, null); // Reason:
         addInteraction(lhs, R_MINUS_E, graph, null); // Reason:
         addInteraction(lhs, R_MINUS_EH1, graph, null); // Reason:
         addInteraction(lhs, R_PLUS_E, graph, null); // Reason:
         addInteraction(lhs, R_PLUS_EH1, graph, null); // Reason:
         addInteraction(lhs, R_PLUS_EH2, graph, null); // Reason:
         addInteraction(lhs, R_MOD_W, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH1, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH2, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH3, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH4, graph, null); // Reason:
      }

      {
         final String lhs = R_MOD_W;
         addSameRuleInteraction(lhs, graph, null);
         addInteraction(lhs, R_A, graph, null); // Reason:
         addInteraction(lhs, R_I, graph, null); // Reason:
         addInteraction(lhs, R_FIND_U, graph, null); // Reason:
         addInteraction(lhs, R_MINUS_E, graph, null); // Reason:
         addInteraction(lhs, R_MINUS_EH1, graph, null); // Reason:
         addInteraction(lhs, R_MINUS_EH2, graph, null); // Reason:
         addInteraction(lhs, R_PLUS_E, graph, null); // Reason:
         addInteraction(lhs, R_PLUS_EH1, graph, null); // Reason:
         addInteraction(lhs, R_PLUS_EH2, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH1, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH2, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH3, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH4, graph, null); // Reason:
      }

      {
         final String lhs = R_MOD_WH1;
         addSameRuleInteraction(lhs, graph, null);
         addInteraction(lhs, R_A, graph, null); // Reason:
         addInteraction(lhs, R_I, graph, null); // Reason:
         addInteraction(lhs, R_FIND_U, graph, Interaction.DS); // Reason:
         addInteraction(lhs, R_MINUS_E, graph, null); // Reason:
         addInteraction(lhs, R_MINUS_EH1, graph, null); // Reason:
         addInteraction(lhs, R_MINUS_EH2, graph, null); // Reason:
         addInteraction(lhs, R_PLUS_E, graph, null); // Reason:
         addInteraction(lhs, R_PLUS_EH1, graph, null); // Reason:
         addInteraction(lhs, R_PLUS_EH2, graph, null); // Reason:
         addInteraction(lhs, R_MOD_W, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH1, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH2, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH3, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH4, graph, null); // Reason:
      }

      {
         final String lhs = R_MOD_WH2;
         addSameRuleInteraction(lhs, graph, null);
         addInteraction(lhs, R_A, graph, null); // Reason:
         addInteraction(lhs, R_I, graph, null); // Reason:
         addInteraction(lhs, R_FIND_U, graph, Interaction.DS); // Reason:
         addInteraction(lhs, R_MINUS_E, graph, null); // Reason:
         addInteraction(lhs, R_MINUS_EH1, graph, null); // Reason:
         addInteraction(lhs, R_MINUS_EH2, graph, null); // Reason:
         addInteraction(lhs, R_PLUS_E, graph, null); // Reason:
         addInteraction(lhs, R_PLUS_EH1, graph, null); // Reason:
         addInteraction(lhs, R_PLUS_EH2, graph, null); // Reason:
         addInteraction(lhs, R_MOD_W, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH1, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH2, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH3, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH4, graph, null); // Reason:
      }

      {
         final String lhs = R_MOD_WH3;
         addSameRuleInteraction(lhs, graph, null);
         addInteraction(lhs, R_A, graph, null); // Reason:
         addInteraction(lhs, R_I, graph, null); // Reason:
         addInteraction(lhs, R_FIND_U, graph, null); // Reason:
         addInteraction(lhs, R_MINUS_E, graph, null); // Reason:
         addInteraction(lhs, R_MINUS_EH1, graph, null); // Reason:
         addInteraction(lhs, R_MINUS_EH2, graph, null); // Reason:
         addInteraction(lhs, R_PLUS_E, graph, null); // Reason:
         addInteraction(lhs, R_PLUS_EH1, graph, null); // Reason:
         addInteraction(lhs, R_PLUS_EH2, graph, null); // Reason:
         addInteraction(lhs, R_MOD_W, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH1, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH2, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH3, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH4, graph, null); // Reason:
      }

      {
         final String lhs = R_MOD_WH4;
         addSameRuleInteraction(lhs, graph, null);
         addInteraction(lhs, R_A, graph, null); // Reason:
         addInteraction(lhs, R_I, graph, null); // Reason:
         addInteraction(lhs, R_FIND_U, graph, null); // Reason:
         addInteraction(lhs, R_MINUS_E, graph, null); // Reason:
         addInteraction(lhs, R_MINUS_EH1, graph, null); // Reason:
         addInteraction(lhs, R_MINUS_EH2, graph, null); // Reason:
         addInteraction(lhs, R_PLUS_E, graph, null); // Reason:
         addInteraction(lhs, R_PLUS_EH1, graph, null); // Reason:
         addInteraction(lhs, R_PLUS_EH2, graph, null); // Reason:
         addInteraction(lhs, R_MOD_W, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH1, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH2, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH3, graph, null); // Reason:
         addInteraction(lhs, R_MOD_WH4, graph, null); // Reason:
      }
      return graph;
   }

   private List<String> getNodeIds(Graph graph)
   {
      return graph.getNodeSet().stream().map(Node::getId).collect(Collectors.toList());
   }

   public List<LoopCondition> getLoopConditions()
   {
      // @formatter:off
		final List<LoopCondition> loopConditions = Arrays.asList(new LoopCondition(R_FIND_U, LoopType.SUCCESS),
				new LoopCondition(R_MINUS_EH1, LoopType.SUCCESS), new LoopCondition(R_MINUS_EH2, LoopType.SUCCESS),
				new LoopCondition(R_MINUS_E, LoopType.FAILURE), new LoopCondition(R_PLUS_EH1, LoopType.SUCCESS),
				new LoopCondition(R_PLUS_EH2, LoopType.SUCCESS), new LoopCondition(R_PLUS_E, LoopType.FAILURE),
				new LoopCondition(R_MOD_WH1, LoopType.SUCCESS), new LoopCondition(R_MOD_WH2, LoopType.SUCCESS),
				new LoopCondition(R_MOD_WH3, LoopType.SUCCESS), new LoopCondition(R_MOD_WH4, LoopType.SUCCESS),
				new LoopCondition(R_MOD_W, LoopType.FAILURE));
		// @formatter:on
      return loopConditions;
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

   public Spliterator<Edge> streamOutgoingEdges(Graph graph, final Node node)
   {
      return node.getEachEnteringEdge().spliterator();
   }

   public String getUiLabel(Edge edge)
   {
      return edge.getAttribute("ui.label").toString();
   }

   public void showGraph(Graph graph)
   {
      graph.display(false);
   }

   public void determineCycles(Graph graph, final List<LoopCondition> loopConditions)
   {
      final int nodeCount = graph.getNodeCount();
      final Node[] nodes = graph.getNodeSet().toArray(new Node[nodeCount]);
      final Map<Node, Integer> nodeToIndex = new HashMap<>();
      for (int i = 0; i < nodeCount; ++i)
      {
         nodeToIndex.put(nodes[i], i);
      }

      boolean[][] adjMatrix = new boolean[nodeCount][nodeCount];
      IntStream.range(0, nodeCount).forEach(i -> IntStream.range(0, nodeCount).forEach(j -> adjMatrix[i][j] = false));
      for (final Edge edge : graph.getEdgeSet())
      {
         Node sourceNode = edge.getSourceNode();
         Node targetNode = edge.getTargetNode();
         adjMatrix[nodeToIndex.get(sourceNode)][nodeToIndex.get(targetNode)] = true;
      }

      final ElementaryCyclesSearch ecs = new ElementaryCyclesSearch(adjMatrix, nodes);
      final List<Vector<Node>> cyclesAsNodes = ecs.getElementaryCycles();
      cyclesAsNodes.forEach(c -> closeCycle(c));

      final List<List<Edge>> cyclesAsEdges = asEdgeBasedCycles(cyclesAsNodes);

      for (final List<Edge> cycleAsEdges : cyclesAsEdges)
      {
         final List<LoopCondition> relevantLoopConditions = findRelevantLoopConditions(cycleAsEdges, loopConditions);
         for (final LoopCondition relevantLoopCondition : relevantLoopConditions)
         {
            System.out.printf("Condition: %20s    ", relevantLoopCondition);
            final Iterator<Edge> iter = cycleAsEdges.iterator();
            while (iter.hasNext())
            {
               final Edge edge = iter.next();
               final boolean isReason = isReasonForRelevance(edge, relevantLoopCondition);
               final StringBuilder sb = new StringBuilder();
               sb.append(edge.getSourceNode().getId());
               sb.append(" -");
               if (isReason)
                  sb.append("**");
               sb.append(extractLabel(edge));
               if (isReason)
                  sb.append("**");
               sb.append("-> ");

               if (!iter.hasNext())
                  sb.append(edge.getTargetNode().getId());

               System.out.print(sb);
            }
            System.out.print("\n");
         }
      }
   }

   private void configureLayout(Graph graph)
   {
      graph.addAttribute("ui.quality");
      graph.addAttribute("ui.antialias");
      graph.setAttribute("ui.stylesheet", "node {" + //
            "text-alignment: center; " + //
            "text-color: black; " + //
            "text-style: bold;" + //
            "text-size: 14px;" + //
            "size: 10px, 10px;" + //
            "fill-mode: plain; /* Default. */" + //
            "fill-color: black; /* Default is black.*/" + //
            "stroke-mode: plain; /* Default is none.*/" + //
            "stroke-color: blue; /* Default is black.*/}" + //
            // "node#Ra {fill-color: red;text-background-mode:
            // plain;text-background-color:white;}" + //
            "edge { shape:blob;" + //
            "text-alignment: along;\n" + //
            "text-size: 14px;" + //
            "arrow-shape: arrow;" + //
            "shape: cubic-curve;}");
   }

   private List<String> extractLabel(final Edge edge)
   {
      return (List<String>) edge.getAttribute("ui.label");
   }

   private List<LoopCondition> findRelevantLoopConditions(final List<Edge> cycleAsEdges, final List<LoopCondition> loopConditions)
   {
      new ArrayList<>();
      final List<LoopCondition> relevantLoopConditions = loopConditions.stream()
            .filter(loopCondition -> cycleAsEdges.stream().anyMatch(edge -> isReasonForRelevance(edge, loopCondition))).collect(Collectors.toList());
      return relevantLoopConditions;
   }

   private boolean isReasonForRelevance(Edge edge, LoopCondition loopCondition)
   {
      if (!loopCondition.getConditionRuleName().equals(edge.getTargetNode().getId()))
         return false;

      final List<String> edgeLabel = extractLabel(edge);
      switch (loopCondition.getType())
      {
      case SUCCESS:
         return containsDependency(edgeLabel);
      case FAILURE:
         return containsConflict(edgeLabel);
      default:
         throw new IllegalArgumentException(loopCondition.toString());
      }
   }

   private boolean containsConflict(List<String> edgeLabel)
   {
      return edgeLabel.stream().anyMatch(interaction -> interaction.startsWith("C"));
   }

   private boolean containsDependency(List<String> edgeLabel)
   {
      return edgeLabel.stream().anyMatch(interaction -> interaction.startsWith("D"));
   }

   private List<List<Edge>> asEdgeBasedCycles(final List<Vector<Node>> cyclesAsNodes)
   {
      return cyclesAsNodes.stream().map(InteractionGraphProducerMain::asEdgeBasedCycle).collect(Collectors.toList());
   }

   private static ArrayList<Edge> asEdgeBasedCycle(final Vector<Node> cycleAsNodes)
   {
      final ArrayList<Edge> cycleAsEdges = new ArrayList<Edge>();
      Node previousNode = null;
      for (final Node node : cycleAsNodes)
      {
         if (previousNode != null)
            cycleAsEdges.add(previousNode.getEdgeToward(node));

         previousNode = node;
      }
      return cycleAsEdges;
   }

   private boolean closeCycle(final Vector<Node> cycle)
   {
      return cycle.add(cycle.get(0));
   }

   private List<String> determineLabel(Node previousNode, Node node)
   {
      final Edge edge = previousNode.getEdgeToward(node);
      return extractLabel(edge);
   }

   private static void addRuleNode(final String id, final int x, final int y, Graph graph)
   {
      final Node node = graph.addNode(id);
      node.setAttribute("ui.label", id);
      node.setAttribute("xyz", x, y, 0);
   }

   private void addSameRuleInteraction(final String nodeId, final Graph graph, final Interaction... interactions)
   {
      addInteraction(nodeId, nodeId, graph, interactions);
   }

   private void addInteraction(final String sourceId, final String targetId, final Graph graph, final Interaction... interactions)
   {
      if (interactions == null)
         return;

      if (!containsNode(graph, sourceId) || !containsNode(graph, targetId))
         return;

      if (this.ignoreLoops && sourceId.equals(targetId))
         return;

      final Frequency freqDist = new Frequency();
      Arrays.stream(interactions).forEach(interaction -> freqDist.addValue(formatEdgeLabel(interaction).get(0)));
      final Iterator<Entry<Comparable<?>, Long>> iter = freqDist.entrySetIterator();
      while (iter.hasNext())
      {
         final Entry<Comparable<?>, Long> next = iter.next();
         final Comparable<?> label = next.getKey() + (next.getValue() > 1 ? "*" + next.getValue() : "");
         final String edgeId = sourceId + "-" + label + "->" + targetId;
         final Edge edge = graph.addEdge(edgeId, sourceId, targetId, true);
         edge.setAttribute("ui.label", label);
      }
   }

   private static boolean containsNode(final Graph graph, final String sourceId)
   {
      return graph.getNode(sourceId) != null;
   }

   private static List<String> formatEdgeLabel(final Interaction... interactions)
   {
      return Arrays.stream(interactions).map(Interaction::format).collect(Collectors.toList());
   }
}
