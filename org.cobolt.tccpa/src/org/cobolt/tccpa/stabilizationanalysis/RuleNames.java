package org.cobolt.tccpa.stabilizationanalysis;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * The rule names and utility methods related to the CSV data format
 *
 * @author Roland Kluge - Initial implementation
 *
 */
public class RuleNames
{
   public static final String R_FIND_U = "Rfind-u";

   public static final String R_I = "Ri";

   public static final String R_A = "Ra";

   public static final String R_MINUS_E = "R-e";

   public static final String R_MINUS_EH1 = "R-e,h,1";

   public static final String R_MINUS_EH2 = "R-e,h,2";

   public static final String R_PLUS_E = "R+e";

   public static final String R_PLUS_EH1 = "R+e,h,1";

   public static final String R_PLUS_EH2 = "R+e,h,2";

   public static final String R_MOD_W = "Rmod-w";

   public static final String R_MOD_WH1 = "Rmod-w,h,1";

   public static final String R_MOD_WH2 = "Rmod-w,h,2";

   public static final String R_MOD_WH3 = "Rmod-w,h,3";

   public static final String R_MOD_WH4 = "Rmod-w,h,4";

   public static final String R_PLUS_N = "R+n";

   public static final String R_MINUS_N = "R-n";

   public static final String R_UNLOCK = "Runlock";

   public static final String R_DELETE_LOCK = "Rdellock";

   /**
    * Contains the names of the rules that only appear in the long version of the paper
    */
   private static final Set<String> LONG_VERSION_RULES = new HashSet<>(
         Arrays.asList(R_MOD_W, R_MOD_WH1, R_MOD_WH2, R_MOD_WH3, R_MOD_WH4, R_PLUS_E, R_PLUS_EH1, R_PLUS_EH2));

   private static final int PROCESS_REGION_TC = 0;

   private static final int PROCESS_REGION_MINUS_E = 1;

   private static final int PROCESS_REGION_PLUS_E = 2;

   private static final int PROCESS_REGION_MOD_W = 3;

   private static final int PROCESS_REGION_UNLOCK = 4;

   public static boolean shallOmitRule(final String ruleName, final boolean isLongVersion)
   {
      if (LONG_VERSION_RULES.contains(ruleName))
         return !isLongVersion;
      else
         return false;
   }

   public static boolean isContextEventRule(String rule)
   {
      switch (rule)
      {
      case R_MINUS_E:
      case R_PLUS_E:
      case R_MOD_W:
         return true;
      default:
         return false;
      }
   }

   public static boolean areInSameProcessRegion(final String rule1, final String rule2)
   {
      if (getProcessRegionId(rule1) == getProcessRegionId(rule2))
         return true;

      for (final String firstRule : Arrays.asList(rule1, rule2))
      {
         final String secondRule = firstRule.equals(rule1) ? rule2 : rule1;
         if (R_UNLOCK.equals(firstRule) && isContextEventHandlerProcessRegion(getProcessRegionId(secondRule)))
         {
            return true;
         }
      }
      return false;
   }

   private static boolean isContextEventHandlerProcessRegion(int processRegionId)
   {
      return Arrays.asList(PROCESS_REGION_MINUS_E, PROCESS_REGION_PLUS_E, PROCESS_REGION_MOD_W, PROCESS_REGION_UNLOCK).contains(processRegionId);
   }

   public static int getProcessRegionId(final String rule)
   {
      switch (rule)
      {
      case R_FIND_U:
      case R_I:
      case R_A:
         return PROCESS_REGION_TC;
      case R_MINUS_E:
      case R_MINUS_EH1:
      case R_MINUS_EH2:
         return PROCESS_REGION_MINUS_E;
      case R_PLUS_E:
      case R_PLUS_EH1:
      case R_PLUS_EH2:
         return PROCESS_REGION_PLUS_E;
      case R_MOD_W:
      case R_MOD_WH1:
      case R_MOD_WH2:
      case R_MOD_WH3:
      case R_MOD_WH4:
         return PROCESS_REGION_MOD_W;
      case R_UNLOCK:
         return PROCESS_REGION_UNLOCK;
      default:
         throw new IllegalArgumentException("No progress regions known for " + rule);
      }
   }

}
