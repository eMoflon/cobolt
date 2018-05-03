package org.cobolt.tccpa.interactiongraph;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import static org.cobolt.tccpa.interactiongraph.RuleNames.R_A;
import static org.cobolt.tccpa.interactiongraph.RuleNames.R_I;
import static org.cobolt.tccpa.interactiongraph.RuleNames.R_FIND_U;
import static org.cobolt.tccpa.interactiongraph.RuleNames.R_MINUS_E;
import static org.cobolt.tccpa.interactiongraph.RuleNames.R_MINUS_EH1;
import static org.cobolt.tccpa.interactiongraph.RuleNames.R_MINUS_EH2;
import static org.cobolt.tccpa.interactiongraph.RuleNames.R_PLUS_E;
import static org.cobolt.tccpa.interactiongraph.RuleNames.R_PLUS_EH1;
import static org.cobolt.tccpa.interactiongraph.RuleNames.R_PLUS_EH2;
import static org.cobolt.tccpa.interactiongraph.RuleNames.R_MOD_W;
import static org.cobolt.tccpa.interactiongraph.RuleNames.R_MOD_WH1;
import static org.cobolt.tccpa.interactiongraph.RuleNames.R_MOD_WH2;
import static org.cobolt.tccpa.interactiongraph.RuleNames.R_MOD_WH3;
import static org.cobolt.tccpa.interactiongraph.RuleNames.R_MOD_WH4;
import static org.cobolt.tccpa.interactiongraph.RuleNames.R_UNLOCK;
import static org.cobolt.tccpa.interactiongraph.RuleNames.R_DELETE_LOCK;;

public class LatexUtil
{

   public static final String CMD = "\\";

   public static final String NL = CMD + CMD;

   private static final String LATEX_GTR_PREFIX = CMD + "gtr";

   private static final Map<String, String> RULE_NAME_TO_LATEX = new HashMap<>();

   static
   {
      RULE_NAME_TO_LATEX.put(R_I, "I");
      RULE_NAME_TO_LATEX.put(R_A, "A");
      RULE_NAME_TO_LATEX.put(R_UNLOCK, "Unlock");
      RULE_NAME_TO_LATEX.put(R_DELETE_LOCK, "DeleteLock");
      RULE_NAME_TO_LATEX.put(R_MOD_WH4, "HandleLinkWeightModificationSelfI");
      RULE_NAME_TO_LATEX.put(R_MOD_WH3, "HandleLinkWeightModificationRemoteI");
      RULE_NAME_TO_LATEX.put(R_MOD_WH2, "HandleLinkWeightModificationSelfA");
      RULE_NAME_TO_LATEX.put(R_MOD_WH1, "HandleLinkWeightModificationRemoteA");
      RULE_NAME_TO_LATEX.put(R_MOD_W, "LinkWeightModification");
      RULE_NAME_TO_LATEX.put(R_PLUS_EH2, "HandleLinkAdditionRemote");
      RULE_NAME_TO_LATEX.put(R_PLUS_EH1, "HandleLinkAdditionSelf");
      RULE_NAME_TO_LATEX.put(R_PLUS_E, "LinkAddtion");
      RULE_NAME_TO_LATEX.put(R_MINUS_EH2, "HandleLinkRemovalRemote");
      RULE_NAME_TO_LATEX.put(R_MINUS_EH1, "HandleLinkRemovalSelf");
      RULE_NAME_TO_LATEX.put(R_MINUS_E, "LinkRemoval");
      RULE_NAME_TO_LATEX.put(R_FIND_U, "FindU");

      // Add common prefix to all rules
      RULE_NAME_TO_LATEX.keySet().forEach(key -> RULE_NAME_TO_LATEX.put(key, LATEX_GTR_PREFIX + RULE_NAME_TO_LATEX.get(key)));
   }

   public static String formatRule(final String loopConditionRule)
   {
      return RULE_NAME_TO_LATEX.get(loopConditionRule);
   }

   public static String formatLoopType(LoopType loopType)
   {
      switch (loopType)
      {
      case SUCCESS:
         return CMD + "guardSuccess";
      case FAILURE:
         return CMD + "guardFailure";
      default:
         throw new IllegalArgumentException();
      }
   }

   public static String getLatexCodeForSequence(InteractionSequence sequence)
   {
      final boolean reduceOperatorSpacing = false;
      final StringBuilder sb = new StringBuilder();
      sb.append("(");
      for (final Iterator<Interaction> iterator = sequence.getInteractions().iterator(); iterator.hasNext();)
      {
         final Interaction interaction = iterator.next();
         sb.append(formatRule(interaction.getLhsRule()));

         if (reduceOperatorSpacing)
            sb.append("{");

         sb.append(formatInteractionSymbol(interaction));

         if (reduceOperatorSpacing)
            sb.append("}");

         if (!iterator.hasNext())
         {
            sb.append(formatRule(interaction.getRhsRule()));
         }
      }
      sb.append(")");
      return sb.toString();
   }

   private static String formatInteractionSymbol(final Interaction interaction)
   {
      String prefix = CMD + "cda";
      switch (interaction.getType())
      {
      case CONFLICT:
         prefix += "Conflict";
         break;
      case DEPENDENCY:
         prefix += "Dependency";
         break;
      default:
         throw new IllegalArgumentException();
      }

      switch (interaction.getCategory())
      {
      case LOCAL:
         prefix += "Local";
         break;
      case REMOTE:
         prefix += "Remote";
         break;
      case SAME_MATCH:
         prefix += "SameMatch";
         break;
      default:
         throw new IllegalArgumentException();
      }

      return prefix + "Symbol";
   }

   static String formatInteractionTable(final Map<LoopCondition, List<InteractionSequence>> interactionSequences,
         final Map<LoopCondition, List<InteractionSequence>> filteredSequences)
   {
      final StringBuilder sb = new StringBuilder();
      sb.append("%\n");
      sb.append(String.format("%% Generated on %s\n", new SimpleDateFormat("YYYY-MM-dd HH:mm:ss").format(new Date())));
      final List<LoopCondition> loopConditions = new ArrayList<>(interactionSequences.keySet());
      Collections.sort(loopConditions);
      for (final Iterator<LoopCondition> iterator = loopConditions.iterator(); iterator.hasNext();)
      {
         final LoopCondition loopCondition = iterator.next();
         final String loopConditionRule = loopCondition.getConditionRuleName();
         final LoopType loopType = loopCondition.getType();
         sb.append(formatRule(loopConditionRule)).append("&\n");
         sb.append(formatLoopType(loopType)).append("&\n");

         final List<InteractionSequence> allSequences = interactionSequences.get(loopCondition);
         final List<InteractionSequence> sequences = filteredSequences.get(loopCondition);

         sb.append(CMD).append("makecell[l]{\n");
         sb.append("%").append(String.format("[%d filtered seqs., %d seqs.]\n", sequences.size(), allSequences.size()));
         if (sequences.isEmpty())
         {
            sb.append("$\\emptyset$\n");
         }

         for (final InteractionSequence sequence : sequences)
         {
            sb.append("$").append(getLatexCodeForSequence(sequence)).append("$%\n");
            sb.append("%").append(sequence.format()).append("\n");
            sb.append("%\n");
            sb.append(",").append(NL);
         }

         sb.append("}%makecell\n");
         sb.append(NL).append("\n");
         if (iterator.hasNext())
            sb.append(CMD).append("midrule\n");
      }
      sb.append("%\n");

      return sb.toString();
   }

}
