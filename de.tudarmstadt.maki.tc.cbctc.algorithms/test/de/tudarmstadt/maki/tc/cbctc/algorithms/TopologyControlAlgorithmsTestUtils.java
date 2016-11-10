package de.tudarmstadt.maki.tc.cbctc.algorithms;

public final class TopologyControlAlgorithmsTestUtils {
	private TopologyControlAlgorithmsTestUtils() {
		throw new UnsupportedOperationException();
	}

	public static String getPathToDistanceTestGraph(final int i) {
		return "instances/testgraph_D" + i + ".grapht";
	}

	public static String getPathToEnergyTestGraph(final int i) {
		return "instances/testgraph_E" + i + ".grapht";
	}

}