package org.cobolt.tccpa.interactiongraph;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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

   public static final String R_MOD_WH4 = "Rmod-w,h,4";

   public static final String R_MOD_WH3 = "Rmod-w,h,3";

   public static final String R_MOD_WH2 = "Rmod-w,h,2";

   public static final String R_MOD_WH1 = "Rmod-w,h,1";

   public static final String R_UNLOCK = "Runlock";

   /**
    * Contains the names of the rules that only appear in the long version of the paper
    */
   private static final Set<String> LONG_VERSION_RULES = new HashSet<>(
         Arrays.asList(R_MOD_W, R_MOD_WH1, R_MOD_WH2, R_MOD_WH3, R_MOD_WH4, R_PLUS_E, R_PLUS_EH1, R_PLUS_EH2));

   public static boolean shallOmitRule(final String ruleName, final boolean isLongVersion)
   {
      if (LONG_VERSION_RULES.contains(ruleName))
         return !isLongVersion;
      else
         return false;
   }
}
