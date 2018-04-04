package org.cobolt.tccpa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.cobolt.tccpa.InteractionGraphProducerMain.LoopType;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;

import de.normalisiert.utils.graphs.ElementaryCyclesSearch;

public class InteractionGraphProducerMain
{

   private static final String R_MOD_WH4 = "Rmod-w,h,4";

   private static final String R_MOD_WH3 = "Rmod-w,h,3";

   private static final String R_MOD_WH2 = "Rmod-w,h,2";

   private static final String R_MOD_WH1 = "Rmod-w,h,1";

   private static final String R_MOD_W = "Rmod-w";

   private static final String R_PLUS_EH2 = "R+e,h,2";

   private static final String R_PLUS_EH1 = "R+e,h,1";

   private static final String R_PLUS_E = "R+e";

   private static final String R_MINUS_EH2 = "R-e,h,2";

   private static final String R_MINUS_EH1 = "R-e,h,1";

   private static final String R_MINUS_E = "R-e";

   private static final String R_FIND_U = "Rfind-u";

   private static final String R_I = "Ri";

   private static final String R_A = "Ra";

   private final boolean usePaperRulesOnly = true;

   private final boolean ignoreLoops = true;

   public class LoopCondition
   {
      private final String conditionRuleName;

      private final LoopType type;

      public LoopCondition(final String conditionRuleName, final LoopType type)
      {
         this.conditionRuleName = conditionRuleName;
         this.type = type;
      }

      public String getConditionRuleName()
      {
         return conditionRuleName;
      }

      public LoopType getType()
      {
         return type;
      }

      @Override
      public String toString()
      {
         return String.format("%s ==%s==> ...", this.getConditionRuleName(), this.type);
      }
   }

   public enum LoopType {
      SUCCESS("[S]"), FAILURE("[F]");

      private String mnemonic;

      private LoopType(final String mnemonic)
      {
         this.mnemonic = mnemonic;
      }

      @Override
      public String toString()
      {
         return this.mnemonic;
      }
   }

   public enum Interaction {
      CM(InteractionType.CONFLICT, InteractionCategory.SAME_MATCH), CS(InteractionType.CONFLICT, InteractionCategory.SELF), CR(InteractionType.CONFLICT,
            InteractionCategory.REMOTE), DM(InteractionType.DEPENDENCY, InteractionCategory.SAME_MATCH), DS(InteractionType.DEPENDENCY,
                  InteractionCategory.SELF), DR(InteractionType.DEPENDENCY, InteractionCategory.REMOTE), NONE(null, null);

      private InteractionType interactionType;

      private InteractionCategory interactionCategory;

      private Interaction(final InteractionType interactionType, final InteractionCategory interactionCategory)
      {
         this.interactionType = interactionType;
         this.interactionCategory = interactionCategory;
      }

      public InteractionCategory getInteractionCategory()
      {
         return interactionCategory;
      }

      public InteractionType getInteractionType()
      {
         return interactionType;
      }
   }

   public enum InteractionType {
      CONFLICT("C"), DEPENDENCY("D");

      private String mnemonic;

      private InteractionType(final String mnemonic)
      {
         this.mnemonic = mnemonic;
      }

      public String getMnemonic()
      {
         return mnemonic;
      }
   }

   public enum InteractionCategory {
      SAME_MATCH("m"), SELF("s"), REMOTE("r");
      private String mnemonic;

      private InteractionCategory(final String mnemonic)
      {
         this.mnemonic = mnemonic;
      }

      public String getMnemonic()
      {
         return mnemonic;
      }

   }

   public static void main(String[] args)
   {
      new InteractionGraphProducerMain().run();
   }

