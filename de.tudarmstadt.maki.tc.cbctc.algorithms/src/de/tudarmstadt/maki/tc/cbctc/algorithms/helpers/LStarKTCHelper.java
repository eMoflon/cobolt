package de.tudarmstadt.maki.tc.cbctc.algorithms.helpers;

import de.tudarmstadt.maki.simonstrator.tc.testing.TopologyControlTestHelper;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyControlAlgorithms;

/**
 * Helper class for {@link UnderlayTopologyControlAlgorithms#LSTAR_KTC}
 * 
 * @author Roland Kluge - Initial implementation
 *
 */
public class LStarKTCHelper
{
   /**
    * This helper method checks whether the l*-kTC-specific condition holds, given the three hop count values and the strech limit a.
    * @param hopCount1
    * @param hopCount2
    * @param hopCount3
    * @param a see {@link UnderlayTopologyControlAlgorithms#LSTAR_KTC_PARAM_A}
    * @return whether the predicate is fulfilled
    */
   public static boolean checkPredicate(final int hopCount1, final int hopCount2, final int hopCount3, final double a)
   {
      if (Math.min(hopCount1, Math.min(hopCount2, hopCount3)) < 0)
         return false;
      else
         return TopologyControlTestHelper.implies(hopCount1 == hopCount2, true)
               && TopologyControlTestHelper.implies(hopCount1 > hopCount2, (hopCount3 + 1) * 1.0 / Math.max(1, hopCount1) < a)
               && TopologyControlTestHelper.implies(hopCount1 < hopCount2, (hopCount3 + 1) * 1.0 / Math.max(1, hopCount2) < a);
   }

}
