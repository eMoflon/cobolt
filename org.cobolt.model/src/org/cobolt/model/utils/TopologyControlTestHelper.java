package org.cobolt.model.utils;

import org.junit.Assert;

/**
 * Test utilities for simonstrator-topology-control
 * 
 * @author Roland Kluge - Initial implementation
 *
 */
public class TopologyControlTestHelper {

	/**
	 * Describes a tolerance of 0.0
	 */
	public static final double EPS_0 = 0.0;

	/**
	 * Describes a tolerance of 1e-6
	 */
	public static final double EPS_6 = 1e-6;

	/**
	 * Checks the given expected and actual value for equality with zero tolerance.
	 * 
	 * @param expected
	 *            the expected value
	 * @param actual
	 *            the actual value
	 * @see EPS_0
	 */
	public static void assertEquals0(final double expected, final double actual) {
		Assert.assertEquals(expected, actual, EPS_0);
	}

	/**
	 * Checks the given expected and actual value for equality with 1e-6 absolute
	 * tolerance.
	 * 
	 * @param expected
	 *            the expected value
	 * @param actual
	 *            the actual value
	 * @see EPS_6
	 */
	public static void assertEquals6(final double expected, final double actual) {
		Assert.assertEquals(expected, actual, EPS_6);
	}
}
