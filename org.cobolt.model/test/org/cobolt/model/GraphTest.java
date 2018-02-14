package org.cobolt.model;

import java.util.Arrays;

import org.cobolt.model.Node;
import org.cobolt.model.Edge;
import org.cobolt.model.ModelFactory;
import org.cobolt.model.Topology;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class GraphTest {

	final ModelFactory factory = ModelFactory.eINSTANCE;

	private Topology graph;

	@Before
	public void setUp() throws Exception {
		this.graph = factory.createTopology();
	}

	@Test
	public void testAddNode() throws Exception {
		final Node node = graph.addNode();
		Assert.assertEquals(graph, node.getTopology());
		Assert.assertEquals(Arrays.asList(node), graph.getNodes());
	}

	@Test
	public void testAddDirectedEdge() throws Exception {
		final Node source = graph.addNode();
		final Node target = graph.addNode();
		final Edge edge = graph.addDirectedEdge(source, target);
		Assert.assertEquals(graph, edge.getTopology());
		Assert.assertEquals(Arrays.asList(edge), graph.getEdges());

	}

	@Test
	public void testAddUndirectedEdge() throws Exception {
		final Node node1 = graph.addNode();
		final Node node2 = graph.addNode();
		graph.addUndirectedEdge(node1, node2);

		final EList<Edge> edges = graph.getEdges();
		final Edge edge1 = edges.get(0);
		final Edge edge2 = edges.get(1);
		Assert.assertEquals(graph, edge1.getTopology());
		Assert.assertEquals(graph, edge2.getTopology());
		Assert.assertEquals(2, edges.size());
		Assert.assertTrue(edge1.isReverseEdgeOf(edge2));

		final Node node3 = graph.addNode();
		final Edge edge3 = graph.addUndirectedEdge("fwd", "bwd", node1, node3);
		final Edge edge4 = edge3.getReverseEdge();
		Assert.assertEquals(4, edges.size());
		Assert.assertEquals("fwd", edge3.getId());
		Assert.assertEquals("bwd", edge4.getId());

	}

	@Test
	public void testGetEdgeById() throws Exception {
		final Node node1 = graph.addNode();
		final Node node2 = graph.addNode();
		final Edge edge = graph.addUndirectedEdge(node1, node2);
		edge.setId("e12");
		edge.getReverseEdge().setId(""); // No edge id may be null!

		Assert.assertEquals(edge, graph.getEdgeById("e12"));
		Assert.assertEquals(null, graph.getEdgeById("e21"));

		Assert.assertEquals(2, graph.getEdgeCount());
		graph.removeEdgeById("e12");
		Assert.assertEquals(1, graph.getEdgeCount());
	}

	@Test
	public void testGetNodeById() throws Exception {
		final Node node1 = graph.addNode();
		node1.setId("n1");

		Assert.assertEquals(node1, graph.getNodeById("n1"));
		Assert.assertEquals(null, graph.getNodeById("n2"));

		graph.removeNodeById("n1");
		Assert.assertEquals(0, graph.getNodeCount());
	}

	@Test
	public void testRemoveNodeWithIncidentEdges() throws Exception {
		final Node n1 = graph.addNode();
		final Node n2 = graph.addNode();
		graph.addUndirectedEdge(n1, n2);

		Assert.assertEquals(2, graph.getEdgeCount());
		graph.removeNode(n1);
		Assert.assertEquals(0, graph.getEdgeCount());
		Assert.assertEquals(1, graph.getNodeCount());
	}

	@Test
	public void testSetEdgeWeight() throws Exception {
		final Node source = graph.addNode();
		final Node target = graph.addNode();
		final Edge edge = graph.addDirectedEdge(source, target);
		edge.setWeight(3.5);
		Assert.assertEquals(3.5, edge.getWeight(), 0.0);
	}

	@Test
	@Ignore
	public void testForMemoryLeaks() throws Exception {
		for (int i = 0; i < (int) 1e6; ++i) {
			final Node node = graph.addNode("n" + i);
			EcoreUtil.delete(node);
			Thread.sleep(20);
		}
	}

}
