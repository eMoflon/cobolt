package de.tudarmstadt.maki.simonstrator.tc.filtering;

import java.util.Comparator;

import de.tudarmstadt.maki.simonstrator.api.common.graph.GraphElementProperties;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.component.sis.type.SiSType;

public class AttributeComparingEdgeFilter<T extends Number> implements EdgeFilter {

	private final T threshold;
	private final SiSType<? extends T> property;
	private final Comparator<T> comparator;

	public AttributeComparingEdgeFilter(final SiSType<? extends T> property, final T threshold) {
		this(property, threshold, new Comparator<T>() {
			@Override
			public int compare(T o1, T o2) {
				return Double.compare(o1.doubleValue(), o2.doubleValue());
			}
		});
	}

	public AttributeComparingEdgeFilter(final SiSType<? extends T> attribute, final T threshold,
			Comparator<T> comparator) {
		this.threshold = threshold;
		this.property = attribute;
		this.comparator = comparator;
	}

	@Override
	public boolean ignoreEdge(IEdge edge) {
		GraphElementProperties.validateThatPropertyIsPresent(edge, this.property);
		return comparator.compare(edge.getProperty(this.property), this.threshold) < 0;
	}

}
