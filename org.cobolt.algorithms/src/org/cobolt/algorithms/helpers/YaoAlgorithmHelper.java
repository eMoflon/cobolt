package org.cobolt.algorithms.helpers;

import org.cobolt.algorithms.YaoGraphAlgorithm;

/**
 * Helper class for {@link YaoGraphAlgorithm}
 * 
 * @author Roland Kluge - Initial implementation
 *
 */
public class YaoAlgorithmHelper {
	/**
	 * This helper method checks whether the l*-kTC-specific condition holds, given
	 * the three hop count values and the strech limit a.
	 * 
	 * @param edgeAngle12
	 *            angle of the removal candidate
	 * @param edgeAngle13
	 *            angle of some other link in the same cone
	 * @param coneCount
	 *            see UnderlayTopologyControlAlgorithms#YAO_PARAM_CONE_COUNT
	 * @return whether the predicate is fulfilled
	 */
	public static boolean checkPredicate(final double edgeAngle12, final double edgeAngle13, final int coneCount) {
		final double anglePerCone = 360.0 / coneCount;
		final int cone12 = (int) Math.floor(edgeAngle12 / anglePerCone);
		final int cone13 = (int) Math.floor(edgeAngle13 / anglePerCone);
		return cone12 % coneCount == cone13 % coneCount;
	}

}
