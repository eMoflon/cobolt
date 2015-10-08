package de.tudarmstadt.maki.modeling.jvlc.graph;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.tudarmstadt.maki.modeling.jvlc.JvlcFactory;
import de.tudarmstadt.maki.modeling.jvlc.KTCLink;
import de.tudarmstadt.maki.modeling.jvlc.KTCNode;
import de.tudarmstadt.maki.modeling.jvlc.LinkState;
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
		Assert.assertSame(LinkState.UNCLASSIFIED, link.getState());

		final KTCLink link12 = this.topology.getKTCLinkById("e12");
		Assert.assertSame(link, link12);

		Assert.assertSame(n2, this.topology.getKTCNodeById("n2"));
		Assert.assertNull(topology.getKTCNodeById("not-there"));
	}
}
