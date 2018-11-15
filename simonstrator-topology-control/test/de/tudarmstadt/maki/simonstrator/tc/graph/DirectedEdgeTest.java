package de.tudarmstadt.maki.simonstrator.tc.graph;

import org.junit.Assert;
import org.junit.Test;

import de.tudarmstadt.maki.simonstrator.api.common.graph.DirectedEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;

public class DirectedEdgeTest {

	@Test
	public void testToString() throws Exception {
		final DirectedEdge e12 = new DirectedEdge(INodeID.get("n1"), INodeID.get("n2"));
		final DirectedEdge eBA = new DirectedEdge(INodeID.get("nB"), INodeID.get("nA"));
//		e12.setProperty(GenericGraphElementProperties.REVERSE_EDGE, eBA);
//		eBA.setProperty(GenericGraphElementProperties.REVERSE_EDGE, e12);

		Assert.assertTrue(e12.toString().contains("n1"));
		Assert.assertTrue(eBA.toString().contains("nB"));
	}
}
