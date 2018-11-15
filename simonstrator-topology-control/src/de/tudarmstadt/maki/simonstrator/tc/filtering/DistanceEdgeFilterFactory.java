package de.tudarmstadt.maki.simonstrator.tc.filtering;

import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyProperties;

public class DistanceEdgeFilterFactory implements EdgeFilterFactory {

	private Double threshold;

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	@Override
	public EdgeFilter createEdgeFilter() {
		return new AttributeComparingEdgeFilter<Number>(
				UnderlayTopologyProperties.DISTANCE, threshold);
	}

	@Override
	public String toString() {
		return String.format("DistanceEdgeFilterFactory [threshold=%.3f]", this.threshold);
	}
}