   private void run()
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
      graph.addAttribute("ui.quality");
      graph.addAttribute("ui.antialias");
      graph.setNullAttributesAreErrors(true);
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
            //            "node#Ra {fill-color: red;text-background-mode: plain;text-background-color:white;}" + //
            "edge { shape:blob;" + //
            "text-alignment: along;\n" + //
            "text-size: 14px;" + //
            "arrow-shape: arrow;" + //
            "shape: cubic-curve;}");

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
         addInteraction(lhs, R_I, graph, Interaction.NONE); //Reason: Complementary due to PAC. No interaction.
         addInteraction(lhs, R_FIND_U, graph, Interaction.CS); // Reason: Link no longer unmarked
         addInteraction(lhs, R_MINUS_E, graph, Interaction.NONE); // Reason: R-e only sensitive to inactive links
         addInteraction(lhs, R_MINUS_EH1, graph, Interaction.NONE); // Reason: handlers only sensitive to inactive links
         addInteraction(lhs, R_MINUS_EH2, graph, Interaction.NONE); // Reason: handlers only sensitive to inactive links
         addInteraction(lhs, R_PLUS_E, graph, Interaction.CS, Interaction.CR);
         addInteraction(lhs, R_PLUS_EH1, graph, Interaction.DS);
         addInteraction(lhs, R_PLUS_EH2, graph, Interaction.DR);
         addInteraction(lhs, R_MOD_W, graph, Interaction.CS, Interaction.CR, Interaction.CR); // Reason: New partial match of LHS
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
         addInteraction(lhs, R_MINUS_E, graph, Interaction.CS, Interaction.CR); // Reasons: (i)/(ii) new partial match of PACs
         addInteraction(lhs, R_MINUS_EH1, graph, Interaction.DS); // Reason: new match of LHS
         addInteraction(lhs, R_MINUS_EH2, graph, Interaction.DR); // Reason: new match of LHS
         addInteraction(lhs, R_MOD_W, graph, Interaction.CS, Interaction.CR); // Reasons: (i)/(ii) new partial match of PACs
         addInteraction(lhs, R_MOD_WH1, graph, Interaction.DS); // Reason: new match of LHS
         addInteraction(lhs, R_MOD_WH2, graph, Interaction.DR); // Reason: new match of LHS
         addInteraction(lhs, R_MOD_WH3, graph, Interaction.NONE); // Reason: Not possible to produce a new match of LHS
         addInteraction(lhs, R_MOD_WH4, graph, Interaction.NONE); // Reason: Not possible to produce a new match of LHS
      }

      {
         final String lhs = R_MINUS_E;
         addSameRuleInteraction(lhs, graph, Interaction.CM);
         addInteraction(lhs, R_A, graph, Interaction.CS); // Reason: the link that would have been activated
         addInteraction(lhs, R_I, graph, Interaction.CS, Interaction.CS, Interaction.CR); // Reasons: (i) inactivated link, (ii)/(iii) triangle links
         addInteraction(lhs, R_FIND_U, graph, Interaction.CS); // Reason: The link to be unclassified
         addInteraction(lhs, R_MINUS_EH1, graph, Interaction.CS); // Reasons: (i) Remove to-be-unmarked link e12 (other links are uncritical due to NAC-PAC pairs)
         addInteraction(lhs, R_MINUS_EH2, graph, Interaction.CR); // Reasons: (i) Remove to-be-unmarked link e32 (other links are uncritical due to NAC-PAC pairs)
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
         addInteraction(lhs, R_A, graph, Interaction.NONE); // Reason: The unmarked link is not eligible for activation
         addInteraction(lhs, R_I, graph, Interaction.DS); // Reasons: This rule is only applicable if e12 is inactive
         addInteraction(lhs, R_FIND_U, graph, Interaction.DS); // Reason: Newly unmarked link
         addInteraction(lhs, R_MINUS_E, graph, Interaction.DS, Interaction.DR); // Reasons: Prepares self or remote link removal
         addInteraction(lhs, R_MINUS_EH2, graph, Interaction.CR); // Reasons: Inactivates a link that would have been inactivated on a remote node
         addInteraction(lhs, R_PLUS_E, graph, Interaction.NONE); // Reason: Does not create new node or match of no-parallel-links NAC
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
         addInteraction(lhs, R_A, graph, Interaction.NONE); // Reason: The unmarked link is not eligible for activation
         addInteraction(lhs, R_I, graph, Interaction.DS); // Reasons: This rule is only applicable if e12 is inactive
         addInteraction(lhs, R_FIND_U, graph, Interaction.DS); // Reason: Newly unmarked link
         addInteraction(lhs, R_MINUS_E, graph, Interaction.DS, Interaction.DR); // Reasons: Prepares self or remote link removal
         addInteraction(lhs, R_MINUS_EH1, graph, Interaction.CR); // Reasons: Inactivates a link that would have been inactivated on a remote node
         addInteraction(lhs, R_MINUS_EH2, graph, Interaction.CM, Interaction.CS, Interaction.CR); // Reason: Unmarks a link that would have been unmarked from remote or due to another pending link removal
         addInteraction(lhs, R_PLUS_E, graph, Interaction.NONE); // Reason: Does not create new node or match of no-parallel-links NAC
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

      // What kinds of loops exist in the specification
      //@formatter:off
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
            new LoopCondition(R_MOD_W, LoopType.FAILURE)
            );
      //@formatter:on

      // Which circles exist? (Use Johnson's algorithm)
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

      final List<List<Edge>> cyclesAsEdges = buildEdgeBasedCycles(cyclesAsNodes);

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
               if (isReason) sb.append("**");
               sb.append(extractLabel(edge));
               if (isReason) sb.append("**");
               sb.append("-> ");

               if (!iter.hasNext())
                  sb.append(edge.getTargetNode().getId());

               System.out.print(sb);
            }
            System.out.print("\n");
         }
      }

      graph.display(false);
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

   private List<List<Edge>> buildEdgeBasedCycles(final List<Vector<Node>> cyclesAsNodes)
   {
      List<List<Edge>> cyclesAsEdges = new ArrayList<>(cyclesAsNodes.size());
      for (final Vector<Node> cycleAsNodes : cyclesAsNodes)
      {
         closeCycle(cycleAsNodes);
         final ArrayList<Edge> cycleAsEdges = new ArrayList<Edge>();
         cyclesAsEdges.add(cycleAsEdges);
         Node previousNode = null;
         for (final Node node : cycleAsNodes)
         {
            if (previousNode != null)
               cycleAsEdges.add(previousNode.getEdgeToward(node));

            previousNode = node;
         }
      }
      return cyclesAsEdges;
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

      if (interactions.length == 1 && interactions[0] == Interaction.NONE)
         return;

      if (!containsNode(graph, sourceId) || !containsNode(graph, targetId))
         return;

      if (this.ignoreLoops && sourceId.equals(targetId))
         return;

      final String edgeId = sourceId + "->" + targetId;
      final Edge edge = graph.addEdge(edgeId, sourceId, targetId, true);
      edge.setAttribute("ui.label", formatEdgeLabel(interactions));
   }

   private static boolean containsNode(final Graph graph, final String sourceId)
   {
      return graph.getNode(sourceId) != null;
   }

   private static List<String> formatEdgeLabel(final Interaction... interactions)
   {
      return Arrays.stream(interactions)
            .map(interaction -> String.format("%s%s", interaction.getInteractionType().getMnemonic(), interaction.getInteractionCategory().getMnemonic()))
            .collect(Collectors.toList());
   }
}
