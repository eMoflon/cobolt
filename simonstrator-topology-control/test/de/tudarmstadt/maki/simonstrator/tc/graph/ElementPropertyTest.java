package de.tudarmstadt.maki.simonstrator.tc.graph;

import org.junit.Assert;
import org.junit.Test;

import de.tudarmstadt.maki.simonstrator.api.common.graph.GraphElementProperty;

public class ElementPropertyTest {
	@Test
	public void testEqualityBasedOnNamesOnly() throws Exception {
		final GraphElementProperty<Double> p1 = new GraphElementProperty<>("my-value", Double.class);
		final GraphElementProperty<Double> p2 = new GraphElementProperty<>("my-value", Double.class);

		Assert.assertEquals(p1, p2);

		final GraphElementProperty<String> p3 = new GraphElementProperty<>("my-value", String.class);
		Assert.assertEquals(p1, p3);
	}
}
