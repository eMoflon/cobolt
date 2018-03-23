package org.cobolt.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.StopWatch;
import org.cobolt.model.Node;
import org.cobolt.model.Edge;
import org.cobolt.model.ModelFactory;
import org.cobolt.model.Topology;
import org.cobolt.model.impl.NodeImpl;
import org.eclipse.emf.common.util.EList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link Node} and {@link NodeImpl}
 * 
 * @author Roland Kluge - Initial implementation
 */
public class NodeTest {
	private Topology topology;

	private Node n1;

	private Node n2;

	private Node n3;

	private Node n4;

	private static final ModelFactory modelFactory = ModelFactory.eINSTANCE;

	@Before
	public void setup() {
		this.topology = modelFactory.createTopology();
		this.n1 = createNode("n1");
		this.n2 = createNode("n2");
		this.n3 = createNode("n3");
		this.n4 = createNode("n4");
	}

	@Test
	public void testBuildLocalView_NoSelfNode() throws Exception {
		this.n1.setLocalViewHorizon(2);
		this.n1.buildLocalView();
	}

	@Test
	public void testBuildLocalView_ZeroView() throws Exception {
		this.n1.setLocalViewHorizon(0);
		Edge e12 = createEdge(n1, n2);
		Edge e13 = createEdge(n1, n3);
		topology.getNodes().addAll(Arrays.asList(n1, n2, n3));
		topology.getEdges().addAll(Arrays.asList(e12, e13));

		this.n1.buildLocalView();

		EList<Edge> localView = this.n1.getEdgesInLocalView();
		Assert.assertEquals(0, localView.size());
	}

	@Test
	public void testBuildLocalView_AllOutgoingAndContained() throws Exception {
		this.n1.setLocalViewHorizon(1);
		Edge e12 = createEdge(n1, n2);
		Edge e13 = createEdge(n1, n3);
		topology.getNodes().addAll(Arrays.asList(n1, n2, n3));
		topology.getEdges().addAll(Arrays.asList(e12, e13));

		this.n1.buildLocalView();

		EList<Edge> localView = this.n1.getEdgesInLocalView();
		Assert.assertTrue(localView.contains(e12));
		Assert.assertTrue(localView.contains(e13));
	}

	@Test
	public void testBuildLocalView_OutgoingAndIncomingAndAllContained() throws Exception {
		Edge e12 = createEdge(n1, n2);
		Edge e31 = createEdge(n3, n1);
		topology.getNodes().addAll(Arrays.asList(n1, n2, n3));
		topology.getEdges().addAll(Arrays.asList(e12, e31));

		this.n1.setLocalViewHorizon(1);
		this.n1.buildLocalView();

		EList<Edge> localView = this.n1.getEdgesInLocalView();
		Assert.assertTrue(localView.contains(e12));
		Assert.assertTrue(localView.contains(e31));
	}

	@Test
	public void testBuildLocalView_OutgoingAndIncomingAndSomeNotContained() throws Exception {
		Edge e12 = createEdge(n1, n2);
		Edge e31 = createEdge(n3, n1);
		Edge e34 = createEdge(n3, n4);
		Edge e43 = createEdge(n4, n3);
		topology.getNodes().addAll(Arrays.asList(n1, n2, n3));
		topology.getEdges().addAll(Arrays.asList(e12, e31, e34, e43));

		this.n1.setLocalViewHorizon(1);
		this.n1.buildLocalView();

		final EList<Edge> localView = this.n1.getEdgesInLocalView();
		Assert.assertTrue(localView.contains(e12));
		Assert.assertTrue(localView.contains(e31));
		Assert.assertFalse(localView.contains(e34));
		Assert.assertFalse(localView.contains(e43));

		// Check whether local view is properly updated (I)
		this.n1.setLocalViewHorizon(0);
		this.n1.buildLocalView();
		Assert.assertEquals(0, localView.size());

		// Check whether local view is properly updated (II)
		this.n1.setLocalViewHorizon(2);
		this.n1.buildLocalView();
		Assert.assertTrue(localView.contains(e12));
		Assert.assertTrue(localView.contains(e31));
		Assert.assertTrue(localView.contains(e34));
		Assert.assertTrue(localView.contains(e43));
	}

	@Test
	public void testBuildLocalView_Scalability() throws Exception {
		final double creationProbability = 0.005;
		final Random random = new Random(100123);
		final int nodeCount = 1000;
		List<Node> newNodes = streamIntArray(nodeCount).stream().map(NodeTest::createNode).collect(Collectors.toList());
		topology.getNodes().addAll(newNodes);
		newNodes.forEach(n1 -> newNodes.stream()//
				.filter(n2 -> !n2.equals(n1))//
				.forEach(n2 -> {
					if (creationProbability > random.nextDouble()) {
						topology.getEdges().add(createEdge(n1, n2));
					}
				}));

		double nodeDegree = getAverageNodeDegree(topology);

		System.out.println(String.format("Node degree: %.2f", nodeDegree));

		for (int k = 0; k < 10; ++k) {
			StopWatch watch = new StopWatch();
			watch.start();
			this.n1.setLocalViewHorizon(k);
			this.n1.buildLocalView();
			watch.stop();
			int edgeCountInLocalView = this.n1.getEdgesInLocalView().size();
			System.out.println(String.format("#k=%d, localViewCount=%d, time=%.2fms", k, edgeCountInLocalView,
					watch.getNanoTime() / 1e6));
		}
	}

	private static double getAverageNodeDegree(final Topology topology) {
		return topology.getNodes().stream().map(node -> node.getDegree()).reduce((a, b) -> a + b).get()
				/ ((double) topology.getNodeCount());
	}

	private List<Integer> streamIntArray(final int count) {
		List<Integer> list = new ArrayList<>();
		for (int value = 1; value <= count; ++value) {
			list.add(value);
		}
		return list;
	}

	private Edge createEdge(Node n1, Node n2) {
		Edge e12 = modelFactory.createEdge();
		e12.setSource(n1);
		e12.setTarget(n2);
		return e12;
	}

	private static Node createNode(int index) {
		return createNode("n" + index);
	}

	private static Node createNode(String id) {
		Node n1 = modelFactory.createNode();
		n1.setId(id);
		return n1;
	}
}
