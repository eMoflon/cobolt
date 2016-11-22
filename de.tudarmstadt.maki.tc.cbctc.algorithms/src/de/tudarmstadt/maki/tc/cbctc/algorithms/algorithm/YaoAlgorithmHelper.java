package de.tudarmstadt.maki.tc.cbctc.algorithms.algorithm;

import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyControlAlgorithms;

/**
 * Helper class for {@link UnderlayTopologyControlAlgorithms#YAO}
 * 
 * @author Roland Kluge - Initial implementation
 *
 */
public class YaoAlgorithmHelper
{
   /**
    * This helper method checks whether the l*-kTC-specific condition holds, given the three hop count values and the strech limit a.
    * @param edgeAngle12
    * @param edgeAngle13
    * @param coneCount see {@link UnderlayTopologyControlAlgorithms#YAO_CONE_COUNT}
    * @return whether the predicate is fulfilled
    */
   public static boolean checkPredicate(final double edgeAngle12, final double edgeAngle13, final double coneCount)
   {
      return true;
   }

}
