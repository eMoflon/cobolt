package de.tudarmstadt.maki.simonstrator.tc.weighting;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.tudarmstadt.maki.simonstrator.api.common.graph.DirectedEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.tc.testing.TopologyControlTestHelper;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyProperties;

public class CsvEdgeWeightingFunctionTest {

	private CsvEdgeWeightingFunction function;

	@Before
	public void setup() throws Exception {
		this.function = new CsvEdgeWeightingFunction("1;2\n2;4\n3;9\n4;8", 0, 1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testTooShortLine() throws Exception {
		new CsvEdgeWeightingFunction("1;2", 1, 2);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDuplicates() throws Exception {
		new CsvEdgeWeightingFunction("1;2\n1;2", 0, 1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNoEntries() throws Exception {
		new CsvEdgeWeightingFunction("hello;world\nhi;there", 0, 1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNullEdge() throws Exception {
		function.calculateWeight(null, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testMissingDistance() throws Exception {
		function.calculateWeight(new DirectedEdge(INodeID.get("n1"), INodeID.get("n2")), null);
	}

	@Test
	public void testPerfectMatch() throws Exception {
		final IEdge edge = createEdge(1.0);
		final double weight = function.calculateWeight(edge, null);
		Assert.assertEquals(2.0, weight, TopologyControlTestHelper.EPS_6);
	}

	@Test
	public void testPerfectMatch2() throws Exception {
		final IEdge edge = createEdge(2.0);
		final double weight = function.calculateWeight(edge, null);
		Assert.assertEquals(4.0, weight, TopologyControlTestHelper.EPS_6);
	}

	@Test
	public void testInterpolationMean1() throws Exception {
		final IEdge edge = createEdge(2.5);
		final double weight = function.calculateWeight(edge, null);
		Assert.assertEquals(0.5 * (4 + 9), weight, TopologyControlTestHelper.EPS_6);
	}

	@Test
	public void testInterpolationMean2() throws Exception {
		final IEdge edge = createEdge(3.5);
		final double weight = function.calculateWeight(edge, null);
		Assert.assertEquals(0.5 * (9 + 8), weight, TopologyControlTestHelper.EPS_6);
	}

	@Test
	public void testInterpolationMean3() throws Exception {
		final IEdge edge = createEdge(1.1);
		final double weight = function.calculateWeight(edge, null);
		Assert.assertEquals(0.1 * 2 + 0.9 * 4, weight, TopologyControlTestHelper.EPS_6);
	}

	@Test
	public void testInterpolationMin() throws Exception {
		final IEdge edge = createEdge(0);
		final double weight = function.calculateWeight(edge, null);
		Assert.assertEquals(2, weight, TopologyControlTestHelper.EPS_6);
	}

	@Test
	public void testInterpolationMax() throws Exception {
		final IEdge edge = createEdge(5);
		final double weight = function.calculateWeight(edge, null);
		Assert.assertEquals(8, weight, TopologyControlTestHelper.EPS_6);
	}

	private static IEdge createEdge(double value) {
		final DirectedEdge edge = new DirectedEdge(INodeID.get("n1"), INodeID.get("n2"));
		edge.setProperty(UnderlayTopologyProperties.DISTANCE, value);
		return edge;
	}
}
