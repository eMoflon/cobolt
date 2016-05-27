package de.tudarmstadt.maki.modeling.jvlc;

import java.util.Comparator;

import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.ecore.EAttribute;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import de.tudarmstadt.maki.modeling.graphmodel.Edge;
import de.tudarmstadt.maki.modeling.graphmodel.listener.GraphContentAdapter;

public class EMFTest {

	@Ignore("Performance test")
	@Test
	public void testSorting() throws Exception {

		Topology topology = JvlcFactory.eINSTANCE.createTopology();
		KTCNode previous = topology.addKTCNode("n1", Double.NaN);
		int numberOfNodes = 100000;
		for (int nodeCount = 2; nodeCount <= numberOfNodes; ++nodeCount) {
			KTCNode next = topology.addKTCNode("n" + nodeCount, Double.NaN);
			topology.addKTCLink("e" + (nodeCount - 1) + "->" + nodeCount, previous, next, Math.random() * 100,
					Double.NaN);
			previous = next;
		}
		Assert.assertEquals(numberOfNodes, topology.getNodeCount());

		topology.eAdapters().add(new GraphContentAdapter() {
			@Override
			protected void edgeAttributeChanged(Edge edge, EAttribute attribute, Object oldValue) {
				super.edgeAttributeChanged(edge, attribute, oldValue);
				Assert.fail("Sorting should not trigger a notification, but something happened: " + edge + " - "
						+ attribute + " - " + oldValue);
			}
		});

		final long tic = System.currentTimeMillis();
		ECollections.sort(topology.getEdges(), new Comparator<Edge>() {
			@Override
			public int compare(Edge o1, Edge o2) {
				final KTCLink link1 = (KTCLink) o1;
				final KTCLink link2 = (KTCLink) o2;
				return Double.compare(link1.getDistance(), link2.getDistance());
			}

		});
		final long toc = System.currentTimeMillis();
		final double durationInSeconds = (toc - tic) / 1e3;
		System.out.println("Duration for " + topology.getEdgeCount() + " edges: " + durationInSeconds + " seconds");
	}
}
