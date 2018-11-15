package de.tudarmstadt.maki.simonstrator.tc.filtering;

import org.junit.Assert;
import org.junit.Test;

import de.tudarmstadt.maki.simonstrator.api.Graphs;
import de.tudarmstadt.maki.simonstrator.api.common.graph.GenericGraphElementProperties;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.GraphBuilder;

/**
 * Unit tests for {@link AttributeComparingEdgeFilter}
 */
public class AttributeComparingEdgeFilterTest {

	@Test
	public void testEmptyGraph() throws Exception {
		final Graph graph = Graphs.createGraph();
		final Graph filteredGraph = TopologyFilterUtils.filter(graph,
				new AttributeComparingEdgeFilter<>(GenericGraphElementProperties.WEIGHT, 3.0));

		Assert.assertEquals(0, filteredGraph.getNodeCount());
		Assert.assertEquals(0, filteredGraph.getEdgeCount());
	}

	@Test
	public void testSmallGraph() throws Exception {
		final Graph graph = GraphBuilder.create().//
				n("n1").n("n2").n("n3").//
				e("n1", "n2", "e12", 3.0).e("n1", "n3", "e13", 1.0).//
				done();
		final Graph filteredGraph = TopologyFilterUtils.filter(graph,
				new AttributeComparingEdgeFilter<>(GenericGraphElementProperties.WEIGHT, 3.0));

		Assert.assertEquals(3, graph.getNodeCount());
		Assert.assertEquals(2, graph.getEdgeCount());
		Assert.assertEquals(3, filteredGraph.getNodeCount());
		Assert.assertEquals(1, filteredGraph.getEdgeCount());
	}
}
