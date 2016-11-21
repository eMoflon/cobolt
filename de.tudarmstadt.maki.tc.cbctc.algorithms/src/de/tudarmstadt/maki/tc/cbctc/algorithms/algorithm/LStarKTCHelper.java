package de.tudarmstadt.maki.tc.cbctc.algorithms.algorithm;

public class LStarKTCHelper
{

   public static boolean checkPredicate(final int hopCount1, final int hopCount2, final int hopCount3, final double a)
   {
      if(Math.min(hopCount1, Math.min(hopCount2, hopCount3)) < 0)
         return false;
      else
         return
               hopCount1 == hopCount2 
            || implies(hopCount1 > hopCount2, (hopCount3 + 1) * 1.0 / Math.max(1, hopCount1) < a)
            || implies(hopCount1 < hopCount2, (hopCount3 + 1) * 1.0 / Math.max(1, hopCount2) < a);
   }

   private static boolean implies(boolean premise, boolean conclusion)
   {
      return !premise || conclusion;
   }

}
