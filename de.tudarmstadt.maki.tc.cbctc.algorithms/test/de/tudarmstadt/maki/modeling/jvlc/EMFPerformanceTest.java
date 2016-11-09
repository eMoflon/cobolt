package de.tudarmstadt.maki.modeling.jvlc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.ecore.EAttribute;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import de.tudarmstadt.maki.tc.cbctc.algorithms.AlgorithmsFactory;
import de.tudarmstadt.maki.tc.cbctc.algorithms.KTCLink;
import de.tudarmstadt.maki.tc.cbctc.algorithms.KTCNode;
import de.tudarmstadt.maki.tc.cbctc.algorithms.Topology;
import de.tudarmstadt.maki.tc.cbctc.model.Edge;
import de.tudarmstadt.maki.tc.cbctc.model.listener.GraphContentAdapter;

public class EMFPerformanceTest {

	@Ignore("Performance test")
	@Test
	public void testSorting() throws Exception {

		Topology topology = AlgorithmsFactory.eINSTANCE.createTopology();
		KTCNode previous = topology.addKTCNode("n1", Double.NaN);
		int numberOfNodes = 10;
		for (int nodeCount = 2; nodeCount <= numberOfNodes; ++nodeCount) {
			KTCNode next = topology.addKTCNode("n" + nodeCount, Double.NaN);
			topology.addKTCLink("e" + (nodeCount - 1) + "->" + nodeCount, previous, next, Math.random() * 100,
					Double.NaN);
			previous = next;
		}
		Assert.assertEquals(numberOfNodes, topology.getNodeCount());
		final List<Edge> copiedLinks = new ArrayList<>(topology.getEdges());

		topology.eAdapters().add(new GraphContentAdapter() {
			@Override
			protected void edgeAttributeChanged(Edge edge, EAttribute attribute, Object oldValue) {
				super.edgeAttributeChanged(edge, attribute, oldValue);
				Assert.fail("Sorting should not trigger a notification, but something happened: " + edge + " - "
						+ attribute + " - " + oldValue);
			}
		});

		{
			final long tic = System.currentTimeMillis();
			ECollections.sort(topology.getEdges(), new Comparator<Edge>() {
				@Override
				public int compare(Edge o1, Edge o2) {
					final KTCLink link1 = (KTCLink) o1;
					final KTCLink link2 = (KTCLink) o2;
					return Double.compare(link1.getWeight(), link2.getWeight());
				}

			});
			final long toc = System.currentTimeMillis();
			final double durationInSeconds = (toc - tic) / 1e3;
			// System.out.println(topology.getEdges().stream().map(e ->
			// (KTCLink) e).map(KTCLink::getDistance)
			// .collect(Collectors.toList()));
			System.out.println("[EMFCollections] Duration for " + topology.getEdgeCount() + " edges: "
					+ durationInSeconds + " seconds");
		}

		{
			final long tic = System.currentTimeMillis();
			Collections.sort(copiedLinks, new Comparator<Edge>() {
				@Override
				public int compare(Edge o1, Edge o2) {
					final KTCLink link1 = (KTCLink) o1;
					final KTCLink link2 = (KTCLink) o2;
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
