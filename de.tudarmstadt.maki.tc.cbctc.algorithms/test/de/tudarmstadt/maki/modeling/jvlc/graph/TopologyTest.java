package de.tudarmstadt.maki.modeling.jvlc.graph;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import de.tudarmstadt.maki.tc.cbctc.model.Edge;
import de.tudarmstadt.maki.tc.cbctc.model.EdgeState;
import de.tudarmstadt.maki.tc.cbctc.model.ModelFactory;
import de.tudarmstadt.maki.tc.cbctc.model.Node;
import de.tudarmstadt.maki.tc.cbctc.model.Topology;
import de.tudarmstadt.maki.tc.cbctc.model.utils.TopologyUtils;

public class TopologyTest {

	private Topology topology;

	@Before
	public void setup() {
		this.topology = ModelFactory.eINSTANCE.createTopology();
	}

	@Test
	public void testCreateEdge() throws Exception {
		final Node n1 = TopologyUtils.addNode(this.topology, "n1", 1.2);
		final Node n2 = TopologyUtils.addNode(this.topology, "n2", 2.5);
		final Edge link = TopologyUtils.addEdge(this.topology, "e12", n1, n2, 15.7, 2.9);
		Assert.assertSame(EdgeState.UNCLASSIFIED, link.getState());

		final Edge link12 = this.topology.getEdgeById("e12");
		Assert.assertSame(link, link12);

		Assert.assertSame(n2, this.topology.getNodeById("n2"));
		Assert.assertNull(topology.getNodeById("not-there"));
	}

	@Ignore("Performance test for unclassification effort")
	@Test
	public void testRuntimeOfLinkUnclassification() throws Exception {
		for (int edgeCount : Arrays.asList((int) 1e3, (int) 1e4, (int)5e4, (int) 1e5, (int) 2e5, (int) 5e5, (int) 1e6)) {
			Node previousNode = TopologyUtils.addNode(this.topology, "n" + 1, 0.0);
			for (int i = 2; i <= edgeCount; ++i) {
				Node currentNode = TopologyUtils.addNode(this.topology, "n" + i, 0.0);
				TopologyUtils.addEdge(this.topology, "e" + (i - 1) + "-" + i, previousNode, currentNode, 1, 1, EdgeState.ACTIVE);
				previousNode = currentNode;
			}

			for (int runs = 0; runs <= 10; ++runs) {
				final long tic = System.currentTimeMillis();
				for (final Edge edge : topology.getEdges()) {
					edge.setState(EdgeState.UNCLASSIFIED);
				}
				final long toc = System.currentTimeMillis();

				final int graphSize = topology.getEdgeCount();
				final long elapsedTime = toc - tic;
				// System.out.println("Size: " + graphSize + " - Elapsed time: "
				// + elapsedTime + "ms.");
				System.out.println(graphSize + ";" + elapsedTime);
			}
		}
	}
}
