package de.tudarmstadt.maki.tc.cbctc.algorithms.helpers;

import de.tudarmstadt.maki.tc.cbctc.algorithms.LStarKTC;

/**
 * Helper class for {@link LStarKTC}
 * 
 * @author Roland Kluge - Initial implementation
 *
 */
public class LStarKTCHelper
{
   /**
    * This helper method checks whether the l*-kTC-specific condition holds, given the three hop count values and the strech limit a.
    * 
    * Link e12 is the removal candidate, node3 is the relay node.
    * 
    * @param hopCount1
    * @param hopCount2
    * @param hopCount3
    * @param a see UnderlayTopologyControlAlgorithms#LSTAR_KTC_PARAM_A
    * @return whether the predicate is fulfilled
    */
   public static boolean checkPredicate(final int hopCount1, final int hopCount2, final int hopCount3, final double a)
   {
      if (Math.min(hopCount1, Math.min(hopCount2, hopCount3)) < 0)
         return false;
      else
         return LogicHelper.implies(hopCount1 == hopCount2, true)
               && LogicHelper.implies(hopCount1 > hopCount2, (hopCount3 + 1) * 1.0 / Math.max(1, hopCount1) < a)
               && LogicHelper.implies(hopCount1 < hopCount2, (hopCount3 + 1) * 1.0 / Math.max(1, hopCount2) < a);
   }

}
