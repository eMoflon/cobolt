package de.tudarmstadt.maki.modeling.jvlc;

import org.junit.Assert;

import de.tudarmstadt.maki.tc.cbctc.algorithms.Topology;

public final class JvlcTestHelper {
	private JvlcTestHelper() {
		throw new UnsupportedOperationException();
	}

	public static final double EPS_0 = 0.0;
	public static final double EPS_6 = 1e-6;

	public static String getPathToDistanceTestGraph(final int i) {
		return "instances/testgraph_D" + i + ".grapht";
	}

	public static String getPathToEnergyTestGraph(final int i) {
		return "instances/testgraph_E" + i + ".grapht";
	}

	public static void assertHasDistance(final Topology topology, final String id, final double distance) {
		Assert.assertEquals("Distance mismatch of " + id + ".", distance, topology.getKTCLinkById(id).getWeight(),
				EPS_0);
	}

}