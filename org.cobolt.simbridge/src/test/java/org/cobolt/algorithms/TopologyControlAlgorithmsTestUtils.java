package org.cobolt.algorithms;

import static org.junit.Assert.assertEquals;

import org.cobolt.algorithms.facade.EMoflonFacade;
import org.cobolt.model.Edge;
import org.junit.Assert;

import de.tudarmstadt.maki.simonstrator.tc.facade.ITopologyControlFacade;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmParamters;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyControlAlgorithms;

/**
 * Test utilities for the (model-based) topology control algorithms
 *
 * @author Roland Kluge - Initial implementation
 *
 */
public final class TopologyControlAlgorithmsTestUtils {
	private TopologyControlAlgorithmsTestUtils() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Returns the path to the distance-related test case with the given index
	 *
	 * @param i
	 *              the index
	 * @return the project-relative path to the test case
	 */
	public static String getPathToDistanceTestGraph(final int i) {
		return "instances/testgraph_D" + i + ".grapht";
	}

	/**
	 * Returns the path to the energy-related test case with the given index
	 *
	 * @param i
	 *              the index
	 * @return the project-relative path to the test case
	 */
	public static String getPathToEnergyTestGraph(final int i) {
		return "instances/testgraph_E" + i + ".grapht";
	}

	/**
	 * Returns the path to the hop-count-related test case with the given index
	 *
	 * @param i
	 *              the index
	 * @return the project-relative path to the test case
	 */
	public static String getPathToHopCountTestGraph(final int i) {
		return "instances/testgraph_H" + i + ".grapht";
	}

	/**
	 * Returns the path to the angle-related test case with the given index
	 *
	 * @param i
	 *              the index
	 * @return the project-relative path to the test case
	 */
	public static String getPathToAngleTestGraph(final int i) {
		return "instances/testgraph_A" + i + ".grapht";
	}

	/**
	 * Asserts that the weight attribute of each edge in the facade's topology is
	 * set, i.e., unequal to EMoflonFacade#DEFAULT_VALUE_FOR_UNDEFINED_ATTRIBUTES
	 *
	 * @param facade
	 */
	public static void assertWeightSet(final EMoflonFacade facade) {
		// @formatter:off
		final Edge edgeWithUnsetWeight = facade.getTopology().getEdges().stream().filter(
				e -> new Double(e.getWeight()).equals(new Double(EMoflonFacade.DEFAULT_VALUE_FOR_UNDEFINED_ATTRIBUTES)))
				.findAny().orElse(null);
		// @formatter:on
		Assert.assertNull(String.format("The weight of the following edge is not set: '%s'", edgeWithUnsetWeight),
				edgeWithUnsetWeight);
	}

	/**
	 * Invokes the given facade that must be configured to run a kTC-style algorithm
	 * with the given k-value.
	 *
	 * @param facade
	 *                   the given facade
	 * @param k
	 *                   see {@link UnderlayTopologyControlAlgorithms#KTC_PARAM_K}
	 */
	public static void runFacadeKTC(final ITopologyControlFacade facade, final double k) {
		facade.run(TopologyControlAlgorithmParamters.create(UnderlayTopologyControlAlgorithms.KTC_PARAM_K, k));
	}

	/**
	 * Configures the given facade to run
	 * {@link UnderlayTopologyControlAlgorithms#LSTAR_KTC}
	 *
	 * @param facade
	 *                   the facade
	 * @param k
	 *                   see {@link UnderlayTopologyControlAlgorithms#KTC_PARAM_K}
	 * @param a
	 *                   see
	 *                   {@link UnderlayTopologyControlAlgorithms#LSTAR_KTC_PARAM_A}
	 */
	public static void runFacadeLStarKTC(final ITopologyControlFacade facade, final double k, final double a) {
		facade.run(TopologyControlAlgorithmParamters.create(UnderlayTopologyControlAlgorithms.KTC_PARAM_K, k,
				UnderlayTopologyControlAlgorithms.LSTAR_KTC_PARAM_A, a));
	}

	/**
	 * Configures the given facade to run
	 * {@link UnderlayTopologyControlAlgorithms#YAO}
	 *
	 * @param facade
	 *                      the facade
	 * @param coneCount
	 *                      see
	 *                      {@link UnderlayTopologyControlAlgorithms#YAO_PARAM_CONE_COUNT}
	 */
	public static void runFacadeYao(final ITopologyControlFacade facade, final int coneCount) {
		facade.run(TopologyControlAlgorithmParamters.create(UnderlayTopologyControlAlgorithms.YAO_PARAM_CONE_COUNT,
				coneCount));
	}

	/**
	 * Checks for constraint violations after an invocation of the TC algorithm
	 *
	 * @param facade
	 *                   the facade to use
	 */
	public static void assertNoConstraintViolationsAfterTopologyControl(final EMoflonFacade facade) {
		facade.checkConstraintsAfterTopologyControl();
		assertEquals(0, facade.getConstraintViolationCount());
	}

	/**
	 * Checks for constraint violations after an invocation of a context event
	 * handler
	 *
	 * @param facade
	 *                   the facade to use
	 */
	public static void assertNoConstraintViolationsAfterContextEventHandling(final EMoflonFacade facade) {
		facade.checkConstraintsAfterContextEvent();
		assertEquals(0, facade.getConstraintViolationCount());
	}

}