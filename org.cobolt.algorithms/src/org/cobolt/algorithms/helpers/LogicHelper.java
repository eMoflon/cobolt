package org.cobolt.algorithms.helpers;

class LogicHelper {

	/**
	 * Returns the logic implication of the given premise and conclusion variables
	 *
	 * @param premise
	 * @param conclusion
	 * @return premise => conclusion
	 */
	static boolean implies(final boolean premise, final boolean conclusion) {
		return !premise || conclusion;
	}
}
