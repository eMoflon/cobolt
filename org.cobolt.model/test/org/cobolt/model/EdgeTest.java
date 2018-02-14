package org.cobolt.model;

import org.cobolt.model.Node;
import org.cobolt.model.Edge;
import org.cobolt.model.ModelFactory;
import org.cobolt.model.Topology;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class EdgeTest {

	ModelFactory factory = ModelFactory.eINSTANCE;

	private Topology topology;

	private Node node1;

	private Node node2;

	@Before
	public void setUp() throws Exception {
		this.topology = factory.createTopology();
		node1 = topology.addNode();
		node2 = topology.addNode();

		Assert.assertEquals(2, topology.getNodeCount());
		Assert.assertEquals(0, node1.getDegree());
		Assert.assertEquals(0, node2.getDegree());
	}

	@Test
	public void testInjectionMethods() throws Exception {
		topology.addDirectedEdge(node1, node2).toString();
	}

	@Test
	public void testIsReverseOf() throws Exception {
		final Edge edge1 = topology.addDirectedEdge(node1, node2);
		final Edge edge2 = topology.addDirectedEdge(node2, node1);

		Assert.assertTrue(edge1.isReverseEdgeOf(edge2));
		Assert.assertTrue(edge2.isReverseEdgeOf(edge1));
	}

	@Test
	public void testCreateMultiEdge() throws Exception {
		topology.addDirectedEdge(node1, node2);
		topology.addDirectedEdge(node1, node2);
		Assert.assertEquals(2, topology.getEdgeCount());
		Assert.assertEquals(2, node1.getOutdegree());
		Assert.assertEquals(2, node2.getIndegree());
	}
}
