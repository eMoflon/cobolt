package de.tudarmstadt.maki.tc.cbctc.model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.tudarmstadt.maki.tc.cbctc.model.Edge;
import de.tudarmstadt.maki.tc.cbctc.model.Graph;
import de.tudarmstadt.maki.tc.cbctc.model.ModelFactory;
import de.tudarmstadt.maki.tc.cbctc.model.Node;

public class EdgeTest {

	ModelFactory factory = ModelFactory.eINSTANCE;

	private Graph graph;

	private Node node1;

	private Node node2;

	@Before
	public void setUp() throws Exception {
		this.graph = factory.createGraph();
		node1 = graph.addNode();
		node2 = graph.addNode();

		Assert.assertEquals(2, graph.getNodeCount());
		Assert.assertEquals(0, node1.getDegree());
		Assert.assertEquals(0, node2.getDegree());
	}

	@Test
	public void testInjectionMethods() throws Exception {
		graph.addDirectedEdge(node1, node2).toString();
	}

	@Test
	public void testIsReverseOf() throws Exception {
		final Edge edge1 = graph.addDirectedEdge(node1, node2);
		final Edge edge2 = graph.addDirectedEdge(node2, node1);

		Assert.assertTrue(edge1.isReverseEdgeOf(edge2));
		Assert.assertTrue(edge2.isReverseEdgeOf(edge1));
	}

	@Test
	public void testCreateMultiEdge() throws Exception {
		graph.addDirectedEdge(node1, node2);
		graph.addDirectedEdge(node1, node2);
		Assert.assertEquals(2, graph.getEdgeCount());
		Assert.assertEquals(2, node1.getOutdegree());
		Assert.assertEquals(2, node2.getIndegree());
	}
}
