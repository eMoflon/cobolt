package de.tudarmstadt.maki.modeling.jvlc.graph;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import de.tudarmstadt.maki.modeling.graphmodel.Edge;
import de.tudarmstadt.maki.modeling.graphmodel.EdgeState;
import de.tudarmstadt.maki.modeling.jvlc.JvlcFactory;
import de.tudarmstadt.maki.modeling.jvlc.KTCLink;
import de.tudarmstadt.maki.modeling.jvlc.KTCNode;
import de.tudarmstadt.maki.modeling.jvlc.Topology;

public class TopologyTest {

	private Topology topology;

	@Before
	public void setup() {
		this.topology = JvlcFactory.eINSTANCE.createTopology();
	}

	@Test
	public void testCreateKTCLink() throws Exception {
		final KTCNode n1 = this.topology.addKTCNode("n1", 1.2);
		final KTCNode n2 = this.topology.addKTCNode("n2", 2.5);
		final KTCLink link = this.topology.addKTCLink("e12", n1, n2, 15.7, 2.9);
		Assert.assertSame(EdgeState.UNCLASSIFIED, link.getState());

		final KTCLink link12 = this.topology.getKTCLinkById("e12");
		Assert.assertSame(link, link12);

		Assert.assertSame(n2, this.topology.getKTCNodeById("n2"));
		Assert.assertNull(topology.getKTCNodeById("not-there"));
	}

	@Ignore("Performance test for unclassification effort")
	@Test
	public void testRuntimeOfLinkUnclassification() throws Exception {
		for (int edgeCount : Arrays.asList((int) 1e3, (int) 1e4, (int)5e4, (int) 1e5, (int) 2e5, (int) 5e5, (int) 1e6)) {
			KTCNode previousNode = this.topology.addKTCNode("n" + 1, 0.0);
			for (int i = 2; i <= edgeCount; ++i) {
				KTCNode currentNode = this.topology.addKTCNode("n" + i, 0.0);
				this.topology.addKTCLink("e" + (i - 1) + "-" + i, previousNode, currentNode, 1, 1, EdgeState.ACTIVE);
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
