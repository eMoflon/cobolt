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
import static org.cobolt.tccpa.interactiongraph.RuleNames.R_DELETE_LOCK;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math3.util.Pair;
import org.graphstream.graph.Graph;

public class InteractionGraphLayout
{

   static final int factor = 300;

   static final int tcRuleColumn = 0 * factor;

   static final int minusEColumn = 1 * factor;

   static final int plusEColumn = 2 * factor;

   static final int modwColumn = 3 * factor;

   static final int unlockColumn = 4 * factor;

   static final int row1 = -1 * factor;

   static final int row2 = -2 * factor;

   static final int row3 = -3 * factor;

   static final int row4 = -4 * factor;

   static final int row5 = -5 * factor;

   static final Map<String, Pair<Integer, Integer>> RULE_NODE_POSITIONS = new HashMap<>();
   static
   {
      RULE_NODE_POSITIONS.put(R_A, Pair.create(tcRuleColumn, row1));
      RULE_NODE_POSITIONS.put(R_I, Pair.create(tcRuleColumn, row2));
      RULE_NODE_POSITIONS.put(R_FIND_U, Pair.create(tcRuleColumn, row3));
      RULE_NODE_POSITIONS.put(R_MINUS_E, Pair.create(minusEColumn, row1));
      RULE_NODE_POSITIONS.put(R_MINUS_EH1, Pair.create(minusEColumn, row2));
      RULE_NODE_POSITIONS.put(R_MINUS_EH2, Pair.create(minusEColumn, row3));
      RULE_NODE_POSITIONS.put(R_PLUS_E, Pair.create(plusEColumn, row1));
      RULE_NODE_POSITIONS.put(R_PLUS_EH1, Pair.create(plusEColumn, row4));
      RULE_NODE_POSITIONS.put(R_PLUS_EH2, Pair.create(plusEColumn, row5));
      RULE_NODE_POSITIONS.put(R_MOD_W, Pair.create(modwColumn, row1));
      RULE_NODE_POSITIONS.put(R_MOD_WH1, Pair.create(modwColumn, row2));
      RULE_NODE_POSITIONS.put(R_MOD_WH2, Pair.create(modwColumn, row3));
      RULE_NODE_POSITIONS.put(R_MOD_WH3, Pair.create(modwColumn, row4));
      RULE_NODE_POSITIONS.put(R_MOD_WH4, Pair.create(modwColumn, row5));
      RULE_NODE_POSITIONS.put(R_UNLOCK, Pair.create(unlockColumn, row1));
      RULE_NODE_POSITIONS.put(R_DELETE_LOCK, Pair.create(unlockColumn, row2));
   }

   public static Pair<Integer, Integer> getPosition(String ruleName)
   {
      final Pair<Integer, Integer> position = RULE_NODE_POSITIONS.get(ruleName);
      if (position != null)
         return position;
      else
         throw new IllegalArgumentException("Position for '" + ruleName + "' unknown.");
   }

   static void configureLayout(Graph graph)
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

   static final String ELEMENT_ATTRIBUTE_UILABEL = "ui.label";

   static final String NODE_ATTRIBUTE_XYZ = "xyz";
}
