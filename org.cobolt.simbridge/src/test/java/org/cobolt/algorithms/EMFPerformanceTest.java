package org.cobolt.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.cobolt.model.Edge;
import org.cobolt.model.ModelFactory;
import org.cobolt.model.Node;
import org.cobolt.model.Topology;
import org.cobolt.model.listener.GraphContentAdapter;
import org.cobolt.model.utils.TopologyUtils;
import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.ecore.EAttribute;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class EMFPerformanceTest {

	@Ignore("Performance test")
	@Test
	public void testSorting() throws Exception {

		final Topology topology = ModelFactory.eINSTANCE.createTopology();
		Node previous = TopologyUtils.addNode(topology, "n1", Double.NaN);
		final int numberOfNodes = 10;
		for (int nodeCount = 2; nodeCount <= numberOfNodes; ++nodeCount) {
			final Node next = TopologyUtils.addNode(topology, "n" + nodeCount, Double.NaN);
			TopologyUtils.addEdge(topology, "e" + (nodeCount - 1) + "->" + nodeCount, previous.getId(), next.getId(),
					Math.random() * 100, Double.NaN);
			previous = next;
		}
		Assert.assertEquals(numberOfNodes, topology.getNodeCount());
		final List<Edge> copiedLinks = new ArrayList<>(topology.getEdges());

		topology.eAdapters().add(new GraphContentAdapter() {
			@Override
			protected void edgeAttributeChanged(final Edge edge, final EAttribute attribute, final Object oldValue) {
				super.edgeAttributeChanged(edge, attribute, oldValue);
				Assert.fail("Sorting should not trigger a notification, but something happened: " + edge + " - "
						+ attribute + " - " + oldValue);
			}
		});

		{
			final long tic = System.currentTimeMillis();
			ECollections.sort(topology.getEdges(), new Comparator<Edge>() {
				@Override
				public int compare(final Edge o1, final Edge o2) {
					final Edge link1 = o1;
					final Edge link2 = o2;
					return Double.compare(link1.getWeight(), link2.getWeight());
				}

			});
			final long toc = System.currentTimeMillis();
			final double durationInSeconds = (toc - tic) / 1e3;
			// System.out.println(topology.getEdges().stream().map(e ->
			// (Edge) e).map(Edge::getDistance)
			// .collect(Collectors.toList()));
			System.out.println("[EMFCollections] Duration for " + topology.getEdgeCount() + " edges: "
					+ durationInSeconds + " seconds");
		}

		{
			final long tic = System.currentTimeMillis();
			Collections.sort(copiedLinks, new Comparator<Edge>() {
				@Override
				public int compare(final Edge o1, final Edge o2) {
					final Edge link1 = o1;
					final Edge link2 = o2;
					return Double.compare(link1.getWeight(), link2.getWeight());
				}

			});
			final long toc = System.currentTimeMillis();
			final double durationInSeconds = (toc - tic) / 1e3;
			System.out.println("[java.util.Collections] Duration for " + copiedLinks.size() + " edges: "
					+ durationInSeconds + " seconds");
		}
	}
}
